package com.esp.localjobs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.esp.localjobs.managers.PositionManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.esp.localjobs.data.models.Location
import kotlinx.android.synthetic.main.fragment_add.range_seekbar
import kotlinx.android.synthetic.main.fragment_filter_results.range_value
import kotlinx.android.synthetic.main.fragment_location_picker.*

class LocationPickerFragment : DialogFragment(), View.OnClickListener {
    private var mapBoxMap: MapboxMap? = null
    private lateinit var mapView: MapView
    private val filterViewModel: FilterViewModel by activityViewModels()

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

        val centerPositionButton = view.findViewById<ImageView>(R.id.center_user_position_button)
        centerPositionButton.setOnClickListener(this)

        // initialize seekbar and set listener
        setRangeTextView(range_seekbar.progress)
        range_seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                setRangeTextView(progress)
            }
        })
    }

    override fun onResume() {
        super.onResume()

        // set dialog fragment size (width and height values in fragment_ingredients_details.xml do not work)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    /**
     * For setting the value next to seek bar
     * @param value The value corresponding to the seekbar position
     */
    private fun setRangeTextView(value: Int) {
        range_value.text = getString(R.string.distance, value)
    }

    /* ****************************************************
     * MAP RELATED METHODS BELOW
     ***************************************************** */

    /**
     * Get map-view and create an hovering marker at the center of the map.
     * TODO: add user location marker, see https://docs.mapbox.com/help/interactive-tools/marker-playground/
     */
    private fun setupMapView() {
        mapView.getMapAsync { map ->
            mapBoxMap = map
            map.setStyle(Style.MAPBOX_STREETS) { }
            hovering_marker.visibility = View.VISIBLE
            navigateToLastKnownPosition()
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
            R.id.apply_button -> updateViewModel() // TODO close dialog fragment
        }
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

        // center view on user location
        val cameraPosition = CameraPosition.Builder()
            .target(
                LatLng(
                    location.latitude,
                    location.longitude)
            )
            .zoom(16.0)
            .bearing(180.0)
            .tilt(30.0)
            .build()
        mapBoxMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000)
    }

    private fun updateViewModel() {
        // get location coordinates of the center of the map-view
        if (mapBoxMap != null) {
            val latLng = (mapBoxMap as MapboxMap).cameraPosition.target
            filterViewModel.location = Location(latLng.latitude, latLng.longitude)
        } else
            filterViewModel.location = null
    }
}
