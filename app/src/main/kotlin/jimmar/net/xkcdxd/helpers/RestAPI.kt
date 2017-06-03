package jimmar.net.xkcdxd.helpers

import jimmar.net.xkcdxd.classes.Strip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Created by Jimmar on 2/6/17.
 */

class RestAPI(onSuccess: (Strip) -> Unit,
              onFailure: () -> Unit) : Callback<Strip>{
    
    private val xkcdAPI: XkcdAPI
    private val onSuccessCallback: (Strip) -> Unit
    private val onFailureCallback: () -> Unit
    
    init {
        
        val retrofit = Retrofit.Builder()
                .baseUrl("http://xkcd.com/")
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
        
        onSuccessCallback = onSuccess
        onFailureCallback = onFailure
        xkcdAPI = retrofit.create(XkcdAPI::class.java)
    }
    
    fun getLatestStrip(): Call<Strip>{
        return xkcdAPI.getLatest()
    }
    
    fun getStrip(number: Int): Call<Strip>{
        return xkcdAPI.getComic(number)
    }

    override fun onResponse(call: Call<Strip>?, response: Response<Strip>?) {
        onSuccessCallback(response!!.body()!!)
    }

    override fun onFailure(call: Call<Strip>?, t: Throwable?) {
        t!!.printStackTrace()
        onFailureCallback()
    }
    
}
