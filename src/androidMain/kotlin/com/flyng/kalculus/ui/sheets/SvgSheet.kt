package com.flyng.kalculus.ui.sheets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val SHEET_HEIGHT_FRACTION = 0.9f

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable fun SvgSheet(
    sheetState: ModalBottomSheetState,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(SvgTab.BUNDLE) }

    Surface {
        Column(modifier = modifier.fillMaxWidth()) {
            TabRow(
                modifier = Modifier.fillMaxWidth(),
                selectedTabIndex = selectedTab.ordinal
            ) {
                SvgTab.values().forEach { svgTab ->
                    Tab(
                        selected = selectedTab == svgTab,
                        onClick = { selectedTab = svgTab },
                        text = {
                            Text(
                                modifier = Modifier.padding(vertical = 12.dp),
                                text = svgTab.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        unselectedContentColor = MaterialTheme.colorScheme.inversePrimary
                    )
                }
            }
            AnimatedContent(targetState = selectedTab) { tab ->
                when (tab) {
                    SvgTab.BUNDLE -> SvgTabBundle(heightFraction = SHEET_HEIGHT_FRACTION, sheetState = sheetState)
                    SvgTab.CUSTOM -> SvgTabOnline(heightFraction = SHEET_HEIGHT_FRACTION)
                }
            }
        }
    }
}