package com.esp.localjobs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Fragment used to set filter params (longitude, latitude, range, tags)
 */
class FilterResultsFragment : Fragment() {
    private val args: FilterResultsFragmentArgs by navArgs()
    private var filteringJobs: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filter_results, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filteringJobs = args.filteringJobs
        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            if (filteringJobs)
                findNavController().navigate(R.id.action_destination_filter_to_destination_jobs)
            else
                findNavController().navigate(R.id.action_destination_filter_to_destination_proposals)
        }
    }
}
