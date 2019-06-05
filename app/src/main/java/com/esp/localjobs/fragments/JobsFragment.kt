package com.esp.localjobs.fragments

import android.content.Context
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
import androidx.navigation.fragment.findNavController
import com.esp.localjobs.R
import com.esp.localjobs.adapters.JobItem
import com.esp.localjobs.fragments.FiltersFragment.Companion.FILTER_FRAGMENT_TAG
import com.esp.localjobs.viewModels.FilterViewModel
import com.esp.localjobs.viewModels.JobsViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_filter_status.*
import kotlinx.android.synthetic.main.fragment_jobs.view.*

/**
 * Fragment used to display a list of jobs
 */
class JobsFragment : Fragment() {

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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        loadJobs()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI(view)

        observeChangesInJobList()
        observeFilters()
    }

    private fun setupUI(view: View) = with(view) {
        job_list.adapter = adapter
        fabAdd.setOnClickListener {
            findNavController().navigate(R.id.destination_add)
        }
        filters_button.setOnClickListener {
            FiltersFragment().show(fragmentManager!!, FILTER_FRAGMENT_TAG)
        }

        postponeEnterTransition()
        viewTreeObserver
            .addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
    }

    private fun observeChangesInJobList() {
        jobsViewModel.jobs.observe(viewLifecycleOwner, Observer { jobs ->
            Log.d("JobFragment", "reported ${jobs?.size ?: 0} jobs")
            adapter.update(jobs?.map { JobItem(it) } ?: listOf())
        })
    }

    private fun observeFilters() {
        filterViewModel.activeFilters.observe(viewLifecycleOwner, Observer {
            Log.d("JobFragment", "Filters changed!")
            loadJobs()
            updateFilterUI()
        })
    }

    private fun loadJobs() {
        // Listen for jobs near user selected location or his last known position.
        // If the location is null ( which is an edge case, like a factory reset ) then load all jobs
        filterViewModel.location?.let {
            jobsViewModel.loadJobs(
                it,
                filterViewModel.range.toDouble()
            )
        } ?: jobsViewModel.loadJobs()
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
                    if (query != null) onSearchClick(query)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return true
                }
            })
        }
    }

    /**
     * Update filter viewmodel and refresh jobs
     * Set filterViewModel.userRequestedFilteredResults to true to notify the fragments that the user requested
     * a filtered search.
     */
    private fun onSearchClick(query: String) {
        filterViewModel.setQuery(query)
        // loadJobs() this is done automatically thanks to the filterLiveData
    }
}
