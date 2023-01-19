package com.flyng.kalculus.core.manager

import android.animation.Animator
import androidx.core.animation.doOnEnd
import androidx.lifecycle.Lifecycle
import com.flyng.kalculus.core.CoreEngine

/**
 * The [CoreEngine] uses this class to conveniently manage the [Animator]s under a lifecycle-aware maner when the host
 * activity for fragment changes lifecycle event.
 */
class AnimationManager {
    private val animators = mutableListOf<AnimatorPack>()

    /**
     * Puts the [animator] under lifecycle-aware management and starts it. When the animator ends either implicitly or
     * explicitly, the manager removes the animator from the internal management list.
     */
    fun submit(animator: Animator) {
        animator.apply {
            val pack = AnimatorPack(this)
            doOnEnd { animators.remove(pack) }
            if (!isStarted) start()
            animators.add(pack)
        }
    }

    /**
     * Resumes all previously paused animators, should be called when the host activity or fragment transitions to
     * [Lifecycle.Event.ON_RESUME].
     */
    fun resume() {
        animators.forEach { (animator, wasRunning) -> if (wasRunning) animator.resume() }
    }

    /**
     * Pauses all the ongoing animators, should be called when the host activity or fragment transitions to
     * [Lifecycle.Event.ON_PAUSE] to prevent infinite-duration and/or expensive animations from exhausting performance.
     */
    fun pause() {
        animators.forEach { pack ->
            pack.wasRunning = pack.animator.isRunning
            pack.animator.pause()
        }
    }

    /**
     * Cancels all animations that are currently managed by this manager, should be call when the host activity or
     * fragment transitions to [Lifecycle.Event.ON_DESTROY].
     */
    fun cancel() {
        // remove listeners before canceling to prevent concurrent exception
        animators.forEach { (animator, _) ->
            animator.removeAllListeners()
            animator.cancel()
        }
    }

    private data class AnimatorPack(val animator: Animator, var wasRunning: Boolean = true)
}