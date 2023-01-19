package com.flyng.kalculus.model

import android.opengl.Matrix
import com.flyng.kalculus.core.CoreEngine
import com.flyng.kalculus.exposition.visual.primitive.ColorType
import com.flyng.kalculus.foundation.algebra.linear.IHat2D
import com.flyng.kalculus.foundation.algebra.linear.Vec2D
import com.flyng.kalculus.ingredient.conic.Circle2D
import com.flyng.kalculus.ingredient.vector.Vector2D
import com.google.android.filament.Entity

class FourierSeriesModel(private val core: CoreEngine) {
    private val arrows = mutableListOf<Arrow>()

    fun attach(length: Float, theta: Float): Pair<Float, Float> {
        val (lastX, lastY) = if (arrows.isNotEmpty()) arrows.last().vec.head() else (0.0f to 0.0f)
        val (headX, headY) = IHat2D.rotate(theta).scale(length).translate(Vec2D(lastX, lastY))
        val vec = Vector2D.Builder()
            .tail(lastX, lastY)
            .head(headX, headY)
            .color(ColorType.BASE)
            .build()
        val cir = Circle2D.Builder()
            .center((headX + lastX) / 2, (headY + lastY) / 2)
            .radius(length / 2)
            .color(ColorType.BASE)
            .build()
        val entity = core.render(vec + cir)
        arrows.add(Arrow(entity, vec, lastX, lastY))
        return headX to headY
    }

    fun drop() {
        val arrow = arrows.removeLast()
        core.destroy(arrow.entity)
    }

    fun clear() {
        val entities = arrows.map { it.entity }
        arrows.clear()
        entities.forEach { core.destroy(it) }
    }

    fun transition(fraction: Float): Pair<Float, Float> {
        var prevX = 0.0f
        var prevY = 0.0f
        arrows.forEachIndexed { idx, (entity, vec, tailX, tailY) ->
            val freq = if (idx % 2 == 0) idx / 2 else -(idx + 1) / 2

            val mat = FloatArray(16)
            Matrix.setIdentityM(mat, 0)
            Matrix.translateM(mat, 0, prevX - tailX, prevY - tailY, 0.0f)
            Matrix.translateM(mat, 0, tailX, tailY, 0.0f)
            Matrix.rotateM(mat, 0, fraction * freq * 360, 0.0f, 0.0f, 1.0f)
            Matrix.translateM(mat, 0, -tailX, -tailY, 0.0f)

            core.transformer.setTransform(core.transformer.getInstance(entity), mat)
            vec.transform(mat)

            val (headX, headY) = vec.head()
            prevX = headX
            prevY = headY
        }
        return prevX to prevY
    }

    private data class Arrow(
        @Entity val entity: Int,
        val vec: Vector2D,
        val tailX: Float,
        val tailY: Float
    )
}