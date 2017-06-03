package jimmar.net.xkcdxd.helpers

import jimmar.net.xkcdxd.classes.Strip
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by Jimmar on 2/6/17.
 */

interface XkcdAPI {
    @GET("/info.0.json")
    fun getLatest(): Call<Strip>;
    
    @GET("{number}/info.0.json")
    fun getComic(@Path("number") number:  Int): Call<Strip>
}
