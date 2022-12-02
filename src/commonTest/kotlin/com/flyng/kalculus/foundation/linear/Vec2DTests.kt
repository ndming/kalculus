package com.flyng.kalculus.foundation.linear

import com.flyng.kalculus.foundation.algebra.linear.*
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals

class Vec2DTests {
    private val vec45 = Vec2D(1, 1)

    private val vec60 = Vec2D(0.5, sqrt(3.0) / 2)

    private val vec135 = Vec2D(-1, 1)

    private val vec120 = Vec2D(-0.5, sqrt(3.0) / 2)

    private val vec240 = Vec2D(-0.5, -sqrt(3.0) / 2)

    private val vec300 = Vec2D(0.5, -sqrt(3.0) / 2)

    companion object {
        private const val SCALAR_1 = 5.5f
        private const val SCALAR_2 = -7.0
        private const val PI_OVER_3 = PI / 3
        private const val PI_OVER_2 = PI / 2
        private const val TWO_PI = 2 * PI
        private val TOLERANCE = 1 / 10.0f.pow(Precision.DEFAULT)
    }

    @Test
    fun lengthOfVector() {
        assertEquals(0.0f, Origin2D.length, TOLERANCE, "Length of $Origin2D")
        assertEquals(1.0f, IHat2D.length, TOLERANCE, "Length of $IHat2D")
        assertEquals(1.0f, JHat2D.length, TOLERANCE, "Length of $JHat2D")
        assertEquals(sqrt(2.0f), vec45.length, TOLERANCE, "Length of $vec45")
        assertEquals(sqrt(2.0f), vec135.length, TOLERANCE, "Length of $vec135")
    }

    @Test
    fun polarAngleOfVector() {
        assertEquals(0.0f, Origin2D.theta, TOLERANCE, "Theta of $Origin2D")
        assertEquals(0.0f, IHat2D.theta, TOLERANCE, "Theta of $IHat2D")
        assertEquals(-PI.toFloat(), (-IHat2D).theta, TOLERANCE, "Theta of ${-IHat2D}")
        assertEquals(PI.toFloat() / 2, JHat2D.theta, TOLERANCE, "Theta of $JHat2D")
        assertEquals(-PI.toFloat() / 2, (-JHat2D).theta, TOLERANCE, "Theta of ${-JHat2D}")
        assertEquals(PI.toFloat() / 4, vec45.theta, TOLERANCE, "Theta of $vec45")
        assertEquals(-3 * PI.toFloat() / 4, (-vec45).theta, TOLERANCE, "Theta of $vec45")
        assertEquals(3 * PI.toFloat() / 4, vec135.theta, TOLERANCE, "Theta of $vec135")
        assertEquals(-PI.toFloat() / 4, (-vec135).theta, TOLERANCE, "Theta of ${-vec135}")
    }

    @Test
    fun vectorAddition() {
        assertEquals(vec45, IHat2D + JHat2D, "$IHat2D + $JHat2D")
    }

    @Test
    fun vectorSubtraction() {
        assertEquals(vec135, JHat2D - IHat2D, "$JHat2D - $IHat2D")
    }

    @Test
    fun scalarMultiplication() {
        assertEquals(Vec2D(5, 0), 5 * IHat2D, "5$IHat2D")
        assertEquals(Vec2D(7, -7), vec135 * -7, "-7$vec135")
    }

    @Test
    fun vectorTranslation() {
        assertEquals(Vec2D(0, 2), vec45.copy().translate(vec135), "Translate $vec45 by $vec135")
    }

    @Test
    fun vectorScaling() {
        assertEquals(Vec2D(SCALAR_1, SCALAR_1), vec45.copy().scale(SCALAR_1), "Scale $vec45 by $SCALAR_1")
        assertEquals(Vec2D(-SCALAR_2, SCALAR_2), vec135.copy().scale(SCALAR_2), "Scale $vec135 by $SCALAR_2")
    }

    @Test
    fun vectorRotation() {
        assertEquals(JHat2D, IHat2D.rotate(PI_OVER_2), "Rotate $IHat2D by $PI_OVER_2 radian")
        assertEquals(-JHat2D, IHat2D.rotate(-PI_OVER_2), "Rotate $IHat2D by ${-PI_OVER_2} radian")
        assertEquals(vec120, vec60.copy().rotate(PI_OVER_3), "Rotate $vec60 by $PI_OVER_3 radian")
        assertEquals(vec120, vec300.copy().rotate(-PI), "Rotate $vec300 by ${-PI_OVER_2} radian")
        assertEquals(vec240, vec240.copy().rotate(TWO_PI), "Rotate $vec240 by $TWO_PI radian")
    }
}
