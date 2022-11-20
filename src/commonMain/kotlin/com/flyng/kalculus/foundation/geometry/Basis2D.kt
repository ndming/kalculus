package com.flyng.kalculus.foundation.geometry

/**
 * Singleton for the origin point `(0, 0)` in 2D plane.
 */
internal object Origin2D : Vec2D(0, 0) {
    override fun translate(vec2D: Vec2D) = copy().translate(vec2D)

    override fun scale(scalar: Number) = copy().scale(scalar)

    override fun rotate(radian: Float) = copy().rotate(radian)

    override fun rotate(radian: Double) = copy().rotate(radian)
}

/**
 * Singleton for the unit vector `(1, 0)` in 2D plane.
 */
internal object IHat2D : Vec2D(1, 0) {
    override fun translate(vec2D: Vec2D) = copy().translate(vec2D)

    override fun scale(scalar: Number) = copy().scale(scalar)

    override fun rotate(radian: Float) = copy().rotate(radian)

    override fun rotate(radian: Double) = copy().rotate(radian)
}


/**
 * Singleton for the unit vector `(0, 1)` in 2D plane.
 */
internal object JHat2D : Vec2D(0, 1) {
    override fun translate(vec2D: Vec2D) = copy().translate(vec2D)

    override fun scale(scalar: Number) = copy().scale(scalar)

    override fun rotate(radian: Float) = copy().rotate(radian)

    override fun rotate(radian: Double) = copy().rotate(radian)
}