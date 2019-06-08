package com.esp.localjobs.utils

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.animation.doOnEnd

object AnimationsUtils {

    fun popup(view: View) {
        view.scaleX = 0f
        view.scaleY = 0f
        view.animate()
            .setStartDelay(400)
            .setInterpolator(OvershootInterpolator())
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
    }

    fun popout(view: View) {
        view.animate()
            .setInterpolator(OvershootInterpolator())
            .alpha(10f)
            .scaleX(0f)
            .scaleY(0f)
    }

    fun animateToFinalColor(view: View, colorFrom: Int, colorTo: Int, onEnd: () -> Unit = {}) {

        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 150 // milliseconds
        colorAnimation.addUpdateListener { animator -> view.setBackgroundColor(animator.animatedValue as Int) }
        colorAnimation.start()
        colorAnimation.doOnEnd {
            onEnd()
        }
    }
}