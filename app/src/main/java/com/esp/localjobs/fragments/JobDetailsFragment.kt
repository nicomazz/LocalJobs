package com.esp.localjobs.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import com.esp.localjobs.R
import com.esp.localjobs.databinding.FragmentJobDetailsBinding
import com.esp.localjobs.viewModels.LoginViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_job_details.view.*

/**
 * Fragment used to display the details of a job.
 * Params:
 * jobID: String -> the ID of a job
 */

class JobDetailsFragment : Fragment() {
    private val args: JobDetailsFragmentArgs by navArgs()
    private val loginViewModel: LoginViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        return FragmentJobDetailsBinding.inflate(inflater, container, false).apply {
            job = args.job
        }.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.explode)
        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.explode)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Picasso.get().load("https://picsum.photos/200").into(view.imageView)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // hide other icons (like current user profile
        menu.forEach { it.isVisible = false }
        // if the user owns the job allow him to edit it
        if (args.job.uid != loginViewModel.getUserId())
            return
        inflater.inflate(R.menu.menu_edit, menu)
        val editMenu = menu.findItem(R.id.menu_edit_item)
        editMenu.setOnMenuItemClickListener {
            val action =
                JobDetailsFragmentDirections.actionDestinationJobDetailsToDestinationEdit(args.job)
            findNavController().navigate(action.actionId, action.arguments)
            true
        }
    }
}
