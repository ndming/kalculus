package com.flyng.kalculus.core.lifecycle

import android.animation.Animator
import androidx.core.animation.doOnEnd
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

open class LifecycleAnimation : LifecycleEventObserver {
    private val animators = mutableListOf<Animator>()

    /**
     * Submits the [animator] and fires the animation under lifecycle-awared manner.
     */
    protected fun submit(animator: Animator) {
        animator.apply {
            doOnEnd { animators.remove(it) }
            start()
        }.also { animators.add(it) }
    }

    private fun resume() {
        // resume previously paused infinite animators
        animators.forEach { if (it.isPaused) it.resume() }
    }

    private fun pause() {
        // pause only infinite animators, but NOT finite-duration ones because it would require
        // the finite-duration animators in sync with the surface callback
        animators.forEach {
            if (it.totalDuration == Animator.DURATION_INFINITE) {
                it.pause()
            }
        }
    }

    private fun destroy() {
        // cancel an animator effectively triggers the onEnd callback
        // so the animator gets removed from the animator list
        animators.forEach { it.cancel() }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> resume()
            Lifecycle.Event.ON_PAUSE -> pause()
            Lifecycle.Event.ON_DESTROY -> {
                destroy()
                source.lifecycle.removeObserver(this)
            }

            else -> return
        }
    }
}
