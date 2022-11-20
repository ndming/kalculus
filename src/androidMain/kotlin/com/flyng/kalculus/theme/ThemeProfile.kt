package com.flyng.kalculus.theme

import androidx.compose.material3.ColorScheme
import com.flyng.kalculus.theme.firewater.firewaterDarkColors
import com.flyng.kalculus.theme.firewater.firewaterLightColors
import com.flyng.kalculus.theme.naturalist.naturalistDarkColors
import com.flyng.kalculus.theme.naturalist.naturalistLightColors

enum class ThemeProfile(val lightScheme: ColorScheme, val darkScheme: ColorScheme) {
    Firewater(firewaterLightColors, firewaterDarkColors),
    Naturalist(naturalistLightColors, naturalistDarkColors),
}
