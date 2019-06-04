package com.esp.localjobs.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels

import com.esp.localjobs.R
import com.esp.localjobs.viewModels.FilterViewModel
import kotlinx.android.synthetic.main.fragment_filter_status.*

class FilterStatusFragment : Fragment() {

    private val filterViewModel: FilterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filter_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val locationName = filterViewModel.getLocation(context!!)?.city ?: getString(R.string.unknown_location)
        val range = filterViewModel.range
        location_status.setText(getString(R.string.location_status, range, locationName))
    }
}
