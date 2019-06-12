package com.esp.localjobs.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.esp.localjobs.R
import com.esp.localjobs.adapters.JobItem
import com.esp.localjobs.data.models.Location
import com.esp.localjobs.data.repository.JobsRepository
import com.esp.localjobs.fragments.FiltersFragment.Companion.FILTER_FRAGMENT_TAG
import com.esp.localjobs.fragments.map.LocationPickerFragment
import com.esp.localjobs.viewModels.FilterViewModel
import com.esp.localjobs.viewModels.JobsViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_filter_status.*
import kotlinx.android.synthetic.main.fragment_jobs.view.*

/**
 * Fragment used to display a list of jobs
 */
class JobsFragment : Fragment(), LocationPickerFragment.OnLocationPickedListener {

    private val jobsViewModel: JobsViewModel by activityViewModels()
    private val filterViewModel: FilterViewModel by activityViewModels()

    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_jobs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI(view)

        observeChangesInJobList()
        observeFilters()
    }

    private fun setupUI(view: View) = with(view) {
        job_list.adapter = adapter
        job_list.itemAnimator = null

        fabAdd.setOnClickListener {
            val extras = FragmentNavigatorExtras(
                fabAdd to "toolbar"
            )
            findNavController().navigate(R.id.destination_add, null, null, extras)
        }
        filters_button.setOnClickListener {
            FiltersFragment().show(fragmentManager!!, FILTER_FRAGMENT_TAG)
        }
        location_status.setOnClickListener { onLocationClick() }
        location_icon.setOnClickListener { onLocationClick() }

        postponeEnterTransition()
        viewTreeObserver
            .addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
    }

    private fun observeChangesInJobList() {
        jobsViewModel.jobs.observe(viewLifecycleOwner, Observer { jobs ->
            Log.d(TAG, "reported ${jobs?.size ?: 0} jobs")
            adapter.update(jobs?.map { JobItem(it) } ?: listOf())
        })
    }

    private fun observeFilters() {
        filterViewModel.activeFilters.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "Filters changed!")
            loadJobs(it)
            updateFilterUI()
        })
    }

    private fun loadJobs(filter: JobsRepository.JobFilter) {
        // Listen for jobs near user selected location or his last known position.
        // If the location is null ( which is an edge case, like a factory reset ) then load all jobs
        Log.d(TAG, filter.toString() + "City:  ${filter.location?.city}, LatLng: ${filter.location?.l}")
        adapter.clear() // remove cached items (necessary)
        jobsViewModel.loadJobs(filter)
    }

    private fun updateFilterUI() {
        val locationName = filterViewModel.location?.city ?: getString(R.string.unknown_location)
        val range = filterViewModel.range
        location_status.text = getString(R.string.location_status, range, locationName)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        val searchView = menu.findItem(R.id.action_search_item).actionView as SearchView
        setupSearchView(searchView)
    }

    /**
     * Setup search view icon.
     * The search view is expanded by default and focused on fragment creation.
     */
    private fun setupSearchView(searchView: SearchView) {
        searchView.apply {
            requestFocus()
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        filterViewModel.setQuery(it)
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let {
                        filterViewModel.setQuery(newText)
                    }
                    return true
                }
            })
        }
    }

    /**
     * On click of the top location filter status launch LocationPickerFragment
     */
    private fun onLocationClick() {
        fragmentManager?.let { fm ->
            LocationPickerFragment.newInstanceShow(this, fm, filterViewModel.location)
        }
    }

    override fun onLocationPicked(location: Location) {
        Log.d(TAG, "location: $location")
        filterViewModel.setLocation(location)
    }

    companion object {
        const val TAG = "JobsFragment"
    }
}
