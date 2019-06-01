package com.esp.localjobs.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.esp.localjobs.R
import com.esp.localjobs.viewModels.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
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
            Picasso.get().load(photoUrl).transform(CropCircleTransformation()).into(profilePicture)
        }

        logout.setOnClickListener {
            viewModel.logOut()
            findNavController().popBackStack()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_navigation, menu)
        for (i in 0.until(menu.size()))
            menu.getItem(i).isVisible = false
    }
}
