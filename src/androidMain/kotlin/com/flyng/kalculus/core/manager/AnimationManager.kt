package com.flyng.kalculus.core.manager

import android.animation.Animator
import androidx.core.animation.doOnEnd
import androidx.lifecycle.Lifecycle
import com.flyng.kalculus.core.CoreEngine

/**
 * The [CoreEngine] uses this class to conveniently manage the [Animator]s under a lifecycle-aware maner when the host
 * activity for fragment changes lifecyle event.
 */
class AnimationManager {
    private val animators = mutableListOf<Animator>()

    /**
     * Puts the [animator] under lifecycle-aware management and starts it. When the animator ends either implicitly or
     * explicitly, the manager removes the animator from the internal management list.
     */
    fun submit(animator: Animator) {
        animator.apply {
            doOnEnd { animators.remove(it) }
            if (!isStarted) start()
            animators.add(this)
        }
    }

    /**
     * Resumes all previously paused animators, should be called when the host activiy or fragment transitions to
     * [Lifecycle.Event.ON_RESUME].
     */
    fun resume() {
        animators.forEach { if (it.isPaused) it.resume() }
    }

    /**
     * Pauses all the ongoing animators, should be called when the host activiy or fragment transitions to
     * [Lifecycle.Event.ON_PAUSE] to prevent infinite-duration and/or expensive animations from exhausting performance.
     */
    fun pause() {
        animators.forEach { it.pause() }
    }

    /**
     * Cancels all animations that are currently managed by this manager, should be call when the host activity or
     * fragment transitions to [Lifecycle.Event.ON_DESTROY].
     */
    fun cancel() {
        // cancel an animator effectively triggers the onEnd callback
        // so the animator gets removed from the animator list
        animators.forEach { it.cancel() }
    }
}