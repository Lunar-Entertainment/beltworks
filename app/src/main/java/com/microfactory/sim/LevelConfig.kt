package com.microfactory.sim

data class PlacedTile(
    val coord: GridCoord,
    val tile: TileEntity
)

data class LevelConfig(
    val id: Int,
    val name: String,
    val subtitle: String,
    val width: Int = 8,
    val height: Int = 10,
    val parBelts: Int,
    val spawners: List<PlacedTile>,
    val hatches: List<PlacedTile>,
    val rocks: List<GridCoord>,
    val hint: String
) {
    fun calculateStars(beltsUsed: Int): Int = when {
        beltsUsed <= parBelts -> 3
        beltsUsed <= parBelts + 4 -> 2
        else -> 1
    }
}
