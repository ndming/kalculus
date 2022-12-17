package com.flyng.kalculus.ingredient

import com.flyng.kalculus.exposition.visual.primitive.Color

abstract class IngredientBuilder<T> {
    protected var color = Color.default

    /**
     * Sets the basic color for the ingredient, in SRGB space.
     */
    fun color(color: Color) = this.apply {
        this.color = color
    }

    /**
     * Constructs the ingredient [T] with the specified attributes.
     */
    abstract fun build(): T
}
