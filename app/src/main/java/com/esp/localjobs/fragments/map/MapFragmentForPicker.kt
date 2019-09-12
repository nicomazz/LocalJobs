package com.esp.localjobs.fragments.map

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.esp.localjobs.data.models.Location
import com.esp.localjobs.viewModels.MapViewModel
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.fragment_map.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log2

private const val ARG_START_LATITUDE = "start-location-latitude"
private const val ARG_START_LONGITUDE = "start-location-longitude"

/**
 * Map fragment inside LocationPickerFragment displaying a hovering marker at the center
 * @author Francesco Pham
 */
class MapFragmentForPicker : MapFragment() {

    override var startLocation: Location? = null
    private val mapViewModel: MapViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            if (containsKey(ARG_START_LATITUDE) && containsKey(ARG_START_LONGITUDE)) {
                val lat = getDouble(ARG_START_LATITUDE)
                val lon = getDouble(ARG_START_LONGITUDE)
                startLocation = Location(lat, lon)
            }
        }
    }

    override fun onMapReady(map: MapboxMap) {
        super.onMapReady(map)
        map.setStyle(Style.MAPBOX_STREETS)
        hovering_marker.visibility = View.VISIBLE
        center_user_position_button.visibility = View.VISIBLE

        map.apply {
            addOnCameraIdleListener(mapIdleListener)
            addOnCameraMoveListener(mapMoveListener)
        }

        mapViewModel.radius.observe(viewLifecycleOwner, Observer { radius ->
            if (radius != null) {
                val zoom = distanceToZoom(radius)
                centerMap(mapViewModel.location.value, zoom, false)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapboxMap.removeOnCameraIdleListener(mapIdleListener)
        mapboxMap.removeOnCameraMoveListener(mapMoveListener)
    }

    private val mapIdleListener = {
        if (isAdded) {
            mapViewModel.setLocation(getCenterLocation())
            updateMetersPerPixel()
        }
    }

    private val mapMoveListener = {
        if (isAdded) {
            updateMetersPerPixel()
        }
    }

    private fun updateMetersPerPixel() {
        val metersPerPixel = mapboxMap.projection.getMetersPerPixelAtLatitude(getCenterLocation().l[0])
        mapViewModel.setMetersPerPixel(metersPerPixel)
    }

    /**
     * Convert distance in meters to zoom level which contains that circle radius
     * @param distance Distance in meters
     * @return Corresponding zoom level
     */
    private fun distanceToZoom(distance: Double): Double {
        val earthCircumference = 40075017
        val latitude = mapboxMap.cameraPosition.target.latitude * PI / 180
        return log2(earthCircumference*cos(latitude) / (distance*3))
    }

    /**
     * Get location coordinates of the center of the map-view
     * @return null if could not retrieve
     */
    private fun getCenterLocation(): Location {
        // get location coordinates of the center of the map-view
        val latLng = mapboxMap.cameraPosition.target
        return Location(latLng.latitude, latLng.longitude)
    }

    // todo rimuovere questo
    companion object {
        @JvmStatic
        fun newInstance(startLocation: Location?) =
            MapFragmentForPicker().apply {
                arguments = Bundle().apply {
                    if (startLocation != null) {
                        putDouble(ARG_START_LATITUDE, startLocation.l[0])
                        putDouble(ARG_START_LONGITUDE, startLocation.l[1])
                    }
                }
            }
    }
}