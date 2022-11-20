package com.flyng.kalculus.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun KalculusTheme(
    profile: ThemeProfile = ThemeProfile.Firewater,
    themeMode: ThemeMode = ThemeMode.Light,
    content: @Composable () -> Unit
) {
    val colors = if (themeMode == ThemeMode.Light) {
        profile.lightScheme
    } else {
        profile.darkScheme
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
