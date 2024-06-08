package com.flyng.kalculus.ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*

@Composable
fun KalculusWindow(count: Int, onClick: (Int) -> Unit) {
    Canvas {
        Button(attrs = {
            onClick { onClick(count - 1) }
        }) {
            Text("-")
        }

        Span({ style { padding(15.px) } }) {
            Text("$count")
        }

        Button(attrs = {
            onClick { onClick(count + 1) }
        }) {
            Text("+")
        }
    }
}