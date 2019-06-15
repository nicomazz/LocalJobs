package com.esp.localjobs.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.esp.localjobs.R
import com.esp.localjobs.data.repository.userFirebaseRepository
import com.esp.localjobs.databinding.FragmentUserProfileBinding
import com.esp.localjobs.viewModels.LoginViewModel
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.fragment_user_profile.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Fragment used to display the details of any user.
 * If the userID given is null the fragment will show the current user data allowing him to edit photos and description.
 *
 * Params:
 * userID: String? -> the ID of any user.
 */
@InternalCoroutinesApi
class UserProfileFragment : Fragment(), CoroutineScope {
    private val args: UserProfileFragmentArgs by navArgs()

    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main
    private lateinit var binding: FragmentUserProfileBinding

    private val loginViewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_profile, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mJob = Job()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = args.userID

        if (userId == null)
            setupCurrentUserProfile()
        else
            setupUserDetails(userId)
    }

    private fun setupCurrentUserProfile() {
        name.text = getString(R.string.not_logged_in)
        logout.visibility = View.GONE
        login.visibility = View.VISIBLE

        loginViewModel.getCurrentUser()?.let {
            binding.user = it
            logout.visibility = View.VISIBLE
            login.visibility = View.GONE
            Picasso.get().load(it.photoUrl).transform(CropCircleTransformation()).into(profilePicture)
        }

        logout.setOnClickListener {
            loginViewModel.logOut()
            findNavController().popBackStack()
        }
        login.setOnClickListener {
            findNavController().navigate(R.id.destination_login)
        }
    }

    private fun setupUserDetails(userId: String) = launch {
        val user = userFirebaseRepository.getUserDetails(userId)
        if (!isActive)
            return@launch

        binding.user = user
        // todo use binding
        if (user == null || user.photoUrl.isEmpty())
            Picasso.get().load(R.drawable.default_profile).transform(CropCircleTransformation()).into(profilePicture)
        else
            Picasso.get().load(user.photoUrl).transform(CropCircleTransformation()).into(profilePicture)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_navigation, menu)
        for (i in 0.until(menu.size()))
            menu.getItem(i).isVisible = false
    }
}
