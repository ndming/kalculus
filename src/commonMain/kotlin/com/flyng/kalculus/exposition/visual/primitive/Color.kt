package com.flyng.kalculus.exposition.visual.primitive

data class Color(val red: Float, val green: Float, val blue: Float, val alpha: Float) {
    companion object {
        val default = Color(0.0f, 0.0f, 0.0f, 1.0f)
    }
}
