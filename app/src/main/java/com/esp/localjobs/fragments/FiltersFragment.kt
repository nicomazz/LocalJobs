package com.esp.localjobs.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Location
import com.esp.localjobs.data.repository.JobsRepository.JobFilter
import com.esp.localjobs.data.repository.MAX_RANGE_KM
import com.esp.localjobs.fragments.map.LocationPickerFragment
import com.esp.localjobs.viewModels.FilterViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_add.range_value
import kotlinx.android.synthetic.main.fragment_filters.*
import kotlinx.android.synthetic.main.fragment_filters.type_radio_group

/**
 * Fragment used to set filter params (longitude, latitude, range, text)
 */
class FiltersFragment :
    BottomSheetDialogFragment(),
    View.OnClickListener,
    LocationPickerFragment.OnLocationPickedListener {

    // private val args: FilterResultsFragmentArgs by navArgs()
    private val filterViewModel: FilterViewModel by activityViewModels()
    private var userSelectedLocation: Location? = null

    companion object {
        const val FILTER_FRAGMENT_TAG = "filter_fragment_tag"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filters, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filterViewModel.activeFilters.observe(viewLifecycleOwner, Observer {
            updateView(it)
        })

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

    private fun updateView(filters: JobFilter) = with(filters) {
        val checkedId = if (filteringJobs) R.id.radio_job else R.id.radio_proposal
        type_radio_group.check(checkedId)
        range_value.text = range.toString()
        range_seek_bar.progress = range
        min_salary_edit_text.setText(salary?.toString() ?: "")
        location?.let {
            val locationText =
                if (it.city != null) it.city
                else getString(R.string.coordinates, it.l[0].toString(), it.l[1].toString())
            filter_location_edit_text.setText(locationText)
        }
    }

    private fun updateViewModel() {
        // check selected radio type
        val userSelectedJob = type_radio_group.checkedRadioButtonId == R.id.radio_job

        filterViewModel.setFilters(
            JobFilter(
                filteringJobs = userSelectedJob,
                range = range_value.text.toString().toInt(),
                salary = min_salary_edit_text.text.toString().toFloatOrNull(),
                location = userSelectedLocation ?: filterViewModel.location
            )
        )
    }

    /**
     * Update filter viewmodel and navigate back to the calling fragment.
     * Set filterViewModel.userRequestedFilteredResults to true to notify the fragments that the user requested
     * a filtered search.
     */
    private fun onSearchClick() {
        updateViewModel()
        dismiss()
    }

    /**
     * Handle view items click
     */
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.apply_button -> onSearchClick()
            R.id.cancel_button -> dismiss()
            R.id.filter_location_edit_text -> {
                fragmentManager?.let { fm ->
                    LocationPickerFragment.newInstanceShow(this, fm, filterViewModel.location)
                }
            }
        }
    }

    private fun setupSeekBar() {
        // todo check if a material seekbar is available
        with(range_seek_bar) {
            max = MAX_RANGE_KM
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
        apply_button.setOnClickListener(this)
        cancel_button.setOnClickListener(this)
        filter_location_edit_text.setOnClickListener(this)
    }
}
