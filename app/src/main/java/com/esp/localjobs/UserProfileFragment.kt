package com.esp.localjobs


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userID: String? = args.userID
        //user_profile_text_view.text = userID ?: getCurrentUserID()
        //safe args example
        user_profile_text_view.text = userID ?: "Current user profile"

    }


}
