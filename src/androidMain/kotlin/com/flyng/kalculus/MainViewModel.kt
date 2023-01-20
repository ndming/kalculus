package com.flyng.kalculus

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.AssetManager
import android.view.animation.LinearInterpolator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnResume
import androidx.lifecycle.*
import com.flyng.kalculus.core.CoreEngine
import com.flyng.kalculus.core.manager.CameraManager
import com.flyng.kalculus.exposition.visual.primitive.ColorType
import com.flyng.kalculus.foundation.calculus.FourierSeries
import com.flyng.kalculus.ingredient.conic.Circle2D
import com.flyng.kalculus.ingredient.curve.Segment2D
import com.flyng.kalculus.ingredient.grid.Grid2D
import com.flyng.kalculus.io.SvgLoader
import com.flyng.kalculus.io.SvgSampler
import com.flyng.kalculus.model.FourierSeriesModel
import com.flyng.kalculus.theme.ThemeMode
import com.flyng.kalculus.theme.ThemeProfile
import kotlin.math.atan2
import kotlin.math.sqrt

class MainViewModel(context: Context, owner: LifecycleOwner) : ViewModel() {
    val profile: LiveData<ThemeProfile>
        get() = _profile

    private val _profile: MutableLiveData<ThemeProfile> by lazy {
        MutableLiveData(ThemeProfile.Firewater)
    }

    fun onProfileToggle() {
        val update = if (profile.value == ThemeProfile.Firewater) ThemeProfile.Naturalist else ThemeProfile.Firewater
        _profile.postValue(update)
    }

    val mode: LiveData<ThemeMode>
        get() = _mode

    private val _mode: MutableLiveData<ThemeMode> by lazy {
        MutableLiveData<ThemeMode>(ThemeMode.Light)
    }

    fun onModeToggle() {
        val update = if (mode.value == ThemeMode.Light) ThemeMode.Dark else ThemeMode.Light
        _mode.postValue(update)
    }

    val core = CoreEngine(context,profile.value ?: ThemeProfile.Firewater,mode.value ?: ThemeMode.Light)

    private val bundles = context.assets.list("bundles")!!
    private var currentBundle = 0

    private val fsModel = FourierSeriesModel(core)
    private var samples = SvgSampler.fromSvgData(SvgLoader.fromAssets(context.assets, "bundles/${bundles[currentBundle]}"))

    private var baseDuration = 1000 * samples.size.toFloat() / (SvgSampler.SAMPLING_FACTOR * 10)

    var arrows by mutableStateOf(0)
        private set
    var playing by mutableStateOf(false)
        private set
    var durationScale by mutableStateOf(1.0f)
        private set
    var following by mutableStateOf(false)
        private set
    var caching by mutableStateOf(false)
        private set

    private var px = 0.0f
    private var py = 0.0f

    private var tracing = true

    init {
        // bind the core to the owner's lifecycle
        owner.lifecycle.addObserver(core)

        // observe the global change of profile
        profile.observe(owner, object : Observer<ThemeProfile> {
            var firstCall = true
            override fun onChanged(profile: ThemeProfile) {
                if (firstCall) {
                    firstCall = false
                } else {
                    core.themeManager.setProfile(profile)
                }
            }
        })

        // observe the global change of mode
        mode.observe(owner, object : Observer<ThemeMode> {
            var firstCall = true
            override fun onChanged(themeMode: ThemeMode) {
                if (firstCall) {
                    firstCall = false
                } else {
                    core.themeManager.setMode(themeMode)
                }
            }
        })

        val grid = Grid2D.Builder()
            .center(0, 0)
            .spacing(1.0f)
            .color(ColorType.BASE, 0.25f)
            .build()

        val origin = Circle2D.Builder()
            .center(0, 0)
            .radius(0.02f)
            .strokeWidth(0.1f)
            .build()

        val axisX = Segment2D.Builder()
            .begin(-100, 0)
            .final(100, 0)
            .build()

        val axisY = Segment2D.Builder()
            .begin(0, -100)
            .final(0, 100)
            .build()

        core.render(grid + origin + axisX + axisY)
    }

    private val animator: ValueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f).apply {
        interpolator = LinearInterpolator()
        duration = (baseDuration / durationScale).toLong()
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            val (x, y) = fsModel.transition(it.animatedValue as Float)
            val dist = sqrt((x - px) * (x - px) + (y - py) * (y - py))
            if (tracing && dist < CUTOFF_DISTANCE) { trace(x, y) }
            px = x
            py = y
            if (following) { core.cameraManager.centerAt(x, y) }
        }
        doOnResume { playing = true }
    }

    private fun trace(x: Float, y: Float) {
        val seg = Segment2D.Builder()
            .begin(px, py)
            .final(x, y)
            .width(SEGMENT_SCALE_FACTOR * CameraManager.STANDARD_VIEW_PORT.toFloat() / core.cameraManager.zoom)
            .color(ColorType.SPOT)
            .build()
        val id = core.render(seg)

        core.meshManager[id]?.instances?.let { instances ->
            val alphaAnimator = ValueAnimator.ofFloat(1.0f, 0.0f).apply {
                interpolator = LinearInterpolator()
                duration = (baseDuration / durationScale).toLong()
                repeatCount = 0
                addUpdateListener { alpha ->
                    instances.forEach { (instance, _) ->
                        if (instance.material.hasParameter("alpha")) {
                            instance.setParameter("alpha", alpha.animatedValue as Float)
                        }
                    }
                }
                doOnEnd { core.destroy(id) }
            }
            core.animationManager.submit(alphaAnimator)
        }
    }

    fun addArrow() {
        animator.pause()

        val n = if (arrows % 2 == 0) arrows / 2 else -(arrows + 1) / 2
        val (nx, ny) = FourierSeries.fromSamplesAt(samples, n)

        val length = sqrt(nx * nx + ny * ny)
        val theta = atan2(ny, nx)

        fsModel.transition(0.0f)
        fsModel.attach(length, theta)
        val (x, y) = fsModel.transition(animator.animatedValue as Float)
        if (following) { core.cameraManager.centerAt(x, y) }

        arrows += 1

        if (animator.isPaused && playing) animator.resume()
    }

    fun dropArrow() {
        if (arrows > 0) {
            fsModel.drop()
            arrows -= 1
        }
    }

    fun togglePlaying() {
        playing = if (!animator.isStarted) {
            core.animationManager.submit(animator)
            true
        } else if (!animator.isPaused) {
            animator.pause()
            false
        } else {
            animator.resume()
            true
        }
    }

    fun setSpeed(factor: Float) {
        tracing = false
        durationScale = factor
        animator.duration = (baseDuration / durationScale).toLong()
    }

    fun applySpeed() { tracing = true }

    fun toggleCamera() {
        following = !following
        if (!following) {
            core.cameraManager.reset()
        }
    }

    fun applySampleChange(index: Int, assets: AssetManager) {
        if (index != currentBundle) {
            animator.cancel()
            currentBundle = index
            fsModel.clear()
            samples = SvgSampler.fromSvgData(SvgLoader.fromAssets(assets, "bundles/${bundles[currentBundle]}"))
            baseDuration = 1000 * samples.size.toFloat() / (SvgSampler.SAMPLING_FACTOR * 10)
            arrows = 0
            playing = false
            durationScale = 1.0f
            following = false
            px = 0.0f
            py = 0.0f
            tracing = true
            animator.currentPlayTime = 0
            animator.duration = (baseDuration / durationScale).toLong()
        }
    }

    class Factory(
        private val context: Context,
        private val owner: LifecycleOwner,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(context, owner) as T
        }
    }

    companion object {
        private const val SEGMENT_SCALE_FACTOR = 0.015f
        private const val CUTOFF_DISTANCE = 1.0f
    }
}
