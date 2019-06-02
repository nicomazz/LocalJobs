package com.esp.localjobs.fragments.map

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.esp.localjobs.data.models.Location
import com.esp.localjobs.viewModels.MapViewModel
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.fragment_map.*

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        center_user_position_button.setOnClickListener {
            centerMap()
        }
        mapContainer?.getMapAsync(this)
    }

    override fun onMapReady(map: MapboxMap) {
        super.onMapReady(map)
        map.setStyle(Style.MAPBOX_STREETS)
        hovering_marker.visibility = View.VISIBLE
        center_user_position_button.visibility = View.VISIBLE

        map.apply {
            removeOnCameraIdleListener(mapIdleListener)
            addOnCameraIdleListener(mapIdleListener)
        }
    }

    private val mapIdleListener = {
        mapViewModel.setLocation(getCenterLocation())
    }

    /**
     * Get location coordinates of the center of the map-view
     * @return null if could not retrieve
     */
    private fun getCenterLocation(): Location {
        // get location coordinates of the center of the map-view
        val latLng = mapboxMap?.cameraPosition?.target!!
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