package com.flyng.kalculus

import android.content.Context
import android.content.res.AssetManager
import androidx.lifecycle.*
import com.flyng.kalculus.core.CoreEngine
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

    init {
        owner.lifecycle.addObserver(core)

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
