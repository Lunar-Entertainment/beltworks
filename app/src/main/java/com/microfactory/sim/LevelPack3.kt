package com.microfactory.sim

internal object LevelPack3 {
    val levels: List<LevelConfig> = listOf(
        // 31: Introduces Mixed Spawner + Filter
        LevelConfig(
            id = 31,
            name = "First Sort",
            subtitle = "Separate Mixed Iron & Copper Ore",
            width = 8,
            height = 10,
            parBelts = 11,
            spawners = listOf(
                PlacedTile(GridCoord(3, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 8), TileEntity.Hatch(ItemType.IRON_INGOT, 8)),
                PlacedTile(GridCoord(6, 8), TileEntity.Hatch(ItemType.COPPER_INGOT, 8))
            ),
            rocks = listOf(
                GridCoord(3, 5), GridCoord(4, 5), GridCoord(0, 4), GridCoord(7, 4)
            ),
            hint = "Place a Filter set to Iron Ore to split the mixed stream into separate Smelters.",
            newMechanicBadge = "Filter"
        ),
        // 32
        LevelConfig(
            id = 32,
            name = "Sorted Press",
            subtitle = "Filter Ingots Into Plate & Wire",
            width = 8,
            height = 10,
            parBelts = 10,
            spawners = listOf(
                PlacedTile(GridCoord(1, 4), TileEntity.Spawner(ItemType.IRON_INGOT, Direction.EAST, 1, secondaryItemType = ItemType.COPPER_INGOT))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(6, 2), TileEntity.Hatch(ItemType.IRON_PLATE, 10)),
                PlacedTile(GridCoord(6, 7), TileEntity.Hatch(ItemType.COPPER_WIRE, 10))
            ),
            rocks = listOf(
                GridCoord(4, 4), GridCoord(4, 5), GridCoord(2, 1), GridCoord(2, 8)
            ),
            hint = "Filter Iron Ingot upward and Copper Ingot downward, then stamp each with a Press."
        ),
        // 33
        LevelConfig(
            id = 33,
            name = "Scrap Loop",
            subtitle = "One Mixed Spawner Into Circuits",
            width = 9,
            height = 11,
            parBelts = 13,
            spawners = listOf(
                PlacedTile(GridCoord(4, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 9), TileEntity.Hatch(ItemType.CIRCUIT, 8))
            ),
            rocks = listOf(
                GridCoord(4, 5), GridCoord(1, 3), GridCoord(7, 3), GridCoord(1, 7), GridCoord(7, 7)
            ),
            hint = "Sort the mixed ores, refine both wings into Plates and Wire, then reunite them in an Assembler."
        ),
        // 34
        LevelConfig(
            id = 34,
            name = "Double Sort",
            subtitle = "Merge Twin Mixed Streams",
            width = 9,
            height = 11,
            parBelts = 19,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 2, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(7, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 2, secondaryItemType = ItemType.IRON_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 9), TileEntity.Hatch(ItemType.IRON_PLATE, 12)),
                PlacedTile(GridCoord(7, 9), TileEntity.Hatch(ItemType.COPPER_WIRE, 12))
            ),
            rocks = listOf(
                GridCoord(4, 2), GridCoord(4, 5), GridCoord(4, 8), GridCoord(2, 6), GridCoord(6, 6)
            ),
            hint = "Filter both mixed streams and use a Bridge to cross Iron to the left and Copper to the right."
        ),
        // 35: Introduces Incinerator
        LevelConfig(
            id = 35,
            name = "Clean Burn",
            subtitle = "Filter & Destroy Unwanted Ore",
            width = 8,
            height = 10,
            parBelts = 7,
            spawners = listOf(
                PlacedTile(GridCoord(1, 4), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1, secondaryItemType = ItemType.COPPER_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(6, 4), TileEntity.Hatch(ItemType.IRON_INGOT, 12))
            ),
            rocks = listOf(
                GridCoord(3, 2), GridCoord(4, 2), GridCoord(3, 6), GridCoord(4, 6)
            ),
            hint = "Use a Filter to send Copper Ore into a Trash (Incinerator) so Iron Ore keeps flowing.",
            newMechanicBadge = "Trash"
        ),
        // 36
        LevelConfig(
            id = 36,
            name = "Copper Purge",
            subtitle = "Assemble Gears From Mixed Parts",
            width = 8,
            height = 10,
            parBelts = 11,
            spawners = listOf(
                PlacedTile(GridCoord(3, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_WIRE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(3, 8), TileEntity.Hatch(ItemType.GEAR, 10))
            ),
            rocks = listOf(
                GridCoord(1, 4), GridCoord(6, 4), GridCoord(1, 6), GridCoord(6, 6)
            ),
            hint = "Filter out Copper Wire into Trash, then split Iron Plates into a Gear Assembler."
        ),
        // 37
        LevelConfig(
            id = 37,
            name = "Selective Circuit",
            subtitle = "Purge Raw Ore From Plate Feed",
            width = 9,
            height = 11,
            parBelts = 12,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.EAST, 1, secondaryItemType = ItemType.IRON_ORE)),
                PlacedTile(GridCoord(7, 2), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.WEST, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 9), TileEntity.Hatch(ItemType.CIRCUIT, 10))
            ),
            rocks = listOf(
                GridCoord(4, 3), GridCoord(4, 4), GridCoord(2, 7), GridCoord(6, 7)
            ),
            hint = "Filter Iron Plates toward the Assembler and either Trash or Smelt+Press the extra Iron Ore."
        ),
        // 38
        LevelConfig(
            id = 38,
            name = "Bridge & Filter",
            subtitle = "Sort & Cross to Opposite Bays",
            width = 9,
            height = 11,
            parBelts = 15,
            spawners = listOf(
                PlacedTile(GridCoord(1, 5), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.EAST, 1, secondaryItemType = ItemType.COPPER_WIRE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(7, 2), TileEntity.Hatch(ItemType.IRON_PLATE, 12)),
                PlacedTile(GridCoord(7, 8), TileEntity.Hatch(ItemType.COPPER_WIRE, 12))
            ),
            rocks = listOf(
                GridCoord(4, 1), GridCoord(4, 3), GridCoord(4, 7), GridCoord(4, 9), GridCoord(5, 5)
            ),
            hint = "Split the mixed feed with a Filter and weave through the central rock openings."
        ),
        // 39
        LevelConfig(
            id = 39,
            name = "Triple Mix",
            subtitle = "Refine Mixed Ore Alongside Plates",
            width = 9,
            height = 12,
            parBelts = 19,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(7, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 10), TileEntity.Hatch(ItemType.CIRCUIT, 12)),
                PlacedTile(GridCoord(7, 10), TileEntity.Hatch(ItemType.GEAR, 8))
            ),
            rocks = listOf(
                GridCoord(3, 5), GridCoord(5, 5), GridCoord(1, 8), GridCoord(6, 8)
            ),
            hint = "Filter Copper Ore on the left into Wire for Circuits, Trash Iron Ore, and split Plates on the right."
        ),
        // 40
        LevelConfig(
            id = 40,
            name = "Refinery Purge",
            subtitle = "High-Volume Copper Wire Extraction",
            width = 9,
            height = 12,
            parBelts = 12,
            spawners = listOf(
                PlacedTile(GridCoord(4, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1, secondaryItemType = ItemType.IRON_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 10), TileEntity.Hatch(ItemType.COPPER_WIRE, 14))
            ),
            rocks = listOf(
                GridCoord(2, 4), GridCoord(6, 4), GridCoord(2, 7), GridCoord(6, 7), GridCoord(4, 6)
            ),
            hint = "Filter Copper Ore around the center rock to Smelter + Press while burning Iron Ore."
        ),
        // 41
        LevelConfig(
            id = 41,
            name = "Gear Sorting",
            subtitle = "Pass Finished Gears, Craft From Plates",
            width = 9,
            height = 12,
            parBelts = 15,
            spawners = listOf(
                PlacedTile(GridCoord(1, 5), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.EAST, 1, secondaryItemType = ItemType.GEAR))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(7, 5), TileEntity.Hatch(ItemType.GEAR, 14))
            ),
            rocks = listOf(
                GridCoord(4, 3), GridCoord(4, 4), GridCoord(4, 6), GridCoord(4, 7)
            ),
            hint = "Filter pre-made Gears straight into the hatch while assembling Iron Plates into extra Gears."
        ),
        // 42
        LevelConfig(
            id = 42,
            name = "Dual Filter",
            subtitle = "Two Mixed Lines, Precision Logic",
            width = 10,
            height = 12,
            parBelts = 22,
            spawners = listOf(
                PlacedTile(GridCoord(2, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(7, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_WIRE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 10), TileEntity.Hatch(ItemType.IRON_PLATE, 10)),
                PlacedTile(GridCoord(7, 10), TileEntity.Hatch(ItemType.CIRCUIT, 14))
            ),
            rocks = listOf(
                GridCoord(4, 4), GridCoord(5, 4), GridCoord(4, 7), GridCoord(5, 7)
            ),
            hint = "Sort both mixed feeds: refine Iron on the left and route Plates + Wire into Circuits on the right."
        ),
        // 43
        LevelConfig(
            id = 43,
            name = "Waste Control",
            subtitle = "Filter Noise From Twin Streams",
            width = 10,
            height = 12,
            parBelts = 19,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(8, 2), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.WEST, 1, secondaryItemType = ItemType.IRON_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(5, 10), TileEntity.Hatch(ItemType.CIRCUIT, 16))
            ),
            rocks = listOf(
                GridCoord(4, 4), GridCoord(5, 4), GridCoord(3, 8), GridCoord(6, 8)
            ),
            hint = "Filter out unwanted ore to Trash on both flanks so only clean Plates and Wire reach the Assembler."
        ),
        // 44
        LevelConfig(
            id = 44,
            name = "Sorting Yard",
            subtitle = "Tier 4 Triple-Hatch Logistics",
            width = 10,
            height = 12,
            parBelts = 24,
            spawners = listOf(
                PlacedTile(GridCoord(5, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(8, 10), TileEntity.Hatch(ItemType.COPPER_WIRE, 10)),
                PlacedTile(GridCoord(5, 10), TileEntity.Hatch(ItemType.IRON_INGOT, 10)),
                PlacedTile(GridCoord(1, 10), TileEntity.Hatch(ItemType.GEAR, 8))
            ),
            rocks = listOf(
                GridCoord(3, 4), GridCoord(3, 5), GridCoord(6, 6), GridCoord(6, 7)
            ),
            hint = "Filter the center mixed stream into Iron Ingot and Copper Wire while building Gears on the left."
        ),
        // 45: Introduces Overflow
        LevelConfig(
            id = 45,
            name = "Spillway",
            subtitle = "Fill Priority Hatch, Then Overflow",
            width = 8,
            height = 10,
            parBelts = 9,
            spawners = listOf(
                PlacedTile(GridCoord(1, 4), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.EAST, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 2), TileEntity.Hatch(ItemType.IRON_PLATE, 10)),
                PlacedTile(GridCoord(6, 7), TileEntity.Hatch(ItemType.IRON_PLATE, 10))
            ),
            rocks = listOf(
                GridCoord(3, 6), GridCoord(4, 6), GridCoord(6, 3)
            ),
            hint = "Place an Overflow gate pointing to the top hatch first; once full, excess spills to the second.",
            newMechanicBadge = "Overflow"
        ),
        // 46
        LevelConfig(
            id = 46,
            name = "Priority Line",
            subtitle = "Overflow Excess Ingots Into Press",
            width = 9,
            height = 11,
            parBelts = 11,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(6, 2), TileEntity.Hatch(ItemType.IRON_INGOT, 8)),
                PlacedTile(GridCoord(6, 8), TileEntity.Hatch(ItemType.IRON_PLATE, 12))
            ),
            rocks = listOf(
                GridCoord(3, 5), GridCoord(4, 5), GridCoord(5, 5)
            ),
            hint = "Smelt Iron Ore and use Overflow to fill the Ingot hatch first, spilling the rest to a Press."
        ),
        // 47
        LevelConfig(
            id = 47,
            name = "Backpressure",
            subtitle = "Route Assembler Surplus to Quota",
            width = 9,
            height = 11,
            parBelts = 14,
            spawners = listOf(
                PlacedTile(GridCoord(3, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(3, 9), TileEntity.Hatch(ItemType.GEAR, 8)),
                PlacedTile(GridCoord(7, 5), TileEntity.Hatch(ItemType.IRON_PLATE, 12))
            ),
            rocks = listOf(
                GridCoord(1, 5), GridCoord(1, 6), GridCoord(5, 3)
            ),
            hint = "Feed a Gear line primarily and let an Overflow gate divert spare Iron Plates right."
        ),
        // 48: Introduces MOTOR
        LevelConfig(
            id = 48,
            name = "Electric Motor",
            subtitle = "Combine Gears & Circuits Into Motors",
            width = 9,
            height = 11,
            parBelts = 9,
            spawners = listOf(
                PlacedTile(GridCoord(1, 4), TileEntity.Spawner(ItemType.GEAR, Direction.EAST, 2)),
                PlacedTile(GridCoord(7, 4), TileEntity.Spawner(ItemType.CIRCUIT, Direction.WEST, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 9), TileEntity.Hatch(ItemType.MOTOR, 8))
            ),
            rocks = listOf(
                GridCoord(4, 2), GridCoord(2, 7), GridCoord(6, 7)
            ),
            hint = "Feed Gears and Circuits into opposite inputs of an Assembler to manufacture Electric Motors.",
            newMechanicBadge = "Motor"
        ),
        // 49
        LevelConfig(
            id = 49,
            name = "Motor Line",
            subtitle = "Craft Gears & Circuits Into Motors",
            width = 9,
            height = 12,
            parBelts = 19,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(7, 1), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.SOUTH, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 10), TileEntity.Hatch(ItemType.MOTOR, 8))
            ),
            rocks = listOf(
                GridCoord(4, 3), GridCoord(4, 4), GridCoord(1, 8), GridCoord(7, 8)
            ),
            hint = "Split Iron Plates to both a Gear Assembler and a Circuit Assembler, then combine them for Motors."
        ),
        // 50
        LevelConfig(
            id = 50,
            name = "Mini Hub",
            subtitle = "Full Ore-to-Motor Industrial Chain",
            width = 9,
            height = 12,
            parBelts = 21,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(7, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 10), TileEntity.Hatch(ItemType.MOTOR, 6))
            ),
            rocks = listOf(
                GridCoord(4, 4), GridCoord(4, 5), GridCoord(2, 9), GridCoord(6, 9)
            ),
            hint = "Refine Iron Ore & Copper Ore from scratch into Gears and Circuits, uniting at a Motor Assembler."
        ),
        // 51
        LevelConfig(
            id = 51,
            name = "Motor Cascade",
            subtitle = "Overflow Excess Parts to Quota",
            width = 9,
            height = 12,
            parBelts = 20,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.GEAR, Direction.EAST, 1)),
                PlacedTile(GridCoord(7, 2), TileEntity.Spawner(ItemType.CIRCUIT, Direction.WEST, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 10), TileEntity.Hatch(ItemType.MOTOR, 10)),
                PlacedTile(GridCoord(1, 10), TileEntity.Hatch(ItemType.GEAR, 10))
            ),
            rocks = listOf(
                GridCoord(4, 4), GridCoord(3, 7), GridCoord(5, 7)
            ),
            hint = "Send Gears to the Motor Assembler via Overflow so surplus Gears drain to the left hatch."
        ),
        // 52
        LevelConfig(
            id = 52,
            name = "Overflow Valve",
            subtitle = "Dual Quota With Mixed Overflow",
            width = 9,
            height = 12,
            parBelts = 16,
            spawners = listOf(
                PlacedTile(GridCoord(4, 1), TileEntity.Spawner(ItemType.CIRCUIT, Direction.SOUTH, 1, secondaryItemType = ItemType.GEAR))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 10), TileEntity.Hatch(ItemType.MOTOR, 8)),
                PlacedTile(GridCoord(1, 7), TileEntity.Hatch(ItemType.CIRCUIT, 8)),
                PlacedTile(GridCoord(7, 7), TileEntity.Hatch(ItemType.GEAR, 8))
            ),
            rocks = listOf(
                GridCoord(2, 4), GridCoord(6, 4), GridCoord(2, 9), GridCoord(6, 9)
            ),
            hint = "Filter Circuits and Gears into the Motor Assembler, using Overflow to spill extras to side hatches."
        ),
        // 53
        LevelConfig(
            id = 53,
            name = "Sorted Motors",
            subtitle = "Purge Raw Ore From Motor Stream",
            width = 10,
            height = 12,
            parBelts = 18,
            spawners = listOf(
                PlacedTile(GridCoord(1, 4), TileEntity.Spawner(ItemType.GEAR, Direction.EAST, 1, secondaryItemType = ItemType.IRON_ORE)),
                PlacedTile(GridCoord(8, 4), TileEntity.Spawner(ItemType.CIRCUIT, Direction.WEST, 1, secondaryItemType = ItemType.COPPER_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(5, 10), TileEntity.Hatch(ItemType.MOTOR, 10))
            ),
            rocks = listOf(
                GridCoord(4, 2), GridCoord(5, 2), GridCoord(3, 7), GridCoord(6, 7)
            ),
            hint = "Filter out raw ore into Trash on both sides while merging Gears and Circuits into Motors."
        ),
        // 54
        LevelConfig(
            id = 54,
            name = "Wire Flood",
            subtitle = "Balance Plate & Wire Into Two Bays",
            width = 10,
            height = 12,
            parBelts = 22,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(5, 10), TileEntity.Hatch(ItemType.MOTOR, 8)),
                PlacedTile(GridCoord(8, 10), TileEntity.Hatch(ItemType.CIRCUIT, 10))
            ),
            rocks = listOf(
                GridCoord(4, 4), GridCoord(5, 4), GridCoord(3, 8), GridCoord(6, 8)
            ),
            hint = "Build a Motor factory in the center and divert extra Circuits to the bottom-right hatch."
        ),
        // 55
        LevelConfig(
            id = 55,
            name = "Twin Core",
            subtitle = "Crossed Motor Feeds Through Pillars",
            width = 10,
            height = 12,
            parBelts = 21,
            spawners = listOf(
                PlacedTile(GridCoord(8, 2), TileEntity.Spawner(ItemType.GEAR, Direction.WEST, 2)),
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.CIRCUIT, Direction.EAST, 2)),
                PlacedTile(GridCoord(5, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(5, 10), TileEntity.Hatch(ItemType.MOTOR, 10)),
                PlacedTile(GridCoord(1, 10), TileEntity.Hatch(ItemType.IRON_INGOT, 10))
            ),
            rocks = listOf(
                GridCoord(4, 4), GridCoord(6, 4), GridCoord(3, 7), GridCoord(7, 7)
            ),
            hint = "Use a Bridge to cross the Iron Ore line cleanly while assembling Motors below."
        )
    )
}
