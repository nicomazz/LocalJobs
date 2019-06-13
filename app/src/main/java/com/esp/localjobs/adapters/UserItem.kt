package com.esp.localjobs.adapters

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.BindingAdapter
import com.esp.localjobs.LocalJobsApplication
import com.esp.localjobs.R
import com.esp.localjobs.data.repository.userFirebaseRepository
import com.esp.localjobs.databinding.ItemUserBinding
import com.squareup.picasso.Picasso
import com.xwray.groupie.databinding.BindableItem
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch

@InternalCoroutinesApi
class UserItem(val userId: String, val onClickAction: UserClickListener = UserClickListener.SENDMAIL) : BindableItem<ItemUserBinding>() {
    enum class UserClickListener {
        SENDMAIL,
        GOTOPROFILE
    }

    override fun getId() = userId.hashCode().toLong()

    override fun bind(viewBinding: ItemUserBinding, position: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            val user = userFirebaseRepository.getUserDetails(userId)
            viewBinding.user = user
            viewBinding.mainLayout.setOnClickListener {
                when (onClickAction) {
                    UserClickListener.SENDMAIL -> {
                        user?.mail?.let { sendMail(it) } ?: Log.e("userItem", "No mail found")
                    }
                    UserClickListener.GOTOPROFILE -> {
                        // TODO()
                    }
                }

            }
        }
    }

    override fun getLayout() = R.layout.item_user
}

fun sendMail(destination: String) {
    val intent = Intent(Intent.ACTION_SENDTO) // it's not ACTION_SEND
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_SUBJECT, "Subject of email")
    intent.putExtra(Intent.EXTRA_TEXT, "Body of email")
    intent.data = Uri.parse("mailto:$destination") // or just "mailto:" for blank
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // this will make such that when user returns to your app, your app is displayed, instead of the email app.
    startActivity(LocalJobsApplication.applicationContext(), intent, null)
}

@BindingAdapter("avatar")
fun ImageView.setAvatar(avatar: String?) {
    Picasso.get().load(avatar).transform(CropCircleTransformation()).into(this)
}