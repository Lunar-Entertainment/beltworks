package com.microfactory.sim

/**
 * Immutable 2D coordinate on the factory grid.
 */
data class GridCoord(val x: Int, val y: Int) {
    operator fun plus(dir: Direction): GridCoord = GridCoord(x + dir.dx, y + dir.dy)

    fun directionTo(other: GridCoord): Direction? =
        Direction.fromDelta(other.x - x, other.y - y)
}
