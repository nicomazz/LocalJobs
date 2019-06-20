package com.esp.localjobs.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.esp.localjobs.R

class CircleOverlayView : FrameLayout {

    var radius: Float = 0f
    private var bitmap: Bitmap? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        if (radius != 0f) {
            createWindowFrame(radius)
            canvas.drawBitmap(bitmap!!, 0f, 0f, null)
        }
    }

    override fun isInEditMode(): Boolean {
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        bitmap = null
    }

    private fun createWindowFrame(radius: Float) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val osCanvas = Canvas(bitmap!!)

        val outerRectangle = RectF(0f, 0f, width.toFloat(), height.toFloat())

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.TRANSPARENT
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
        osCanvas.drawRect(outerRectangle, paint)

        paint.color = ContextCompat.getColor(context, R.color.colorPrimary)
        paint.alpha = 99
        val centerX = (width / 2).toFloat()
        val centerY = (height / 2).toFloat()
        osCanvas.drawCircle(centerX, centerY, radius, paint)
    }
}