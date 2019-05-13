package com.esp.localjobs

import com.esp.localjobs.models.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Fragment used to set filter params (longitude, latitude, range, text)
 * TODO dialog on back button pressed: discard filter options?
 * TODO add place picker
 */
class FilterResultsFragment : Fragment() {
    private val args: FilterResultsFragmentArgs by navArgs()
    private lateinit var rangeTextView: TextView
    private lateinit var rangeSeekBar: SeekBar
    private lateinit var searchView: SearchView

    private val filterViewModel: FilterViewModel by activityViewModels()

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

        rangeTextView = view.findViewById(R.id.range_text_view)
        rangeSeekBar = view.findViewById(R.id.range_seek_bar)

        // I'm not observing values to avoid loosing changes on screen rotation
        updateView()

        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { onSearchClick() }

        val seekBarHandler = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                rangeTextView.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        }

        rangeSeekBar.max = filterViewModel.MAX_RANGE_KM
        rangeSeekBar.setOnSeekBarChangeListener(seekBarHandler)

        val resetDefaultButton = view.findViewById<Button>(R.id.reset_default_button)
        resetDefaultButton.setOnClickListener {
            filterViewModel.setDefaultValues()
            updateView()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_search, menu)
        searchView = menu.findItem(R.id.action_search_item).actionView as SearchView
        setupSearchView()
    }

    private fun updateView() {
        rangeTextView.text = filterViewModel.range.value.toString()
        rangeSeekBar.progress = filterViewModel.range.value ?: -1
    }

    private fun updateViewModel() {
        filterViewModel.query.value = searchView.query.toString()
        filterViewModel.range.value = rangeTextView.text.toString().toInt()
        filterViewModel.location.value = Location(0.0, 0.0)
    }

    /**
     * Setup search view icon.
     * The search view is expanded by default and focused on fragment creation.
     */
    private fun setupSearchView() {
        searchView.setIconifiedByDefault(false) // expand search view
        searchView.requestFocus()
        val queryTextListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                onSearchClick()
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean { return true }
        }
        searchView.setOnQueryTextListener(queryTextListener)
    }

    /**
     * Update filter viewmodel and navigate back to the calling fragment.
     * Set filterViewModel.userRequestedFilteredResults to true to notify the fragments that the user requested
     * a filtered search.
     */
    private fun onSearchClick() {
        updateViewModel()
        filterViewModel.userRequestedFilteredResults.value = true
        if (args.filteringJobs)
            findNavController().navigate(R.id.action_destination_filter_to_destination_jobs)
        else
            findNavController().navigate(R.id.action_destination_filter_to_destination_proposals)
    }
}
