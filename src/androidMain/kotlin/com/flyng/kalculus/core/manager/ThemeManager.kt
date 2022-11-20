package com.flyng.kalculus.core.manager

import android.util.Log
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import com.flyng.kalculus.BuildConfig
import com.flyng.kalculus.renderable.Renderable
import com.flyng.kalculus.theme.ThemeMode
import com.flyng.kalculus.theme.ThemeProfile
import com.google.android.filament.Colors
import com.google.android.filament.MaterialInstance
import com.google.android.filament.Skybox

class ThemeManager(
    initialProfile: ThemeProfile = ThemeProfile.Firewater,
    initialMode: ThemeMode = ThemeMode.Light,
) {
    private var profile = initialProfile
    private var mode = initialMode

    fun theme() = profile to mode

    /**
     * Call this function when the global [ThemeProfile] of the application change.
     * @param profile the new [ThemeProfile] to update.
     */
    fun setThemeProfile(profile: ThemeProfile, skybox: Skybox, instances: List<MaterialInstance>) {
        // Only set the new configuration when it actually changes
        if (this.profile != profile) {
            // Update the internal states
            this.profile = profile
            // Set the new configuration
            setSkyboxColor(skybox)
            setInstanceColor(instances)
        }
    }

    /**
     * Call this function when the global [ThemeMode] of the application change.
     * @param mode the new [ThemeMode] to update.
     */
    fun setThemeMode(mode: ThemeMode, skybox: Skybox, instances: List<MaterialInstance>) {
        if (this.mode != mode) {
            this.mode = mode
            setSkyboxColor(skybox)
            setInstanceColor(instances)
        }
    }

    /**
     * Extracts the current color in appropriate [ColorSpaces] for [Skybox].
     */
    fun skyboxColor() = if (mode == ThemeMode.Light) {
        profile.lightScheme.surface.convert(ColorSpaces.CieXyz)
    } else {
        profile.darkScheme.surface.convert(ColorSpaces.CieXyz)
    }

    /**
     * Extracts the current color in appropriate [ColorSpaces] for [Renderable] components.
     */
    fun renderableColor() = if (mode == ThemeMode.Light) {
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
        val color = renderableColor()
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
