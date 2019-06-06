package com.esp.localjobs.utils

import android.content.Context
import android.location.Geocoder
import java.io.IOException
import java.util.Locale

object GeocodingUtils {
    /**
     * Convert coordinates into a city name
     * @return null if could not retrieve any
     */
    fun coordinatesToCity(context: Context, latitude: Double, longitude: Double): String? {
        // todo to implement in a non-blocking way
        try { // Sometimes gcd.getFromLocation(..) throws IOException, causing crash
            val gcd = Geocoder(context, Locale.getDefault())
            val addresses = gcd.getFromLocation(latitude, longitude, 1)
            return if (addresses.size > 0) addresses[0].locality else null
        } catch (e: IOException) { // IOException when no internet connection
            return null
        }
    }
}