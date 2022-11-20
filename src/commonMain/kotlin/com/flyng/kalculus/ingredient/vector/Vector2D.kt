package com.flyng.kalculus.ingredient.vector

import com.flyng.kalculus.foundation.geometry.IHat2D
import com.flyng.kalculus.foundation.geometry.JHat2D
import com.flyng.kalculus.foundation.geometry.Vec2D
import com.flyng.kalculus.foundation.geometry.minus
import com.flyng.kalculus.visual.Visual
import com.flyng.kalculus.visual.boundary.Boundary
import com.flyng.kalculus.visual.primitive.MaterialLocal
import com.flyng.kalculus.visual.primitive.Primitive
import com.flyng.kalculus.visual.primitive.Topology
import com.flyng.kalculus.visual.vertex.PositionAttribute
import com.flyng.kalculus.visual.vertex.Vertex
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Displays an arrow-like 2D vector. The vector needs only a [head] and a [tail] coordinate in world space to define
 * itself.
 */
class Vector2D internal constructor(
    private val head: Vec2D,
    private val tail: Vec2D = Vec2D(0, 0),
) : Visual {
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

        fun create(x: Number, y: Number) = Vector2D(head = Vec2D(x, y))
    }

    override fun primitives() = listOf(
        Primitive(Topology.TRIANGLES, 0, indices().size, MaterialLocal.dynamic_color),
    )

    override fun vertices(color: ULong) = produceVertices().map {
        Vertex(data = arrayOf(PositionAttribute(it.x, it.y, 0.0f)))
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
}
