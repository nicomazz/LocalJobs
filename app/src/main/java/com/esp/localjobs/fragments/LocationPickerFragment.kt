package com.esp.localjobs.fragments

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Location
import com.esp.localjobs.managers.PositionManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.fragment_location_picker.*
import java.io.IOException
import java.util.Locale

private const val TAG = "LocationPickerFragmet"

class LocationPickerFragment(
    private val locationPickedCallback: OnLocationPickedListener,
    private val startLocation: Location?
) : DialogFragment(), View.OnClickListener {

    private lateinit var mapBoxMap: MapboxMap
    private lateinit var mapView: MapView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(activity!!.applicationContext, getString(R.string.mabBoxToken))
        return inflater.inflate(R.layout.fragment_location_picker, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // map
        mapView = view.findViewById(R.id.map_view)
        mapView.setOnTouchListener { v, _ ->
            this.onClick(v)
            false // false: event not consumed
        }
        mapView.onCreate(savedInstanceState)
        // navigate to last known position
        setupMapView()

        center_user_position_button.setOnClickListener(this)
        apply_button.setOnClickListener(this)
        cancel_button.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()

        // set dialog fragment size (width and height values in fragment_ingredients_details.xml do not work)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    /**
     * This interface should be implemented when using this fragment.
     * onLocationPicked is called when the apply button is pressed
     */
    interface OnLocationPickedListener {
        fun onLocationPicked(location: Location)
    }

    /**
     * Get map-view and create an hovering marker at the center of the map.
     * TODO: add user location marker, see https://docs.mapbox.com/help/interactive-tools/marker-playground/
     */
    private fun setupMapView() {
        mapView.getMapAsync { map ->
            mapBoxMap = map
            map.setStyle(Style.MAPBOX_STREETS) { }
            hovering_marker.visibility = View.VISIBLE
            if (startLocation == null)
                navigateToLastKnownPosition()
            else
                navigateToPosition(startLocation)
        }
    }

    /**
     * Handle view items click
     */
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.map_view -> {
                hovering_marker.setImageResource(R.drawable.ic_location_on_red_900_36dp)
            }
            R.id.center_user_position_button -> navigateToLastKnownPosition()
            R.id.apply_button -> {
                getSelectedLocation().let {
                    locationPickedCallback.onLocationPicked(it)
                    dismiss()
                }
            }
            R.id.cancel_button -> dismiss()
        }
    }

    /**
     * Get location coordinates of the center of the map-view
     * @return null if could not retrieve
     */
    private fun getSelectedLocation(): Location {
        // get location coordinates of the center of the map-view
        val latLng = mapBoxMap.cameraPosition.target
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

    /**
     * If the last known position of the device is not null, center the map view on it and set the hovering
     * marker color to blue
     */
    private fun navigateToLastKnownPosition() {
        val location = PositionManager.getInstance(context!!).getLastKnownPosition()
        if (location == null) {
            Toast.makeText(context!!, "Last position unknown", Toast.LENGTH_LONG).show()
            return
        }
        hovering_marker.setImageResource(R.drawable.ic_location_on_blue_900_36dp)
        navigateToPosition(Location(location.latitude, location.longitude))
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
                    location.l[1])
            )
            .zoom(16.0)
            .bearing(180.0)
            .tilt(30.0)
            .build()
        mapBoxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000)
    }
}
