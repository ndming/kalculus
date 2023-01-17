package com.flyng.kalculus.ingredient.curve

import com.flyng.kalculus.exposition.visual.Visual
import com.flyng.kalculus.exposition.visual.boundary.Boundary
import com.flyng.kalculus.exposition.visual.primitive.ColorType
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

class Segment2D private constructor(
    private val begin: Vec2D,
    private val final: Vec2D,
    private val width: Float,
    private val color: ColorType,
) : Visual {

    private fun scan(): List<Vec2D> {
        // length and theta of a Vec2D are computed every time we query for their values,
        // so it's better to compute them once here
        val normal = final - begin
        val length = normal.length
        val theta = normal.theta

        return if (width > 0.0f) {
            val p0 = begin.copy().translate(JHat2D.rotate(theta).scale(width / 2))
            val p1 = p0.copy().translate(IHat2D.rotate(theta - PI / 2).scale(width))
            val mirrorTransform = IHat2D.rotate(theta).scale(length)
            val p2 = p0.copy().translate(mirrorTransform)
            val p3 = p1.copy().translate(mirrorTransform)
            listOf(p0, p1, p2, p3)
        } else {
            listOf(begin, final)
        }
    }

    class Builder : IngredientBuilder<Segment2D>() {
        private val begin = Vec2D(0, 0)

        private val final = Vec2D(0, 0)

        private var width = 0.0f

        fun begin(x: Number, y: Number) = this.apply {
            begin.x = x.toFloat()
            begin.y = y.toFloat()
        }

        fun final(x: Number, y: Number) = this.apply {
            final.x = x.toFloat()
            final.y = y.toFloat()
        }

        fun width(value: Float) = this.apply { width = value }

        override fun build(): Segment2D {
            if (width < 0) {
                throw RuntimeException("Negative width is disallowed: $width")
            }
            return Segment2D(begin, final, width, type)
        }
    }

    override fun primitives() = listOf(
        Primitive(
            if (width > 0) Topology.TRIANGLE_STRIP else Topology.LINES, 0, indices().size, DynamicColor(color)
        )
    )

    override fun vertices() = scan().map { Vertex(listOf(PositionAttribute(it.x, it.y, 0.0f))) }

    override fun indices() = ShortArray(if (width > 0.0f) 4 else 2) { it.toShort() }

    override fun boundary(): Boundary {
        return Boundary(
            centerX = (final.x + begin.x) / 2,
            centerY = (final.y + begin.y) / 2,
            centerZ = 0.0f,
            halfExtentX = 0.01f,  // should be length / 2 * cos(theta) of the normal vector
            halfExtentY = 0.01f,  // should be length / 2 * sin(theta) of the normal vector
            halfExtentZ = 0.01f
        )
    }
}