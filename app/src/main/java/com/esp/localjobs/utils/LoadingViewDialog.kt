package com.esp.localjobs.utils

import android.app.Activity
import android.app.Dialog
import android.view.Window
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.esp.localjobs.R

class LoadingViewDialog(private val activity: Activity) {

    private lateinit var dialog: Dialog

    fun showDialog() {
        dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        // set cancelable false so that it's never get hidden
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.loading)
        val gifImageView: ImageView = dialog.findViewById(R.id.loading_gif)

        val imageViewTarget = GlideDrawableImageViewTarget(gifImageView)

        Glide.with(activity)
            .load(R.drawable.loading)
            .placeholder(R.drawable.loading)
            .centerCrop()
            .crossFade()
            .into(imageViewTarget)

        dialog.show()
    }

    fun hideDialog() {
        dialog.dismiss()
    }
}
