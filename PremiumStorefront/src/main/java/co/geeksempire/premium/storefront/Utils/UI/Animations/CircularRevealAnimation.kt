/*
 * Copyright © 2021 By Geeks Empire.
 *
 * Created by Elias Fazel
 * Last modified 5/31/21, 12:37 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package co.geeksempire.premium.storefront.Utils.UI.Animations

import android.animation.Animator
import android.content.Context
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import co.geeksempire.premium.storefront.Utils.UI.Display.displayX
import co.geeksempire.premium.storefront.Utils.UI.Display.displayY
import net.geeksempire.balloon.optionsmenu.library.Utils.dpToInteger
import kotlin.math.hypot

interface AnimationListener {
    fun animationFinished() {}
}

class CircularRevealAnimation (private val animationListener: AnimationListener) {

    fun startForActivityRoot(context: Context, rootView: View, xPosition: Int = (displayX(context) / 2), yPosition: Int = (displayY(context) / 2)) {

        val rootLayout = rootView
        rootLayout.visibility = View.INVISIBLE

        val viewTreeObserver = rootLayout.viewTreeObserver

        if (viewTreeObserver.isAlive) {

            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {

                override fun onGlobalLayout() {

                    val finalRadius = hypot(displayX(context).toDouble(), displayY(context).toDouble())

                    val circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout,
                        xPosition,
                        yPosition,
                        dpToInteger(context, 51).toFloat(),
                        finalRadius.toFloat())

                    circularReveal.duration = 1111
                    circularReveal.interpolator = AccelerateInterpolator()

                    rootLayout.visibility = View.VISIBLE
                    circularReveal.start()

                    rootLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    circularReveal.addListener(object : Animator.AnimatorListener {

                        override fun onAnimationRepeat(animation: Animator) {

                        }

                        override fun onAnimationEnd(animation: Animator) {

                            animationListener.animationFinished()

                            rootLayout.visibility = View.VISIBLE

                        }

                        override fun onAnimationCancel(animation: Animator) {

                        }

                        override fun onAnimationStart(animation: Animator) {

                        }

                    })
                }
            })

        } else {

            rootLayout.visibility = View.VISIBLE

        }

    }

    fun startForView(context: Context, rootView: View, xPosition: Int = (displayX(context) / 2), yPosition: Int = (displayY(context) / 2)) {

        val rootLayout = rootView
        rootLayout.visibility = View.INVISIBLE

        val viewTreeObserver = rootLayout.viewTreeObserver

        if (viewTreeObserver.isAlive) {

            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {

                override fun onGlobalLayout() {

                    val finalRadius = hypot(displayX(context).toDouble(), displayY(context).toDouble())

                    val circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout,
                        xPosition,
                        yPosition,
                        dpToInteger(context, 51).toFloat(),
                        finalRadius.toFloat())

                    circularReveal.duration = 531
                    circularReveal.interpolator = AccelerateInterpolator()

                    rootLayout.visibility = View.VISIBLE
                    circularReveal.start()

                    rootLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    circularReveal.addListener(object : Animator.AnimatorListener {

                        override fun onAnimationRepeat(animation: Animator) {

                        }

                        override fun onAnimationEnd(animation: Animator) {

                            animationListener.animationFinished()

                            rootLayout.visibility = View.VISIBLE

                        }

                        override fun onAnimationCancel(animation: Animator) {

                        }

                        override fun onAnimationStart(animation: Animator) {

                        }

                    })
                }
            })

        } else {

            rootLayout.visibility = View.VISIBLE

        }

    }

}