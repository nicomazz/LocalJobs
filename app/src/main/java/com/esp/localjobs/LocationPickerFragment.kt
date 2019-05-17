package com.esp.localjobs

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.DialogFragment
import com.esp.localjobs.managers.PositionManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.fragment_filter_results.*
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_filter_results.range_value


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class LocationPickerFragment : DialogFragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var mapBoxMap: MapboxMap? = null
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(activity!!.applicationContext, getString(R.string.mabBoxToken))
        return inflater.inflate(R.layout.fragment_location_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // map
        mapView = view.findViewById(R.id.map_view)
        mapView.setOnTouchListener { v, _ ->
            this.onClick(v)
            false // false: event not consumed
        }
        mapView.onCreate(savedInstanceState)
        setupMapView()

        // initialize seekbar and set listener
        setRangeTextView(range_seekbar.progress)
        range_seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                setRangeTextView(progress)
            }
        })
    }

    override fun onResume() {
        super.onResume()

        //set dialog fragment size (width and height values in fragment_ingredients_details.xml do not work)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LocationPickerFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LocationPickerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    /**
     * For setting the value next to seek bar
     * @param value The value corresponding to the seekbar position
     */
    private fun setRangeTextView(value: Int){
        range_value.text = getString(R.string.distance, value)
    }



    /*****************************************************
     * MAP RELATED METHODS BELOW
     ******************************************************/

    /**
     * Get map-view and create an hovering marker at the center of the map.
     * TODO: add user location marker, see https://docs.mapbox.com/help/interactive-tools/marker-playground/
     */
    private fun setupMapView() {
        mapView.getMapAsync { map ->
            mapBoxMap = map
            map.setStyle(Style.MAPBOX_STREETS) { }
        }
    }

    /**
     * Handle view items click
     */
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.map_view -> stopObservingPosition()
            R.id.center_user_position_button -> startObservingPosition()
        }
    }

    private fun stopObservingPosition() {
        hovering_marker.setImageResource(R.drawable.ic_location_on_red_900_36dp)
        PositionManager.getInstance(context!!).currentBestLocation.removeObserver(locationObserver)
    }

    private fun startObservingPosition() {
        hovering_marker.setImageResource(R.drawable.ic_location_on_blue_900_36dp)
        PositionManager.getInstance(context!!).apply {
            startListeningForPosition()
            currentBestLocation.observe(viewLifecycleOwner, locationObserver)
        }
    }

    /**
     * Center the map on user location - if location is null hide location marker
     */
    private val locationObserver = Observer<android.location.Location?> { newLocation ->
        hovering_marker.visibility = if (newLocation == null) View.INVISIBLE else View.VISIBLE
        newLocation?.let {
            // center view on user location
            val cameraPosition = CameraPosition.Builder()
                .target(
                    LatLng(
                        newLocation.latitude,
                        newLocation.longitude)
                )
                .zoom(16.0)
                .bearing(180.0)
                .tilt(30.0)
                .build()
            mapBoxMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 3000)
        }
    }
}
