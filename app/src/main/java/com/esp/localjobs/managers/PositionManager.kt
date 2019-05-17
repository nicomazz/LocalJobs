package com.esp.localjobs.managers

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.mapbox.android.core.permissions.PermissionsManager

private const val TWO_MINUTES: Long = 1000 * 60 * 2

/**
 * Singleton used to start listening for position, any update will modify @variable currentBestLocation which can be
 * observed.
 */
class PositionManager private constructor(private val context: Context) {
    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var isListening: Boolean = false
    val currentBestLocation = MutableLiveData<Location?>()

    /**
     * Register a listener that updates @variable currentBestPosition, which is initialized with the last known
     * position.
     */
    @SuppressLint("MissingPermission")
    fun startListeningForPosition(): Boolean {
        if (isListening)
            return true
        if (PermissionsManager.areLocationPermissionsGranted(context)) {
            // init value with last position
            getLastKnownPosition()?.let {
                currentBestLocation.value = it
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
            isListening = true
            return true
        }
        return false
    }

    fun stopListeningForPosition() {
        locationManager.removeUpdates(locationListener)
        isListening = false
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            if (location == null || !isBetterLocation(location, currentBestLocation.value))
                return
            currentBestLocation.value = location
        }
        override fun onProviderDisabled(provider: String?) { }
        override fun onProviderEnabled(provider: String?) { }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) { }
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownPosition(): Location? {
        if (PermissionsManager.areLocationPermissionsGranted(context)) {
            val netPos = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val gpsPos = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (netPos == null)
                return null
            return if (isBetterLocation(netPos, gpsPos)) netPos else gpsPos
        }
        return null
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    private fun isBetterLocation(location: Location, currentBestLocation: Location?): Boolean {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true
        }

        // Check whether the new location fix is newer or older
        val timeDelta: Long = location.time - currentBestLocation.time
        val isSignificantlyNewer: Boolean = timeDelta > TWO_MINUTES
        val isSignificantlyOlder: Boolean = timeDelta < -TWO_MINUTES

        when {
            // If it's been more than two minutes since the current location, use the new location
            // because the user has likely moved
            isSignificantlyNewer -> return true
            // If the new location is more than two minutes older, it must be worse
            isSignificantlyOlder -> return false
        }

        // Check whether the new location fix is more or less accurate
        val isNewer: Boolean = timeDelta > 0L
        val accuracyDelta: Float = location.accuracy - currentBestLocation.accuracy
        val isLessAccurate: Boolean = accuracyDelta > 0f
        val isMoreAccurate: Boolean = accuracyDelta < 0f
        val isSignificantlyLessAccurate: Boolean = accuracyDelta > 200f

        // Check if the old and new location are from the same provider
        val isFromSameProvider: Boolean = location.provider == currentBestLocation.provider

        // Determine location quality using a combination of timeliness and accuracy
        return when {
            isMoreAccurate -> true
            isNewer && !isLessAccurate -> true
            isNewer && !isSignificantlyLessAccurate && isFromSameProvider -> true
            else -> false
        }
    }

    companion object : SingletonHolder<PositionManager, Context>(::PositionManager)
}

open class SingletonHolder<out T, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile private var instance: T? = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}