package com.esp.localjobs.fragments.map

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Location
import com.esp.localjobs.utils.GeocodingUtils
import com.esp.localjobs.viewModels.MapViewModel
import kotlinx.android.synthetic.main.fragment_location_picker.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * A DialogFragment to pick a location displaying a map
 * @author Francesco Pham
 */
class LocationPickerFragment : DialogFragment(), View.OnClickListener, CoroutineScope {

    companion object {
        const val TAG = "LocationPickerFragment"
        const val ARG_START_LATITUDE = "start-location-latitude"
        const val ARG_START_LONGITUDE = "start-location-longitude"

        @JvmStatic
        fun newInstance(startLocation: Location? = null) =
            LocationPickerFragment().apply {
                arguments = Bundle().apply {
                    if (startLocation != null) {
                        putDouble(ARG_START_LATITUDE, startLocation.l[0])
                        putDouble(ARG_START_LONGITUDE, startLocation.l[1])
                    }
                }
            }
    }

    private lateinit var locationPickedCallback: OnLocationPickedListener
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    private val mapViewModel: MapViewModel by activityViewModels()
    private var startLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            if (containsKey(ARG_START_LATITUDE) && containsKey(ARG_START_LONGITUDE)) {
                val lat = getDouble(ARG_START_LATITUDE)
                val lon = getDouble(ARG_START_LONGITUDE)
                startLocation = Location(lat, lon)
            }
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
     * Handle view items click
     */
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.apply_button -> {
                mapViewModel.location.value?.let {
                    launch {
                        it.city = GeocodingUtils.coordinatesToCity(context!!, it.latLng().first, it.latLng().second)
                        CoroutineScope(Dispatchers.Main).launch {
                            locationPickedCallback.onLocationPicked(it)
                        }
                    }
                    dismiss()
                }
            }
            R.id.cancel_button -> dismiss()
        }
    }
}
