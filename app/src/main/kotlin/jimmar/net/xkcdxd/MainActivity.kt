package jimmar.net.xkcdxd

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.ActionBarActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import jimmar.net.xkcdxd.navigationDrawer.NavigationDrawerCallbacks
import jimmar.net.xkcdxd.navigationDrawer.NavigationDrawerFragment

class MainActivity : ActionBarActivity(), NavigationDrawerCallbacks {

    private var mToolbar: Toolbar? = null
    private lateinit var mNavigationDrawerFragment: NavigationDrawerFragment

    companion object {
        lateinit var favorites: List<String>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mToolbar = findViewById(R.id.toolbar_actionbar) as Toolbar
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        mNavigationDrawerFragment = fragmentManager.findFragmentById(R.id.fragment_drawer) as NavigationDrawerFragment
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, findViewById(R.id.drawer) as DrawerLayout, mToolbar)

        favorites = retrieveFavorites()
    }

    private fun retrieveFavorites(): List<String> {
        val pref = getPreferences(Context.MODE_PRIVATE)
        val list = mutableListOf<String>()
        val latestValue = pref.getInt("fav_latest_value", 0)

        (0 until latestValue).mapTo(list) { pref.getString("favorite_$it", "0 - none") }
        list.sort()
        return list
    }

    private fun saveFavorites() {
        val pref = getPreferences(Context.MODE_PRIVATE)
        val editor = pref.edit()
        for (i in 0 until favorites.size) editor.putString("favorite_$i", favorites[i])
        editor.putInt("fav_latest_value", favorites.size);
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
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
    }

    private fun onSectionAttached(number: Int) {
        when (number) {
            1 -> title = getString(R.string.title_section1)
            2 -> title = getString(R.string.title_section2)
        }
    }

    fun restoreActionBar() {
        supportActionBar!!.navigationMode = ActionBar.NAVIGATION_MODE_STANDARD
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

    fun setMTitle(mTitle: String){
        title = mTitle
    }

}