package com.microfactory.sim

internal object LevelPackExpert1 {
    val levels: List<LevelConfig> = listOf(
        // 81
        LevelConfig(
            id = 81,
            name = "Blind Switchyard",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 22,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(8, 2), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.WEST, 1, secondaryItemType = ItemType.IRON_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 10), TileEntity.Hatch(ItemType.MOTOR, 10)),
                PlacedTile(GridCoord(7, 10), TileEntity.Hatch(ItemType.CIRCUIT, 10))
            ),
            rocks = listOf(
                GridCoord(4, 4), GridCoord(5, 4), GridCoord(2, 7), GridCoord(7, 7), GridCoord(4, 9), GridCoord(5, 9)
            ),
            hint = "",
            isExpert = true
        ),
        // 82
        LevelConfig(
            id = 82,
            name = "Choke Manifold",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 24,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_WIRE)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(5, 10), TileEntity.Hatch(ItemType.MOTOR, 12)),
                PlacedTile(GridCoord(8, 10), TileEntity.Hatch(ItemType.GEAR, 12))
            ),
            rocks = listOf(
                GridCoord(0, 5), GridCoord(1, 5), GridCoord(3, 5), GridCoord(4, 5),
                GridCoord(6, 5), GridCoord(7, 5), GridCoord(9, 5)
            ),
            hint = "",
            isExpert = true
        ),
        // 83
        LevelConfig(
            id = 83,
            name = "Counterflow",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 25,
            spawners = listOf(
                PlacedTile(GridCoord(1, 10), TileEntity.Spawner(ItemType.IRON_ORE, Direction.NORTH, 1)),
                PlacedTile(GridCoord(8, 10), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.NORTH, 1)),
                PlacedTile(GridCoord(5, 10), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.NORTH, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 1), TileEntity.Hatch(ItemType.CIRCUIT, 12)),
                PlacedTile(GridCoord(7, 1), TileEntity.Hatch(ItemType.MOTOR, 10))
            ),
            rocks = listOf(
                GridCoord(3, 4), GridCoord(6, 4), GridCoord(4, 7), GridCoord(5, 7)
            ),
            hint = "",
            isExpert = true
        ),
        // 84
        LevelConfig(
            id = 84,
            name = "Split Core",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 22,
            spawners = listOf(
                PlacedTile(GridCoord(4, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(5, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 10), TileEntity.Hatch(ItemType.GEAR, 12)),
                PlacedTile(GridCoord(5, 10), TileEntity.Hatch(ItemType.MOTOR, 10)),
                PlacedTile(GridCoord(8, 10), TileEntity.Hatch(ItemType.COPPER_WIRE, 14))
            ),
            rocks = listOf(
                GridCoord(2, 4), GridCoord(7, 4), GridCoord(3, 8), GridCoord(6, 8)
            ),
            hint = "",
            isExpert = true
        ),
        // 85
        LevelConfig(
            id = 85,
            name = "Slag Gate",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 24,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.GEAR, Direction.EAST, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(8, 2), TileEntity.Spawner(ItemType.CIRCUIT, Direction.WEST, 2, secondaryItemType = ItemType.IRON_ORE)),
                PlacedTile(GridCoord(5, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(5, 10), TileEntity.Hatch(ItemType.MOTOR, 14))
            ),
            rocks = listOf(
                GridCoord(2, 5), GridCoord(7, 5), GridCoord(4, 7), GridCoord(5, 7), GridCoord(2, 9), GridCoord(7, 9)
            ),
            hint = "",
            isExpert = true
        ),
        // 86
        LevelConfig(
            id = 86,
            name = "Deadlock Ridge",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 24,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1)),
                PlacedTile(GridCoord(1, 10), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.EAST, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(8, 2), TileEntity.Hatch(ItemType.MOTOR, 10)),
                PlacedTile(GridCoord(8, 9), TileEntity.Hatch(ItemType.CIRCUIT, 12))
            ),
            rocks = listOf(
                GridCoord(4, 2), GridCoord(4, 3), GridCoord(4, 8), GridCoord(4, 9), GridCoord(6, 5), GridCoord(6, 6)
            ),
            hint = "",
            isExpert = true
        ),
        // 87
        LevelConfig(
            id = 87,
            name = "Twin Helix",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 26,
            spawners = listOf(
                PlacedTile(GridCoord(2, 1), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.SOUTH, 1, secondaryItemType = ItemType.IRON_PLATE)),
                PlacedTile(GridCoord(7, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_WIRE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 10), TileEntity.Hatch(ItemType.MOTOR, 12)),
                PlacedTile(GridCoord(8, 10), TileEntity.Hatch(ItemType.MOTOR, 12))
            ),
            rocks = listOf(
                GridCoord(4, 3), GridCoord(5, 3), GridCoord(3, 6), GridCoord(6, 6), GridCoord(4, 9), GridCoord(5, 9)
            ),
            hint = "",
            isExpert = true
        ),
        // 88
        LevelConfig(
            id = 88,
            name = "Narrow Gauge",
            subtitle = "Expert Protocol",
            width = 9,
            height = 12,
            parBelts = 22,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(7, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 10), TileEntity.Hatch(ItemType.MOTOR, 12))
            ),
            rocks = listOf(
                GridCoord(0, 4), GridCoord(1, 4), GridCoord(2, 4),
                GridCoord(6, 4), GridCoord(7, 4), GridCoord(8, 4),
                GridCoord(3, 7), GridCoord(5, 7)
            ),
            hint = "",
            isExpert = true
        ),
        // 89
        LevelConfig(
            id = 89,
            name = "Four-Way Bind",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 26,
            spawners = listOf(
                PlacedTile(GridCoord(4, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.WEST, 1)),
                PlacedTile(GridCoord(5, 1), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.EAST, 1)),
                PlacedTile(GridCoord(4, 10), TileEntity.Spawner(ItemType.IRON_ORE, Direction.WEST, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(5, 6), TileEntity.Hatch(ItemType.MOTOR, 12)),
                PlacedTile(GridCoord(8, 10), TileEntity.Hatch(ItemType.GEAR, 12))
            ),
            rocks = listOf(
                GridCoord(2, 3), GridCoord(7, 3), GridCoord(2, 8), GridCoord(7, 8)
            ),
            hint = "",
            isExpert = true
        ),
        // 90
        LevelConfig(
            id = 90,
            name = "Iron Cascade",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 24,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(3, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(8, 2), TileEntity.Hatch(ItemType.IRON_PLATE, 16)),
                PlacedTile(GridCoord(8, 6), TileEntity.Hatch(ItemType.GEAR, 12)),
                PlacedTile(GridCoord(8, 10), TileEntity.Hatch(ItemType.MOTOR, 10))
            ),
            rocks = listOf(
                GridCoord(5, 2), GridCoord(5, 4), GridCoord(5, 7), GridCoord(5, 9)
            ),
            hint = "",
            isExpert = true
        ),
        // 91
        LevelConfig(
            id = 91,
            name = "Mirror Lock",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 26,
            spawners = listOf(
                PlacedTile(GridCoord(1, 5), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.NORTH, 1, secondaryItemType = ItemType.COPPER_INGOT)),
                PlacedTile(GridCoord(8, 5), TileEntity.Spawner(ItemType.COPPER_INGOT, Direction.SOUTH, 1, secondaryItemType = ItemType.IRON_PLATE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 1), TileEntity.Hatch(ItemType.CIRCUIT, 14)),
                PlacedTile(GridCoord(5, 10), TileEntity.Hatch(ItemType.MOTOR, 12))
            ),
            rocks = listOf(
                GridCoord(3, 4), GridCoord(6, 4), GridCoord(3, 7), GridCoord(6, 7)
            ),
            hint = "",
            isExpert = true
        ),
        // 92
        LevelConfig(
            id = 92,
            name = "Sieve Grid",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 25,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1, secondaryItemType = ItemType.GEAR)),
                PlacedTile(GridCoord(8, 2), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.WEST, 1, secondaryItemType = ItemType.COPPER_WIRE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 10), TileEntity.Hatch(ItemType.MOTOR, 14)),
                PlacedTile(GridCoord(7, 10), TileEntity.Hatch(ItemType.CIRCUIT, 12))
            ),
            rocks = listOf(
                GridCoord(4, 3), GridCoord(5, 3), GridCoord(1, 6), GridCoord(8, 6), GridCoord(4, 8), GridCoord(5, 8)
            ),
            hint = "",
            isExpert = true
        ),
        // 93
        LevelConfig(
            id = 93,
            name = "Valve Corridor",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 24,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(5, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 10), TileEntity.Hatch(ItemType.GEAR, 14)),
                PlacedTile(GridCoord(7, 10), TileEntity.Hatch(ItemType.MOTOR, 14))
            ),
            rocks = listOf(
                GridCoord(0, 6), GridCoord(1, 6), GridCoord(3, 6), GridCoord(4, 6),
                GridCoord(6, 6), GridCoord(7, 6), GridCoord(9, 6)
            ),
            hint = "",
            isExpert = true
        ),
        // 94
        LevelConfig(
            id = 94,
            name = "Bifurcate",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 25,
            spawners = listOf(
                PlacedTile(GridCoord(5, 5), TileEntity.Spawner(ItemType.IRON_ORE, Direction.NORTH, 1)),
                PlacedTile(GridCoord(4, 6), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Hatch(ItemType.GEAR, 12)),
                PlacedTile(GridCoord(8, 1), TileEntity.Hatch(ItemType.CIRCUIT, 12)),
                PlacedTile(GridCoord(5, 10), TileEntity.Hatch(ItemType.MOTOR, 12))
            ),
            rocks = listOf(
                GridCoord(2, 3), GridCoord(7, 3), GridCoord(2, 8), GridCoord(7, 8)
            ),
            hint = "",
            isExpert = true
        ),
        // 95
        LevelConfig(
            id = 95,
            name = "Apex Exchange",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 26,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.EAST, 1, secondaryItemType = ItemType.GEAR)),
                PlacedTile(GridCoord(8, 10), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.WEST, 1, secondaryItemType = ItemType.COPPER_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(8, 1), TileEntity.Hatch(ItemType.MOTOR, 14)),
                PlacedTile(GridCoord(1, 10), TileEntity.Hatch(ItemType.CIRCUIT, 12))
            ),
            rocks = listOf(
                GridCoord(4, 4), GridCoord(5, 4), GridCoord(4, 7), GridCoord(5, 7), GridCoord(2, 5), GridCoord(7, 6)
            ),
            hint = "",
            isExpert = true
        ),
        // 96
        LevelConfig(
            id = 96,
            name = "Crossbar Vault",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 26,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(1, 10), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.NORTH, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(5, 5), TileEntity.Hatch(ItemType.MOTOR, 14)),
                PlacedTile(GridCoord(8, 10), TileEntity.Hatch(ItemType.GEAR, 14))
            ),
            rocks = listOf(
                GridCoord(3, 3), GridCoord(6, 3), GridCoord(3, 7), GridCoord(6, 7), GridCoord(5, 2), GridCoord(5, 9)
            ),
            hint = "",
            isExpert = true
        ),
        // 97
        LevelConfig(
            id = 97,
            name = "Dual Sieve",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 26,
            spawners = listOf(
                PlacedTile(GridCoord(2, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(7, 1), TileEntity.Spawner(ItemType.COPPER_INGOT, Direction.SOUTH, 1, secondaryItemType = ItemType.IRON_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 10), TileEntity.Hatch(ItemType.MOTOR, 14)),
                PlacedTile(GridCoord(7, 10), TileEntity.Hatch(ItemType.CIRCUIT, 14))
            ),
            rocks = listOf(
                GridCoord(4, 2), GridCoord(5, 2), GridCoord(3, 5), GridCoord(6, 5), GridCoord(4, 8), GridCoord(5, 8)
            ),
            hint = "",
            isExpert = true
        ),
        // 98
        LevelConfig(
            id = 98,
            name = "Pinch Point",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 24,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1)),
                PlacedTile(GridCoord(1, 9), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.EAST, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(8, 2), TileEntity.Hatch(ItemType.GEAR, 16)),
                PlacedTile(GridCoord(8, 6), TileEntity.Hatch(ItemType.MOTOR, 12)),
                PlacedTile(GridCoord(8, 9), TileEntity.Hatch(ItemType.COPPER_WIRE, 16))
            ),
            rocks = listOf(
                GridCoord(4, 0), GridCoord(4, 1), GridCoord(4, 3), GridCoord(4, 4),
                GridCoord(4, 7), GridCoord(4, 8), GridCoord(4, 10), GridCoord(4, 11)
            ),
            hint = "",
            isExpert = true
        ),
        // 99
        LevelConfig(
            id = 99,
            name = "Spiral Press",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 26,
            spawners = listOf(
                PlacedTile(GridCoord(4, 5), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.EAST, 1, secondaryItemType = ItemType.COPPER_WIRE)),
                PlacedTile(GridCoord(5, 6), TileEntity.Spawner(ItemType.IRON_ORE, Direction.WEST, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Hatch(ItemType.MOTOR, 14)),
                PlacedTile(GridCoord(8, 10), TileEntity.Hatch(ItemType.CIRCUIT, 14))
            ),
            rocks = listOf(
                GridCoord(2, 2), GridCoord(7, 2), GridCoord(2, 9), GridCoord(7, 9), GridCoord(4, 3), GridCoord(5, 8)
            ),
            hint = "",
            isExpert = true
        ),
        // 100
        LevelConfig(
            id = 100,
            name = "Centurion Gate",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 28,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(5, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 10), TileEntity.Hatch(ItemType.GEAR, 16)),
                PlacedTile(GridCoord(5, 10), TileEntity.Hatch(ItemType.MOTOR, 16)),
                PlacedTile(GridCoord(8, 10), TileEntity.Hatch(ItemType.CIRCUIT, 14))
            ),
            rocks = listOf(
                GridCoord(3, 4), GridCoord(6, 4), GridCoord(2, 7), GridCoord(7, 7), GridCoord(4, 8), GridCoord(5, 8)
            ),
            hint = "",
            isExpert = true
        ),
        // 101
        LevelConfig(
            id = 101,
            name = "Backlash",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 25,
            spawners = listOf(
                PlacedTile(GridCoord(1, 10), TileEntity.Spawner(ItemType.IRON_ORE, Direction.NORTH, 1, secondaryItemType = ItemType.IRON_PLATE)),
                PlacedTile(GridCoord(8, 10), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.NORTH, 1, secondaryItemType = ItemType.COPPER_WIRE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 1), TileEntity.Hatch(ItemType.MOTOR, 14)),
                PlacedTile(GridCoord(7, 1), TileEntity.Hatch(ItemType.GEAR, 14))
            ),
            rocks = listOf(
                GridCoord(4, 3), GridCoord(5, 3), GridCoord(2, 6), GridCoord(7, 6), GridCoord(4, 8), GridCoord(5, 8)
            ),
            hint = "",
            isExpert = true
        ),
        // 102
        LevelConfig(
            id = 102,
            name = "Alloy Loom",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 26,
            spawners = listOf(
                PlacedTile(GridCoord(1, 3), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.EAST, 1)),
                PlacedTile(GridCoord(1, 8), TileEntity.Spawner(ItemType.COPPER_INGOT, Direction.EAST, 1)),
                PlacedTile(GridCoord(8, 5), TileEntity.Spawner(ItemType.IRON_ORE, Direction.WEST, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(5, 1), TileEntity.Hatch(ItemType.CIRCUIT, 14)),
                PlacedTile(GridCoord(5, 10), TileEntity.Hatch(ItemType.MOTOR, 14))
            ),
            rocks = listOf(
                GridCoord(3, 4), GridCoord(3, 7), GridCoord(6, 3), GridCoord(6, 8)
            ),
            hint = "",
            isExpert = true
        ),
        // 103
        LevelConfig(
            id = 103,
            name = "Shear Line",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 25,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.GEAR, Direction.SOUTH, 2, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.CIRCUIT, Direction.SOUTH, 2, secondaryItemType = ItemType.IRON_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 10), TileEntity.Hatch(ItemType.MOTOR, 15)),
                PlacedTile(GridCoord(7, 10), TileEntity.Hatch(ItemType.IRON_PLATE, 15))
            ),
            rocks = listOf(
                GridCoord(4, 4), GridCoord(5, 4), GridCoord(3, 7), GridCoord(6, 7), GridCoord(1, 5), GridCoord(8, 5)
            ),
            hint = "",
            isExpert = true
        ),
        // 104
        LevelConfig(
            id = 104,
            name = "Dead Reckoning",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 26,
            spawners = listOf(
                PlacedTile(GridCoord(2, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.WEST, 1)),
                PlacedTile(GridCoord(7, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.EAST, 1)),
                PlacedTile(GridCoord(5, 10), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.NORTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 10), TileEntity.Hatch(ItemType.MOTOR, 14)),
                PlacedTile(GridCoord(7, 10), TileEntity.Hatch(ItemType.CIRCUIT, 14))
            ),
            rocks = listOf(
                GridCoord(4, 2), GridCoord(5, 2), GridCoord(3, 5), GridCoord(6, 5), GridCoord(4, 7), GridCoord(5, 7)
            ),
            hint = "",
            isExpert = true
        ),
        // 105
        LevelConfig(
            id = 105,
            name = "Copper Crucible",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 26,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.EAST, 1, secondaryItemType = ItemType.IRON_ORE)),
                PlacedTile(GridCoord(8, 9), TileEntity.Spawner(ItemType.IRON_ORE, Direction.WEST, 1, secondaryItemType = ItemType.COPPER_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(8, 2), TileEntity.Hatch(ItemType.COPPER_WIRE, 18)),
                PlacedTile(GridCoord(1, 9), TileEntity.Hatch(ItemType.GEAR, 16)),
                PlacedTile(GridCoord(5, 6), TileEntity.Hatch(ItemType.MOTOR, 12))
            ),
            rocks = listOf(
                GridCoord(3, 4), GridCoord(6, 4), GridCoord(3, 7), GridCoord(6, 7)
            ),
            hint = "",
            isExpert = true
        ),
        // 106
        LevelConfig(
            id = 106,
            name = "Bridgehead",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 26,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(4, 1), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(8, 10), TileEntity.Hatch(ItemType.CIRCUIT, 14)),
                PlacedTile(GridCoord(1, 10), TileEntity.Hatch(ItemType.MOTOR, 14))
            ),
            rocks = listOf(
                GridCoord(2, 4), GridCoord(2, 5), GridCoord(5, 5), GridCoord(5, 6), GridCoord(7, 7), GridCoord(7, 8)
            ),
            hint = "",
            isExpert = true
        ),
        // 107
        LevelConfig(
            id = 107,
            name = "Torque Matrix",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 27,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1, secondaryItemType = ItemType.COPPER_WIRE)),
                PlacedTile(GridCoord(8, 10), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.WEST, 1, secondaryItemType = ItemType.IRON_PLATE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 10), TileEntity.Hatch(ItemType.MOTOR, 15)),
                PlacedTile(GridCoord(7, 1), TileEntity.Hatch(ItemType.GEAR, 15))
            ),
            rocks = listOf(
                GridCoord(4, 3), GridCoord(5, 3), GridCoord(2, 6), GridCoord(7, 5), GridCoord(4, 8), GridCoord(5, 8)
            ),
            hint = "",
            isExpert = true
        ),
        // 108
        LevelConfig(
            id = 108,
            name = "Overpressure",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 26,
            spawners = listOf(
                PlacedTile(GridCoord(1, 5), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1)),
                PlacedTile(GridCoord(8, 5), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.WEST, 1)),
                PlacedTile(GridCoord(5, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 10), TileEntity.Hatch(ItemType.GEAR, 16)),
                PlacedTile(GridCoord(5, 10), TileEntity.Hatch(ItemType.MOTOR, 15)),
                PlacedTile(GridCoord(8, 10), TileEntity.Hatch(ItemType.CIRCUIT, 14))
            ),
            rocks = listOf(
                GridCoord(3, 3), GridCoord(6, 3), GridCoord(3, 7), GridCoord(6, 7)
            ),
            hint = "",
            isExpert = true
        ),
        // 109
        LevelConfig(
            id = 109,
            name = "Switchback",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 26,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.SOUTH, 1, secondaryItemType = ItemType.IRON_ORE)),
                PlacedTile(GridCoord(8, 2), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 10), TileEntity.Hatch(ItemType.MOTOR, 16)),
                PlacedTile(GridCoord(8, 10), TileEntity.Hatch(ItemType.COPPER_WIRE, 16))
            ),
            rocks = listOf(
                GridCoord(2, 4), GridCoord(3, 4), GridCoord(6, 4), GridCoord(7, 4),
                GridCoord(4, 7), GridCoord(5, 7)
            ),
            hint = "",
            isExpert = true
        ),
        // 110
        LevelConfig(
            id = 110,
            name = "Gridlock Zero",
            subtitle = "Expert Protocol",
            width = 10,
            height = 12,
            parBelts = 28,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(1, 10), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.NORTH, 1)),
                PlacedTile(GridCoord(8, 10), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.NORTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 5), TileEntity.Hatch(ItemType.MOTOR, 16)),
                PlacedTile(GridCoord(5, 6), TileEntity.Hatch(ItemType.CIRCUIT, 16))
            ),
            rocks = listOf(
                GridCoord(3, 3), GridCoord(6, 3), GridCoord(3, 8), GridCoord(6, 8)
            ),
            hint = "",
            isExpert = true
        )
    )
}
