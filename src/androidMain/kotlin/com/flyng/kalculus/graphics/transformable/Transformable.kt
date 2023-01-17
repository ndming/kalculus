package com.flyng.kalculus.graphics.transformable

import com.flyng.kalculus.exposition.stateful.Stateful
import com.google.android.filament.Entity
import com.google.android.filament.TransformManager

actual interface Transformable {
    fun translate(
        stateful: Stateful,
        @Entity entity: Int,
        transformer: TransformManager,
        moveX: Float,
        moveY: Float,
        moveZ: Float
    )

    fun rotate(
        stateful: Stateful,
        @Entity entity: Int,
        transformer: TransformManager,
        axisX: Float,
        axisY: Float,
        axisZ: Float,
        angle: Float
    )

    fun offsetRotateXY(
        stateful: Stateful,
        @Entity entity: Int,
        transformer: TransformManager,
        angle: Float,
        offsetX: Float = 0.0f,
        offsetY: Float = 0.0f
    )
}
