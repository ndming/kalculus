package com.flyng.kalculus.core.manager

import android.util.Log
import androidx.compose.ui.graphics.colorspace.ColorSpace
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import com.flyng.kalculus.BuildConfig
import com.flyng.kalculus.graphics.renderable.Renderable
import com.flyng.kalculus.theme.ThemeMode
import com.flyng.kalculus.theme.ThemeProfile
import com.google.android.filament.Colors
import com.google.android.filament.MaterialInstance
import com.google.android.filament.Scene
import com.google.android.filament.Skybox

class ThemeManager(
    private val scene: Scene,
    private val meshManager: MeshManager,
    initialProfile: ThemeProfile = ThemeProfile.Firewater,
    initialMode: ThemeMode = ThemeMode.Light,
) {
    private var profile = initialProfile
    private var mode = initialMode

    /**
     * Call this function when the global [ThemeProfile] of the application change.
     * @param profile the new [ThemeProfile] to update.
     */
    fun setProfile(profile: ThemeProfile) {
        // Only set the new configuration when it actually changes
        if (this.profile != profile) {
            // Update the internal states
            this.profile = profile
            // Set the new configuration
            scene.skybox?.let {
                setSkyboxColor(it)
            }
            setInstanceColor(meshManager.materialInstances)
        }
    }

    /**
     * Call this function when the global [ThemeMode] of the application change.
     * @param mode the new [ThemeMode] to update.
     */
    fun setMode(mode: ThemeMode) {
        if (this.mode != mode) {
            this.mode = mode
            scene.skybox?.let {
                setSkyboxColor(it)
            }
            setInstanceColor(meshManager.materialInstances)
        }
    }

    /**
     * Extracts the current theme color in suitable [ColorSpace] for [Skybox].
     */
    fun skyboxColor() = if (mode == ThemeMode.Light) {
        profile.lightScheme.surface.convert(ColorSpaces.CieXyz)
    } else {
        profile.darkScheme.surface.convert(ColorSpaces.CieXyz)
    }

    /**
     * Extracts the current theme color in suitable [ColorSpace] for [Renderable]'s base color.
     */
    fun baseColor() = if (mode == ThemeMode.Light) {
        profile.lightScheme.onPrimaryContainer.convert(ColorSpaces.Srgb)
    } else {
        profile.darkScheme.onPrimaryContainer.convert(ColorSpaces.Srgb)
    }

    /**
     * Uses the internal [profile] and [mode] to set the new skybox color.
     */
    private fun setSkyboxColor(skybox: Skybox) {
        val color = skyboxColor()
        skybox.setColor(color.red, color.green, color.blue, color.alpha)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "skybox color change: (${color.red}, ${color.green}, ${color.blue}, ${color.alpha})")
        }
    }

    private fun setInstanceColor(instances: List<MaterialInstance>) {
        val color = baseColor()
        instances.forEach { instance ->
            if (instance.material.hasParameter("baseColor")) {
                instance.setParameter("baseColor", Colors.RgbType.SRGB, color.red, color.green, color.blue)
            }
        }
    }

    companion object {
        private const val TAG = "CoreEngineThemeManager"
    }
}
