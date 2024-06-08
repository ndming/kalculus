package com.flyng.kalculus

import com.flyng.kalculus.core.CoreEngine
import com.flyng.kalculus.kotlinext.filament.init

fun main() {
    init(arrayOf("plain_color.filamat")) { CoreEngine() }
}