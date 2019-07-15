package jimmar.net.xkcdxd

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import jimmar.net.xkcdxd.navigationDrawer.NavigationDrawerCallbacks
import jimmar.net.xkcdxd.navigationDrawer.NavigationDrawerFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), NavigationDrawerCallbacks {

    private var mToolbar: Toolbar? = null
    private lateinit var mNavigationDrawerFragment: NavigationDrawerFragment

    companion object {
        lateinit var favorites: MutableList<String>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        mToolbar = toolbar_actionbar as Toolbar?
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mNavigationDrawerFragment = fragment_drawer as NavigationDrawerFragment
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, drawer, mToolbar)

        favorites = retrieveFavorites()
    }

    private fun retrieveFavorites(): MutableList<String> {
        val pref = getPreferences(Context.MODE_PRIVATE)
        val list = mutableListOf<String>()
        val latestValue = pref.getInt("fav_latest_value", 0)

        (0 until latestValue).mapTo(list) { pref.getString("favorite_$it", "0 - none")!! }
        list.sort()
        return list
    }

    private fun saveFavorites() {
        val pref = getPreferences(Context.MODE_PRIVATE)
        val editor = pref.edit()
        for (i in 0 until favorites.size) editor.putString("favorite_$i", favorites[i])
        editor.putInt("fav_latest_value", favorites.size)
        editor.apply()
    }

    override fun onNavigationDrawerItemSelected(position: Int) {
        var fragment: Fragment? = null
        when (position) {
            0 -> fragment = ComicPageFragment()
            1 -> fragment = FavoritePageFragment()
        }
        switchContent(fragment)
    }

    fun switchContent(fragment: Fragment?) {
        fragment?.let { supportFragmentManager.beginTransaction().replace(R.id.container, it).commit() }
    }

    fun restoreActionBar() {
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.title = title
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (!mNavigationDrawerFragment.isDrawerOpen) {
            restoreActionBar()
            return true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_settings)
            return true

        return super.onOptionsItemSelected(item)
    }

    fun setMTitle(mTitle: String) {
        title = mTitle
    }

    override fun onPause() {
        saveFavorites()
        super.onPause()
    }
}
