package com.flyng.kalculus.exposition.visual

import com.flyng.kalculus.exposition.visual.boundary.Boundary
import com.flyng.kalculus.exposition.visual.primitive.Primitive
import com.flyng.kalculus.exposition.visual.vertex.Vertex

interface Visual {
    fun primitives(): List<Primitive>

    fun vertices(): List<Vertex>

    fun indices(): ShortArray

    fun boundary(): Boundary
}
