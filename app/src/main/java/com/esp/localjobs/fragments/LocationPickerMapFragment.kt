package com.esp.localjobs.fragments

import android.os.Bundle
import android.view.View
import com.esp.localjobs.data.models.Location
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.fragment_map.*

/**
 * Map fragment inside LocationPickerFragment displaying a hovering marker at the center
 * @author Francesco Pham
 */
class LocationPickerMapFragment(private val startLocation: Location?) : MapFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMap(::onMapSetup, startLocation)
        center_user_position_button.setOnClickListener {
            centerMap()
        }
    }

    private fun onMapSetup(mapBoxMap: MapboxMap) {
        mapBoxMap.setStyle(Style.MAPBOX_STREETS)
        hovering_marker.visibility = View.VISIBLE
        center_user_position_button.visibility = View.VISIBLE
    }
}