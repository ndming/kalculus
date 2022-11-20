package com.flyng.kalculus.visual

import com.flyng.kalculus.visual.boundary.Boundary
import com.flyng.kalculus.visual.primitive.Primitive
import com.flyng.kalculus.visual.vertex.Vertex

interface Visual {
    fun primitives(): List<Primitive>

    fun vertices(color: ULong): List<Vertex>

    fun indices(): ShortArray

    fun boundary(): Boundary
}
