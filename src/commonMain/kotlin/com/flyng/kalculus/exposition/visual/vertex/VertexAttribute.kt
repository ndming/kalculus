package com.flyng.kalculus.exposition.visual.vertex

sealed class VertexAttribute(val size: Int, val normalized: Boolean)

data class PositionAttribute(val x: Float, val y: Float, val z: Float) : VertexAttribute(ByteSize.FLOAT * 3, true)

data class ColorAttribute(val color: Int) : VertexAttribute(ByteSize.INT, false)
