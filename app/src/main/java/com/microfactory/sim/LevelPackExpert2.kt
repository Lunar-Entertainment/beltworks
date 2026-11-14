package com.microfactory.sim

internal object LevelPackExpert2 {
    val levels: List<LevelConfig> = listOf(
        // 111
        LevelConfig(
            id = 111,
            name = "Obsidian Maze",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 28,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(8, 11), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.WEST, 1, secondaryItemType = ItemType.IRON_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(8, 1), TileEntity.Hatch(ItemType.MOTOR, 16)),
                PlacedTile(GridCoord(1, 11), TileEntity.Hatch(ItemType.CIRCUIT, 16))
            ),
            rocks = listOf(
                GridCoord(4, 2), GridCoord(4, 3), GridCoord(5, 9), GridCoord(5, 10),
                GridCoord(2, 6), GridCoord(3, 6), GridCoord(6, 6), GridCoord(7, 6)
            ),
            hint = "",
            isExpert = true
        ),
        // 112
        LevelConfig(
            id = 112,
            name = "Quadra Feed",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 30,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(4, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(6, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 11), TileEntity.Hatch(ItemType.GEAR, 18)),
                PlacedTile(GridCoord(5, 11), TileEntity.Hatch(ItemType.MOTOR, 18)),
                PlacedTile(GridCoord(8, 11), TileEntity.Hatch(ItemType.CIRCUIT, 18))
            ),
            rocks = listOf(
                GridCoord(2, 4), GridCoord(5, 4), GridCoord(7, 4),
                GridCoord(3, 8), GridCoord(6, 8)
            ),
            hint = "",
            isExpert = true
        ),
        // 113
        LevelConfig(
            id = 113,
            name = "Rift Crossing",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 28,
            spawners = listOf(
                PlacedTile(GridCoord(1, 3), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.EAST, 1, secondaryItemType = ItemType.GEAR)),
                PlacedTile(GridCoord(8, 3), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.WEST, 1, secondaryItemType = ItemType.COPPER_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 11), TileEntity.Hatch(ItemType.MOTOR, 16)),
                PlacedTile(GridCoord(7, 11), TileEntity.Hatch(ItemType.CIRCUIT, 16))
            ),
            rocks = listOf(
                GridCoord(0, 6), GridCoord(1, 6), GridCoord(2, 6),
                GridCoord(7, 6), GridCoord(8, 6), GridCoord(9, 6),
                GridCoord(4, 9), GridCoord(5, 9)
            ),
            hint = "",
            isExpert = true
        ),
        // 114
        LevelConfig(
            id = 114,
            name = "Turbine Shaft",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 28,
            spawners = listOf(
                PlacedTile(GridCoord(1, 11), TileEntity.Spawner(ItemType.IRON_ORE, Direction.NORTH, 1)),
                PlacedTile(GridCoord(8, 11), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.NORTH, 1)),
                PlacedTile(GridCoord(5, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Hatch(ItemType.MOTOR, 16)),
                PlacedTile(GridCoord(8, 1), TileEntity.Hatch(ItemType.GEAR, 16)),
                PlacedTile(GridCoord(5, 11), TileEntity.Hatch(ItemType.CIRCUIT, 16))
            ),
            rocks = listOf(
                GridCoord(3, 4), GridCoord(6, 4), GridCoord(3, 8), GridCoord(6, 8)
            ),
            hint = "",
            isExpert = true
        ),
        // 115
        LevelConfig(
            id = 115,
            name = "Flux Bottleneck",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 27,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(8, 2), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 11), TileEntity.Hatch(ItemType.MOTOR, 16)),
                PlacedTile(GridCoord(5, 11), TileEntity.Hatch(ItemType.GEAR, 16)),
                PlacedTile(GridCoord(8, 11), TileEntity.Hatch(ItemType.COPPER_WIRE, 18))
            ),
            rocks = listOf(
                GridCoord(0, 5), GridCoord(1, 5), GridCoord(2, 5), GridCoord(3, 5),
                GridCoord(6, 5), GridCoord(7, 5), GridCoord(8, 5), GridCoord(9, 5),
                GridCoord(4, 8), GridCoord(5, 8)
            ),
            hint = "",
            isExpert = true
        ),
        // 116
        LevelConfig(
            id = 116,
            name = "Inverted Forge",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 28,
            spawners = listOf(
                PlacedTile(GridCoord(4, 11), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.NORTH, 1, secondaryItemType = ItemType.COPPER_WIRE)),
                PlacedTile(GridCoord(5, 11), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.NORTH, 1, secondaryItemType = ItemType.IRON_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Hatch(ItemType.MOTOR, 16)),
                PlacedTile(GridCoord(8, 1), TileEntity.Hatch(ItemType.CIRCUIT, 16))
            ),
            rocks = listOf(
                GridCoord(2, 3), GridCoord(7, 3), GridCoord(4, 6), GridCoord(5, 6), GridCoord(2, 9), GridCoord(7, 9)
            ),
            hint = "",
            isExpert = true
        ),
        // 117
        LevelConfig(
            id = 117,
            name = "Triple Filter",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 30,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.GEAR, Direction.EAST, 2, secondaryItemType = ItemType.COPPER_WIRE)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.CIRCUIT, Direction.WEST, 2, secondaryItemType = ItemType.IRON_PLATE)),
                PlacedTile(GridCoord(5, 11), TileEntity.Spawner(ItemType.IRON_ORE, Direction.NORTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 11), TileEntity.Hatch(ItemType.MOTOR, 18)),
                PlacedTile(GridCoord(8, 11), TileEntity.Hatch(ItemType.GEAR, 16))
            ),
            rocks = listOf(
                GridCoord(3, 4), GridCoord(6, 4), GridCoord(2, 7), GridCoord(7, 7), GridCoord(4, 9), GridCoord(5, 9)
            ),
            hint = "",
            isExpert = true
        ),
        // 118
        LevelConfig(
            id = 118,
            name = "Double Helix",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 28,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 11), TileEntity.Hatch(ItemType.CIRCUIT, 16)),
                PlacedTile(GridCoord(8, 11), TileEntity.Hatch(ItemType.MOTOR, 16))
            ),
            rocks = listOf(
                GridCoord(4, 2), GridCoord(5, 2),
                GridCoord(2, 5), GridCoord(3, 5), GridCoord(6, 5), GridCoord(7, 5),
                GridCoord(4, 8), GridCoord(5, 8)
            ),
            hint = "",
            isExpert = true
        ),
        // 119
        LevelConfig(
            id = 119,
            name = "Slag Reactor",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 29,
            spawners = listOf(
                PlacedTile(GridCoord(1, 6), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(8, 6), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.WEST, 1, secondaryItemType = ItemType.COPPER_WIRE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 1), TileEntity.Hatch(ItemType.MOTOR, 16)),
                PlacedTile(GridCoord(7, 1), TileEntity.Hatch(ItemType.GEAR, 16)),
                PlacedTile(GridCoord(5, 11), TileEntity.Hatch(ItemType.CIRCUIT, 16))
            ),
            rocks = listOf(
                GridCoord(4, 3), GridCoord(5, 3), GridCoord(4, 9), GridCoord(5, 9)
            ),
            hint = "",
            isExpert = true
        ),
        // 120
        LevelConfig(
            id = 120,
            name = "Zero Margin",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 30,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.WEST, 1)),
                PlacedTile(GridCoord(1, 11), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.EAST, 2)),
                PlacedTile(GridCoord(8, 11), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.WEST, 2))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 6), TileEntity.Hatch(ItemType.MOTOR, 18)),
                PlacedTile(GridCoord(5, 6), TileEntity.Hatch(ItemType.CIRCUIT, 18))
            ),
            rocks = listOf(
                GridCoord(2, 3), GridCoord(7, 3), GridCoord(2, 9), GridCoord(7, 9),
                GridCoord(4, 4), GridCoord(5, 4), GridCoord(4, 8), GridCoord(5, 8)
            ),
            hint = "",
            isExpert = true
        ),
        // 121
        LevelConfig(
            id = 121,
            name = "Crossfire Apex",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 28,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_INGOT)),
                PlacedTile(GridCoord(8, 10), TileEntity.Spawner(ItemType.COPPER_INGOT, Direction.NORTH, 1, secondaryItemType = ItemType.IRON_PLATE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(8, 2), TileEntity.Hatch(ItemType.MOTOR, 16)),
                PlacedTile(GridCoord(1, 10), TileEntity.Hatch(ItemType.GEAR, 16)),
                PlacedTile(GridCoord(5, 6), TileEntity.Hatch(ItemType.CIRCUIT, 16))
            ),
            rocks = listOf(
                GridCoord(3, 4), GridCoord(6, 4), GridCoord(3, 8), GridCoord(6, 8)
            ),
            hint = "",
            isExpert = true
        ),
        // 122
        LevelConfig(
            id = 122,
            name = "Spiral Nexus",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 29,
            spawners = listOf(
                PlacedTile(GridCoord(4, 6), TileEntity.Spawner(ItemType.IRON_ORE, Direction.NORTH, 1)),
                PlacedTile(GridCoord(5, 6), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Hatch(ItemType.MOTOR, 16)),
                PlacedTile(GridCoord(8, 1), TileEntity.Hatch(ItemType.CIRCUIT, 16)),
                PlacedTile(GridCoord(1, 11), TileEntity.Hatch(ItemType.GEAR, 16)),
                PlacedTile(GridCoord(8, 11), TileEntity.Hatch(ItemType.COPPER_WIRE, 18))
            ),
            rocks = listOf(
                GridCoord(2, 3), GridCoord(7, 3), GridCoord(2, 9), GridCoord(7, 9)
            ),
            hint = "",
            isExpert = true
        ),
        // 123
        LevelConfig(
            id = 123,
            name = "Vortex Press",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 30,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1, secondaryItemType = ItemType.GEAR)),
                PlacedTile(GridCoord(5, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_WIRE)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 11), TileEntity.Hatch(ItemType.MOTOR, 18)),
                PlacedTile(GridCoord(7, 11), TileEntity.Hatch(ItemType.CIRCUIT, 18))
            ),
            rocks = listOf(
                GridCoord(3, 4), GridCoord(6, 4), GridCoord(1, 7), GridCoord(8, 7), GridCoord(4, 9), GridCoord(5, 9)
            ),
            hint = "",
            isExpert = true
        ),
        // 124
        LevelConfig(
            id = 124,
            name = "Iron Bastion",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 28,
            spawners = listOf(
                PlacedTile(GridCoord(1, 11), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1)),
                PlacedTile(GridCoord(8, 11), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.WEST, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 1), TileEntity.Hatch(ItemType.MOTOR, 18)),
                PlacedTile(GridCoord(7, 1), TileEntity.Hatch(ItemType.GEAR, 18))
            ),
            rocks = listOf(
                GridCoord(0, 4), GridCoord(1, 4), GridCoord(3, 4), GridCoord(6, 4), GridCoord(8, 4), GridCoord(9, 4),
                GridCoord(4, 7), GridCoord(5, 7)
            ),
            hint = "",
            isExpert = true
        ),
        // 125
        LevelConfig(
            id = 125,
            name = "Deadbolt",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 30,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.EAST, 1, secondaryItemType = ItemType.IRON_ORE)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.WEST, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(5, 11), TileEntity.Spawner(ItemType.IRON_ORE, Direction.NORTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 11), TileEntity.Hatch(ItemType.MOTOR, 18)),
                PlacedTile(GridCoord(8, 11), TileEntity.Hatch(ItemType.CIRCUIT, 18))
            ),
            rocks = listOf(
                GridCoord(3, 3), GridCoord(6, 3), GridCoord(4, 6), GridCoord(5, 6), GridCoord(3, 9), GridCoord(6, 9)
            ),
            hint = "",
            isExpert = true
        ),
        // 126
        LevelConfig(
            id = 126,
            name = "Apex Conduit",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 30,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1)),
                PlacedTile(GridCoord(1, 10), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.EAST, 1)),
                PlacedTile(GridCoord(8, 6), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.WEST, 1, secondaryItemType = ItemType.COPPER_WIRE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(8, 2), TileEntity.Hatch(ItemType.MOTOR, 18)),
                PlacedTile(GridCoord(8, 10), TileEntity.Hatch(ItemType.CIRCUIT, 18)),
                PlacedTile(GridCoord(5, 1), TileEntity.Hatch(ItemType.GEAR, 16))
            ),
            rocks = listOf(
                GridCoord(4, 3), GridCoord(4, 4), GridCoord(4, 8), GridCoord(4, 9)
            ),
            hint = "",
            isExpert = true
        ),
        // 127
        LevelConfig(
            id = 127,
            name = "Fracture Grid",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 30,
            spawners = listOf(
                PlacedTile(GridCoord(2, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(7, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1, secondaryItemType = ItemType.IRON_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 11), TileEntity.Hatch(ItemType.GEAR, 18)),
                PlacedTile(GridCoord(4, 11), TileEntity.Hatch(ItemType.MOTOR, 18)),
                PlacedTile(GridCoord(8, 11), TileEntity.Hatch(ItemType.COPPER_WIRE, 18))
            ),
            rocks = listOf(
                GridCoord(4, 3), GridCoord(5, 3), GridCoord(2, 6), GridCoord(7, 6), GridCoord(3, 9), GridCoord(6, 9)
            ),
            hint = "",
            isExpert = true
        ),
        // 128
        LevelConfig(
            id = 128,
            name = "Counterweight",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 30,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.GEAR, Direction.SOUTH, 2, secondaryItemType = ItemType.IRON_ORE)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(5, 11), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.NORTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 11), TileEntity.Hatch(ItemType.MOTOR, 20)),
                PlacedTile(GridCoord(8, 11), TileEntity.Hatch(ItemType.CIRCUIT, 18))
            ),
            rocks = listOf(
                GridCoord(3, 4), GridCoord(6, 4), GridCoord(3, 8), GridCoord(6, 8), GridCoord(4, 6), GridCoord(5, 6)
            ),
            hint = "",
            isExpert = true
        ),
        // 129
        LevelConfig(
            id = 129,
            name = "Siphon Array",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 30,
            spawners = listOf(
                PlacedTile(GridCoord(1, 6), TileEntity.Spawner(ItemType.IRON_ORE, Direction.NORTH, 1)),
                PlacedTile(GridCoord(8, 6), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(4, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.EAST, 1)),
                PlacedTile(GridCoord(5, 11), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.WEST, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(8, 1), TileEntity.Hatch(ItemType.MOTOR, 18)),
                PlacedTile(GridCoord(1, 11), TileEntity.Hatch(ItemType.CIRCUIT, 18))
            ),
            rocks = listOf(
                GridCoord(3, 3), GridCoord(6, 3), GridCoord(3, 9), GridCoord(6, 9)
            ),
            hint = "",
            isExpert = true
        ),
        // 130
        LevelConfig(
            id = 130,
            name = "Razor Lattice",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 31,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.EAST, 1, secondaryItemType = ItemType.COPPER_WIRE)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.WEST, 1, secondaryItemType = ItemType.IRON_PLATE)),
                PlacedTile(GridCoord(1, 11), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1)),
                PlacedTile(GridCoord(8, 11), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.WEST, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 6), TileEntity.Hatch(ItemType.MOTOR, 20)),
                PlacedTile(GridCoord(7, 6), TileEntity.Hatch(ItemType.GEAR, 18))
            ),
            rocks = listOf(
                GridCoord(4, 2), GridCoord(5, 2), GridCoord(4, 10), GridCoord(5, 10),
                GridCoord(4, 5), GridCoord(5, 7)
            ),
            hint = "",
            isExpert = true
        ),
        // 131
        LevelConfig(
            id = 131,
            name = "Core Overload",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 30,
            spawners = listOf(
                PlacedTile(GridCoord(4, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.WEST, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(5, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.EAST, 1, secondaryItemType = ItemType.IRON_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 11), TileEntity.Hatch(ItemType.MOTOR, 18)),
                PlacedTile(GridCoord(5, 11), TileEntity.Hatch(ItemType.CIRCUIT, 18)),
                PlacedTile(GridCoord(8, 11), TileEntity.Hatch(ItemType.GEAR, 18))
            ),
            rocks = listOf(
                GridCoord(2, 4), GridCoord(7, 4), GridCoord(4, 6), GridCoord(5, 6), GridCoord(2, 9), GridCoord(7, 9)
            ),
            hint = "",
            isExpert = true
        ),
        // 132
        LevelConfig(
            id = 132,
            name = "Torque Labyrinth",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 31,
            spawners = listOf(
                PlacedTile(GridCoord(1, 11), TileEntity.Spawner(ItemType.IRON_ORE, Direction.NORTH, 1, secondaryItemType = ItemType.IRON_PLATE)),
                PlacedTile(GridCoord(8, 11), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.NORTH, 1, secondaryItemType = ItemType.COPPER_WIRE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Hatch(ItemType.CIRCUIT, 18)),
                PlacedTile(GridCoord(5, 1), TileEntity.Hatch(ItemType.MOTOR, 18)),
                PlacedTile(GridCoord(8, 1), TileEntity.Hatch(ItemType.GEAR, 18))
            ),
            rocks = listOf(
                GridCoord(0, 4), GridCoord(1, 4), GridCoord(2, 4),
                GridCoord(7, 4), GridCoord(8, 4), GridCoord(9, 4),
                GridCoord(3, 8), GridCoord(4, 8), GridCoord(5, 8), GridCoord(6, 8)
            ),
            hint = "",
            isExpert = true
        ),
        // 133
        LevelConfig(
            id = 133,
            name = "Dual Reactor",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 32,
            spawners = listOf(
                PlacedTile(GridCoord(1, 2), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(8, 2), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(1, 10), TileEntity.Spawner(ItemType.COPPER_INGOT, Direction.NORTH, 1)),
                PlacedTile(GridCoord(8, 10), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.NORTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(4, 2), TileEntity.Hatch(ItemType.MOTOR, 20)),
                PlacedTile(GridCoord(5, 10), TileEntity.Hatch(ItemType.MOTOR, 20))
            ),
            rocks = listOf(
                GridCoord(3, 5), GridCoord(6, 5), GridCoord(3, 7), GridCoord(6, 7)
            ),
            hint = "",
            isExpert = true
        ),
        // 134
        LevelConfig(
            id = 134,
            name = "Deadlock Matrix",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 31,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.CIRCUIT, Direction.EAST, 2, secondaryItemType = ItemType.IRON_ORE)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.GEAR, Direction.WEST, 2, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(5, 11), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.NORTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 11), TileEntity.Hatch(ItemType.MOTOR, 20)),
                PlacedTile(GridCoord(8, 11), TileEntity.Hatch(ItemType.CIRCUIT, 18))
            ),
            rocks = listOf(
                GridCoord(2, 4), GridCoord(7, 4), GridCoord(4, 6), GridCoord(5, 6), GridCoord(2, 8), GridCoord(7, 8)
            ),
            hint = "",
            isExpert = true
        ),
        // 135
        LevelConfig(
            id = 135,
            name = "Hyperweave",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 32,
            spawners = listOf(
                PlacedTile(GridCoord(1, 3), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1, secondaryItemType = ItemType.COPPER_WIRE)),
                PlacedTile(GridCoord(8, 3), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.WEST, 1, secondaryItemType = ItemType.IRON_PLATE)),
                PlacedTile(GridCoord(1, 9), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.EAST, 1)),
                PlacedTile(GridCoord(8, 9), TileEntity.Spawner(ItemType.IRON_ORE, Direction.WEST, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(5, 1), TileEntity.Hatch(ItemType.CIRCUIT, 20)),
                PlacedTile(GridCoord(5, 11), TileEntity.Hatch(ItemType.MOTOR, 20))
            ),
            rocks = listOf(
                GridCoord(3, 5), GridCoord(6, 5), GridCoord(3, 7), GridCoord(6, 7)
            ),
            hint = "",
            isExpert = true
        ),
        // 136
        LevelConfig(
            id = 136,
            name = "Iron Singularity",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 32,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(3, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(6, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, 1)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.SOUTH, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 11), TileEntity.Hatch(ItemType.MOTOR, 20)),
                PlacedTile(GridCoord(4, 11), TileEntity.Hatch(ItemType.GEAR, 20)),
                PlacedTile(GridCoord(8, 11), TileEntity.Hatch(ItemType.CIRCUIT, 20))
            ),
            rocks = listOf(
                GridCoord(0, 5), GridCoord(2, 5), GridCoord(4, 5), GridCoord(5, 5), GridCoord(7, 5), GridCoord(9, 5),
                GridCoord(3, 8), GridCoord(6, 8)
            ),
            hint = "",
            isExpert = true
        ),
        // 137
        LevelConfig(
            id = 137,
            name = "Crucible X",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 32,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.EAST, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(8, 11), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.WEST, 1, secondaryItemType = ItemType.IRON_ORE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(8, 1), TileEntity.Hatch(ItemType.MOTOR, 18)),
                PlacedTile(GridCoord(1, 11), TileEntity.Hatch(ItemType.MOTOR, 18)),
                PlacedTile(GridCoord(5, 6), TileEntity.Hatch(ItemType.CIRCUIT, 18))
            ),
            rocks = listOf(
                GridCoord(3, 3), GridCoord(6, 3), GridCoord(3, 9), GridCoord(6, 9),
                GridCoord(2, 6), GridCoord(7, 6)
            ),
            hint = "",
            isExpert = true
        ),
        // 138
        LevelConfig(
            id = 138,
            name = "Chrono Valve",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 32,
            spawners = listOf(
                PlacedTile(GridCoord(1, 11), TileEntity.Spawner(ItemType.IRON_ORE, Direction.NORTH, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(5, 11), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.NORTH, 1, secondaryItemType = ItemType.IRON_ORE)),
                PlacedTile(GridCoord(8, 11), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.NORTH, 1, secondaryItemType = ItemType.COPPER_WIRE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 1), TileEntity.Hatch(ItemType.MOTOR, 20)),
                PlacedTile(GridCoord(7, 1), TileEntity.Hatch(ItemType.CIRCUIT, 20))
            ),
            rocks = listOf(
                GridCoord(3, 4), GridCoord(6, 4), GridCoord(2, 7), GridCoord(7, 7), GridCoord(4, 9), GridCoord(5, 9)
            ),
            hint = "",
            isExpert = true
        ),
        // 139
        LevelConfig(
            id = 139,
            name = "Omega Gate",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 33,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.WEST, 1, secondaryItemType = ItemType.IRON_ORE)),
                PlacedTile(GridCoord(1, 11), TileEntity.Spawner(ItemType.GEAR, Direction.EAST, 2, secondaryItemType = ItemType.COPPER_WIRE)),
                PlacedTile(GridCoord(8, 11), TileEntity.Spawner(ItemType.CIRCUIT, Direction.WEST, 2, secondaryItemType = ItemType.IRON_PLATE))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(2, 6), TileEntity.Hatch(ItemType.MOTOR, 22)),
                PlacedTile(GridCoord(7, 6), TileEntity.Hatch(ItemType.GEAR, 20))
            ),
            rocks = listOf(
                GridCoord(4, 3), GridCoord(5, 3), GridCoord(4, 9), GridCoord(5, 9),
                GridCoord(0, 6), GridCoord(9, 6)
            ),
            hint = "",
            isExpert = true
        ),
        // 140
        LevelConfig(
            id = 140,
            name = "Grand Architect",
            subtitle = "Expert Protocol",
            width = 10,
            height = 13,
            parBelts = 34,
            spawners = listOf(
                PlacedTile(GridCoord(1, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.SOUTH, 1, secondaryItemType = ItemType.COPPER_ORE)),
                PlacedTile(GridCoord(8, 1), TileEntity.Spawner(ItemType.COPPER_ORE, Direction.SOUTH, 1, secondaryItemType = ItemType.IRON_ORE)),
                PlacedTile(GridCoord(4, 11), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.WEST, 1)),
                PlacedTile(GridCoord(5, 11), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.EAST, 1))
            ),
            hatches = listOf(
                PlacedTile(GridCoord(1, 6), TileEntity.Hatch(ItemType.GEAR, 20)),
                PlacedTile(GridCoord(5, 6), TileEntity.Hatch(ItemType.MOTOR, 22)),
                PlacedTile(GridCoord(8, 6), TileEntity.Hatch(ItemType.CIRCUIT, 20))
            ),
            rocks = listOf(
                GridCoord(3, 3), GridCoord(6, 3), GridCoord(3, 9), GridCoord(6, 9)
            ),
            hint = "",
            isExpert = true
        )
    )
}
