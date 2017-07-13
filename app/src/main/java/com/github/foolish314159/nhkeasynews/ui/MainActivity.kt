package com.github.foolish314159.nhkeasynews.ui

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.github.foolish314159.nhkeasynews.article.NHKArticle
import com.github.foolish314159.nhkeasynews.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, NHKArticleListFragment.OnListFragmentInteractionListener {

    private var drawerToggle : ActionBarDrawerToggle? = null
    internal var currentFragment : Fragment? = null

    override fun onListFragmentInteraction(item: NHKArticle) {
        currentFragment = NHKArticleFragment()
        val args = Bundle()
        args.putParcelable(NHKArticleFragment.ARG_ARTICLE, item)
        currentFragment?.arguments = args

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, currentFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        drawerToggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(drawerToggle!!)
        drawerToggle?.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        // Exit early if fragment was restored
        if (savedInstanceState != null) {
            return
        }

        currentFragment = NHKArticleListFragment()
        currentFragment?.arguments = intent.extras
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, currentFragment).commit()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else if (currentFragment is NHKArticleFragment) {
            val handled = (currentFragment as NHKArticleFragment).onBackPressed()
            if (!handled) {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
