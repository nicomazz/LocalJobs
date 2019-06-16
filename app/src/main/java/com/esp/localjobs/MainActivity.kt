package com.esp.localjobs

import android.Manifest
import android.animation.Animator
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.fragments.JobsFragmentDirections
import com.esp.localjobs.utils.AnimationsUtils
import com.esp.localjobs.utils.FCMHandler
import com.mapbox.mapboxsdk.Mapbox
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import androidx.core.content.edit

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

    private companion object {
        private const val REQUEST_LOCATION_PERMISSION_CODE = 100
        const val TAG = "MainActivity"
        const val APP_PREF = "appPreferences"
        const val FIRST_START_PREF = "firstStart"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // launch intro if first time launching
        val prefs = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE)
        val isFirstStart = prefs.getBoolean(FIRST_START_PREF, true)
        if (isFirstStart) {
            startActivity(Intent(this@MainActivity, IntroActivity::class.java))
            prefs.edit { putBoolean(FIRST_START_PREF, false) }
        }

        Mapbox.getInstance(applicationContext, getString(R.string.mabBoxToken))
        FCMHandler.fetchAndSendFCMToken()

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

        handleIntent(intent)

        setupToolbar(navController, appBarConfiguration)
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
        if (destination.id == R.id.destination_add) {
            val viewRoot = findViewById<View>(android.R.id.content)
            val cx = viewRoot.right
            val cy = viewRoot.bottom
            animateRevealColorFromCoordinates(cx, cy)
        }
    }

    private fun animateRevealColorFromCoordinates(x: Int, y: Int): Animator {
        val viewRoot = mainActivityCoordinator
        val foreground = containerForAnimation
        val finalRadius = Math.hypot(viewRoot.width.toDouble(), viewRoot.height.toDouble()).toFloat()

        val anim = ViewAnimationUtils.createCircularReveal(viewRoot, x, y, 0f, finalRadius)
        anim.duration = 300
        val initialColor = ContextCompat.getColor(this, R.color.colorPrimary)
        viewRoot.setBackgroundColor(initialColor)
        anim.doOnEnd {
            AnimationsUtils.animateToFinalColor(
                viewRoot,
                initialColor,
                Color.TRANSPARENT
            )

            foreground.visibility = View.VISIBLE
        }
        anim.doOnStart {
            foreground.visibility = View.INVISIBLE
        }
        anim.start()

        return anim
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

        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_LOCATION_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    finish()
            }
        }
    }

    private fun handleIntent(intent: Intent) {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        intent.extras?.getString("job_id")?.let {
            val action =
                JobsFragmentDirections.actionDestinationJobsToDestinationJobDetails(Job(id = it), mustBeFetched = true)
            navController.navigate(R.id.action_destination_jobs_to_destination_job_details, action.arguments)
        }

        intent.data?.let {
            Log.d("intent", "$it - param:  ${it.getQueryParameter("job_id")}")
            it.getQueryParameter("job_id")?.let { jobId ->
                val action =
                    JobsFragmentDirections.actionDestinationJobsToDestinationJobDetails(Job(id = jobId), mustBeFetched = true)
                navController.navigate(R.id.action_destination_jobs_to_destination_job_details, action.arguments)
            }
        }
    }
}
