package com.esp.localjobs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.esp.localjobs.LoginViewModel.AuthenticationState.UNAUTHENTICATED
import com.esp.localjobs.LoginViewModel.AuthenticationState.AUTHENTICATED
import com.esp.localjobs.LoginViewModel.AuthenticationState.INVALID_AUTHENTICATION
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_jobs.*

/**
 * Fragment used to display a list of jobs
 */
class JobsFragment : Fragment() {
    private val loginViewModel: LoginViewModel by activityViewModels()
    private val filterViewModel: FilterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_jobs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        // setup search toolbar
        searchToolbar.inflateMenu(R.menu.menu_search)
        searchToolbar.setOnMenuItemClickListener { menuItem -> handleSearchMenuItemClick(menuItem) }

        loginViewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                AUTHENTICATED -> showWelcomeMessage()
                UNAUTHENTICATED -> navController.navigate(R.id.action_destination_jobs_to_destination_login)
                INVALID_AUTHENTICATION -> TODO()
                else -> TODO()
            }
        })

        filterViewModel.userRequestedFilteredResults.observe(viewLifecycleOwner, Observer {
            filterViewModel.userRequestedFilteredResults.value = false
            // fetch filtered data and update view
        })
    }

    private fun showWelcomeMessage() {
        Snackbar.make(
            activity!!.findViewById<View>(android.R.id.content),
            "Welcome ${FirebaseAuth.getInstance().currentUser?.displayName ?: "error"}",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    /**
     * Handle search toolbar item click.
     * On filter item click: navigate to filter fragment
     */
    private fun handleSearchMenuItemClick(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.filter_search_item -> findNavController().navigate(R.id.action_destination_jobs_to_destination_filter)
            else -> TODO()
        }
        return true
    }
}
