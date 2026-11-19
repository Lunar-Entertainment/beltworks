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
        val spawnIntervalTicks: Int,
        val secondaryItemType: ItemType? = null
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

    /**
     * Crosses two orthogonal belt lanes (horizontal + vertical) without mixing items.
     */
    data class Bridge(
        val horizDir: Direction = Direction.EAST,
        val vertDir: Direction = Direction.SOUTH
    ) : TileEntity

    /**
     * Routes items matching [filterItem] to [matchDir] and all other items to [otherDir].
     */
    data class Filter(
        val inputDir: Direction,
        val matchDir: Direction,
        val otherDir: Direction,
        val filterItem: ItemType = ItemType.IRON_ORE,
        val overclocked: Boolean = false
    ) : TileEntity

    /**
     * Prioritizes [primaryDir]; spills excess items to [overflowDir] only when [primaryDir] is blocked.
     */
    data class Overflow(
        val inputDir: Direction,
        val primaryDir: Direction,
        val overflowDir: Direction
    ) : TileEntity

    /**
     * Destroys any incoming item from any side to keep mixed lines from backing up.
     */
    data object Incinerator : TileEntity

    data class Smelter(
        val inputDir: Direction,
        val outputDir: Direction,
        val processTicks: Int = 2,
        val overclocked: Boolean = false
    ) : TileEntity

    data class Press(
        val inputDir: Direction,
        val outputDir: Direction,
        val processTicks: Int = 2,
        val overclocked: Boolean = false
    ) : TileEntity

    data class Assembler(
        val inputDirA: Direction,
        val inputDirB: Direction,
        val outputDir: Direction,
        val processTicks: Int = 3,
        val overclocked: Boolean = false
    ) : TileEntity

    fun isPowerConsumer(): Boolean = when (this) {
        is Smelter, is Press, is Assembler, is Filter -> true
        else -> false
    }

    fun isOverclocked(): Boolean = when (this) {
        is Smelter -> overclocked
        is Press -> overclocked
        is Assembler -> overclocked
        is Filter -> overclocked
        else -> false
    }

    fun withOverclock(enabled: Boolean): TileEntity = when (this) {
        is Smelter -> copy(overclocked = enabled)
        is Press -> copy(overclocked = enabled)
        is Assembler -> copy(overclocked = enabled)
        is Filter -> copy(overclocked = enabled)
        else -> this
    }

    fun basePowerDemand(): Float = when (this) {
        is Smelter -> 4f
        is Press -> 4f
        is Assembler -> 6f
        is Filter -> 2f
        else -> 0f
    }

    fun powerDemand(): Float =
        basePowerDemand() * if (isOverclocked()) 2.5f else 1.0f

    fun isPlayerEditable(): Boolean = when (this) {
        is Belt,
        is Splitter,
        is Bridge,
        is Filter,
        is Overflow,
        Incinerator,
        is Smelter,
        is Press,
        is Assembler -> true
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
        is Bridge -> copy(
            horizDir = vertDir.rotateClockwise(),
            vertDir = horizDir.rotateClockwise()
        )
        is Filter -> copy(
            inputDir = inputDir.rotateClockwise(),
            matchDir = matchDir.rotateClockwise(),
            otherDir = otherDir.rotateClockwise()
        )
        is Overflow -> copy(
            inputDir = inputDir.rotateClockwise(),
            primaryDir = primaryDir.rotateClockwise(),
            overflowDir = overflowDir.rotateClockwise()
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
        Incinerator, Empty, Rock, is Spawner, is Hatch -> this
    }
}
