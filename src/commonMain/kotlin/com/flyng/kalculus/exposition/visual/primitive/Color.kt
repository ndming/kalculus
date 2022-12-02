package com.flyng.kalculus.exposition.visual.primitive

data class Color(var red: Float, var green: Float, var blue: Float, var alpha: Float) {
    companion object {
        val default = Color(0.0f, 0.0f, 0.0f, 1.0f)
    }
}
