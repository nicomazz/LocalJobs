package com.esp.localjobs.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import java.io.ByteArrayOutputStream


object BitmapUtils {
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }

        val bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * Generate a Bitmap from an Android SDK View.
     *
     * @param view the View to be drawn to a Bitmap
     * @return the generated bitmap
     */
    fun viewToBitmap(view: View): Bitmap {
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(measureSpec, measureSpec)

        val measuredWidth = view.measuredWidth
        val measuredHeight = view.measuredHeight

        view.layout(0, 0, measuredWidth, measuredHeight)
        val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.TRANSPARENT)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }


    fun getCompressed(context: Context, uri: Uri): ByteArray {
        val bitmap = decodeImageFromFiles(context, uri, 300, 300)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    fun decodeImageFromFiles(context: Context, uri: Uri, width: Int, height: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)

        val scaleFactor = Math.min(bitmap.width / width, bitmap.height / height);
        val newh = bitmap.height * scaleFactor
        val neww = bitmap.width * scaleFactor

        return Bitmap.createScaledBitmap(bitmap, neww, newh, true)
    }
}