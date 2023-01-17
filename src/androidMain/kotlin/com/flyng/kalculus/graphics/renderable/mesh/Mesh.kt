package com.flyng.kalculus.graphics.renderable.mesh

import com.flyng.kalculus.exposition.visual.primitive.ColorType
import com.google.android.filament.*

actual data class Mesh(
    @Entity val entity: Int,
    val indexBuffer: IndexBuffer,
    val vertexBuffer: VertexBuffer,
    val aabb: Box,
    val instances: List<Pair<MaterialInstance, ColorType>>
)
