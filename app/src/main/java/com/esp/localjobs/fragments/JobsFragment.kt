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
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.viewModels.FilterViewModel
import com.esp.localjobs.viewModels.JobsViewModel
import com.esp.localjobs.viewModels.MapViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_jobs.*
import kotlinx.android.synthetic.main.fragment_jobs.view.*

/**
 * Fragment used to display a list of jobs
 */
class JobsFragment : Fragment() {
    private val jobsViewModel: JobsViewModel by activityViewModels()
    private val filterViewModel: FilterViewModel by activityViewModels()
    private val mapViewModel: MapViewModel by activityViewModels()

    private val adapter = GroupAdapter<ViewHolder>()

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
        observeSelectedJob()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupJobList(view)
        setupBottomSheetBehavior()
        setupAddFab(view)
    }

    private val bottomSheetBehavior: BottomSheetBehavior<View>
        get() = BottomSheetBehavior.from(bottom_sheet)

    private fun setupBottomSheetBehavior() {
        /* val displayMetrics = DisplayMetrics()
         activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
         val hh = (displayMetrics.heightPixels * 0.30).toInt()
         Log.d("new height","heihgt $hh")
         bottom_sheet.layoutParams =
             bottom_sheet.layoutParams.apply { height = (displayMetrics.heightPixels * 0.66).toInt() }*/
        bottomSheetBehavior.saveFlags = BottomSheetBehavior.SAVE_ALL
        bottomSheetBehavior
            .setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                var offset: Float = 0f
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    offset = slideOffset
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    mapViewModel.setBottomPadding((bottomSheet.height * offset).toInt())
                }
            })
    }

    private fun setupAddFab(view: View) {
        view.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.destination_add)
        }
    }

    private fun observeSelectedJob() {
        mapViewModel.selectedJob.observe(this, Observer { job ->
            jobsViewModel.jobs.value?.indexOfFirst { it.id == job?.id }?.let {
                if (it >= 0) {
                    jobList.smoothScrollToPosition(it)
                    bottomSheetBehavior.state = STATE_EXPANDED
                }
                // adapter.notifyDataSetChanged()
                updateJobList(jobsViewModel.jobs.value)
            }
        })
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
        itemAnimator = null
    }

    private fun observeChangesInJobList() {
        jobsViewModel.jobs.observe(this, Observer { jobs ->
            Log.d("JobFragment", "reported ${jobs?.size ?: 0} jobs")
            updateJobList(jobs)
        })
    }

    private fun updateJobList(jobs: List<Job>?) {
        val jobItems = jobs?.map {
            val isSelected = it.id == mapViewModel.selectedJob.value?.id
            JobItem(it, isSelected)
        }
        adapter.update(jobItems ?: listOf())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        val searchView = menu.findItem(R.id.action_search_item).actionView as SearchView
        searchView.setOnSearchClickListener {
            findNavController().navigate(R.id.action_destination_jobs_to_destination_filter)
        }
    }
}
