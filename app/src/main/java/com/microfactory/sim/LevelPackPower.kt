package com.microfactory.sim

/**
 * World 5: High Voltage (Levels 141–145).
 * Showcases the dual-layer Electricity & Grid Expansion system:
 * WindTurbine on WIND_SPOT, HydroGenerator on WATER_CURRENT, PowerCable routing,
 * CapacitorBank transmission buffering & surge storage, and 2x Machine Overclocking.
 */
object LevelPackPower {
    const val WORLD_TITLE = "World 5: High Voltage"

    val levels: List<LevelConfig> = listOf(
        LevelConfig(
            id = 141,
            name = "First Spark",
            subtitle = "World 5: High Voltage · Wind & Cable Routing",
            width = 8,
            height = 10,
            parBelts = 7,
            requiresPower = true,
            newMechanicBadge = "ELECTRICITY",
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
                        targetCount = 12
                    )
                )
            ),
            rocks = listOf(
                GridCoord(4, 2),
                GridCoord(4, 3),
                GridCoord(3, 6)
            ),
            powerTerrain = listOf(
                // High-yield wind ridge on the left and top perimeter ring
                PowerTerrainTile(GridCoord(0, 4), PowerTerrainType.WIND_SPOT),
                PowerTerrainTile(GridCoord(0, 5), PowerTerrainType.WIND_SPOT),
                PowerTerrainTile(GridCoord(1, 5), PowerTerrainType.WIND_SPOT),
                PowerTerrainTile(GridCoord(5, 0), PowerTerrainType.WIND_SPOT),
                // Subsurface rock obstacles on Layer 1
                PowerTerrainTile(GridCoord(2, 5), PowerTerrainType.ROCK),
                PowerTerrainTile(GridCoord(3, 4), PowerTerrainType.ROCK)
            ),
            hint = "Switch to [⚡ Power], place a Wind Turbine on a glowing Wind Spot (+6⚡), and route Power Cables directly under your Smelter (factoryX+2, factoryY+2)."
        ),
        LevelConfig(
            id = 142,
            name = "River Current",
            subtitle = "World 5: High Voltage · Hydro Generators",
            width = 8,
            height = 10,
            parBelts = 14,
            requiresPower = true,
            newMechanicBadge = "HYDRO POWER",
            spawners = listOf(
                PlacedTile(
                    coord = GridCoord(1, 1),
                    tile = TileEntity.Spawner(
                        itemType = ItemType.IRON_ORE,
                        outputDir = Direction.SOUTH,
                        spawnIntervalTicks = 2
                    )
                ),
                PlacedTile(
                    coord = GridCoord(6, 1),
                    tile = TileEntity.Spawner(
                        itemType = ItemType.COPPER_ORE,
                        outputDir = Direction.SOUTH,
                        spawnIntervalTicks = 2
                    )
                )
            ),
            hatches = listOf(
                PlacedTile(
                    coord = GridCoord(2, 8),
                    tile = TileEntity.Hatch(
                        requiredItem = ItemType.IRON_PLATE,
                        targetCount = 12
                    )
                ),
                PlacedTile(
                    coord = GridCoord(6, 8),
                    tile = TileEntity.Hatch(
                        requiredItem = ItemType.COPPER_WIRE,
                        targetCount = 12
                    )
                )
            ),
            rocks = listOf(
                GridCoord(4, 3),
                GridCoord(4, 4),
                GridCoord(4, 5),
                GridCoord(0, 5),
                GridCoord(7, 5)
            ),
            powerTerrain = listOf(
                // Directional river channel along the top and right perimeter
                PowerTerrainTile(GridCoord(3, 0), PowerTerrainType.WATER_CURRENT, Direction.EAST),
                PowerTerrainTile(GridCoord(4, 0), PowerTerrainType.WATER_CURRENT, Direction.EAST),
                PowerTerrainTile(GridCoord(5, 0), PowerTerrainType.WATER_CURRENT, Direction.EAST),
                PowerTerrainTile(GridCoord(10, 4), PowerTerrainType.WATER_CURRENT, Direction.SOUTH),
                PowerTerrainTile(GridCoord(10, 5), PowerTerrainType.WATER_CURRENT, Direction.SOUTH),
                PowerTerrainTile(GridCoord(10, 6), PowerTerrainType.WATER_CURRENT, Direction.SOUTH),
                PowerTerrainTile(GridCoord(0, 7), PowerTerrainType.WIND_SPOT),
                // Rock barrier requiring cable routing
                PowerTerrainTile(GridCoord(4, 2), PowerTerrainType.ROCK),
                PowerTerrainTile(GridCoord(5, 2), PowerTerrainType.ROCK),
                PowerTerrainTile(GridCoord(8, 5), PowerTerrainType.ROCK)
            ),
            hint = "Place Hydro Generators (+8⚡) on the flowing Water Current river tiles on the outer perimeter to power both Smelter + Press lines."
        ),
        LevelConfig(
            id = 143,
            name = "Long Line",
            subtitle = "World 5: High Voltage · Capacitor Transmission Buffer",
            width = 9,
            height = 11,
            parBelts = 14,
            requiresPower = true,
            newMechanicBadge = "CAPACITOR BANK",
            spawners = listOf(
                PlacedTile(
                    coord = GridCoord(2, 5),
                    tile = TileEntity.Spawner(
                        itemType = ItemType.IRON_ORE,
                        outputDir = Direction.EAST,
                        spawnIntervalTicks = 1
                    )
                )
            ),
            hatches = listOf(
                PlacedTile(
                    coord = GridCoord(6, 9),
                    tile = TileEntity.Hatch(
                        requiredItem = ItemType.GEAR,
                        targetCount = 10
                    )
                )
            ),
            rocks = listOf(
                GridCoord(4, 4),
                GridCoord(4, 5),
                GridCoord(2, 8),
                GridCoord(7, 6)
            ),
            powerTerrain = listOf(
                // Remote northern river & wind gusts (>8 tiles from southern factory floor)
                PowerTerrainTile(GridCoord(1, 0), PowerTerrainType.WATER_CURRENT, Direction.EAST),
                PowerTerrainTile(GridCoord(2, 0), PowerTerrainType.WATER_CURRENT, Direction.EAST),
                PowerTerrainTile(GridCoord(0, 1), PowerTerrainType.WIND_SPOT),
                PowerTerrainTile(GridCoord(0, 2), PowerTerrainType.WIND_SPOT),
                // Rock ridge forcing a long transmission line across the Power Layer
                PowerTerrainTile(GridCoord(2, 3), PowerTerrainType.ROCK),
                PowerTerrainTile(GridCoord(3, 3), PowerTerrainType.ROCK),
                PowerTerrainTile(GridCoord(4, 3), PowerTerrainType.ROCK),
                PowerTerrainTile(GridCoord(5, 3), PowerTerrainType.ROCK),
                PowerTerrainTile(GridCoord(6, 6), PowerTerrainType.ROCK)
            ),
            hint = "Cable runs over 8 tiles lose 10% power per 4 extra tiles. Place a Capacitor Bank along the cable path to reset transmission loss and buffer full wattage!"
        ),
        LevelConfig(
            id = 144,
            name = "Overclock Surge",
            subtitle = "World 5: High Voltage · 2.0× Machine Overclocking",
            width = 9,
            height = 11,
            parBelts = 16,
            requiresPower = true,
            newMechanicBadge = "OVERCLOCK 2×",
            spawners = listOf(
                PlacedTile(
                    coord = GridCoord(1, 2),
                    tile = TileEntity.Spawner(
                        itemType = ItemType.IRON_ORE,
                        outputDir = Direction.EAST,
                        spawnIntervalTicks = 1
                    )
                ),
                PlacedTile(
                    coord = GridCoord(7, 2),
                    tile = TileEntity.Spawner(
                        itemType = ItemType.COPPER_ORE,
                        outputDir = Direction.WEST,
                        spawnIntervalTicks = 1
                    )
                )
            ),
            hatches = listOf(
                PlacedTile(
                    coord = GridCoord(4, 9),
                    tile = TileEntity.Hatch(
                        requiredItem = ItemType.CIRCUIT,
                        targetCount = 14
                    )
                )
            ),
            rocks = listOf(
                GridCoord(4, 2),
                GridCoord(4, 3),
                GridCoord(2, 6),
                GridCoord(6, 6)
            ),
            powerTerrain = listOf(
                PowerTerrainTile(GridCoord(0, 3), PowerTerrainType.WATER_CURRENT, Direction.SOUTH),
                PowerTerrainTile(GridCoord(0, 4), PowerTerrainType.WATER_CURRENT, Direction.SOUTH),
                PowerTerrainTile(GridCoord(12, 3), PowerTerrainType.WATER_CURRENT, Direction.SOUTH),
                PowerTerrainTile(GridCoord(12, 4), PowerTerrainType.WATER_CURRENT, Direction.SOUTH),
                PowerTerrainTile(GridCoord(5, 0), PowerTerrainType.WIND_SPOT),
                PowerTerrainTile(GridCoord(7, 0), PowerTerrainType.WIND_SPOT),
                PowerTerrainTile(GridCoord(6, 14), PowerTerrainType.WIND_SPOT),
                PowerTerrainTile(GridCoord(3, 5), PowerTerrainType.ROCK),
                PowerTerrainTile(GridCoord(9, 5), PowerTerrainType.ROCK)
            ),
            hint = "Tap your Smelter, Press, or Assembler (or use the Overclock tool) to toggle 2.0× Overclock speed at 2.5× power draw, backed by Hydro Generators and Capacitor Banks."
        ),
        LevelConfig(
            id = 145,
            name = "High Voltage Core",
            subtitle = "World 5: High Voltage · Dual-Grid Motor Megafactory",
            width = 10,
            height = 12,
            parBelts = 26,
            requiresPower = true,
            newMechanicBadge = "HIGH VOLTAGE",
            spawners = listOf(
                PlacedTile(
                    coord = GridCoord(1, 1),
                    tile = TileEntity.Spawner(
                        itemType = ItemType.IRON_ORE,
                        outputDir = Direction.SOUTH,
                        spawnIntervalTicks = 1,
                        secondaryItemType = ItemType.COPPER_ORE
                    )
                ),
                PlacedTile(
                    coord = GridCoord(8, 1),
                    tile = TileEntity.Spawner(
                        itemType = ItemType.IRON_ORE,
                        outputDir = Direction.SOUTH,
                        spawnIntervalTicks = 1
                    )
                )
            ),
            hatches = listOf(
                PlacedTile(
                    coord = GridCoord(4, 10),
                    tile = TileEntity.Hatch(
                        requiredItem = ItemType.MOTOR,
                        targetCount = 10
                    )
                ),
                PlacedTile(
                    coord = GridCoord(8, 10),
                    tile = TileEntity.Hatch(
                        requiredItem = ItemType.CIRCUIT,
                        targetCount = 10
                    )
                )
            ),
            rocks = listOf(
                GridCoord(4, 3),
                GridCoord(5, 3),
                GridCoord(3, 7),
                GridCoord(6, 7)
            ),
            powerTerrain = listOf(
                // Left & Right Hydro rivers + Top & Bottom Wind Spots
                PowerTerrainTile(GridCoord(0, 3), PowerTerrainType.WATER_CURRENT, Direction.SOUTH),
                PowerTerrainTile(GridCoord(0, 4), PowerTerrainType.WATER_CURRENT, Direction.SOUTH),
                PowerTerrainTile(GridCoord(0, 5), PowerTerrainType.WATER_CURRENT, Direction.SOUTH),
                PowerTerrainTile(GridCoord(13, 4), PowerTerrainType.WATER_CURRENT, Direction.SOUTH),
                PowerTerrainTile(GridCoord(13, 5), PowerTerrainType.WATER_CURRENT, Direction.SOUTH),
                PowerTerrainTile(GridCoord(13, 6), PowerTerrainType.WATER_CURRENT, Direction.SOUTH),
                PowerTerrainTile(GridCoord(4, 0), PowerTerrainType.WIND_SPOT),
                PowerTerrainTile(GridCoord(9, 0), PowerTerrainType.WIND_SPOT),
                PowerTerrainTile(GridCoord(4, 15), PowerTerrainType.WIND_SPOT),
                PowerTerrainTile(GridCoord(9, 15), PowerTerrainType.WIND_SPOT),
                // Subsurface rocks separating East and West grids
                PowerTerrainTile(GridCoord(6, 4), PowerTerrainType.ROCK),
                PowerTerrainTile(GridCoord(6, 5), PowerTerrainType.ROCK),
                PowerTerrainTile(GridCoord(7, 8), PowerTerrainType.ROCK),
                PowerTerrainTile(GridCoord(7, 9), PowerTerrainType.ROCK)
            ),
            hint = "Harness both perimeter rivers with Hydro Generators, bridge long runs with Capacitor Banks, and Overclock key bottlenecks to forge Motors and Circuits."
        )
    )
}
