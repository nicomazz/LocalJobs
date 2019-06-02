package com.esp.localjobs.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.SeekBar
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.esp.localjobs.viewModels.FilterViewModel
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Location
import com.esp.localjobs.fragments.map.LocationPickerFragment
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_add.range_value
import kotlinx.android.synthetic.main.fragment_filter_results.*
import kotlinx.android.synthetic.main.fragment_filter_results.type_radio_group

/**
 * Fragment used to set filter params (longitude, latitude, range, text)
 */
class FilterResultsFragment : Fragment(), View.OnClickListener, LocationPickerFragment.OnLocationPickedListener {
    // private val args: FilterResultsFragmentArgs by navArgs()
    private val filterViewModel: FilterViewModel by activityViewModels()

    private var userSelectedLocation: Location? = null
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_filter_results, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateView()
        setupSeekBar()
        setClickListeners()
    }

    /**
     * Called when apply button is pressed in LocationPickerFragment
     */
    override fun onLocationPicked(location: Location) {
        userSelectedLocation = location
        val locationText =
            if (location.city != null) location.city
            else getString(R.string.coordinates, location.l[0].toString(), location.l[1].toString())
        filter_location_edit_text.setText(locationText)
    }

    private fun updateView() {
        val checkedId = if (filterViewModel.filteringJobs) R.id.radio_job else R.id.radio_proposal
        type_radio_group.check(checkedId)
        range_value.text = filterViewModel.range.toString()
        range_seek_bar.progress = filterViewModel.range
        min_salary_edit_text.setText(filterViewModel.minSalary.toString())
        filterViewModel.location?.let {
            val locationText =
                if (it.city != null) it.city
                else getString(R.string.coordinates, it.l[0].toString(), it.l[1].toString())
            filter_location_edit_text.setText(locationText)
        }
    }

    private fun updateViewModel() {
        // check selected radio type
        val userSelectedJob = type_radio_group.checkedRadioButtonId == R.id.radio_job

        filterViewModel.apply {
            filteringJobs = userSelectedJob
            query = searchView.query.toString()
            range = range_value.text.toString().toInt()
            minSalary = min_salary_edit_text.text.toString().toFloat().toInt()
            userSelectedLocation?.let { selectedLocation ->
                location = selectedLocation
            }
        }
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
            R.id.filter_location_edit_text -> {
                // show location picker dialog
                activity?.supportFragmentManager?.let { fm ->
                    val locationPickerFragment =
                        LocationPickerFragment(this, filterViewModel.location)
                    locationPickerFragment.show(fm, LocationPickerFragment.TAG)
                }
            }
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
        searchView.apply {
            setIconifiedByDefault(false) // expand search view
            requestFocus()
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

    private fun setupSeekBar() {
        with(range_seek_bar) {
            max = filterViewModel.MAX_RANGE_KM
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    range_value.text = progress.toString()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }

    private fun setClickListeners() {
        fab.setOnClickListener(this)
        filter_location_edit_text.setOnClickListener(this)
    }
}
