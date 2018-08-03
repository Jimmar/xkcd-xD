package jimmar.net.xkcdxd

import android.os.Bundle
import android.support.v4.app.ListFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView

/**
 * Created by Jimmar on 29/5/17.
 */

class FavoritePageFragment : ListFragment() {
    lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1, MainActivity.Companion.favorites)
        listView.adapter = adapter

        
        if (adapter.count > 0)
            view.findViewById<TextView>(R.id.no_favorites_tv).visibility = View.GONE

        if (activity == null) {
            (activity as MainActivity).title = "Favorites"
            (activity as MainActivity).restoreActionBar()
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
        val fragment = ComicPageFragment()
        val bundle = Bundle()
        val favoriteString = MainActivity.Companion.favorites[position]
        bundle.putInt("comicNumber", favoriteString.split("-")[0].trim().toInt())
        fragment.arguments = bundle
        (activity as MainActivity).switchContent(fragment)
    }
}
