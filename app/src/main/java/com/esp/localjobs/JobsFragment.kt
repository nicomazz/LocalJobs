package com.esp.localjobs

import android.os.Bundle
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
import com.esp.localjobs.LoginViewModel.AuthenticationState.UNAUTHENTICATED
import com.esp.localjobs.LoginViewModel.AuthenticationState.AUTHENTICATED
import com.esp.localjobs.LoginViewModel.AuthenticationState.INVALID_AUTHENTICATION
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

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
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_jobs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        loginViewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                AUTHENTICATED -> showWelcomeMessage()
                UNAUTHENTICATED -> navController.navigate(R.id.action_destination_jobs_to_destination_login)
                INVALID_AUTHENTICATION -> TODO()
                else -> TODO()
            }
        })

        // todo fix this
/*        filterViewModel.userRequestedFilteredResults.observe(viewLifecycleOwner, Observer {
            filterViewModel.userRequestedFilteredResults.value = false
            // fetch filtered data and update view
        })*/
    }

    private fun showWelcomeMessage() {
        Snackbar.make(
            activity!!.findViewById<View>(android.R.id.content),
            "Welcome ${FirebaseAuth.getInstance().currentUser?.displayName ?: "error"}",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        val searchView = menu.findItem(R.id.action_search_item).actionView as SearchView
        searchView.setOnSearchClickListener {
            findNavController().navigate(R.id.action_destination_jobs_to_destination_filter)
        }
    }
}
