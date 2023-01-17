package com.flyng.kalculus.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun KalculusTheme(
    profile: ThemeProfile = ThemeProfile.Firewater,
    themeMode: ThemeMode = ThemeMode.Light,
    dynamic: Boolean = false,
    content: @Composable () -> Unit
) {
    val isLight = themeMode == ThemeMode.Light
    val colorScheme = when {
        dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isLight) dynamicLightColorScheme(context) else dynamicDarkColorScheme(context)
        }

        isLight -> profile.lightScheme
        else -> profile.darkScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surfaceVariant.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, true)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLight
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
