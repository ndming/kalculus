package com.flyng.kalculus.core.manager

import android.animation.AnimatorSet
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import com.flyng.kalculus.core.lifecycle.LifecycleAnimation
import com.google.android.filament.Camera
import com.google.android.filament.View

class CameraManager(private val camera: Camera, private val view: View) : LifecycleAnimation() {
    private var currentZoom = 1.0f
    private var currentOffsetX = 0.0f
    private var currentOffsetY = 0.0f

    fun zoom(amount: Float, faciliate: Boolean = true) {
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
        currentOffsetX += x / view.viewport.width
        currentOffsetY += VERTICAL_ORIENTATION * y / view.viewport.height
        camera.setShift(currentOffsetX.toDouble(), currentOffsetY.toDouble())
    }

    fun resize(width: Int, height: Int) {
        val svp = STANDARD_VIEW_PORT
        if (width < height) {
            val ratio = height.toDouble() / width.toDouble()
            camera.setProjection(
                Camera.Projection.ORTHO,
                -svp, svp, -svp * ratio, svp * ratio, 0.0, 1.0
            )
        } else {
            val ratio = width.toDouble() / height.toDouble()
            camera.setProjection(
                Camera.Projection.ORTHO,
                -svp * ratio, svp * ratio, -svp, svp, 0.0, 1.0
            )
        }
    }

    fun reset() {
        val zoomAnimator = ValueAnimator.ofFloat(currentZoom, 1.0f).apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = SNAP_ORIGIN_DURATION
            repeatCount = 0
            addUpdateListener {
                currentZoom = it.animatedValue as Float
                camera.setScaling(currentZoom.toDouble(), currentZoom.toDouble())
            }
        }
        val offsetAnimator = ValueAnimator.ofObject(
            OffsetEvaluator(), Offset(currentOffsetX, currentOffsetY), Offset(0.0f, 0.0f)
        ).apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = SNAP_ORIGIN_DURATION
            repeatCount = 0
            addUpdateListener {
                currentOffsetX = (it.animatedValue as Offset).x
                currentOffsetY = (it.animatedValue as Offset).y
                camera.setShift(currentOffsetX.toDouble(), currentOffsetY.toDouble())
            }
        }
        val orginSnap = AnimatorSet().apply {
            play(zoomAnimator).with(offsetAnimator)
        }
        // submit animator to lifecycle-awared animation
        submit(orginSnap)
    }

    companion object {
        private const val STANDARD_VIEW_PORT = 1.0
        private const val ZOOM_ENHACE_FACTOR = 1.0f
        private const val VERTICAL_ORIENTATION = -1.0f
        private const val SNAP_ORIGIN_DURATION = 500L
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