package com.esp.localjobs

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.esp.localjobs.managers.PositionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.delay

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
*/

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val REQUEST_LOCATION_PERMISSION_CODE = 100
    // private var positionServiceJob: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestLocationPermissions()

        // TODO check if GPS is enabled
        // if (!PositionManager.getInstance(applicationContext).startListeningForPosition()) {
        //  positionServiceJob = GlobalScope.launch(Dispatchers.Main) { tryUntilOk() }
        // }

        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        navController.addOnDestinationChangedListener { _, destination, _ -> onDestinationChangeListener(destination) }
        // declare top destinations - these won't show the upp button
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.destination_jobs,
                R.id.destination_proposals,
                R.id.destination_login
            )
        )

        setupToolbar(navController, appBarConfiguration)
        setupBottomNavigationMenu(navController)
    }

    private fun setupBottomNavigationMenu(navController: NavController) {
        findViewById<BottomNavigationView>(R.id.bottom_nav_view)
            .setupWithNavController(navController)
    }

    private fun setupToolbar(navController: NavController, appBarConfiguration: AppBarConfiguration) {
        setSupportActionBar(toolbar)
        findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfiguration) // automatically show/handle up button
    }

    /**
     * Handle destination change.
     * Hide bottom navigation and menu nav items when not in jobs or proposals fragment
     */
    private fun onDestinationChangeListener(destination: NavDestination) {
        // bottom bar
        bottom_nav_view.visibility = when (destination.id) {
            R.id.destination_jobs, R.id.destination_proposals -> View.VISIBLE
            else -> View.GONE
        }
    }

    /**
     * Handle navigation items click
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        val navigated = NavigationUI.onNavDestinationSelected(item!!, navController)
        // if the clicked item is not recognized by the navController then pass it to super.onOptionsItemSelected
        return navigated || super.onOptionsItemSelected(item)
    }

    /**
     * Called when the action-bar back button is clicked.
     */
    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_navigation, menu)
        return true
    }

    /**
     * Show dialog to request location permissions
     */
    private fun requestLocationPermissions() {

        ActivityCompat.requestPermissions(this@MainActivity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_LOCATION_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    finish()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        PositionManager.getInstance(applicationContext).stopListeningForPosition()
    }

    override fun onDestroy() {
        super.onDestroy()
    //    positionServiceJob?.cancel(null)
    }

    /**
     * Tries to start listening position ( non-blocking )
     */
    private suspend fun tryUntilOk() {
        Toast.makeText(this, "Failed to start listening for position", Toast.LENGTH_LONG).show()
        delay(1000)
        if (!PositionManager.getInstance(applicationContext).startListeningForPosition()) {
            tryUntilOk()
        } else
            Toast.makeText(this, "Started location service", Toast.LENGTH_LONG).show()
    }
}
