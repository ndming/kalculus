package com.flyng.kalculus.visual.primitive

data class Primitive(
    val topology: Topology,
    val offset: Int,
    val count: Int,
    val material: MaterialLocal,
)
