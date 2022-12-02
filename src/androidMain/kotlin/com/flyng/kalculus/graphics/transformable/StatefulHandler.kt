package com.flyng.kalculus.graphics.transformable

import android.opengl.Matrix
import com.flyng.kalculus.exposition.stateful.Stateful
import com.google.android.filament.TransformManager

actual object StatefulHandler : Transformable {
    override fun translate(
        stateful: Stateful,
        entity: Int,
        transformManager: TransformManager,
        moveX: Float,
        moveY: Float,
        moveZ: Float
    ) {
        val mat = FloatArray(16)
        Matrix.setIdentityM(mat, 0)
        Matrix.translateM(mat, 0, moveX, moveY, moveZ)
        transformManager.setTransform(transformManager.getInstance(entity), mat)
        stateful.transform(mat)
    }
}