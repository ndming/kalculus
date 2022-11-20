package com.flyng.kalculus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.Observer
import com.flyng.kalculus.core.CoreEngine
import com.flyng.kalculus.ingredient.vector.Vector2D
import com.flyng.kalculus.theme.KalculusTheme
import com.flyng.kalculus.theme.ThemeMode
import com.flyng.kalculus.theme.ThemeProfile
import com.flyng.kalculus.ui.KalculusScreen
import com.google.android.filament.Filament
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val coreEngine = CoreEngine(
            context = this, owner = this, assetManager = assets,
            initialProfile = mainViewModel.profile.value ?: ThemeProfile.Firewater,
            initialMode = mainViewModel.mode.value ?: ThemeMode.Light
        )

        mainViewModel.profile.observe(this, object : Observer<ThemeProfile> {
            var firstCall = true
            override fun onChanged(profile: ThemeProfile) {
                if (firstCall) {
                    firstCall = false
                } else {
                    coreEngine.setThemeProfile(profile)
                }
            }
        })

        mainViewModel.mode.observe(this, object : Observer<ThemeMode> {
            var firstCall = true
            override fun onChanged(themeMode: ThemeMode) {
                if (firstCall) {
                    firstCall = false
                } else {
                    coreEngine.setThemeMode(themeMode)
                }
            }
        })

        setContent {
            KalculusTheme(
                profile = mainViewModel.profile.observeAsState(ThemeProfile.Firewater).value,
                themeMode = mainViewModel.mode.observeAsState(ThemeMode.Light).value
            ) {
                KalculusScreen(coreEngine.surfaceView) {
                    coreEngine.render(
                        Vector2D.create(
                            Random.nextInt(0, 5),
                            Random.nextInt(-5, 5)
                        )
                    )
                }
            }
        }
    }

    companion object {
        init {
            // load the JNI library needed by most API calls
            Filament.init()
        }
    }
}
