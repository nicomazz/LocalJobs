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
import com.esp.localjobs.viewModels.LoginViewModel
import com.esp.localjobs.viewModels.LoginViewModel.AuthenticationState.AUTHENTICATED
import com.esp.localjobs.viewModels.LoginViewModel.AuthenticationState.INVALID_AUTHENTICATION
import com.esp.localjobs.viewModels.LoginViewModel.AuthenticationState.UNAUTHENTICATED
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.models.Location
import com.esp.localjobs.managers.GeoHashUtils
import com.esp.localjobs.viewModels.AddViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.fragment_add.*

private const val TAG = "AddFragment"

/**
 * Fragment used to push a job/proposal to remote db
 */
class AddFragment : Fragment(), LocationPickerFragment.OnLocationPickedListener {
    private val addViewModel: AddViewModel by activityViewModels()
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
        setupRadioButton()
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
                val locationPickerFragment = LocationPickerFragment(this, null)
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

    private fun setupRadioButton() {
        type_radio_group.setOnCheckedChangeListener { _, checkedId ->
            val type = view?.findViewById<RadioButton>(checkedId)?.tag
            when (type) {
                "job" -> range_div.visibility = View.GONE
                "proposal" -> range_div.visibility = View.VISIBLE
                else -> TODO()
            }
        }
    }

    /**
     * Called when submit button is pressed
     */
    private fun onSubmit() {
        if (!validateForm())
            return

        // retrieve content of the form
        val selectedTypeId = type_radio_group.checkedRadioButtonId
        val type = view?.findViewById<RadioButton>(selectedTypeId)
            ?.tag // the tag is how we identify the type inside data object
        val title = title_edit_text.text.toString()
        // val location = selectedLocation?.latitude.toString() + ' ' + selectedLocation?.longitude.toString()
        val location = GeoPoint(selectedLocation!!.latitude, selectedLocation!!.longitude)
        val city = location_edit_text.text.toString()
        val range = range_seekbar.progress.toString()
        val salary = salary_edit_text.text.toString()
        val description = description_edit_text.text.toString()

        Log.d(TAG, "$type, $title, $location, $range, $salary, $description")

        // TODO replace snackbars with a loading bar
        addViewModel.status.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                AddViewModel.AddStatus.WAITING -> {
                    // show loading
                }
                AddViewModel.AddStatus.SUCCESS -> {
                    Snackbar.make(
                        activity!!.findViewById<View>(android.R.id.content),
                        getString(R.string.add_job_success),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack()
                }
                AddViewModel.AddStatus.FAILURE -> {

                    Snackbar.make(
                        activity!!.findViewById<View>(android.R.id.content),
                        getString(R.string.add_job_failure),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        })


        when (type) {
            "job" -> {
                val job = Job(
                    title,
                    description,
                    GeoHashUtils.encode(location.latitude, location.longitude),
                    listOf(location.latitude, location.longitude),
                    city,
                    salary,
                    false,
                    loginViewModel.getUserId()
                )
                addViewModel.addJobToRepository(job)
            }
            "proposal" -> {
                // Proposal(title, description, location, city, salary, range, true, "uid")
            }
            else -> TODO()
        }
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

    private fun validateForm(): Boolean {
        var anyError = false
        if (selectedLocation == null) {
            location_view.error = "Please pick a location"
            anyError = true
        }
        if (title_edit_text.text.toString().isEmpty()) {
            title_view.error = "Please insert a title"
            anyError = true
        }
        return !anyError
    }
}
