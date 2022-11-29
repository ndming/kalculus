package com.flyng.kalculus.exposition.visual.primitive

data class Primitive(
    val topology: Topology,
    val offset: Int,
    val count: Int,
    val material: MaterialLocal,
)
