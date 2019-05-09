package com.esp.localjobs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_jobs.*

/**
 * Fragment used to display a list of proposals
 */
class ProposalsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_proposals, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setup search toolbar
        searchToolbar.inflateMenu(R.menu.menu_search)
        searchToolbar.setOnMenuItemClickListener { menuItem -> handleSearchMenuItemClick(menuItem) }
    }

    /**
     * Handle search toolbar item click.
     * On filter item click: navigate to filter fragment
     */
    private fun handleSearchMenuItemClick(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.filter_search_item -> findNavController().navigate(R.id.action_destination_proposals_to_destination_filter)
            else -> TODO()
        }
        return true
    }
}
