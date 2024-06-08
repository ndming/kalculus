package com.flyng.kalculus.ui

import android.animation.ValueAnimator
import android.view.SurfaceView
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.animation.doOnEnd
import com.flyng.kalculus.MainViewModel
import com.flyng.kalculus.theme.ThemeMode
import com.flyng.kalculus.theme.ThemeProfile
import com.flyng.kalculus.ui.svg.SvgSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable fun KalculusScreen(
    surfaceView: SurfaceView,
    modifier: Modifier = Modifier,
    vm: MainViewModel,
) {
    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    ModalBottomSheetLayout(
        modifier = modifier,
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        sheetContent = { SvgSheet(sheetState) }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                horizontalAlignment = Alignment.End
            ) {
                var slideEnabled by remember { mutableStateOf(true) }

                var visible by remember { mutableStateOf(true) }

                val visibleDuration = 600

                Row(modifier = Modifier.fillMaxWidth()) {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(visibleDuration)) + slideInHorizontally(tween(visibleDuration)),
                        exit = fadeOut(tween(visibleDuration)) + slideOutHorizontally(tween(visibleDuration))
                    ) {
                        Column {
                            val mode by vm.mode.observeAsState(ThemeMode.Light)
                            val profile by vm.profile.observeAsState(ThemeProfile.Firewater)
                            IconButton(
                                modifier = Modifier.padding(8.dp).padding(top = 8.dp),
                                onClick = vm::onModeToggle
                            ) {
                                Icon(
                                    imageVector = if (mode == ThemeMode.Light) {
                                        Icons.Default.DarkMode
                                    } else {
                                        Icons.Default.LightMode
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            IconButton(
                                modifier = Modifier.padding(8.dp),
                                onClick = vm::onProfileToggle
                            ) {
                                Icon(
                                    imageVector = if (profile == ThemeProfile.Firewater) {
                                        Icons.Default.Forest
                                    } else {
                                        Icons.Default.LocalFireDepartment
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            IconButton(
                                modifier = Modifier.padding(8.dp),
                                onClick = vm::toggleCamera
                            ) {
                                Icon(
                                    imageVector = if (vm.following) Icons.Default.GpsOff else Icons.Default.GpsFixed,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            IconButton(
                                modifier = Modifier.padding(8.dp),
                                onClick = {
                                    if (vm.durationScale != 1.0f) {
                                        slideEnabled = false
                                        val anim = ValueAnimator.ofFloat(vm.durationScale, 1.0f).apply {
                                            duration = 1000
                                            interpolator = AccelerateDecelerateInterpolator()
                                            repeatCount = 0
                                            addUpdateListener { vm.setSpeed(it.animatedValue as Float) }
                                            doOnEnd {
                                                vm.applySpeed()
                                                slideEnabled = true
                                            }
                                        }
                                        vm.core.animationManager.submit(anim)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(visibleDuration)) + slideInVertically(tween(visibleDuration)),
                        exit = fadeOut(tween(visibleDuration)) + slideOutVertically(tween(visibleDuration))
                    ) {
                        Row {
                            Button(
                                modifier = Modifier.padding(16.dp),
                                onClick = {
                                    if (vm.playing) { vm.togglePlaying() }
                                    scope.launch {
                                        sheetState.animateTo(
                                            ModalBottomSheetValue.Expanded,
                                            tween(durationMillis = 700)
                                        )
                                    }
                                },
                                enabled = !vm.caching,
                                colors = ButtonDefaults
                                    .buttonColors(containerColor = MaterialTheme.colorScheme.onPrimaryContainer)
                            ) {
                                Text(
                                    text = "SVG",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                modifier = Modifier.padding(top = 16.dp, end = 16.dp, start = 16.dp),
                                onClick = { visible = !visible }
                            ) {
                                Icon(
                                    imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(visibleDuration)) + slideInHorizontally(tween(visibleDuration)) { it / 2 },
                            exit = fadeOut(tween(visibleDuration)) + slideOutHorizontally(tween(visibleDuration)) { it / 2 }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                FilledIconButton(
                                    modifier = Modifier.padding(16.dp),
                                    onClick = vm::addArrow,
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropUp,
                                        contentDescription = null,
                                    )
                                }
                                Text(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    text = "${vm.arrows}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                FilledIconButton(
                                    modifier = Modifier.padding(16.dp),
                                    onClick = vm::dropArrow,
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                    )
                                }
                            }
                        }
                    }
                }
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(visibleDuration)) + slideInVertically(tween(visibleDuration)) { it / 2 },
                    exit = fadeOut(tween(visibleDuration)) + slideOutVertically(tween(visibleDuration)) { it / 2 }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledIconButton(
                            modifier = Modifier.padding(16.dp),
                            onClick = vm::togglePlaying,
                            enabled = !vm.caching,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            AnimatedContent(vm.caching) { caching ->
                                if (caching) {
                                    CircularProgressIndicator()
                                } else {
                                    Icon(
                                        imageVector = if (vm.playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                    )
                                }
                            }
                        }
                        Slider(
                            modifier = Modifier.padding(start = 16.dp, end = 32.dp),
                            value = vm.durationScale,
                            valueRange = 0.25f..4.0f,
                            onValueChange = vm::setSpeed,
                            onValueChangeFinished = vm::applySpeed,
                            enabled = slideEnabled,
                            colors = SliderDefaults.colors(
                                inactiveTrackColor = MaterialTheme.colorScheme.inversePrimary,
                                activeTrackColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                thumbColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                        )
                    }
                }
            }
        }
    }
}
