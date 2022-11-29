package com.flyng.kalculus.ui

import android.view.SurfaceView
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import com.flyng.kalculus.MainViewModel
import com.flyng.kalculus.theme.ThemeMode
import com.flyng.kalculus.theme.ThemeProfile

@Composable
fun KalculusScreen(
    surfaceView: SurfaceView,
    modifier: Modifier = Modifier,
    vm: MainViewModel,
    onDrawVector: () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
                .pointerInput(Unit) {
                    detectTransformGestures(panZoomLock = true) { _, pan, zoom, _ ->
                        vm.core.cameraManager.zoom(zoom)
                        vm.core.cameraManager.shift(pan.x, pan.y)
                    }
                    detectTapGestures(onDoubleTap = { vm.core.cameraManager.reset() })
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        vm.core.cameraManager.shift(dragAmount.x, dragAmount.y)
                    }
                },
            factory = { surfaceView }
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                FilledIconButton(
                    onClick = {
                        val update = if (vm.profile.value == ThemeProfile.Firewater) {
                            ThemeProfile.Naturalist
                        } else {
                            ThemeProfile.Firewater
                        }
                        vm.onProfileChange(update)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                    )
                }
                FilledIconButton(
                    onClick = {
                        val update = if (vm.mode.value == ThemeMode.Light) {
                            ThemeMode.Dark
                        } else {
                            ThemeMode.Light
                        }
                        vm.onThemeModeChange(update)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                FilledIconButton(
                    onClick = vm.core.cameraManager::reset
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
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
}
