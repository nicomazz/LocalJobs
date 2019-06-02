package com.esp.localjobs.fragments.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Location
import com.esp.localjobs.utils.PositionManager
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import kotlinx.android.synthetic.main.fragment_map.*

/**
 * A simple fragment showing a basic map providing some useful methods.
 * Extend this class to add more features.
 * @author Francesco Pham
 */
open class MapFragment : Fragment(), OnMapReadyCallback {

    private var lastCameraMovementPosition: Location? = null
    protected var mapboxMap: MapboxMap? = null
    protected var mapContainer: MapView? = null

    open var startLocation: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_map, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapContainer = map_container.apply {
            onCreate(savedInstanceState)
        }
        center_user_position_button.setOnClickListener {
            centerMap()
        }
    }

    override fun onMapReady(map: MapboxMap) = with(map) {
        mapboxMap = this

        // disable tilt and rotate gestures
        uiSettings.apply {
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
        }

        centerMap(startLocation)
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

    fun centerMap(pos: LatLng) {
        centerMap(Location(latitude = pos.latitude, longitude = pos.longitude))
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
        lastCameraMovementPosition = location
        mapboxMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 600)
    }

    fun navigateToLastPosition() {
        lastCameraMovementPosition?.let {
            navigateToPosition(it)
        }
    }

    override fun onResume() {
        super.onResume()
        mapContainer?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapContainer?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapContainer?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapContainer?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapContainer?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapContainer?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapContainer?.onSaveInstanceState(outState)
    }
}
