package com.flyng.kalculus.ingredient.conic

import com.flyng.kalculus.exposition.visual.Visual
import com.flyng.kalculus.exposition.visual.boundary.Boundary
import com.flyng.kalculus.exposition.visual.primitive.ColorType
import com.flyng.kalculus.exposition.visual.primitive.DynamicColor
import com.flyng.kalculus.exposition.visual.primitive.Primitive
import com.flyng.kalculus.exposition.visual.primitive.Topology
import com.flyng.kalculus.exposition.visual.vertex.PositionAttribute
import com.flyng.kalculus.exposition.visual.vertex.Vertex
import com.flyng.kalculus.foundation.algebra.linear.Vec2D
import com.flyng.kalculus.ingredient.IngredientBuilder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Represents a 2D circle with [center] and [radius]. An optional [color] can be specified for the initial look of the
 * ingredient upon first-time render.
 */
class Circle2D private constructor(
    private val center: Vec2D,
    private val radius: Float,
    private val stroke: Float,
    private val color: ColorType,
    private val alpha: Float,
) : Visual {

    private fun scan(): List<Vec2D> {
        val firstHalf = mutableListOf<Vec2D>()
        var theta = 0.0f
        while (theta < 2 * PI) {
            if (stroke == 0.0f) {
                val x = radius * cos(theta) + center.x
                val y = radius * sin(theta) + center.y
                firstHalf.add(Vec2D(x, y))
            } else {
                val x0 = (radius - stroke / 2) * cos(theta) + center.x
                val y0 = (radius - stroke / 2) * sin(theta) + center.y
                firstHalf.add(Vec2D(x0, y0))
                val x1 = (radius + stroke / 2) * cos(theta) + center.x
                val y1 = (radius + stroke / 2) * sin(theta) + center.y
                firstHalf.add(Vec2D(x1, y1))
            }
            theta += STEP
        }
        return firstHalf
    }


    companion object {
        private const val FACTOR = 0.01f
        private const val STEP = (FACTOR * PI).toFloat()
        private const val STRIPS = (2 / FACTOR).toInt()
    }

    /**
     * Builder class for [Circle2D].
     */
    internal class Builder : IngredientBuilder<Circle2D>() {
        private val center = Vec2D(0, 0)

        private var radius = 1.0f

        private var stroke = 0.0f

        /**
         * Sets the center coordinate ([x], [y]) in world space of the circle.
         */
        fun center(x: Number, y: Number) = this.apply {
            center.x = x.toFloat()
            center.y = y.toFloat()
        }

        /**
         * Sets the radius of the circle.
         */
        fun radius(value: Float) = this.apply { radius = value }

        /**
         * Sets the [width] of the circle boundary.
         */
        fun strokeWidth(width: Float) = this.apply { stroke = width }

        override fun build(): Circle2D {
            if (radius < 0) {
                throw RuntimeException("Negative radius is disallowed: $radius")
            }
            return Circle2D(center, radius, if (stroke < 0) 0.0f else stroke, type, alpha)
        }
    }

    override fun primitives() = listOf(
        Primitive(
            topology = if (stroke == 0.0f) Topology.LINE_STRIP else Topology.TRIANGLE_STRIP,
            offset = 0,
            count = indices().size,
            material = DynamicColor(color, alpha)
        )
    )

    override fun vertices() = scan().map {
        Vertex(listOf(PositionAttribute(it.x, it.y, 0.0f)))
    }

    override fun indices() = if (stroke == 0.0f) {
        ShortArray(STRIPS) { it.toShort() } + shortArrayOf(0)
    } else {
        ShortArray(2 * STRIPS) { it.toShort() } + shortArrayOf(0, 1)
    }

    override fun boundary() = Boundary(
        centerX = center.x,
        centerY = center.y,
        centerZ = 0.0f,
        halfExtentX = radius,
        halfExtentY = radius,
        halfExtentZ = 0.01f
    )
}