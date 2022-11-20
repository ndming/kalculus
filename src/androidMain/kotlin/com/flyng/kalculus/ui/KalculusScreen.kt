package com.flyng.kalculus.ui

import android.view.SurfaceView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flyng.kalculus.MainViewModel
import com.flyng.kalculus.theme.ThemeMode
import com.flyng.kalculus.theme.ThemeProfile

@Composable
fun KalculusScreen(
    surfaceView: SurfaceView,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = viewModel(),
    onDrawVector: () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize().align(Alignment.Center),
            factory = { surfaceView }
        )
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            FilledIconButton(
                onClick = {
                    val update = if (mainViewModel.profile.value == ThemeProfile.Firewater) {
                        ThemeProfile.Naturalist
                    } else {
                        ThemeProfile.Firewater
                    }
                    mainViewModel.onProfileChange(update)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                )
            }
            FilledIconButton(
                onClick = {
                    val update = if (mainViewModel.mode.value == ThemeMode.Light) {
                        ThemeMode.Dark
                    } else {
                        ThemeMode.Light
                    }
                    mainViewModel.onThemeModeChange(update)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                )
            }
            FilledIconButton(
                onClick = onDrawVector
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                )
            }
        }
    }
}
