package com.esp.localjobs.utils

import android.content.Context


object GeocodingUtils {
    /**
     * Convert coordinates into a city name
     * @return null if could not retrieve any (i.e. in the middle of the ocean)
     */
    fun coordinatesToCity(context: Context, latitude: Double, longitude: Double): String? {
        //todo to implement in a non-blocking way
        return "Via Gradenigo"
        /*try { // Sometimes gcd.getFromLocation(..) throws IOException, causing crash
            val gcd = Geocoder(context, Locale.getDefault())
            val addresses = gcd.getFromLocation(latitude, longitude, 1)
            return if (addresses.size > 0) addresses[0].locality else null
        } catch (e: IOException) {
            Toast.makeText(context, context.getString(R.string.error_retrieving_location_name), Toast.LENGTH_SHORT)
                .show()
        }
        return null*/
    }
}