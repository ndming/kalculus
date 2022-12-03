package com.flyng.kalculus.exposition.visual

import com.flyng.kalculus.exposition.visual.boundary.Boundary
import com.flyng.kalculus.exposition.visual.primitive.Primitive
import com.flyng.kalculus.exposition.visual.vertex.Vertex

interface Visual {
    fun primitives(): List<Primitive>

    fun vertices(): List<Vertex>

    fun indices(): ShortArray

    fun boundary(): Boundary

    operator fun plus(other: Visual) = object : Visual {
        // prevent multiple evaluations
        private val vertices = this@Visual.vertices()
        private val indices = this@Visual.indices()

        override fun primitives(): List<Primitive> {
            // each offset value of primitives on the second Visual has an additional offset caused by the index list
            // of the first Visual since we are concatenating the two index lists
            val offset = indices.size
            return this@Visual.primitives() + other.primitives().onEach { it.offset += offset }
        }

        override fun vertices() = vertices + other.vertices()

        override fun indices() = indices + other.indices().map {
            if (it + vertices.size > Short.MAX_VALUE) {
                throw RuntimeException("Cannot combine ${this@Visual} and $other because of index overflow.")
            }
            (it + vertices.size).toShort()
        }

        override fun boundary() = this@Visual.boundary() + other.boundary()
    }
}
