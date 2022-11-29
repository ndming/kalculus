package com.flyng.kalculus.graphics.transformable

import com.flyng.kalculus.exposition.stateful.Stateful
import com.google.android.filament.Entity
import com.google.android.filament.TransformManager

actual interface Transformable {
    fun translate(
        stateful: Stateful,
        @Entity entity: Int,
        transformManager: TransformManager,
        moveX: Float,
        moveY: Float,
        moveZ: Float
    )
}
