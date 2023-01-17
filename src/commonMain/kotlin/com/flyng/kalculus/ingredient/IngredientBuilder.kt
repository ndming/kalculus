package com.flyng.kalculus.ingredient

import com.flyng.kalculus.exposition.visual.primitive.ColorType

abstract class IngredientBuilder<T> {
    protected var type = ColorType.BASE
    protected var alpha = 1.0f

    /**
     * Sets the basic color for the ingredient, in SRGB space.
     */
    fun color(type: ColorType, alpha: Float = 1.0f) = this.apply {
        this.type = type
        this.alpha = alpha
    }

    /**
     * Constructs the ingredient [T] with the specified attributes.
     */
    abstract fun build(): T
}
