package com.microfactory.sim

object LevelRepository {
    val levels: List<LevelConfig> = listOf(
        LevelConfig(
            id = 1,
            name = "First Melt",
            subtitle = "Smelt Iron Ore into Ingots",
            width = 8,
            height = 10,
            parBelts = 7,
            spawners = listOf(
                PlacedTile(
                    coord = GridCoord(1, 3),
                    tile = TileEntity.Spawner(
                        itemType = ItemType.IRON_ORE,
                        outputDir = Direction.EAST,
                        spawnIntervalTicks = 2
                    )
                )
            ),
            hatches = listOf(
                PlacedTile(
                    coord = GridCoord(6, 6),
                    tile = TileEntity.Hatch(
                        requiredItem = ItemType.IRON_INGOT,
                        targetCount = 10
                    )
                )
            ),
            rocks = listOf(
                GridCoord(4, 2),
                GridCoord(4, 3),
                GridCoord(3, 6)
            ),
            hint = "Drag Belt from the spawner, place a Smelter along the path, and route Iron Ingots to the hatch."
        ),
        LevelConfig(
            id = 2,
            name = "Heavy Metal",
            subtitle = "Smelt Ore & Press into Plates",
            width = 8,
            height = 10,
            parBelts = 11,
            spawners = listOf(
                PlacedTile(
                    coord = GridCoord(1, 1),
                    tile = TileEntity.Spawner(
                        itemType = ItemType.IRON_ORE,
                        outputDir = Direction.SOUTH,
                        spawnIntervalTicks = 2
                    )
                )
            ),
            hatches = listOf(
                PlacedTile(
                    coord = GridCoord(6, 8),
                    tile = TileEntity.Hatch(
                        requiredItem = ItemType.IRON_PLATE,
                        targetCount = 15
                    )
                )
            ),
            rocks = listOf(
                GridCoord(1, 5),
                GridCoord(2, 5),
                GridCoord(3, 5),
                GridCoord(5, 2),
                GridCoord(5, 3),
                GridCoord(5, 6),
                GridCoord(4, 7)
            ),
            hint = "Route Iron Ore into a Smelter, then feed the Iron Ingots into a Press to stamp Iron Plates."
        ),
        LevelConfig(
            id = 3,
            name = "Gearworks",
            subtitle = "Split Plates into an Assembler",
            width = 8,
            height = 10,
            parBelts = 11,
            spawners = listOf(
                PlacedTile(
                    coord = GridCoord(3, 1),
                    tile = TileEntity.Spawner(
                        itemType = ItemType.IRON_PLATE,
                        outputDir = Direction.SOUTH,
                        spawnIntervalTicks = 1
                    )
                )
            ),
            hatches = listOf(
                PlacedTile(
                    coord = GridCoord(3, 8),
                    tile = TileEntity.Hatch(
                        requiredItem = ItemType.GEAR,
                        targetCount = 10
                    )
                )
            ),
            rocks = listOf(
                GridCoord(1, 4),
                GridCoord(6, 4),
                GridCoord(1, 5),
                GridCoord(6, 5)
            ),
            hint = "Place a Splitter to divide Iron Plates evenly into both side inputs of an Assembler to forge Gears."
        )
    )

    fun getLevel(id: Int): LevelConfig =
        levels.firstOrNull { it.id == id } ?: levels.first()
}
