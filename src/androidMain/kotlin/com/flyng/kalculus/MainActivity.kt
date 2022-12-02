package com.flyng.kalculus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.livedata.observeAsState
import com.flyng.kalculus.exposition.visual.primitive.Color
import com.flyng.kalculus.ingredient.vector.Vector2D
import com.flyng.kalculus.theme.KalculusTheme
import com.flyng.kalculus.theme.ThemeMode
import com.flyng.kalculus.theme.ThemeProfile
import com.flyng.kalculus.ui.KalculusScreen
import com.google.android.filament.Filament

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vm: MainViewModel by viewModels(
            factoryProducer = { MainViewModel.Factory(this, this, assets) }
        )

        setContent {
            KalculusTheme(
                profile = vm.profile.observeAsState(ThemeProfile.Firewater).value,
                themeMode = vm.mode.observeAsState(ThemeMode.Light).value
            ) {
                KalculusScreen(
                    surfaceView = vm.core.surfaceView,
                    vm = vm
                ) {

                }
            }
        }

        val color = vm.core.themeManager.baseColor().let {
            Color(it.red, it.green, it.blue, it.alpha)
        }

        val vectorX = Vector2D.Builder()
            .head(1, 0)
            .tail(0, 0)
            .color(color)
            .build()

        val vectorY = Vector2D.Builder()
            .head(0, 1)
            .tail(0, 0)
            .color(color)
            .build()

        vm.core.render(vectorX)
        vm.core.render(vectorY)
    }

    companion object {
        init {
            // load the JNI library needed by most API calls
            Filament.init()
        }
    }
}
