package com.flyng.kalculus.core.manager

import android.animation.AnimatorSet
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd
import com.google.android.filament.Camera
import com.google.android.filament.View
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Responsible for manipulating the perspective to the render area.
 */
class CameraManager(
    private val camera: Camera,
    private val view: View,
    private val animManager: AnimationManager
) {
    private var currentZoom = 1.0f
    private var currentOffsetX = 0.0f
    private var currentOffsetY = 0.0f

    private val offsetEvaluator = OffsetEvaluator()
    private var originSnap: AnimatorSet? = null

    fun zoom(amount: Float, faciliate: Boolean = true) {
        originSnap?.cancel()

        currentZoom *= (amount * ZOOM_ENHACE_FACTOR)
        camera.setScaling(currentZoom.toDouble(), currentZoom.toDouble())

        if (faciliate) {
            // facilitate the effect of single-point scaling
            currentOffsetX *= amount * ZOOM_ENHACE_FACTOR
            currentOffsetY *= amount * ZOOM_ENHACE_FACTOR
            camera.setShift(currentOffsetX.toDouble(), currentOffsetY.toDouble())
        }
    }

    fun shift(x: Float, y: Float) {
        originSnap?.cancel()

        currentOffsetX += x / view.viewport.width
        currentOffsetY += VERTICAL_ORIENTATION * y / view.viewport.height

        camera.setShift(currentOffsetX.toDouble(), currentOffsetY.toDouble())
    }

    fun reset() {
        // only fire a snap amination when the previous one is done
        if (originSnap == null) {
            val depart = sqrt(currentOffsetX * currentOffsetX + currentOffsetY * currentOffsetY)
            val expand = abs(currentZoom - 1.0f)
            val maxDepart = maxOf(depart, expand).coerceIn(0.25f, 0.8f)


            val duration = (maxDepart * SNAP_ORIGIN_FACTOR).toLong()

            val zoomAnimator = ValueAnimator.ofFloat(currentZoom, 1.0f).apply {
                interpolator = AccelerateDecelerateInterpolator()
                this.duration = duration
                repeatCount = 0
                addUpdateListener {
                    currentZoom = it.animatedValue as Float
                    camera.setScaling(currentZoom.toDouble(), currentZoom.toDouble())
                }
            }
            val offsetAnimator = ValueAnimator.ofObject(
                offsetEvaluator, Offset(currentOffsetX, currentOffsetY), Offset(0.0f, 0.0f)
            ).apply {
                interpolator = AccelerateDecelerateInterpolator()
                this.duration = duration
                repeatCount = 0
                addUpdateListener {
                    currentOffsetX = (it.animatedValue as Offset).x
                    currentOffsetY = (it.animatedValue as Offset).y
                    camera.setShift(currentOffsetX.toDouble(), currentOffsetY.toDouble())
                }
            }

            originSnap = AnimatorSet().apply {
                play(zoomAnimator).with(offsetAnimator)
                doOnEnd { originSnap = null }   // reset the animator set when the animation done
            }
            animManager.submit(originSnap ?: return)
            Log.d(TAG, "snap origin from depart=$depart | expand=$expand")
        }
    }

    companion object {
        const val STANDARD_VIEW_PORT = 4.0
        private const val ZOOM_ENHACE_FACTOR = 1.0f
        private const val VERTICAL_ORIENTATION = -1.0f
        private const val SNAP_ORIGIN_FACTOR = 1500

        private const val TAG = "CoreEngineCameraManager"
    }

    private data class Offset(val x: Float, val y: Float)

    private class OffsetEvaluator : TypeEvaluator<Offset> {
        override fun evaluate(fraction: Float, startValue: Offset, endValue: Offset): Offset {
            return Offset(
                startValue.x + fraction * (endValue.x - startValue.x),
                startValue.y + fraction * (endValue.y - startValue.y)
            )
        }
    }
}