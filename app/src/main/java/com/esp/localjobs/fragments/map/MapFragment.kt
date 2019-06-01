package com.esp.localjobs.fragments.map

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Location
import com.esp.localjobs.utils.PositionManager
import com.esp.localjobs.viewModels.MapViewModel
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlinx.android.synthetic.main.fragment_map.*
import java.io.IOException
import java.util.*

/**
 * A simple fragment showing a basic map providing some useful methods.
 * Extend this class to add more features.
 * @author Francesco Pham
 */
open class MapFragment : Fragment() {

    private val mapViewModel: MapViewModel by activityViewModels()

    protected lateinit var mapboxMap: MapboxMap
    private lateinit var mapContainer: MapView

    open var startLocation: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_map, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapContainer = map_container
        mapContainer.run {
            onCreate(savedInstanceState)
            getMapAsync { map ->
                onMapReady(map)
            }
        }
        center_user_position_button.setOnClickListener {
            centerMap()
        }
    }

    open fun onMapReady(map: MapboxMap) = with(map) {
        mapboxMap = this

        // disable tilt and rotate gestures
        uiSettings.apply {
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
        }

        startLocation?.let { centerMap(it) }
        removeOnCameraIdleListener(mapIdleListener)
        addOnCameraIdleListener(mapIdleListener)
    }

    private val mapIdleListener = {
        mapViewModel.setLocation(getCenterLocation())
    }

    /**
     * Center the map on a location. Last known position is used when target is null
     */
    fun centerMap(targetLocation: Location? = null) {
        if (targetLocation != null)
            navigateToPosition(targetLocation)
        else {
            PositionManager.getLastKnownPosition(context!!)?.let {
                navigateToPosition(Location(it.latitude, it.longitude))
            } ?: Toast.makeText(context, getString(R.string.position_unknown_toast), Toast.LENGTH_LONG).show()
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
                    location.l[0],
                    location.l[1]
                )
            )
            .zoom(12.0)
            .build()
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 600)
    }

    /**
     * Get location coordinates of the center of the map-view
     * @return null if could not retrieve
     */
    fun getCenterLocation(): Location {
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
            Toast.makeText(context!!, getString(R.string.error_retrieving_location_name), Toast.LENGTH_SHORT).show()
        }
        return null
    }

    override fun onResume() {
        super.onResume()
        mapContainer.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapContainer.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapContainer.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapContainer.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapContainer.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapContainer.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapContainer.onSaveInstanceState(outState)
    }
}
