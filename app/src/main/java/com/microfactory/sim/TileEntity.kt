package com.microfactory.sim

/**
 * Represents the occupant entity of a single cell on the factory grid.
 * Input and output directions denote the side (port face) of the tile where items enter or exit.
 */
sealed interface TileEntity {
    data object Empty : TileEntity

    data object Rock : TileEntity

    data class Spawner(
        val itemType: ItemType,
        val outputDir: Direction,
        val spawnIntervalTicks: Int
    ) : TileEntity

    data class Hatch(
        val requiredItem: ItemType,
        val targetCount: Int,
        var deliveredCount: Int = 0
    ) : TileEntity

    data class Belt(
        val inputDir: Direction,
        val outputDir: Direction
    ) : TileEntity

    data class Splitter(
        val inputDir: Direction,
        val outputLeft: Direction,
        val outputRight: Direction,
        var toggleLeft: Boolean = true
    ) : TileEntity

    data class Smelter(
        val inputDir: Direction,
        val outputDir: Direction,
        val processTicks: Int = 2
    ) : TileEntity

    data class Press(
        val inputDir: Direction,
        val outputDir: Direction,
        val processTicks: Int = 2
    ) : TileEntity

    data class Assembler(
        val inputDirA: Direction,
        val inputDirB: Direction,
        val outputDir: Direction,
        val processTicks: Int = 3
    ) : TileEntity

    fun isPlayerEditable(): Boolean = when (this) {
        is Belt, is Splitter, is Smelter, is Press, is Assembler -> true
        Empty, Rock, is Spawner, is Hatch -> false
    }

    fun rotatedClockwise(): TileEntity = when (this) {
        is Belt -> copy(
            inputDir = inputDir.rotateClockwise(),
            outputDir = outputDir.rotateClockwise()
        )
        is Splitter -> copy(
            inputDir = inputDir.rotateClockwise(),
            outputLeft = outputLeft.rotateClockwise(),
            outputRight = outputRight.rotateClockwise()
        )
        is Smelter -> copy(
            inputDir = inputDir.rotateClockwise(),
            outputDir = outputDir.rotateClockwise()
        )
        is Press -> copy(
            inputDir = inputDir.rotateClockwise(),
            outputDir = outputDir.rotateClockwise()
        )
        is Assembler -> copy(
            inputDirA = inputDirA.rotateClockwise(),
            inputDirB = inputDirB.rotateClockwise(),
            outputDir = outputDir.rotateClockwise()
        )
        Empty, Rock, is Spawner, is Hatch -> this
    }
}
