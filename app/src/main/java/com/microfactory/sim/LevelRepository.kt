package com.microfactory.sim

object LevelRepository {
    private val coreLevels: List<LevelConfig> = listOf(
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
        ),
        LevelConfig(
            id = 4,
            name = "Copper Vein",
            subtitle = "Route Dual Ore Lines Without Crossing",
            width = 8,
            height = 10,
            parBelts = 20,
            spawners = listOf(
                PlacedTile(
                    coord = GridCoord(1, 1),
                    tile = TileEntity.Spawner(
                        itemType = ItemType.COPPER_ORE,
                        outputDir = Direction.SOUTH,
                        spawnIntervalTicks = 2
                    )
                ),
                PlacedTile(
                    coord = GridCoord(6, 1),
                    tile = TileEntity.Spawner(
                        itemType = ItemType.IRON_ORE,
                        outputDir = Direction.SOUTH,
                        spawnIntervalTicks = 2
                    )
                )
            ),
            hatches = listOf(
                PlacedTile(
                    coord = GridCoord(1, 8),
                    tile = TileEntity.Hatch(
                        requiredItem = ItemType.COPPER_INGOT,
                        targetCount = 10
                    )
                ),
                PlacedTile(
                    coord = GridCoord(6, 8),
                    tile = TileEntity.Hatch(
                        requiredItem = ItemType.IRON_INGOT,
                        targetCount = 10
                    )
                )
            ),
            rocks = listOf(
                GridCoord(1, 4),
                GridCoord(2, 4),
                GridCoord(5, 5),
                GridCoord(6, 5),
                GridCoord(0, 5),
                GridCoord(7, 4)
            ),
            hint = "Smelt Copper and Iron simultaneously while weaving both belts cleanly around the rock barriers."
        ),
        LevelConfig(
            id = 5,
            name = "Twin Press",
            subtitle = "Parallelize Smelting for High Volume",
            width = 8,
            height = 10,
            parBelts = 11,
            spawners = listOf(
                PlacedTile(
                    coord = GridCoord(3, 1),
                    tile = TileEntity.Spawner(
                        itemType = ItemType.IRON_ORE,
                        outputDir = Direction.SOUTH,
                        spawnIntervalTicks = 1
                    )
                )
            ),
            hatches = listOf(
                PlacedTile(
                    coord = GridCoord(3, 8),
                    tile = TileEntity.Hatch(
                        requiredItem = ItemType.IRON_PLATE,
                        targetCount = 20
                    )
                )
            ),
            rocks = listOf(
                GridCoord(0, 4),
                GridCoord(1, 4),
                GridCoord(6, 4),
                GridCoord(7, 4),
                GridCoord(3, 5)
            ),
            hint = "Split the fast ore stream into parallel Smelter + Press lanes and merge them back before the hatch."
        ),
        LevelConfig(
            id = 6,
            name = "Full Foundry",
            subtitle = "Refine Raw Ore All the Way to Gears",
            width = 8,
            height = 10,
            parBelts = 12,
            spawners = listOf(
                PlacedTile(
                    coord = GridCoord(1, 1),
                    tile = TileEntity.Spawner(
                        itemType = ItemType.IRON_ORE,
                        outputDir = Direction.EAST,
                        spawnIntervalTicks = 1
                    )
                )
            ),
            hatches = listOf(
                PlacedTile(
                    coord = GridCoord(4, 8),
                    tile = TileEntity.Hatch(
                        requiredItem = ItemType.GEAR,
                        targetCount = 8
                    )
                )
            ),
            rocks = listOf(
                GridCoord(5, 1),
                GridCoord(5, 2),
                GridCoord(1, 5),
                GridCoord(6, 5),
                GridCoord(1, 8)
            ),
            hint = "Build the full production chain: Ore → Smelter → Press → Splitter → Assembler → Hatch."
        ),
        LevelConfig(
            id = 7,
            name = "Dual Alloy",
            subtitle = "Expanded Grid: Copper Bars & Gears",
            width = 9,
            height = 11,
            parBelts = 20,
            spawners = listOf(
                PlacedTile(
                    coord = GridCoord(1, 1),
                    tile = TileEntity.Spawner(
                        itemType = ItemType.COPPER_ORE,
                        outputDir = Direction.SOUTH,
                        spawnIntervalTicks = 2
                    )
                ),
                PlacedTile(
                    coord = GridCoord(7, 1),
                    tile = TileEntity.Spawner(
                        itemType = ItemType.IRON_ORE,
                        outputDir = Direction.SOUTH,
                        spawnIntervalTicks = 1
                    )
                )
            ),
            hatches = listOf(
                PlacedTile(
                    coord = GridCoord(1, 9),
                    tile = TileEntity.Hatch(
                        requiredItem = ItemType.COPPER_INGOT,
                        targetCount = 10
                    )
                ),
                PlacedTile(
                    coord = GridCoord(6, 9),
                    tile = TileEntity.Hatch(
                        requiredItem = ItemType.GEAR,
                        targetCount = 8
                    )
                )
            ),
            rocks = listOf(
                GridCoord(3, 2),
                GridCoord(3, 3),
                GridCoord(3, 4),
                GridCoord(3, 7),
                GridCoord(3, 8),
                GridCoord(1, 5),
                GridCoord(7, 5)
            ),
            hint = "Keep Copper smelting on the left wing while refining Iron into Gears across the right wing."
        ),
        LevelConfig(
            id = 8,
            name = "Clockwork Core",
            subtitle = "Feed Dual Lines into One Assembly",
            width = 9,
            height = 12,
            parBelts = 22,
            spawners = listOf(
                PlacedTile(
                    coord = GridCoord(1, 1),
                    tile = TileEntity.Spawner(
                        itemType = ItemType.IRON_ORE,
                        outputDir = Direction.EAST,
                        spawnIntervalTicks = 2
                    )
                ),
                PlacedTile(
                    coord = GridCoord(7, 1),
                    tile = TileEntity.Spawner(
                        itemType = ItemType.IRON_ORE,
                        outputDir = Direction.WEST,
                        spawnIntervalTicks = 2
                    )
                )
            ),
            hatches = listOf(
                PlacedTile(
                    coord = GridCoord(4, 10),
                    tile = TileEntity.Hatch(
                        requiredItem = ItemType.GEAR,
                        targetCount = 14
                    )
                ),
                PlacedTile(
                    coord = GridCoord(1, 10),
                    tile = TileEntity.Hatch(
                        requiredItem = ItemType.IRON_PLATE,
                        targetCount = 10
                    )
                )
            ),
            rocks = listOf(
                GridCoord(4, 1),
                GridCoord(4, 2),
                GridCoord(2, 5),
                GridCoord(6, 5),
                GridCoord(2, 8),
                GridCoord(6, 8)
            ),
            hint = "Split the left Iron Plate line so half supplies the Gear Assembler and half feeds the Plate hatch."
        )
    )

    val standardLevels: List<LevelConfig> =
        coreLevels + LevelPack2.levels + LevelPack3.levels + LevelPack4.levels

    val expertLevels: List<LevelConfig> =
        LevelPackExpert1.levels + LevelPackExpert2.levels

    val levels: List<LevelConfig> = standardLevels + expertLevels

    fun getLevel(id: Int): LevelConfig =
        levels.firstOrNull { it.id == id } ?: levels.first()

    /**
     * Level 1 is always unlocked in Standard Mode. Each subsequent Standard level
     * unlocks once all preceding Standard levels have at least 1 earned star.
     */
    fun highestUnlockedLevelId(completedLevelIds: Set<Int>): Int {
        var highest = standardLevels.first().id
        val maxId = standardLevels.last().id
        for (level in standardLevels) {
            if (level.id <= highest && level.id in completedLevelIds) {
                highest = (level.id + 1).coerceAtMost(maxId)
            }
        }
        return highest
    }

    /**
     * Level 81 is always unlocked in Expert Mode. Each subsequent Expert level
     * unlocks once all preceding Expert levels have at least 1 earned star.
     */
    fun highestUnlockedExpertLevelId(completedLevelIds: Set<Int>): Int {
        var highest = expertLevels.first().id
        val maxId = expertLevels.last().id
        for (level in expertLevels) {
            if (level.id <= highest && level.id in completedLevelIds) {
                highest = (level.id + 1).coerceAtMost(maxId)
            }
        }
        return highest
    }

    fun isLevelUnlocked(levelId: Int, completedLevelIds: Set<Int>): Boolean {
        val level = levels.firstOrNull { it.id == levelId } ?: return false
        return if (level.isExpert) {
            levelId <= highestUnlockedExpertLevelId(completedLevelIds)
        } else {
            levelId <= highestUnlockedLevelId(completedLevelIds)
        }
    }

    fun nextLevelIdInMode(currentLevel: LevelConfig): Int? {
        val pool = if (currentLevel.isExpert) expertLevels else standardLevels
        val idx = pool.indexOfFirst { it.id == currentLevel.id }
        return if (idx in 0 until pool.lastIndex) pool[idx + 1].id else null
    }
}
