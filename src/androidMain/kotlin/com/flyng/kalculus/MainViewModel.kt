package com.flyng.kalculus

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.AssetManager
import android.view.animation.LinearInterpolator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.animation.doOnEnd
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

class MainViewModel(context: Context, owner: LifecycleOwner, assetManager: AssetManager) : ViewModel() {
    val profile: LiveData<ThemeProfile>
        get() = _profile

    private val _profile: MutableLiveData<ThemeProfile> by lazy {
        MutableLiveData(ThemeProfile.Firewater)
    }

    fun onProfileChange(value: ThemeProfile) {
        if (value != _profile.value) {
            _profile.postValue(value)
        }
    }

    val mode: LiveData<ThemeMode>
        get() = _mode

    private val _mode: MutableLiveData<ThemeMode> by lazy {
        MutableLiveData<ThemeMode>(ThemeMode.Light)
    }

    fun onThemeModeChange(value: ThemeMode) {
        if (value != _mode.value) {
            _mode.postValue(value)
        }
    }

    val core = CoreEngine(
        context, assetManager,
        initialProfile = profile.value ?: ThemeProfile.Firewater,
        initialMode = mode.value ?: ThemeMode.Light
    )

    private val fsModel = FourierSeriesModel(core)
    private val samples = SvgSampler.fromSvgData(SvgLoader.fromAssets(assetManager, "samples/vietnam.svg"))
    private val normalDuration = 1000 * samples.size / (SvgSampler.SAMPLING_FACTOR * 10)

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


    var arrows by mutableStateOf(0)
        private set
    var playing by mutableStateOf(false)
        private set
    var durationScale by mutableStateOf(1.0f)
        private set
    var follow by mutableStateOf(false)
        private set

    private var coordX = 0.0f
    private var coordY = 0.0f

    private var makeSegment = true

    private val animator: ValueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f).apply {
        interpolator = LinearInterpolator()
        duration = (normalDuration / durationScale).toLong()
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            val (x, y) = fsModel.transition(it.animatedValue as Float)
            if (makeSegment) {
                createSegment(x, y)
            }
            coordX = x
            coordY = y
            if (follow) {
                core.cameraManager.centerAt(x, y)
            }
        }
    }

    private fun createSegment(x: Float, y: Float) {
        val seg = Segment2D.Builder()
            .begin(coordX, coordY)
            .final(x, y)
            .width(SEGMENT_SCALE_FACTOR * CameraManager.STANDARD_VIEW_PORT.toFloat() / core.cameraManager.zoom)
            .color(ColorType.SPOT)
            .build()
        val id = core.render(seg)
        core.meshManager[id]?.instances?.let { instances ->
            val alphaAnimator = ValueAnimator.ofFloat(1.0f, 0.0f).apply {
                interpolator = LinearInterpolator()
                duration = (normalDuration / durationScale).toLong()
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
        fsModel.transition(animator.animatedValue as Float)

        arrows += 1

        if (animator.isPaused && playing) animator.resume()
    }

    fun dropArrow() {
        if (arrows > 0) {
            fsModel.drop()
            arrows -= 1
        }
    }

    fun animate() {
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
        makeSegment = false
        durationScale = factor
        animator.duration = (normalDuration / durationScale).toLong()
    }

    fun applySpeed() {
        makeSegment = true
    }

    fun toggleCamera() {
        follow = !follow
        if (!follow) {
            core.cameraManager.reset()
        }
    }

    class Factory(
        private val context: Context,
        private val owner: LifecycleOwner,
        private val assetManager: AssetManager,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(context, owner, assetManager) as T
        }
    }

    companion object {
        private const val SEGMENT_SCALE_FACTOR = 0.02f
    }
}
