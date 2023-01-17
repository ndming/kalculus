package com.flyng.kalculus.exposition.visual.primitive

@Deprecated(
    message = "Color is now restricted to using certain types.",
    replaceWith = ReplaceWith("ColorType", "com.flyng.kalculus.exposition.visual.primitive.ColorType")
)
data class Color(val red: Float, val green: Float, val blue: Float, val alpha: Float) {
    companion object {
        val default = Color(0.0f, 0.0f, 0.0f, 1.0f)
    }
}
