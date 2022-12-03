package com.flyng.kalculus.exposition.visual.primitive

data class Primitive(
    val topology: Topology,
    var offset: Int,
    val count: Int,
    val material: MaterialLocal,
)
