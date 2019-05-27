package com.esp.localjobs.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.esp.localjobs.viewModels.LoginViewModel
import com.esp.localjobs.R
import com.esp.localjobs.adapters.JobItem
import com.esp.localjobs.viewModels.JobsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_jobs.view.*
import kotlinx.android.synthetic.main.fragment_user_profile.*

/**
 * Fragment used to display the details of any user.
 * If the userID given is null the fragment will show the current user data allowing him to edit photos and description.
 *
 * Params:
 * userID: String? -> the ID of any user.
 */
class UserProfileFragment : Fragment() {
    private val args: UserProfileFragmentArgs by navArgs()

    private val viewModel: LoginViewModel by activityViewModels()
    private val myJobsViewModel: JobsViewModel by activityViewModels()

    private val adapter = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        name.text = getString(R.string.not_logged_in)
        logout.visibility = View.GONE
        FirebaseAuth.getInstance().currentUser?.run {
            name.text = displayName
            mail.text = email
            phone.text = phoneNumber
            logout.visibility = View.VISIBLE
        }

        logout.setOnClickListener {
            viewModel.logOut()
            findNavController().popBackStack()
        }

        // setup job list
        view.jobList.adapter = adapter

        myJobsViewModel.jobs.observe(viewLifecycleOwner, Observer { jobs ->
            adapter.update(jobs?.map { JobItem(it) } ?: listOf())
        })
        myJobsViewModel.loadJobs()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_navigation, menu)
        for (i in 0.until(menu.size()))
            menu.getItem(i).isVisible = false
    }
}
