package com.microfactory.sim

internal object LevelPack4 {
    val levels: List<LevelConfig> = listOf(
        // 56
        LevelConfig(
            id = 56,
            name = "Crossed Drives",
            subtitle = "Swap & Filter Mixed Plate/Wire Feeds",
            width = 10,
            height = 12,
            parBelts = 22,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.EAST, 1, secondaryItemType = ItemType.IRON_PLATE)),
                PlacedTile(GridCoord(8, 2), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.WEST, 1, secondaryItemType = ItemType.COPPER_WIRE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(5, 10), TileEntity.Hatch(ItemType.MOTOR, 10))
            ),
            rocks = listOf(
                GridCoord(4, 5), GridCoord(5, 5), GridCoord(2, 8), GridCoord(7, 8)
            ),
            hint = "Filter both mixed streams so Plates feed a Gear line and Plates+Wire feed a Circuit line, uniting into Motors."
        ),
        // 57
        LevelConfig(
            id = 57,
            name = "Copper Spill",
            subtitle = "Overflow Wire Into Dual Outputs",
            width = 10,
            height = 12,
            parBelts = 20,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 10), TileEntity.Hatch(ItemType.MOTOR, 8)),
                PlacedTile(GridCoord(8, 10), TileEntity.Hatch(ItemType.COPPER_WIRE, 14))
            ),
            rocks = listOf(
                GridCoord(5, 3), GridCoord(5, 4), GridCoord(5, 7), GridCoord(5, 8)
            ),
            hint = "Refine Copper into Wire and prioritize the Circuit->Motor chain with an Overflow spilling spare Wire."
        ),
        // 58
        LevelConfig(
            id = 58,
            name = "Servo Line",
            subtitle = "Triple-Quota Precision Foundry",
            width = 10,
            height = 12,
            parBelts = 24,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(5, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 10), TileEntity.Hatch(ItemType.GEAR, 10)),
                PlacedTile(GridCoord(5, 10), TileEntity.Hatch(ItemType.CIRCUIT, 10)),
                PlacedTile(GridCoord(8, 10), TileEntity.Hatch(ItemType.MOTOR, 8))
            ),
            rocks = listOf(
                GridCoord(3, 5), GridCoord(6, 5), GridCoord(3, 8), GridCoord(6, 8)
            ),
            hint = "Route the three top spawners through Smelters, Presses, and Assemblers to satisfy all three bays."
        ),
        // 59
        LevelConfig(
            id = 59,
            name = "Purged Assembly",
            subtitle = "Filter Ore Waste Before Motor Craft",
            width = 10,
            height = 12,
            parBelts = 22,
            spawners = listOf(
                PlacedTile(GridCoord(1, 3), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.EAST, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(8, 3), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.WEST, 1, secondaryItemType = ItemType.IRON_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(5, 10), TileEntity.Hatch(ItemType.MOTOR, 12))
            ),
            rocks = listOf(
                GridCoord(4, 1), GridCoord(5, 1), GridCoord(4, 6), GridCoord(5, 6), GridCoord(1, 9), GridCoord(8, 9)
            ),
            hint = "Either burn raw ores in Trash or Smelt+Press them into bonus Plates and Wire for faster Motors."
        ),
        // 60
        LevelConfig(
            id = 60,
            name = "Dual Motor",
            subtitle = "High-Output Motor Assembly",
            width = 10,
            height = 12,
            parBelts = 24,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_INGOT, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.COPPER_INGOT, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 10), TileEntity.Hatch(ItemType.MOTOR, 14)),
                PlacedTile(GridCoord(7, 10), TileEntity.Hatch(ItemType.IRON_PLATE, 12))
            ),
            rocks = listOf(
                GridCoord(4, 4), GridCoord(5, 4), GridCoord(2, 7), GridCoord(7, 7)
            ),
            hint = "Press both ingot lines rapidly, cascade into Motors, and overflow excess Plates to the right bay."
        ),
        // 61
        LevelConfig(
            id = 61,
            name = "Quadra Belt",
            subtitle = "Cross Four Lanes Through Tight Pillars",
            width = 10,
            height = 12,
            parBelts = 26,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 2)),
                PlacedTile(GridCoord(8, 2), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.WEST, 2)),
                PlacedTile(GridCoord(1, 9), TileEntity.Spawner(ItemType.GEAR, Direction.EAST, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(8, 9), TileEntity.Hatch(ItemType.MOTOR, 10)),
                PlacedTile(GridCoord(5, 10), TileEntity.Hatch(ItemType.IRON_PLATE, 10))
            ),
            rocks = listOf(
                GridCoord(4, 3), GridCoord(5, 3), GridCoord(4, 6), GridCoord(5, 6), GridCoord(3, 8)
            ),
            hint = "Combine Iron & Copper into Circuits, bridge across the Gear feed, and deliver Motors & Plates."
        ),
        // 62
        LevelConfig(
            id = 62,
            name = "Factory Matrix",
            subtitle = "Tier 5 Grand Motor Challenge",
            width = 10,
            height = 12,
            parBelts = 26,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(5, 10), TileEntity.Hatch(ItemType.MOTOR, 12)),
                PlacedTile(GridCoord(1, 10), TileEntity.Hatch(ItemType.GEAR, 10))
            ),
            rocks = listOf(
                GridCoord(3, 3), GridCoord(6, 3), GridCoord(4, 6), GridCoord(5, 6), GridCoord(3, 9), GridCoord(7, 9)
            ),
            hint = "Use Overflow after filling Gears or Motor parts so both quotas complete smoothly."
        ),
        // 63: Tier 6 Megaprojects (10x12 and 10x13)
        LevelConfig(
            id = 63,
            name = "Steel Labyrinth",
            subtitle = "Weave Through Staggered Walls",
            width = 10,
            height = 12,
            parBelts = 26,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.WEST, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 10), TileEntity.Hatch(ItemType.CIRCUIT, 16)),
                PlacedTile(GridCoord(7, 10), TileEntity.Hatch(ItemType.GEAR, 12))
            ),
            rocks = listOf(
                GridCoord(1, 4), GridCoord(2, 4), GridCoord(3, 4),
                GridCoord(6, 6), GridCoord(7, 6), GridCoord(8, 6),
                GridCoord(4, 8), GridCoord(5, 8)
            ),
            hint = "Use Bridges across the staggered rock shelves to connect Plates and Wire."
        ),
        // 64
        LevelConfig(
            id = 64,
            name = "Mixed Nexus",
            subtitle = "Twin Mixed Feeds Into One Hub",
            width = 10,
            height = 12,
            parBelts = 24,
            spawners = listOf(
                PlacedTile(GridCoord(2, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_WIRE)),
                PlacedTile(GridCoord(7, 1), TileEntity.Spawner(ItemType.GEAR, Direction.SOUTH, 2, secondaryItemType = ItemType.CIRCUIT))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(5, 10), TileEntity.Hatch(ItemType.MOTOR, 12)),
                PlacedTile(GridCoord(1, 10), TileEntity.Hatch(ItemType.CIRCUIT, 10))
            ),
            rocks = listOf(
                GridCoord(4, 4), GridCoord(5, 4), GridCoord(4, 7), GridCoord(5, 7)
            ),
            hint = "Filter both mixed streams cleanly: assemble Circuits on the left and Motors in the center."
        ),
        // 65
        LevelConfig(
            id = 65,
            name = "Burn & Build",
            subtitle = "High-Speed Trash-Assisted Foundry",
            width = 10,
            height = 12,
            parBelts = 22,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.EAST, 1, secondaryItemType = ItemType.IRON_ORE)),
                PlacedTile(GridCoord(8, 2), TileEntity.Spawner(ItemType.CIRCUIT, Direction.WEST, 1, secondaryItemType = ItemType.COPPER_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(5, 10), TileEntity.Hatch(ItemType.MOTOR, 14))
            ),
            rocks = listOf(
                GridCoord(3, 5), GridCoord(6, 5), GridCoord(3, 8), GridCoord(6, 8)
            ),
            hint = "Purge raw ores via Filters into Trash so Plate->Gear and Circuit feeds run at full speed."
        ),
        // 66
        LevelConfig(
            id = 66,
            name = "Threefold Path",
            subtitle = "Three Spawners, Three Hatches",
            width = 10,
            height = 13,
            parBelts = 28,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(5, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 11), TileEntity.Hatch(ItemType.IRON_INGOT, 12)),
                PlacedTile(GridCoord(5, 11), TileEntity.Hatch(ItemType.CIRCUIT, 12)),
                PlacedTile(GridCoord(8, 11), TileEntity.Hatch(ItemType.GEAR, 12))
            ),
            rocks = listOf(
                GridCoord(3, 4), GridCoord(7, 4), GridCoord(3, 8), GridCoord(7, 8)
            ),
            hint = "Overflow excess Plates into the Circuit Assembler while keeping Gears and Ingots flowing."
        ),
        // 67
        LevelConfig(
            id = 67,
            name = "Circuit Forge",
            subtitle = "Double Raw Ore to Circuits & Motors",
            width = 10,
            height = 13,
            parBelts = 28,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 11), TileEntity.Hatch(ItemType.CIRCUIT, 12)),
                PlacedTile(GridCoord(7, 11), TileEntity.Hatch(ItemType.MOTOR, 10))
            ),
            rocks = listOf(
                GridCoord(4, 3), GridCoord(5, 3), GridCoord(4, 6), GridCoord(5, 6), GridCoord(4, 9), GridCoord(5, 9)
            ),
            hint = "Use Bridges across the central rock spine to feed parallel Circuit and Motor assemblers."
        ),
        // 68
        LevelConfig(
            id = 68,
            name = "Gatekeeper",
            subtitle = "Overflow Priority Through Tight Choke",
            width = 10,
            height = 13,
            parBelts = 24,
            spawners = listOf(
                PlacedTile(GridCoord(4, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_WIRE)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.GEAR, Direction.SOUTH, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 11), TileEntity.Hatch(ItemType.GEAR, 12)),
                PlacedTile(GridCoord(7, 11), TileEntity.Hatch(ItemType.MOTOR, 10))
            ),
            rocks = listOf(
                GridCoord(1, 5), GridCoord(2, 5), GridCoord(6, 5), GridCoord(7, 5), GridCoord(8, 5)
            ),
            hint = "Filter Plates & Wire, assemble Circuits, and Overflow extra Plates into Gears."
        ),
        // 69
        LevelConfig(
            id = 69,
            name = "Crossfire",
            subtitle = "Four-Way Center Bridge Grid",
            width = 10,
            height = 13,
            parBelts = 28,
            spawners = listOf(
                PlacedTile(GridCoord(1, 6), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1)),
                PlacedTile(GridCoord(8, 6), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.WEST, 1)),
                PlacedTile(GridCoord(5, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(5, 11), TileEntity.Hatch(ItemType.MOTOR, 10)),
                PlacedTile(GridCoord(1, 11), TileEntity.Hatch(ItemType.COPPER_WIRE, 12))
            ),
            rocks = listOf(
                GridCoord(3, 4), GridCoord(7, 4), GridCoord(3, 8), GridCoord(7, 8)
            ),
            hint = "Cross horizontal ore lines beneath vertical assembly feeds using multiple Bridges."
        ),
        // 70
        LevelConfig(
            id = 70,
            name = "Mega Refinery",
            subtitle = "High-Quota Mixed Ore Processing",
            width = 10,
            height = 13,
            parBelts = 28,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1, secondaryItemType = ItemType.IRON_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 11), TileEntity.Hatch(ItemType.IRON_PLATE, 16)),
                PlacedTile(GridCoord(7, 11), TileEntity.Hatch(ItemType.COPPER_WIRE, 16)),
                PlacedTile(GridCoord(5, 11), TileEntity.Hatch(ItemType.CIRCUIT, 12))
            ),
            rocks = listOf(
                GridCoord(4, 3), GridCoord(5, 3), GridCoord(2, 7), GridCoord(7, 7)
            ),
            hint = "Sort both mixed ore feeds, smelt & press everything, then split into all three bottom hatches."
        ),
        // 71
        LevelConfig(
            id = 71,
            name = "Turbo Cascade",
            subtitle = "Overflow Chain Into Three Products",
            width = 10,
            height = 13,
            parBelts = 26,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.EAST, 1)),
                PlacedTile(GridCoord(8, 2), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.WEST, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 11), TileEntity.Hatch(ItemType.IRON_PLATE, 12)),
                PlacedTile(GridCoord(5, 11), TileEntity.Hatch(ItemType.MOTOR, 12)),
                PlacedTile(GridCoord(8, 11), TileEntity.Hatch(ItemType.CIRCUIT, 12))
            ),
            rocks = listOf(
                GridCoord(3, 5), GridCoord(6, 5), GridCoord(3, 9), GridCoord(6, 9)
            ),
            hint = "Prioritize the Motor line or Plate hatch via Overflow so excess feeds the remaining quotas."
        ),
        // 72
        LevelConfig(
            id = 72,
            name = "Twin Mixed Motors",
            subtitle = "Dual Mixed Feeds to Pure Motors",
            width = 10,
            height = 13,
            parBelts = 26,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.GEAR, Direction.SOUTH, 1, secondaryItemType = ItemType.IRON_ORE)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.CIRCUIT, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(5, 11), TileEntity.Hatch(ItemType.MOTOR, 16)),
                PlacedTile(GridCoord(1, 11), TileEntity.Hatch(ItemType.IRON_INGOT, 10)),
                PlacedTile(GridCoord(8, 11), TileEntity.Hatch(ItemType.COPPER_INGOT, 10))
            ),
            rocks = listOf(
                GridCoord(4, 4), GridCoord(5, 4), GridCoord(4, 8), GridCoord(5, 8)
            ),
            hint = "Filter Gears and Circuits to the center Motor Assembler while smelting the separated ores below!"
        ),
        // 73
        LevelConfig(
            id = 73,
            name = "Overpass Matrix",
            subtitle = "Dense Rock Grid With Dual Bridges",
            width = 10,
            height = 13,
            parBelts = 30,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.WEST, 1)),
                PlacedTile(GridCoord(5, 12), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.NORTH, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 11), TileEntity.Hatch(ItemType.CIRCUIT, 14)),
                PlacedTile(GridCoord(8, 11), TileEntity.Hatch(ItemType.MOTOR, 10))
            ),
            rocks = listOf(
                GridCoord(3, 3), GridCoord(7, 3), GridCoord(2, 6), GridCoord(8, 6), GridCoord(4, 9), GridCoord(6, 9)
            ),
            hint = "Bring bottom Iron Plates upward through Bridges to meet refined Iron and Copper streams."
        ),
        // 74
        LevelConfig(
            id = 74,
            name = "Smelter Gauntlet",
            subtitle = "Long-Haul Raw Ore Refinement",
            width = 10,
            height = 13,
            parBelts = 28,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 11), TileEntity.Hatch(ItemType.GEAR, 12)),
                PlacedTile(GridCoord(5, 11), TileEntity.Hatch(ItemType.MOTOR, 10)),
                PlacedTile(GridCoord(8, 11), TileEntity.Hatch(ItemType.COPPER_WIRE, 14))
            ),
            rocks = listOf(
                GridCoord(2, 4), GridCoord(7, 4), GridCoord(4, 6), GridCoord(5, 6), GridCoord(2, 9), GridCoord(7, 9)
            ),
            hint = "Refine both ores high up, split into Circuits and Gears, and balance surplus Copper Wire."
        ),
        // 75
        LevelConfig(
            id = 75,
            name = "Relay Station",
            subtitle = "Overflow & Filter Synchronized",
            width = 10,
            height = 13,
            parBelts = 28,
            spawners = listOf(
                PlacedTile(GridCoord(1, 3), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.EAST, 1, secondaryItemType = ItemType.GEAR)),
                PlacedTile(GridCoord(8, 3), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.WEST, 1, secondaryItemType = ItemType.CIRCUIT))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(5, 11), TileEntity.Hatch(ItemType.MOTOR, 14)),
                PlacedTile(GridCoord(1, 11), TileEntity.Hatch(ItemType.GEAR, 10)),
                PlacedTile(GridCoord(8, 11), TileEntity.Hatch(ItemType.CIRCUIT, 10))
            ),
            rocks = listOf(
                GridCoord(4, 1), GridCoord(5, 1), GridCoord(3, 7), GridCoord(6, 7)
            ),
            hint = "Filter Gears & Circuits straight toward Motors while assembling Plates & Wire into bonus parts."
        ),
        // 76
        LevelConfig(
            id = 76,
            name = "Precision Works",
            subtitle = "Zero-Waste Mixed Refinery",
            width = 10,
            height = 13,
            parBelts = 30,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_WIRE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(3, 11), TileEntity.Hatch(ItemType.MOTOR, 12)),
                PlacedTile(GridCoord(7, 11), TileEntity.Hatch(ItemType.CIRCUIT, 14))
            ),
            rocks = listOf(
                GridCoord(4, 4), GridCoord(5, 4), GridCoord(4, 7), GridCoord(5, 7), GridCoord(1, 8), GridCoord(8, 8)
            ),
            hint = "Filter all four item types cleanly; smelt & press the left ores and feed dual Assemblers."
        ),
        // 77
        LevelConfig(
            id = 77,
            name = "Titan Corridor",
            subtitle = "Narrow Channels, High Quotas",
            width = 10,
            height = 13,
            parBelts = 28,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1)),
                PlacedTile(GridCoord(1, 11), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.EAST, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(8, 2), TileEntity.Hatch(ItemType.GEAR, 12)),
                PlacedTile(GridCoord(8, 6), TileEntity.Hatch(ItemType.MOTOR, 12)),
                PlacedTile(GridCoord(8, 10), TileEntity.Hatch(ItemType.CIRCUIT, 12))
            ),
            rocks = listOf(
                GridCoord(5, 1), GridCoord(5, 4), GridCoord(5, 8), GridCoord(5, 11)
            ),
            hint = "Refine Iron along the upper flank and Copper along the lower flank, bridging into the center Motor bay."
        ),
        // 78
        LevelConfig(
            id = 78,
            name = "Cascade Core",
            subtitle = "Full Multi-Tier Overflow Network",
            width = 10,
            height = 13,
            parBelts = 30,
            spawners = listOf(
                PlacedTile(GridCoord(4, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(5, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(1, 6), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.EAST, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(8, 6), TileEntity.Hatch(ItemType.MOTOR, 14)),
                PlacedTile(GridCoord(2, 11), TileEntity.Hatch(ItemType.GEAR, 12)),
                PlacedTile(GridCoord(7, 11), TileEntity.Hatch(ItemType.CIRCUIT, 12))
            ),
            rocks = listOf(
                GridCoord(3, 4), GridCoord(6, 4), GridCoord(3, 8), GridCoord(6, 8)
            ),
            hint = "Use Overflow and Splitters to keep Motor assembly saturated while filling bottom quotas."
        ),
        // 79
        LevelConfig(
            id = 79,
            name = "Omega Grid",
            subtitle = "Penultimate Industrial Gauntlet",
            width = 10,
            height = 13,
            parBelts = 32,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1, secondaryItemType = ItemType.IRON_ORE)),
                PlacedTile(GridCoord(5, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 11), TileEntity.Hatch(ItemType.GEAR, 12)),
                PlacedTile(GridCoord(5, 11), TileEntity.Hatch(ItemType.MOTOR, 12)),
                PlacedTile(GridCoord(8, 11), TileEntity.Hatch(ItemType.CIRCUIT, 14))
            ),
            rocks = listOf(
                GridCoord(3, 5), GridCoord(7, 5), GridCoord(2, 8), GridCoord(7, 8)
            ),
            hint = "Sort both mixed ore streams, cross with Bridges, and synchronize three parallel assembly lines."
        ),
        // 80
        LevelConfig(
            id = 80,
            name = "Apex Automation",
            subtitle = "Grand Finale: Master Every Machine",
            width = 10,
            height = 13,
            parBelts = 34,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(5, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 11), TileEntity.Hatch(ItemType.GEAR, 10)),
                PlacedTile(GridCoord(5, 11), TileEntity.Hatch(ItemType.MOTOR, 12)),
                PlacedTile(GridCoord(8, 11), TileEntity.Hatch(ItemType.CIRCUIT, 12))
            ),
            rocks = listOf(
                GridCoord(3, 4), GridCoord(6, 4), GridCoord(3, 8), GridCoord(6, 8), GridCoord(1, 9), GridCoord(8, 9)
            ),
            hint = "Combine Filters, Bridges, Splitters, Smelters, Presses, and cascading Assemblers for total mastery."
        )
    )
}
