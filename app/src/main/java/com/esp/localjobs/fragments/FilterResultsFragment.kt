package com.esp.localjobs.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.esp.localjobs.viewModels.FilterViewModel
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Location
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

/**
 * Fragment used to set filter params (longitude, latitude, range, text)
 */
class FilterResultsFragment : Fragment(), View.OnClickListener, LocationPickerFragment.OnLocationPickedListener {
    private val args: FilterResultsFragmentArgs by navArgs()
    private lateinit var rangeTextView: TextView
    private lateinit var rangeSeekBar: SeekBar
    private lateinit var searchView: SearchView
    private lateinit var minSalaryEditText: TextInputEditText
    private lateinit var locationEditText: TextInputEditText
    private var userSelectedLocation: Location? = null
    private val filterViewModel: FilterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_filter_results, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rangeTextView = view.findViewById(R.id.range_value)
        rangeSeekBar = view.findViewById(R.id.range_seek_bar)
        minSalaryEditText = view.findViewById(R.id.min_salary_edit_text)
        locationEditText = view.findViewById(R.id.location_edit_text)

        // I'm not observing values to avoid loosing changes on screen rotation
        updateView()

        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener(this)

        val addressEditText = view.findViewById<TextInputEditText>(R.id.location_edit_text)
        addressEditText.setOnClickListener {
            val fm = activity?.supportFragmentManager
            if (fm != null) {
                val locationPickerFragment = LocationPickerFragment(this)
                locationPickerFragment.show(fm, "location_picker_fragment")
            }
        }

        rangeSeekBar.max = filterViewModel.MAX_RANGE_KM
        rangeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                rangeTextView.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    /**
     * Called when apply button is pressed in LocationPickerFragment
     */
    override fun onLocationPicked(location: Location) {
        userSelectedLocation = location
        val locationText =
            if(location.city != null) location.city
            else getString(R.string.coordinates, location.latitude.toString(), location.longitude.toString())
        locationEditText.setText(locationText)
    }

    private fun updateView() {
        rangeTextView.text = filterViewModel.range.toString()
        rangeSeekBar.progress = filterViewModel.range
        minSalaryEditText.setText(filterViewModel.minSalary.toString())
        locationEditText.setText(filterViewModel.location?.city ?: "")
    }

    private fun updateViewModel() {
        filterViewModel.query = searchView.query.toString()
        filterViewModel.range = rangeTextView.text.toString().toInt()
        filterViewModel.minSalary = minSalaryEditText.text.toString().toInt()
        filterViewModel.location = userSelectedLocation
    }

    /**
     * Update filter viewmodel and navigate back to the calling fragment.
     * Set filterViewModel.userRequestedFilteredResults to true to notify the fragments that the user requested
     * a filtered search.
     */
    private fun onSearchClick() {
        updateViewModel()
        filterViewModel.userRequestedFilteredResults.value = true
        findNavController().popBackStack()
    }

    /**
     * Handle view items click
     */
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fab -> onSearchClick()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_search, menu)
        searchView = menu.findItem(R.id.action_search_item).actionView as SearchView
        setupSearchView()
    }

    /**
     * Setup search view icon.
     * The search view is expanded by default and focused on fragment creation.
     */
    private fun setupSearchView() {
        searchView.setIconifiedByDefault(false) // expand search view
        searchView.requestFocus()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                onSearchClick()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }
}
