package com.flyng.kalculus

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.AssetManager
import android.view.animation.LinearInterpolator
import androidx.lifecycle.*
import com.flyng.kalculus.adapter.StatefulAdapter
import com.flyng.kalculus.core.CoreEngine
import com.flyng.kalculus.exposition.visual.primitive.Color
import com.flyng.kalculus.ingredient.conic.Circle2D
import com.flyng.kalculus.ingredient.grid.Grid2D
import com.flyng.kalculus.ingredient.vector.Vector2D
import com.flyng.kalculus.theme.ThemeMode
import com.flyng.kalculus.theme.ThemeProfile

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

    private val color = core.themeManager.baseColor().let {
        Color(it.red, it.green, it.blue, it.alpha)
    }

    private val vector2D = Vector2D.Builder()
        .head(1, 0)
        .color(color)
        .build()

    private val vecId: Int

    private val grid = Grid2D.Builder()
        .center(0, 0)
        .spacing(1.0f)
        .color(color.copy(alpha = 0.5f))
        .build()

    private val origin = Circle2D.Builder()
        .center(0, 0)
        .radius(0.1f)
        .strokeWidth(0.1f)
        .color(color)
        .build()

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

        vecId = core.render(vector2D)
        core.render(grid + origin)
    }

    private val animator = ValueAnimator.ofFloat(0.0f, 360.0f)
    private fun animate() {
        if (!animator.isStarted) {
            animator.apply {
                interpolator = LinearInterpolator()
                duration = 6000
                repeatMode = ValueAnimator.RESTART
                repeatCount = ValueAnimator.INFINITE
                addUpdateListener { angle ->
                    StatefulAdapter.rotate(
                        vector2D, vecId, core.transformer,
                        0.0f, 0.0f, 1.0f, angle.animatedValue as Float
                    )
                }
            }
            core.animationManager.submit(animator)
        } else if (!animator.isPaused) {
            animator.pause()
        } else {
            animator.resume()
        }
    }

    fun work() {
        animate()
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
}
