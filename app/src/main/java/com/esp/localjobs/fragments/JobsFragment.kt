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
import com.esp.localjobs.viewModels.FilterViewModel
import com.esp.localjobs.viewModels.JobsViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
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
        observeChangesInJobList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupJobList(view)
        setupAddFab(view)
    }

    private fun setupAddFab(view: View) {
        view.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.destination_add)
        }
    }

    private fun loadJobs() {
        // Listen for jobs near user selected location or his last known position.
        // If the location is null ( which is an edge case, like a factory reset ) then load all jobs
        filterViewModel.getLocation(context!!)?.let {
            jobsViewModel.loadJobs(
                it,
                filterViewModel.range.toDouble()
            )
        } ?: jobsViewModel.loadJobs()
    }

    private fun setupJobList(view: View) = with(view.jobList) {
        adapter = this@JobsFragment.adapter
    }

    private fun observeChangesInJobList() {
        jobsViewModel.jobs.observe(this, Observer { jobs ->
            Log.d("JobFragment", "reported ${jobs?.size ?: 0} jobs")
            adapter.update(jobs?.map{ JobItem(it) } ?: listOf())
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        val searchView = menu.findItem(R.id.action_search_item).actionView as SearchView
        searchView.setOnSearchClickListener {
            findNavController().navigate(R.id.action_destination_jobs_to_destination_filter)
        }
    }
}
