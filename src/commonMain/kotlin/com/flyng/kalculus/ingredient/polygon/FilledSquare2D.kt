package com.flyng.kalculus.ingredient.polygon

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
import com.flyng.kalculus.ingredient.IngredientBuilder
import kotlin.math.PI
import kotlin.math.sqrt

class FilledSquare2D private constructor(
    private val center: Vec2D,
    private val halfExtent: Float,
    private val color: ColorType
) : Visual {
    private fun produceVertices(): List<Vec2D> {
        val p0 = center.copy().translate(-JHat2D.scale(halfExtent * sqrt(2.0f)).rotate(-PI / 4))
        val p1 = p0.copy().translate(IHat2D.scale(halfExtent * 2))
        val p2 = p0.copy().translate(JHat2D.scale(halfExtent * 2))
        val p3 = p2.copy().translate(IHat2D.scale(halfExtent * 2))

        return listOf(p0, p1, p2, p3)
    }

    internal class Builder : IngredientBuilder<FilledSquare2D>() {
        private val center = Vec2D(0, 0)

        private var halfExtent = 1.0f

        fun center(x: Number, y: Number) = this.apply {
            center.x = x.toFloat()
            center.y = y.toFloat()
        }

        fun halfExtent(value: Float) = this.apply {
            halfExtent = value
        }

        override fun build() = FilledSquare2D(center, halfExtent, type)
    }

    override fun primitives() = listOf(Primitive(Topology.TRIANGLE_STRIP, 0, indices().size, DynamicColor(color)))

    override fun vertices() = produceVertices().map {
        Vertex(data = listOf(PositionAttribute(it.x, it.y, 0.0f)))
    }

    override fun indices() = ShortArray(4) { it.toShort() }

    override fun boundary() = Boundary(
        centerX = center.x,
        centerY = center.y,
        centerZ = 0.0f,
        halfExtentX = halfExtent,
        halfExtentY = halfExtent,
        halfExtentZ = 0.01f
    )

}