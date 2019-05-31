package com.esp.localjobs.fragments

import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Location
import com.esp.localjobs.managers.PositionManager
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import java.io.IOException
import java.util.Locale

open class MapFragment : Fragment() {

    protected lateinit var mapboxMap: MapboxMap
    private lateinit var mapView: MapView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.map_view)
    }

    fun setupMap(setupCallback: (MapboxMap) -> Unit, mapCenterLocation: Location? = null) {
        mapView.getMapAsync {
            mapboxMap = it

            // disable tilt and rotate gestures
            mapboxMap.uiSettings.isRotateGesturesEnabled = false
            mapboxMap.uiSettings.isTiltGesturesEnabled = false

            setupCallback(mapboxMap)
            centerMap(mapCenterLocation)
        }
    }

    /**
     * Center the map on a location. Last known position is used when target is null
     */
    fun centerMap(targetLocation: Location? = null) {
        if (targetLocation != null)
            navigateToPosition(targetLocation)
        else {
            val location = PositionManager.getInstance(context!!).getLastKnownPosition()
            if (location != null)
                navigateToPosition(Location(location.latitude, location.longitude))
            else
                Toast.makeText(context, "Last position unknown", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Center the map view on given location.
     */
    private fun navigateToPosition(location: Location) {
        // center view on user location
        val cameraPosition = CameraPosition.Builder()
            .target(
                LatLng(
                    location.latitude,
                    location.longitude)
            )
            .zoom(12.0)
            .build()
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000)
    }

    /**
     * Get location coordinates of the center of the map-view
     * @return null if could not retrieve
     */
    fun getSelectedLocation(): Location {
        // get location coordinates of the center of the map-view
        val latLng = mapboxMap.cameraPosition.target
        val city = coordinatesToCity(latLng.latitude, latLng.longitude)
        return Location(latLng.latitude, latLng.longitude, city)
    }

    /**
     * Convert coordinates into a city name
     * @return null if could not retrieve any (i.e. in the middle of the ocean)
     */
    private fun coordinatesToCity(latitude: Double, longitude: Double): String? {
        try { // Sometimes gcd.getFromLocation(..) throws IOException, causing crash
            val gcd = Geocoder(context, Locale.getDefault())
            val addresses = gcd.getFromLocation(latitude, longitude, 1)
            return if (addresses.size > 0) addresses[0].locality else null
        } catch (e: IOException) {
            Toast.makeText(context!!, "Error retrieving location name.", Toast.LENGTH_SHORT).show()
        }
        return null
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
