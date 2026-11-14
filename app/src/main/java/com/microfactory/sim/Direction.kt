package com.microfactory.sim

/**
 * Cardinal grid directions using screen/canvas coordinates where +X is EAST and +Y is SOUTH.
 */
enum class Direction(val dx: Int, val dy: Int) {
    NORTH(0, -1),
    EAST(1, 0),
    SOUTH(0, 1),
    WEST(-1, 0);

    fun opposite(): Direction = when (this) {
        NORTH -> SOUTH
        EAST -> WEST
        SOUTH -> NORTH
        WEST -> EAST
    }

    fun rotateClockwise(): Direction = when (this) {
        NORTH -> EAST
        EAST -> SOUTH
        SOUTH -> WEST
        WEST -> NORTH
    }

    fun rotateCounterClockwise(): Direction = when (this) {
        NORTH -> WEST
        WEST -> SOUTH
        SOUTH -> EAST
        EAST -> NORTH
    }

    companion object {
        fun fromDelta(dx: Int, dy: Int): Direction? = when {
            dx == 0 && dy == -1 -> NORTH
            dx == 1 && dy == 0 -> EAST
            dx == 0 && dy == 1 -> SOUTH
            dx == -1 && dy == 0 -> WEST
            else -> null
        }
    }
}
