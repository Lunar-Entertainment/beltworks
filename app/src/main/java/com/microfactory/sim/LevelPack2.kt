package com.microfactory.sim

internal object LevelPack2 {
    val levels: List<LevelConfig> = listOf(
        // 9: Introduces COPPER_WIRE
        LevelConfig(
            id = 9,
            name = "Copper Spool",
            subtitle = "Smelt & Press Copper into Wire",
            width = 8,
            height = 10,
            parBelts = 8,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.EAST, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(6, 7), TileEntity.Hatch(ItemType.COPPER_WIRE, 12))
            ),
            rocks = listOf(
                GridCoord(4, 1), GridCoord(4, 2), GridCoord(4, 3),
                GridCoord(2, 6), GridCoord(3, 6)
            ),
            hint = "Feed Copper Ore into a Smelter, then stamp Copper Ingots in a Press to produce Copper Wire.",
            newMechanicBadge = "Copper Wire"
        ),
        // 10: Introduces Bridge
        LevelConfig(
            id = 10,
            name = "Overpass",
            subtitle = "Cross Two Belts Without Mixing",
            width = 8,
            height = 10,
            parBelts = 11,
            spawners = listOf(
                PlacedTile(GridCoord(1, 4), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 2)),
                PlacedTile(GridCoord(3, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(6, 4), TileEntity.Hatch(ItemType.IRON_INGOT, 10)),
                PlacedTile(GridCoord(3, 8), TileEntity.Hatch(ItemType.COPPER_WIRE, 10))
            ),
            rocks = listOf(
                GridCoord(2, 3), GridCoord(4, 3), GridCoord(2, 5), GridCoord(4, 5),
                GridCoord(2, 2), GridCoord(4, 6)
            ),
            hint = "Use a Bridge at the center intersection so Iron and Copper lanes cross cleanly.",
            newMechanicBadge = "Bridge"
        ),
        // 11
        LevelConfig(
            id = 11,
            name = "Crossroads",
            subtitle = "Diagonal Crossing Under Pressure",
            width = 8,
            height = 10,
            parBelts = 15,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 2)),
                PlacedTile(GridCoord(6, 2), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.WEST, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(6, 7), TileEntity.Hatch(ItemType.IRON_PLATE, 12)),
                PlacedTile(GridCoord(1, 7), TileEntity.Hatch(ItemType.COPPER_WIRE, 12))
            ),
            rocks = listOf(
                GridCoord(2, 4), GridCoord(5, 4), GridCoord(3, 6), GridCoord(4, 6)
            ),
            hint = "Process both lines into Plates and Wire, then cross them cleanly with a Bridge."
        ),
        // 12
        LevelConfig(
            id = 12,
            name = "Copper Braid",
            subtitle = "Parallel Wire Stamping",
            width = 8,
            height = 10,
            parBelts = 11,
            spawners = listOf(
                PlacedTile(GridCoord(3, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(3, 8), TileEntity.Hatch(ItemType.COPPER_WIRE, 20))
            ),
            rocks = listOf(
                GridCoord(3, 5), GridCoord(0, 4), GridCoord(1, 4), GridCoord(6, 4), GridCoord(7, 4)
            ),
            hint = "Split the fast Copper Ore stream into two parallel Smelter + Press lanes."
        ),
        // 13
        LevelConfig(
            id = 13,
            name = "Twin Cross",
            subtitle = "Narrow Channels, Two Overpasses",
            width = 9,
            height = 11,
            parBelts = 18,
            spawners = listOf(
                PlacedTile(GridCoord(1, 3), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 2)),
                PlacedTile(GridCoord(7, 3), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.WEST, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(7, 7), TileEntity.Hatch(ItemType.IRON_PLATE, 14)),
                PlacedTile(GridCoord(1, 7), TileEntity.Hatch(ItemType.COPPER_WIRE, 14))
            ),
            rocks = listOf(
                GridCoord(4, 2), GridCoord(4, 4), GridCoord(4, 6), GridCoord(4, 8)
            ),
            hint = "Route across the central rock spine using Bridges in the open gaps."
        ),
        // 14
        LevelConfig(
            id = 14,
            name = "Bridgework Gear",
            subtitle = "Cross a Wire Line Mid-Assembly",
            width = 9,
            height = 11,
            parBelts = 17,
            spawners = listOf(
                PlacedTile(GridCoord(1, 5), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.EAST, 1)),
                PlacedTile(GridCoord(4, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(7, 5), TileEntity.Hatch(ItemType.GEAR, 10)),
                PlacedTile(GridCoord(4, 9), TileEntity.Hatch(ItemType.COPPER_WIRE, 12))
            ),
            rocks = listOf(
                GridCoord(2, 3), GridCoord(6, 3), GridCoord(2, 7), GridCoord(6, 7)
            ),
            hint = "Send Copper straight down via a Bridge while splitting Iron Plates into an Assembler."
        ),
        // 15
        LevelConfig(
            id = 15,
            name = "Threaded Maze",
            subtitle = "Navigate Twin Rock Barriers",
            width = 9,
            height = 11,
            parBelts = 22,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 2)),
                PlacedTile(GridCoord(7, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(7, 9), TileEntity.Hatch(ItemType.IRON_INGOT, 14)),
                PlacedTile(GridCoord(1, 9), TileEntity.Hatch(ItemType.COPPER_WIRE, 14))
            ),
            rocks = listOf(
                GridCoord(1, 4), GridCoord(2, 4), GridCoord(6, 4), GridCoord(7, 4),
                GridCoord(1, 6), GridCoord(2, 6), GridCoord(6, 6), GridCoord(7, 6)
            ),
            hint = "Thread both streams through the central corridor using Bridges to swap sides."
        ),
        // 16
        LevelConfig(
            id = 16,
            name = "Wire & Cog",
            subtitle = "Opposite Corners, Full Refinement",
            width = 9,
            height = 11,
            parBelts = 19,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1)),
                PlacedTile(GridCoord(1, 8), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.EAST, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(7, 8), TileEntity.Hatch(ItemType.GEAR, 12)),
                PlacedTile(GridCoord(7, 2), TileEntity.Hatch(ItemType.COPPER_WIRE, 16))
            ),
            rocks = listOf(
                GridCoord(4, 1), GridCoord(4, 9), GridCoord(5, 4), GridCoord(5, 6)
            ),
            hint = "Refine Iron Ore into Gears and cross the Copper Wire line through the middle."
        ),
        // 17
        LevelConfig(
            id = 17,
            name = "Interchange",
            subtitle = "Boxed Center Crossing",
            width = 9,
            height = 12,
            parBelts = 17,
            spawners = listOf(
                PlacedTile(GridCoord(4, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 2)),
                PlacedTile(GridCoord(1, 5), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.EAST, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 10), TileEntity.Hatch(ItemType.IRON_PLATE, 16)),
                PlacedTile(GridCoord(7, 5), TileEntity.Hatch(ItemType.COPPER_WIRE, 16))
            ),
            rocks = listOf(
                GridCoord(3, 4), GridCoord(5, 4), GridCoord(3, 6), GridCoord(5, 6)
            ),
            hint = "Smelt & press both lines before or after the central Bridge intersection."
        ),
        // 18
        LevelConfig(
            id = 18,
            name = "Bridge Mastery",
            subtitle = "Diamond Obstacle Crossing",
            width = 9,
            height = 12,
            parBelts = 22,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.EAST, 1)),
                PlacedTile(GridCoord(7, 2), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.WEST, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(7, 9), TileEntity.Hatch(ItemType.GEAR, 14)),
                PlacedTile(GridCoord(1, 9), TileEntity.Hatch(ItemType.COPPER_WIRE, 18))
            ),
            rocks = listOf(
                GridCoord(4, 4), GridCoord(3, 5), GridCoord(5, 5), GridCoord(4, 6)
            ),
            hint = "Route around the central rock diamond using Bridges where the Iron and Copper lanes meet."
        ),
        // 19: Introduces CIRCUIT
        LevelConfig(
            id = 19,
            name = "First Circuit",
            subtitle = "Assemble Plates & Copper Wire",
            width = 8,
            height = 10,
            parBelts = 8,
            spawners = listOf(
                PlacedTile(GridCoord(1, 4), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.EAST, 2)),
                PlacedTile(GridCoord(6, 4), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.WEST, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(3, 8), TileEntity.Hatch(ItemType.CIRCUIT, 10))
            ),
            rocks = listOf(
                GridCoord(3, 2), GridCoord(4, 2), GridCoord(2, 6)
            ),
            hint = "Feed Iron Plate into one side of an Assembler and Copper Wire into the other to craft Circuits.",
            newMechanicBadge = "Circuit"
        ),
        // 20
        LevelConfig(
            id = 20,
            name = "Raw Silicon",
            subtitle = "Press Ingots Before Assembly",
            width = 8,
            height = 10,
            parBelts = 11,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.IRON_INGOT, Direction.SOUTH, 2)),
                PlacedTile(GridCoord(6, 2), TileEntity.Spawner(ItemType.COPPER_INGOT, Direction.SOUTH, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(3, 8), TileEntity.Hatch(ItemType.CIRCUIT, 10))
            ),
            rocks = listOf(
                GridCoord(3, 4), GridCoord(4, 4), GridCoord(2, 7), GridCoord(5, 7)
            ),
            hint = "Stamp both ingots with Presses, then merge them into a central Assembler."
        ),
        // 21
        LevelConfig(
            id = 21,
            name = "Ore to Board",
            subtitle = "Full Smelt-Press-Assemble Pipeline",
            width = 9,
            height = 11,
            parBelts = 13,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 2)),
                PlacedTile(GridCoord(7, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 9), TileEntity.Hatch(ItemType.CIRCUIT, 10))
            ),
            rocks = listOf(
                GridCoord(4, 3), GridCoord(4, 4), GridCoord(2, 7), GridCoord(6, 7)
            ),
            hint = "Smelt and press both raw ores on the wings, then feed them into a central Assembler."
        ),
        // 22
        LevelConfig(
            id = 22,
            name = "Crossed Logic",
            subtitle = "Swap Ingredients Across a Divider",
            width = 9,
            height = 11,
            parBelts = 16,
            spawners = listOf(
                PlacedTile(GridCoord(7, 2), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.WEST, 2)),
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.EAST, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 9), TileEntity.Hatch(ItemType.CIRCUIT, 12))
            ),
            rocks = listOf(
                GridCoord(4, 1), GridCoord(4, 2), GridCoord(4, 4), GridCoord(4, 5), GridCoord(3, 7), GridCoord(5, 7)
            ),
            hint = "Cross the Plate and Wire lines through the open row using a Bridge before assembling."
        ),
        // 23
        LevelConfig(
            id = 23,
            name = "Circuit Split",
            subtitle = "Share Plates Between Gears & Circuits",
            width = 9,
            height = 11,
            parBelts = 17,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.EAST, 1)),
                PlacedTile(GridCoord(7, 2), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.WEST, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 9), TileEntity.Hatch(ItemType.GEAR, 8)),
                PlacedTile(GridCoord(7, 9), TileEntity.Hatch(ItemType.CIRCUIT, 8))
            ),
            rocks = listOf(
                GridCoord(4, 4), GridCoord(4, 5), GridCoord(3, 8), GridCoord(5, 8)
            ),
            hint = "Split the Iron Plate feed to supply both a Gear Assembler and a Circuit Assembler."
        ),
        // 24
        LevelConfig(
            id = 24,
            name = "Twin Assembly",
            subtitle = "High-Speed Circuit Production",
            width = 9,
            height = 11,
            parBelts = 15,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_INGOT, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(7, 1), TileEntity.Spawner(ItemType.COPPER_INGOT, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 9), TileEntity.Hatch(ItemType.CIRCUIT, 16))
            ),
            rocks = listOf(
                GridCoord(4, 2), GridCoord(4, 3), GridCoord(1, 6), GridCoord(7, 6)
            ),
            hint = "Press both fast ingot streams into Plates and Wire and feed them continuously to an Assembler."
        ),
        // 25
        LevelConfig(
            id = 25,
            name = "Board & Wire",
            subtitle = "Divert Half the Copper to Wire Quota",
            width = 9,
            height = 12,
            parBelts = 19,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 2)),
                PlacedTile(GridCoord(7, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 10), TileEntity.Hatch(ItemType.CIRCUIT, 10)),
                PlacedTile(GridCoord(7, 10), TileEntity.Hatch(ItemType.COPPER_WIRE, 10))
            ),
            rocks = listOf(
                GridCoord(4, 4), GridCoord(4, 5), GridCoord(4, 6), GridCoord(5, 8)
            ),
            hint = "Split Copper Wire between the Circuit Assembler and the right-hand Copper Wire hatch."
        ),
        // 26
        LevelConfig(
            id = 26,
            name = "Copper Core",
            subtitle = "Weave Through Pillar Columns",
            width = 9,
            height = 12,
            parBelts = 17,
            spawners = listOf(
                PlacedTile(GridCoord(1, 5), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 2)),
                PlacedTile(GridCoord(7, 5), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.WEST, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 10), TileEntity.Hatch(ItemType.CIRCUIT, 14))
            ),
            rocks = listOf(
                GridCoord(3, 3), GridCoord(5, 3), GridCoord(3, 7), GridCoord(5, 7), GridCoord(4, 4)
            ),
            hint = "Place Smelters and Presses along the horizontal flanks, converging in the lower center."
        ),
        // 27
        LevelConfig(
            id = 27,
            name = "Clock & Logic",
            subtitle = "Dual Raw Ore Refining",
            width = 9,
            height = 12,
            parBelts = 22,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1)),
                PlacedTile(GridCoord(7, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.WEST, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 10), TileEntity.Hatch(ItemType.GEAR, 10)),
                PlacedTile(GridCoord(7, 10), TileEntity.Hatch(ItemType.CIRCUIT, 10))
            ),
            rocks = listOf(
                GridCoord(4, 2), GridCoord(4, 5), GridCoord(4, 8), GridCoord(2, 6), GridCoord(6, 6)
            ),
            hint = "Split refined Iron Plates so one wing crafts Gears and the other feeds the Circuit Assembler."
        ),
        // 28
        LevelConfig(
            id = 28,
            name = "Triple Threat",
            subtitle = "Fulfill Three Simultaneous Quotas",
            width = 9,
            height = 12,
            parBelts = 22,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(7, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 10), TileEntity.Hatch(ItemType.IRON_PLATE, 10)),
                PlacedTile(GridCoord(4, 10), TileEntity.Hatch(ItemType.CIRCUIT, 10)),
                PlacedTile(GridCoord(7, 10), TileEntity.Hatch(ItemType.COPPER_WIRE, 10))
            ),
            rocks = listOf(
                GridCoord(3, 4), GridCoord(5, 4), GridCoord(3, 8), GridCoord(5, 8)
            ),
            hint = "Split both Iron Plates and Copper Wire so half feeds the central Circuit Assembler."
        ),
        // 29
        LevelConfig(
            id = 29,
            name = "Bus Crossing",
            subtitle = "Multi-Wall Crossing Canyon",
            width = 9,
            height = 12,
            parBelts = 20,
            spawners = listOf(
                PlacedTile(GridCoord(7, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 2)),
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 10), TileEntity.Hatch(ItemType.CIRCUIT, 16))
            ),
            rocks = listOf(
                GridCoord(1, 5), GridCoord(2, 5), GridCoord(6, 5), GridCoord(7, 5),
                GridCoord(4, 3), GridCoord(4, 7)
            ),
            hint = "Channel both lines through the center canyon with a Bridge and assemble Circuits below."
        ),
        // 30
        LevelConfig(
            id = 30,
            name = "Circuit Finale",
            subtitle = "Tier 3 Mastery on Expanded Grid",
            width = 10,
            height = 12,
            parBelts = 25,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 10), TileEntity.Hatch(ItemType.GEAR, 12)),
                PlacedTile(GridCoord(7, 10), TileEntity.Hatch(ItemType.CIRCUIT, 18))
            ),
            rocks = listOf(
                GridCoord(4, 3), GridCoord(5, 3), GridCoord(4, 7), GridCoord(5, 7), GridCoord(1, 7), GridCoord(8, 7)
            ),
            hint = "Split Iron Plates to power both a Gear line on the left and a Circuit line on the right."
        )
    )
}
