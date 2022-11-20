package com.flyng.kalculus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.flyng.kalculus.theme.ThemeMode
import com.flyng.kalculus.theme.ThemeProfile

class MainViewModel : ViewModel() {
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
}
