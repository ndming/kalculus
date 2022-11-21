package com.flyng.kalculus.graphics.renderable.mesh

import org.khronos.webgl.Float32Array
import org.khronos.webgl.Uint16Array

actual data class Mesh(
    val entity: Int,
    val indexBuffer: Uint16Array,
    val vertexBuffer: Float32Array,
    val aabb: dynamic
)
