package com.flyng.kalculus.adapter

import android.opengl.Matrix
import com.flyng.kalculus.exposition.stateful.Stateful
import com.flyng.kalculus.graphics.transformable.Transformable
import com.google.android.filament.TransformManager

actual object StatefulAdapter : Transformable {
    override fun translate(
        stateful: Stateful,
        entity: Int,
        transformer: TransformManager,
        moveX: Float,
        moveY: Float,
        moveZ: Float
    ) {
        val mat = FloatArray(16)
        Matrix.setIdentityM(mat, 0)
        Matrix.translateM(mat, 0, moveX, moveY, moveZ)
        transformer.setTransform(transformer.getInstance(entity), mat)
        stateful.transform(mat)
    }

    override fun rotate(
        stateful: Stateful,
        entity: Int,
        transformer: TransformManager,
        axisX: Float,
        axisY: Float,
        axisZ: Float,
        angle: Float
    ) {
        val mat = FloatArray(16)
        Matrix.setRotateM(mat, 0, angle, axisX, axisY, axisZ)
        transformer.setTransform(transformer.getInstance(entity), mat)
        stateful.transform(mat)
    }
}