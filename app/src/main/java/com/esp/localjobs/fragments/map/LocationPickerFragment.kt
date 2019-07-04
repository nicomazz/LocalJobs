package com.esp.localjobs.fragments.map

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Location
import com.esp.localjobs.utils.GeocodingUtils
import com.esp.localjobs.viewModels.MapViewModel
import kotlinx.android.synthetic.main.fragment_location_picker.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.math.pow

/**
 * A DialogFragment to pick a location displaying a map
 * @author Francesco Pham
 */
class LocationPickerFragment : DialogFragment(), CoroutineScope {

    companion object {
        const val ARG_START_LATITUDE = "start-location-latitude"
        const val ARG_START_LONGITUDE = "start-location-longitude"
        const val ARG_START_DISTANCE = "start-distance"
        private const val TAG = "LocationPickerFragment"
        private const val REQUEST_CODE = 0

        /**
         * Create a new instance and show dialog fragment
         * @param targetFragment The receiving fragment that implements OnLocationPickedListener
         * @param fragmentManager The fragment manager responsible for showing the dialog fragment
         * @param startLocation The initial location the map will be showing
         * @param startDistance Initial distance range value. If this value is null seekbar isn't shown
         */
        fun newInstanceShow(
            targetFragment: Fragment,
            fragmentManager: FragmentManager,
            startLocation: Location? = null,
            startDistance: Int? = null
        ) = with(newInstance(startLocation, startDistance)) {
                setTargetFragment(targetFragment, REQUEST_CODE)
                show(fragmentManager, TAG)
            }

        private fun newInstance(startLocation: Location?, startDistance: Int?) =
            LocationPickerFragment().apply {
                arguments = Bundle().apply {
                    if (startLocation != null) {
                        putDouble(ARG_START_LATITUDE, startLocation.l[0])
                        putDouble(ARG_START_LONGITUDE, startLocation.l[1])
                    }
                    if (startDistance != null)
                        putInt(ARG_START_DISTANCE, startDistance)
                }
            }
    }

    private lateinit var locationPickedCallback: OnLocationPickedListener
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    private val mapViewModel: MapViewModel by activityViewModels()
    private var startLocation: Location? = null
    private var startDistance: Int? = null

    /**
     * This interface should be implemented when using this fragment.
     * onLocationPicked is called when the apply button is pressed
     */
    interface OnLocationPickedListener {
        /**
         * @param location The picked location
         * @param distance Distance range in kilometers
         */
        fun onLocationPicked(location: Location, distance: Int?)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            if (containsKey(ARG_START_LATITUDE) && containsKey(ARG_START_LONGITUDE)) {
                val lat = getDouble(ARG_START_LATITUDE)
                val lon = getDouble(ARG_START_LONGITUDE)
                startLocation = Location(lat, lon)
            }
            if (containsKey(ARG_START_DISTANCE))
                startDistance = getInt(ARG_START_DISTANCE)
        }

        locationPickedCallback = targetFragment as OnLocationPickedListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_location_picker, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = MapFragmentForPicker.newInstance(startLocation)
        childFragmentManager.beginTransaction().apply {
            add(R.id.map_fragment, mapFragment)
            commit()
        }

        apply_button.setOnClickListener { onApply() }
        cancel_button.setOnClickListener { dismiss() }
        startDistance?.let { setupDistanceSeekbarUI(it) }

        mapViewModel.metersPerPixel.observe(viewLifecycleOwner, Observer {
            val radius = seekbarToDistance(range_seek_bar.progress)
            updateCircleRadius(radius)
        })
    }

    override fun onResume() {
        super.onResume()

        // set dialog fragment size (width and height values in fragment_ingredients_details.xml do not work)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private fun onApply() {
        mapViewModel.location.value?.let {
            progress_bar.visibility = View.VISIBLE
            launch {
                it.city = GeocodingUtils.coordinatesToCity(context!!, it.latLng().first, it.latLng().second)
                val distance =
                    if (startDistance != null) // if startDistance is null, distance seekbar is disabled
                        seekbarToDistance(range_seek_bar.progress) / 1000
                    else
                        null
                apply(it, distance)
            }
        }
    }

    /**
     * Return the picked location to the target fragment and dismiss this fragment
     * @param location The picked location
     * @param distance Distance range in kilometers
     */
    private fun apply(location: Location, distance: Int?) = CoroutineScope(Dispatchers.Main).launch {
        if (location.city == null)
            Toast.makeText(context, getString(R.string.error_retrieving_location_name), Toast.LENGTH_SHORT)
                .show()

        locationPickedCallback.onLocationPicked(location, distance)
        dismiss()
    }

    private fun setupDistanceSeekbarUI(initDistance: Int) = with(range_seek_bar) {
        range_div.visibility = View.VISIBLE
        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val radius = seekbarToDistance(progress)
                range_value.text = getString(R.string.distance, radius / 1000)
                mapViewModel.setRadius(radius)
            }
        })

        progress = initDistance
    }

    /**
     * Update circle size overlay
     * @param radius Radius in meters
     */
    private fun updateCircleRadius(radius: Int) {
        val metersPerPixel = mapViewModel.metersPerPixel.value
        if (metersPerPixel != null) {
            map_fragment.radius = (radius / metersPerPixel).toFloat()
            map_fragment.invalidate()
        }
    }

    /**
     * Convert 0-100 seekbar position into a distance in meters
     * @param progress A value from 0 to 100
     * @return Distance in meters
     */
    private fun seekbarToDistance(progress: Int): Int {
        val progress = progress.toDouble()
        return 10 * progress.pow(2.0).toInt() + 1000
    }
}
