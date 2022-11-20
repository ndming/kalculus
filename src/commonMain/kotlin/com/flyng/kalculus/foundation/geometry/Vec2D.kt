package com.flyng.kalculus.foundation.geometry

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal open class Vec2D(x: Number, y: Number) {
    var x: Float = x.toFloat()

    var y: Float = y.toFloat()

    val length: Float
        get() = sqrt(x * x + y * y)

    val theta: Float
        get() = atan2(y, x)

    operator fun component1() = x

    operator fun component2() = y

    operator fun unaryPlus() = this

    operator fun unaryMinus() = Vec2D(-x, -y)

    override fun toString() = "($x, $y)"

    override fun equals(other: Any?): Boolean {
        return if (other is Vec2D) {
            x == other.x && y == other.y
        } else {
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }

    /**
     * Translates this vector by the transform defined by [vec2D].
     * @return this vector to chain the transform operations.
     */
    open fun translate(vec2D: Vec2D) = this.apply {
        x += vec2D.x
        y += vec2D.y
    }

    /**
     * Scales this vector by the amount of [scalar].
     * @return this vector to chain the transform operations.
     */
    open fun scale(scalar: Number) = this.apply {
        when (scalar) {
            is Short -> {
                x *= scalar; y *= scalar
            }

            is Int -> {
                x *= scalar; y *= scalar
            }

            is Long -> {
                x *= scalar; y *= scalar
            }

            is Float -> {
                x *= scalar; y *= scalar
            }

            is Double -> {
                x *= scalar.toFloat(); y *= scalar.toFloat()
            }

            else -> throw IllegalArgumentException(
                "Scalar argument must be either Short, Int, Long, Float, or Double."
            )
        }
    }

    /**
     * Rotates counter-clockwise this vector by the angle [radian].
     * @return this vector to chain the transform operations.
     */
    open fun rotate(radian: Float) = this.apply {
        val oldX = x
        x = x * cos(radian) - y * sin(radian)
        y = oldX * sin(radian) + y * cos(radian)
    }

    /**
     * Rotates counter-clockwise this vector by the angle [radian].
     * @return this vector to chain the transform operations.
     */
    open fun rotate(radian: Double) = rotate(radian.toFloat())

    /**
     * Clones this vector. This will be useful when you want to preserve the initial vector after applying a sequence of
     * vector transformations on it.
     */
    fun copy() = Vec2D(this.x, this.y)
}

internal operator fun Vec2D.plus(other: Vec2D): Vec2D = Vec2D(this.x + other.x, this.y + other.y)

internal operator fun Vec2D.minus(other: Vec2D): Vec2D = Vec2D(this.x - other.x, this.y - other.y)

internal operator fun Number.times(other: Vec2D): Vec2D = when (this) {
    is Short -> Vec2D(this * other.x, this * other.y)
    is Int -> Vec2D(this * other.x, this * other.y)
    is Long -> Vec2D(this * other.x, this * other.y)
    is Float -> Vec2D(this * other.x, this * other.y)
    is Double -> Vec2D(this * other.x, this * other.y)
    else -> throw IllegalArgumentException("Scalar argument must be either Short, Int, Long, Float, or Double.")
}

internal operator fun Vec2D.times(other: Number): Vec2D = other * this
