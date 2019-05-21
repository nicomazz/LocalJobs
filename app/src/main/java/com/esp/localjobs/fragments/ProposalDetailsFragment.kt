package com.esp.localjobs.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.esp.localjobs.FilterViewModel
import com.esp.localjobs.R

class ProposalDetailsFragment : Fragment() {
    private val args: ProposalDetailsFragmentArgs by navArgs()
    private val filterViewModel: FilterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_proposal_details, container, false)
    }
}
