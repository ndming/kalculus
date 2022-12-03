package com.flyng.kalculus.exposition.visual.boundary

data class Boundary(
    val centerX: Float,
    val centerY: Float,
    val centerZ: Float,
    val halfExtentX: Float,
    val halfExtentY: Float,
    val halfExtentZ: Float,
) {

    private fun centerBetween(self: Float, other: Float, halfSelf: Float, halfOther: Float) = if (self < other) {
        (self - halfSelf + other + halfOther) / 2
    } else {
        (self + halfSelf + other - halfOther) / 2
    }

    private fun halfBetween(self: Float, other: Float, halfSelf: Float, halfOther: Float) = if (self < other) {
        (other + halfOther - self + halfSelf) / 2
    } else {
        (self + halfSelf - other + halfOther) / 2
    }

    operator fun plus(other: Boundary) = Boundary(
        centerX = centerBetween(centerX, other.centerX, halfExtentX, other.halfExtentX),
        centerY = centerBetween(centerY, other.centerY, halfExtentY, other.halfExtentY),
        centerZ = centerBetween(centerZ, other.centerZ, halfExtentZ, other.halfExtentZ),
        halfExtentX = halfBetween(centerX, other.centerX, halfExtentX, other.halfExtentX),
        halfExtentY = halfBetween(centerY, other.centerY, halfExtentY, other.halfExtentY),
        halfExtentZ = halfBetween(centerZ, other.centerZ, halfExtentZ, other.halfExtentZ)
    )
}
