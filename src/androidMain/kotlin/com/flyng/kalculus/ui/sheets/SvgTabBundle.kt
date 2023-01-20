package com.flyng.kalculus.ui.sheets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.flyng.kalculus.MainViewModel
import com.flyng.kalculus.theme.ThemeMode
import com.flyng.kalculus.theme.ThemeProfile
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun SvgTabBundle(
    heightFraction: Float,
    sheetState: ModalBottomSheetState,
    modifier: Modifier = Modifier,
    vm: MainViewModel = viewModel()
) {
    var bundleIndex by remember { mutableStateOf(0) }

    val context = LocalContext.current

    val bundles = remember { context.assets.list("bundles")!! }

    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(heightFraction)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.0f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(targetState = bundleIndex) { index ->
                    AsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        model = ImageRequest.Builder(context)
                            .data("file:///android_asset/bundles/${bundles[index]}")
                            .decoderFactory(SvgDecoder.Factory())
                            .build(),
                        contentDescription = bundles[index],
                        alignment = Alignment.Center,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            BundleIndicator(bundles.size, bundleIndex)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { if (bundleIndex == 0) bundleIndex = bundles.size - 1 else bundleIndex -= 1 }
                ) {
                    Icon(
                        imageVector = Icons.Default.SwitchLeft,
                        contentDescription = null
                    )
                }
                AnimatedContent(targetState = bundleIndex) { index ->
                    Text(
                        modifier = Modifier.fillMaxWidth(0.6f),
                        text = bundles[index]
                            .substringAfter('_')
                            .replace('-', ' ')
                            .removeSuffix(".svg")
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
                IconButton(
                    onClick = { if (bundleIndex == bundles.size - 1) bundleIndex = 0 else bundleIndex += 1 }
                ) {
                    Icon(
                        imageVector = Icons.Default.SwitchRight,
                        contentDescription = null
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val verticalPadding = 24.dp

            Row(verticalAlignment = Alignment.CenterVertically) {
                val mode by vm.mode.observeAsState(ThemeMode.Light)
                val profile by vm.profile.observeAsState(ThemeProfile.Firewater)
                IconButton(
                    modifier = Modifier.padding(vertical = verticalPadding).padding(end = 8.dp),
                    onClick = vm::onModeToggle
                ) {
                    Icon(
                        imageVector = if (mode == ThemeMode.Light) {
                            Icons.Default.DarkMode
                        } else {
                            Icons.Default.LightMode
                        },
                        contentDescription = null
                    )
                }
                IconButton(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = vm::onProfileToggle
                ) {
                    Icon(
                        imageVector = if (profile == ThemeProfile.Firewater) {
                            Icons.Default.Forest
                        } else {
                            Icons.Default.LocalFireDepartment
                        },
                        contentDescription = null
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    modifier = Modifier.padding(vertical = verticalPadding, horizontal = 8.dp),
                    onClick = {
                        scope.launch {
                            sheetState.animateTo(
                                ModalBottomSheetValue.Hidden,
                                tween(durationMillis = 600)
                            )
                        }
                    }
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Button(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = {
                        scope.launch {
                            sheetState.animateTo(
                                ModalBottomSheetValue.Hidden,
                                tween(durationMillis = 600)
                            )
                        }
                        vm.applySampleChange(bundleIndex, context.assets)
                    }
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = 2.dp),
                        text = "Apply",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable fun BundleIndicator(
    size: Int,
    index: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.padding(vertical = 8.dp)) {
        repeat(size) {
            Surface(
                modifier = Modifier.size(8.dp).aspectRatio(1.0f),
                shape = CircleShape,
                color = if (it == index) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
            ) {}
            if (it < size - 1) {
                Spacer(modifier = Modifier.padding(4.dp))
            }
        }
    }
}