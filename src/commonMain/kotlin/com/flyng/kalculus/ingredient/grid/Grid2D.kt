package com.flyng.kalculus.ingredient.grid

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

/**
 * Represents a 2D grid from intersected lines. The number of lines is predefined by the [Grid2D.LINES_EXTENT] constant.
 * The grid's [center] and [spacing] can be specified with [Grid2D.Builder] class.
 */
class Grid2D private constructor(
    private val center: Vec2D,
    private val spacing: Float,
    private val color: ColorType,
    private val alpha: Float,
) : Visual {

    private fun produceVertices(): List<Vec2D> {
        val p = LINES_EXTENT * spacing
        val n = -p
        val vertices = mutableListOf<Vec2D>()
        for (i in 0 until LINES_EXTENT * 2 + 1) {
            val lP = Vec2D(n, n + i * spacing).translate(center)
            val rP = Vec2D(p, n + i * spacing).translate(center)
            vertices.add(lP)
            vertices.add(rP)
        }
        for (i in 0 until LINES_EXTENT * 2 + 1) {
            val bP = Vec2D(n + i * spacing, n).translate(center)
            val tP = Vec2D(n + i * spacing, p).translate(center)
            vertices.add(bP)
            vertices.add(tP)
        }
        return vertices
    }

    companion object {
        // number of lines extending out from the center
        private const val LINES_EXTENT = 100
        private const val INDICES = LINES_EXTENT * 8 + 4
    }

    class Builder : IngredientBuilder<Grid2D>() {
        private val center = Vec2D(0, 0)

        private var spacing = 1.0f

        fun center(x: Number, y: Number) = this.apply {
            center.x = x.toFloat()
            center.y = y.toFloat()
        }

        fun spacing(space: Float) = this.apply {
            spacing = space
        }

        override fun build(): Grid2D {
            if (spacing < 0) {
                throw RuntimeException("Negative spacing is disallowed: $spacing")
            }
            return Grid2D(center, spacing, type, alpha)
        }

    }

    override fun primitives() = listOf(Primitive(Topology.LINES, 0, INDICES, DynamicColor(color, alpha)))

    override fun vertices() = produceVertices().map { Vertex(listOf(PositionAttribute(it.x, it.y, 0.0f))) }

    override fun indices() = ShortArray(INDICES) { it.toShort() }

    override fun boundary() = Boundary(
        centerX = center.x,
        centerY = center.y,
        centerZ = 0.0f,
        halfExtentX = LINES_EXTENT * spacing,
        halfExtentY = LINES_EXTENT * spacing,
        halfExtentZ = 0.01f
    )
}
