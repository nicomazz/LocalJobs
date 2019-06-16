package com.esp.localjobs.adapters

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.BindingAdapter
import androidx.navigation.Navigation
import com.esp.localjobs.LocalJobsApplication
import com.esp.localjobs.R
import com.esp.localjobs.data.repository.userFirebaseRepository
import com.esp.localjobs.databinding.ItemUserBinding
import com.esp.localjobs.fragments.JobDetailsFragmentDirections
import com.squareup.picasso.Picasso
import com.xwray.groupie.databinding.BindableItem
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch

@InternalCoroutinesApi
class UserItem(val userId: String) : BindableItem<ItemUserBinding>() {

    override fun getId() = userId.hashCode().toLong()

    override fun bind(viewBinding: ItemUserBinding, position: Int) {
        with(viewBinding) {
            GlobalScope.launch(Dispatchers.Main) {
                val firebaseUser = userFirebaseRepository.getUserDetails(userId)
                user = firebaseUser

                firebaseUser?.mail?.let { mail ->
                    mailIcon.visibility = View.VISIBLE
                    mailIcon.setOnClickListener { sendMail(mail) }
                } ?: Log.e("userItem", "No mail found")

                mainLayout.setOnClickListener { navigateToUserProfile(it, userId) }
            }
        }
    }

    override fun getLayout() = R.layout.item_user
}

fun sendMail(destination: String) {
    val intent = Intent(Intent.ACTION_SENDTO) // it's not ACTION_SEND
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_SUBJECT, "Interest for your post")
    intent.putExtra(Intent.EXTRA_TEXT, "Body of email")
    intent.data = Uri.parse("mailto:$destination") // or just "mailto:" for blank
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // this will make such that when user returns to your app, your app is displayed, instead of the email app.
    startActivity(LocalJobsApplication.applicationContext(), intent, null)
}

fun navigateToUserProfile(view: View, userId: String?) {
    val action =
        JobDetailsFragmentDirections.actionDestinationJobDetailsToDestinationUserProfile(userId ?: "NonExistingId")
    Navigation.findNavController(view)
        .navigate(
            R.id.action_destination_job_details_to_destination_user_profile,
            action.arguments
        )
}

@BindingAdapter("avatar")
fun ImageView.setAvatar(avatar: String?) {
    if (avatar == null || avatar.isEmpty())
        Picasso.get().load(R.drawable.default_profile).transform(CropCircleTransformation()).into(this)
    else
        Picasso.get().load(avatar).transform(CropCircleTransformation()).into(this)
}
