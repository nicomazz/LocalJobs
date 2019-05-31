package com.esp.localjobs.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Location
import kotlinx.android.synthetic.main.fragment_location_picker.*

/**
 * A DialogFragment to pick a location displaying a map
 */
class LocationPickerFragment(
    private val locationPickedCallback: OnLocationPickedListener,
    private val startLocation: Location?
) : DialogFragment(), View.OnClickListener {

    private lateinit var mapFragment: MapFragment

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

        // add map fragment
        mapFragment = LocationPickerMapFragment(startLocation)
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.map_fragment, mapFragment)
        fragmentTransaction.commit()

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
                mapFragment.getSelectedLocation().let {
                    locationPickedCallback.onLocationPicked(it)
                    dismiss()
                }
            }
            R.id.cancel_button -> dismiss()
        }
    }
}
