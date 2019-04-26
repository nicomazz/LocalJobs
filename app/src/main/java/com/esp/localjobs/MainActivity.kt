package com.esp.localjobs

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*

/*
Resources:

-> basic navigation codelab
    https://codelabs.developers.google.com/codelabs/android-navigation/index.html?index=..%2F..%2Findex#0

-> navigation graph (actions and destinations)
    https://developer.android.com/guide/navigation/navigation-getting-started

-> navigation & action bar - menus - bottom navigation etc setup
    https://developer.android.com/guide/navigation/navigation-ui#add_a_navigation_drawer

-> safe args doc:
    https://developer.android.com/guide/navigation/navigation-pass-data#kotlin

-> material design navigation drawer:
    https://material.io/design/components/navigation-drawer.html#
*/

/**
 *
 */
class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        setupSideNavigationMenu(navController)
        setupActionBar(navController)
    }

    /**
     * Bind the nav controller with the NavigationView menu
     */
    private fun setupSideNavigationMenu(navController: NavController) {
        findViewById<NavigationView>(R.id.nav_view).let {
            NavigationUI.setupWithNavController(it, navController)

            //it.setNavigationItemSelectedListener { item: MenuItem ->  menuItemHandler(item) }
        }
    }

    /**
     * Bind the nav controller with the drawer layout, declaring the top-level destinations
     */
    private fun setupActionBar(navController: NavController) {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        //setOf(..IDs..) are the top-level destinations where the nav (hamburger menu) will be visible
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.destination_home, R.id.destination_sign_in, R.id.destination_sign_up),
            drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    /**
     * Handle navigation items click
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        val navigated = NavigationUI.onNavDestinationSelected(item!!, navController)
        //if the clicked item is not recognized by the navController then pass it to super.onOptionsItemSelected
        return navigated || super.onOptionsItemSelected(item)
    }

    /**
     * Called when the action-bar back button is clicked.
     */
    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
    }
}
