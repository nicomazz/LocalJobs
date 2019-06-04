package com.esp.localjobs.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import com.esp.localjobs.R
import com.mapbox.android.core.permissions.PermissionsManager
import java.io.IOException
import java.util.Locale

private const val TWO_MINUTES: Long = 1000 * 60 * 2

object PositionManager {
    /**
     * Retrieve the last known position of the device.
     * Can return null in edge cases like after a factory reset.
     */
    @SuppressLint("MissingPermission")
    fun getLastKnownPosition(context: Context): Location? {
        if (PermissionsManager.areLocationPermissionsGranted(context)) {
            val locationManager: LocationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val netPos = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val gpsPos = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            return if (isBetterLocation(netPos, gpsPos)) netPos else gpsPos
        }
        return null
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    private fun isBetterLocation(location: Location?, currentBestLocation: Location?): Boolean {
        if (location == null)
            return false

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
}

object Utils {
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }

        val bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * Convert coordinates into a city name
     * @return null if could not retrieve any (i.e. in the middle of the ocean)
     */
    fun coordinatesToCity(context: Context, latitude: Double, longitude: Double): String? {
        try { // Sometimes gcd.getFromLocation(..) throws IOException, causing crash
            val gcd = Geocoder(context, Locale.getDefault())
            val addresses = gcd.getFromLocation(latitude, longitude, 1)
            return if (addresses.size > 0) addresses[0].locality else null
        } catch (e: IOException) {
            Toast.makeText(context, context.getString(R.string.error_retrieving_location_name), Toast.LENGTH_SHORT).show()
        }
        return null
    }
}