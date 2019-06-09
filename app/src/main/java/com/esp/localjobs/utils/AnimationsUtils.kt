package com.esp.localjobs.utils

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.animation.doOnEnd

object AnimationsUtils {

    fun popup(view: View, delay: Long = 400) {
        view.scaleX = 0f
        view.scaleY = 0f
        view.animate()
            .setStartDelay(delay)
            .setInterpolator(OvershootInterpolator())
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
    }

    fun popout(view: View, then: () -> Unit = {}) {
        view.animate()
            .setStartDelay(0L)
            .setDuration(100L)
            .setInterpolator(LinearInterpolator())
            .alpha(0f)
            .scaleX(0f)
            .scaleY(0f)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    then()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }
            })
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