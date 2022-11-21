package com.flyng.kalculus.graphics.renderable.mesh

import com.google.android.filament.*

actual data class Mesh(
    @Entity val entity: Int,
    val indexBuffer: IndexBuffer,
    val vertexBuffer: VertexBuffer,
    val aabb: Box,
    val materials: List<MaterialInstance>
)
