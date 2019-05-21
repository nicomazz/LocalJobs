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
import com.esp.localjobs.LocationPickerFragment
import com.esp.localjobs.R
import kotlinx.android.synthetic.main.fragment_add.*

private const val TAG = "AddFragment"

/**
 * Fragment used to push a job/proposal to remote db
 */
class AddFragment : Fragment() {

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

        setupDistanceSeekbar()

        // on submit button click
        submit_button.setOnClickListener { onSubmit() }

        setupAddressChooser()
    }

    private fun setupAddressChooser() {
        address_edit_text.setOnClickListener {
            fragmentManager?.let { fm ->
                val locationPickerFragment = LocationPickerFragment()
                locationPickerFragment.show(fm, "location_picker_fragment")
            }
        }
    }

    private fun setupDistanceSeekbar() {
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
        val address = address_edit_text.text.toString()
        val range = range_seekbar.progress
        val salary = salary_edit_text.text.toString()
        val description = description_edit_text.text.toString()

        // check for required fields
        if (type == null) Log.e(TAG, "null radio button selection")
        title_view.error = if (title.isEmpty()) "Title is required" else null

        Log.d(TAG, "$type, $title, $address, $range, $salary, $description")
        // TODO submit content
    }

    /**
     * For setting the value next to seek bar
     * @param value The value corresponding to the seekbar position
     */
    private fun setRangeTextView(value: Int) {
        range_value.text = getString(R.string.distance, value)
    }
}
