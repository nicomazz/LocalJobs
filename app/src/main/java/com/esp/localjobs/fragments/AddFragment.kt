package com.esp.localjobs.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.SeekBar
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.esp.localjobs.LoginViewModel
import com.esp.localjobs.LoginViewModel.AuthenticationState.AUTHENTICATED
import com.esp.localjobs.LoginViewModel.AuthenticationState.INVALID_AUTHENTICATION
import com.esp.localjobs.LoginViewModel.AuthenticationState.UNAUTHENTICATED
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Location
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_add.*

private const val TAG = "AddFragment"

/**
 * Fragment used to push a job/proposal to remote db
 */
class AddFragment : Fragment(), LocationPickerFragment.OnLocationPickedListener {

    private val loginViewModel: LoginViewModel by activityViewModels()

    private var selectedLocation: Location? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_add, container, false).also {
            setHasOptionsMenu(true)
        }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_navigation, menu)
        menu.forEach { it.isVisible = false }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ensureLogin()

        setupDistanceSeekbarUI()
        submit_button.setOnClickListener { onSubmit() }
        setupLocationEditTextUI()
    }

    private fun ensureLogin() {
        loginViewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                AUTHENTICATED -> {
                }
                UNAUTHENTICATED -> {
                    showUnauthenticatedMessage()
                    findNavController().navigate(R.id.action_destination_add_to_destination_login)
                }
                INVALID_AUTHENTICATION -> TODO()
                else -> TODO()
            }
        })
    }

    private fun showUnauthenticatedMessage() {
        Snackbar.make(
            activity!!.findViewById<View>(android.R.id.content),
            getString(R.string.auth_required),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun setupLocationEditTextUI() {
        location_edit_text.setOnClickListener {
            fragmentManager?.let { fm ->
                val locationPickerFragment = LocationPickerFragment(this)
                locationPickerFragment.show(fm, "location_picker_fragment")
            }
        }
    }

    private fun setupDistanceSeekbarUI() {
        setRangeTextView(range_seekbar.progress)
        range_seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                setRangeTextView(progress)
            }
        })
    }

    /**
     * Called when submit button is pressed
     */
    private fun onSubmit() {
        // retrieve content of the form
        val selectedTypeId = type_radio_group.checkedRadioButtonId
        val type = view?.findViewById<RadioButton>(selectedTypeId)
            ?.tag // the tag is how we identify the type inside data object
        val title = title_edit_text.text.toString()
        val location = selectedLocation?.city
        val range = range_seekbar.progress
        val salary = salary_edit_text.text.toString()
        val description = description_edit_text.text.toString()

        // check for required fields
        if (type == null) Log.e(TAG, "null radio button selection")
        title_view.error = if (title.isEmpty()) "Title is required" else null

        Log.d(TAG, "$type, $title, $location, $range, $salary, $description")
        // TODO submit content
    }

    /**
     * Called when apply button is pressed in LocationPickerFragment
     */
    override fun onLocationPicked(location: Location) {
        val locationText =
            if (location.city != null) location.city
            else getString(R.string.coordinates, location.latitude.toString(), location.longitude.toString())
        location_edit_text.setText(locationText)
        selectedLocation = location
    }

    /**
     * For setting the value next to seek bar
     * @param value The value corresponding to the seekbar position
     */
    private fun setRangeTextView(value: Int) {
        range_value.text = getString(R.string.distance, value)
    }
}
