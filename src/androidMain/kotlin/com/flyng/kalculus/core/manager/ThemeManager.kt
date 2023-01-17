package com.flyng.kalculus.core.manager

import android.util.Log
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import com.flyng.kalculus.BuildConfig
import com.flyng.kalculus.exposition.visual.primitive.ColorType
import com.flyng.kalculus.theme.ThemeMode
import com.flyng.kalculus.theme.ThemeProfile
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
            setInstanceColor(meshManager.instances)
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
            setInstanceColor(meshManager.instances)
        }
    }

    fun colorFromType(type: ColorType, alpha: Float = 1.0f) = when (type) {
        ColorType.BASE -> baseColor(alpha)
        ColorType.SPOT -> spotColor(alpha)
        ColorType.SKYBOX -> skyboxColor(alpha)
    }

    private fun skyboxColor(alpha: Float) = if (mode == ThemeMode.Light) {
        profile.lightScheme.surface.convert(filamentColorSpace).copy(alpha = alpha)
    } else {
        profile.darkScheme.surface.convert(filamentColorSpace).copy(alpha = alpha)
    }

    private fun baseColor(alpha: Float) = if (mode == ThemeMode.Light) {
        profile.lightScheme.primary.convert(filamentColorSpace).copy(alpha = alpha)
    } else {
        profile.darkScheme.primary.convert(filamentColorSpace).copy(alpha = alpha)
    }

    private fun spotColor(alpha: Float) = if (mode == ThemeMode.Light) {
        profile.lightScheme.tertiary.convert(filamentColorSpace).copy(alpha = alpha)
    } else {
        profile.darkScheme.tertiary.convert(filamentColorSpace).copy(alpha = alpha)
    }

    private fun setSkyboxColor(skybox: Skybox) {
        val (red, green, blue, alpha) = skyboxColor(1.0f)
        skybox.setColor(red, green, blue, alpha)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "skybox color change: (${red}, ${green}, ${blue}, ${alpha})")
        }
    }

    private fun setInstanceColor(instances: List<Pair<MaterialInstance, ColorType>>) {
        instances.forEach { (instance, type) ->
            val (red, green, blue) = colorFromType(type)
            if (instance.material.hasParameter("rgb")) {
                instance.setParameter("rgb", red, green, blue)
            }
        }
    }

    companion object {
        private const val TAG = "CoreEngineThemeManager"

        private val filamentColorSpace = ColorSpaces.LinearSrgb
    }
}
