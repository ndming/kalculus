package com.flyng.kalculus.ingredient.vector

import com.flyng.kalculus.exposition.stateful.Stateful
import com.flyng.kalculus.exposition.visual.Visual
import com.flyng.kalculus.exposition.visual.boundary.Boundary
import com.flyng.kalculus.exposition.visual.primitive.Color
import com.flyng.kalculus.exposition.visual.primitive.DynamicColor
import com.flyng.kalculus.exposition.visual.primitive.Primitive
import com.flyng.kalculus.exposition.visual.primitive.Topology
import com.flyng.kalculus.exposition.visual.vertex.PositionAttribute
import com.flyng.kalculus.exposition.visual.vertex.Vertex
import com.flyng.kalculus.foundation.algebra.linear.IHat2D
import com.flyng.kalculus.foundation.algebra.linear.JHat2D
import com.flyng.kalculus.foundation.algebra.linear.Vec2D
import com.flyng.kalculus.foundation.algebra.linear.minus
import com.flyng.kalculus.ingredient.IngredientBuilder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Represents an arrow-like 2D vector. The ingredient requires a [head] and a [tail] coordinate in world space to define
 * itself. It also accepts an optional [color] for specifying the initial appearance.
 * @see Vector2D.Builder
 */
class Vector2D private constructor(
    private val head: Vec2D,
    private val tail: Vec2D,
    private val color: Color,
) : Visual, Stateful {
    private val normal: Vec2D
        get() = head - tail

    private fun produceVertices(): List<Vec2D> {
        // length and theta of a Vec2D are computed every time we query for their values,
        // so it's better to compute them once here
        val length = normal.length
        val theta = normal.theta

        val shaftHalfThickness = length * SHAFT_HALF_THICKNESS_RATIO

        val arrowHeightLength = length * ARROW_HEIGHT_RATIO
        val arrowBaseLength = arrowHeightLength * ARROW_BASE_OVER_HEIGHT_RATIO

        val point0 = tail.copy().translate(JHat2D.rotate(theta).scale(shaftHalfThickness))
        val point1 = point0.copy().translate(IHat2D.rotate(theta).scale(length - arrowHeightLength))
        val point2 = point1.copy().translate(
            IHat2D
                .rotate(PI / 2 + theta).scale(arrowBaseLength / 2 - shaftHalfThickness)
        )
        val point3 = head
        val point4 = point2.copy().translate(
            IHat2D
                .rotate(theta - PI / 2).scale(arrowBaseLength)
        )
        val shaftMirrorTransform = IHat2D.rotate(theta - PI / 2).scale(shaftHalfThickness * 2)
        val point5 = point1.copy().translate(shaftMirrorTransform)
        val point6 = point0.copy().translate(shaftMirrorTransform)

        return listOf(point0, point1, point2, point3, point4, point5, point6)
    }

    companion object {
        private const val SHAFT_HALF_THICKNESS_RATIO = 0.01f
        private const val ARROW_BASE_OVER_HEIGHT_RATIO = 1.0f
        private const val ARROW_HEIGHT_RATIO = 0.1f
    }

    /**
     * Builder class for [Vector2D].
     */
    class Builder : IngredientBuilder<Vector2D>() {
        private val head = Vec2D(0, 0)

        private val tail = Vec2D(0, 0)

        /**
         * Specifies the head coordinates in world space for this vector.
         */
        fun head(x: Number, y: Number) = this.apply {
            head.x = x.toFloat()
            head.y = y.toFloat()
        }

        /**
         * Specifies the tail coordinates in world space for this vector.
         */
        fun tail(x: Number, y: Number) = this.apply {
            tail.x = x.toFloat()
            tail.y = y.toFloat()
        }

        /**
         * Constructs a new [Vector2D].
         */
        override fun build() = Vector2D(head, tail, color)
    }

    override fun primitives() = listOf(
        Primitive(Topology.TRIANGLES, 0, indices().size, DynamicColor(color)),
    )

    override fun vertices() = produceVertices().map {
        Vertex(data = listOf(PositionAttribute(it.x, it.y, 0.0f)))
    }

    override fun indices() = shortArrayOf(0, 6, 1, 1, 6, 5, 2, 4, 3)

    override fun boundary(): Boundary {
        val length = normal.length
        val theta = normal.theta
        return Boundary(
            centerX = (tail.x + head.x) / 2,
            centerY = (tail.y + head.y) / 2,
            centerZ = 0.0f,
            halfExtentX = length / 2 * cos(theta),
            halfExtentY = length / 2 * sin(theta),
            halfExtentZ = 0.01f
        )
    }

    override fun transform(tm: FloatArray) {
        val hx = head.x
        val hy = head.y
        head.x = tm[0] * hx + tm[4] * hy + tm[12]
        head.y = tm[1] * hx + tm[5] * hy + tm[13]

        val tx = tail.x
        val ty = tail.y
        tail.x = tm[0] * tx + tm[4] * ty + tm[12]
        tail.y = tm[1] * tx + tm[5] * ty + tm[13]
    }
}
