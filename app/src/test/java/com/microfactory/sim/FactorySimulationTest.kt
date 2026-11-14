package com.microfactory.sim

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FactorySimulationTest {

    @Test
    fun directionHelpersAndTileRotation() {
        assertEquals(Direction.SOUTH, Direction.NORTH.opposite())
        assertEquals(Direction.WEST, Direction.EAST.opposite())

        assertEquals(Direction.EAST, Direction.NORTH.rotateClockwise())
        assertEquals(Direction.SOUTH, Direction.EAST.rotateClockwise())
        assertEquals(Direction.WEST, Direction.SOUTH.rotateClockwise())
        assertEquals(Direction.NORTH, Direction.WEST.rotateClockwise())

        assertEquals(Direction.WEST, Direction.NORTH.rotateCounterClockwise())

        val belt = TileEntity.Belt(Direction.WEST, Direction.SOUTH)
        assertEquals(
            TileEntity.Belt(Direction.NORTH, Direction.WEST),
            belt.rotatedClockwise()
        )
        assertFalse(TileEntity.Rock.isPlayerEditable())
        assertFalse(TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 2).isPlayerEditable())
        assertFalse(TileEntity.Hatch(ItemType.IRON_INGOT, 5).isPlayerEditable())
        assertTrue(belt.isPlayerEditable())
    }

    @Test
    fun straightBeltMovementAdvancesInLockstepWithoutTeleporting() {
        val sim = FactorySimulation(width = 6, height = 3)
        sim.setTile(GridCoord(0, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, spawnIntervalTicks = 1))
        sim.setTile(GridCoord(1, 1), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim.setTile(GridCoord(2, 1), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim.setTile(GridCoord(3, 1), TileEntity.Belt(Direction.WEST, Direction.EAST))
        val hatch = TileEntity.Hatch(ItemType.IRON_ORE, targetCount = 5)
        sim.setTile(GridCoord(4, 1), hatch)

        // Tick 1: item #1 spawns onto (1,1)
        sim.tick()
        assertEquals(1, sim.items.size)
        assertEquals(GridCoord(0, 1), sim.items[0].prevCoord)
        assertEquals(GridCoord(1, 1), sim.items[0].currentCoord)

        // Tick 2: item #1 moves (1,1)->(2,1), item #2 spawns onto (1,1)
        sim.tick()
        assertEquals(2, sim.items.size)
        assertEquals(listOf(GridCoord(2, 1), GridCoord(1, 1)), sim.items.map { it.currentCoord })

        // Tick 3: item #1 moves (2,1)->(3,1), item #2 moves (1,1)->(2,1), item #3 spawns onto (1,1)
        sim.tick()
        assertEquals(3, sim.items.size)
        assertEquals(
            listOf(GridCoord(3, 1), GridCoord(2, 1), GridCoord(1, 1)),
            sim.items.map { it.currentCoord }
        )

        // Tick 4: item #1 enters Hatch at (4,1), others advance cleanly
        sim.tick()
        val storedHatch = sim.getTile(GridCoord(4, 1)) as TileEntity.Hatch
        assertEquals(1, storedHatch.deliveredCount)
        assertEquals(3, sim.items.size)

        // Run 4 more ticks to fulfill targetCount = 5
        repeat(4) { sim.tick() }
        assertEquals(5, storedHatch.deliveredCount)
        assertTrue(sim.isComplete)
        assertTrue(sim.rollingIpm > 0.0)
    }

    @Test
    fun cornerBeltTurns90Degrees() {
        val sim = FactorySimulation(width = 5, height = 5)
        sim.setTile(GridCoord(0, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, spawnIntervalTicks = 10))
        sim.setTile(GridCoord(1, 1), TileEntity.Belt(Direction.WEST, Direction.EAST))
        // Corner turning EAST -> SOUTH
        sim.setTile(GridCoord(2, 1), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        sim.setTile(GridCoord(2, 2), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim.setTile(GridCoord(2, 3), TileEntity.Hatch(ItemType.IRON_ORE, targetCount = 1))

        sim.tick() // spawns at (1,1)
        assertEquals(GridCoord(1, 1), sim.items.single().currentCoord)

        sim.tick() // enters corner at (2,1)
        assertEquals(GridCoord(2, 1), sim.items.single().currentCoord)

        sim.tick() // exits corner SOUTH onto (2,2)
        assertEquals(GridCoord(1, 1).plus(Direction.EAST), sim.items.single().prevCoord)
        assertEquals(GridCoord(2, 2), sim.items.single().currentCoord)

        sim.tick() // enters Hatch at (2,3)
        val hatch = sim.getTile(GridCoord(2, 3)) as TileEntity.Hatch
        assertEquals(1, hatch.deliveredCount)
        assertTrue(sim.isComplete)
    }

    @Test
    fun splitterAlternates5050BetweenLeftAndRight() {
        val sim = FactorySimulation(width = 5, height = 5)
        sim.setTile(GridCoord(2, 0), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.SOUTH, spawnIntervalTicks = 1))
        sim.setTile(GridCoord(2, 1), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim.setTile(
            GridCoord(2, 2),
            TileEntity.Splitter(
                inputDir = Direction.NORTH,
                outputLeft = Direction.EAST,
                outputRight = Direction.WEST,
                toggleLeft = true
            )
        )
        sim.setTile(GridCoord(3, 2), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim.setTile(GridCoord(4, 2), TileEntity.Hatch(ItemType.IRON_PLATE, targetCount = 4))
        sim.setTile(GridCoord(1, 2), TileEntity.Belt(Direction.EAST, Direction.WEST))
        sim.setTile(GridCoord(0, 2), TileEntity.Hatch(ItemType.IRON_PLATE, targetCount = 4))

        // Run until 8 items have been distributed (should be 4 east, 4 west).
        repeat(20) {
            if (!sim.isComplete) sim.tick()
        }

        val eastHatch = sim.getTile(GridCoord(4, 2)) as TileEntity.Hatch
        val westHatch = sim.getTile(GridCoord(0, 2)) as TileEntity.Hatch
        assertEquals(4, eastHatch.deliveredCount)
        assertEquals(4, westHatch.deliveredCount)
        assertTrue(sim.isComplete)
    }

    @Test
    fun smelterConvertsOreToIngot() {
        val sim = FactorySimulation(width = 6, height = 3)
        sim.setTile(GridCoord(0, 1), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, spawnIntervalTicks = 5))
        sim.setTile(GridCoord(1, 1), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim.setTile(GridCoord(2, 1), TileEntity.Smelter(Direction.WEST, Direction.EAST, processTicks = 2))
        sim.setTile(GridCoord(3, 1), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim.setTile(GridCoord(4, 1), TileEntity.Hatch(ItemType.IRON_INGOT, targetCount = 1))

        sim.tick() // Tick 1: IRON_ORE spawns at (1,1)
        sim.tick() // Tick 2: IRON_ORE enters Smelter at (2,1)
        assertEquals(0, sim.items.size)
        assertTrue(sim.getMachineStatus(GridCoord(2, 1))!!.isProcessing)

        sim.tick() // Tick 3: Smelter process tick 1 of 2
        assertEquals(0, sim.items.size)

        sim.tick() // Tick 4: Smelter process tick 2 of 2 -> emits IRON_INGOT onto (3,1)
        assertEquals(1, sim.items.size)
        assertEquals(ItemType.IRON_INGOT, sim.items.single().type)
        assertEquals(GridCoord(3, 1), sim.items.single().currentCoord)

        sim.tick() // Tick 5: IRON_INGOT delivered to Hatch at (4,1)
        val hatch = sim.getTile(GridCoord(4, 1)) as TileEntity.Hatch
        assertEquals(1, hatch.deliveredCount)
        assertTrue(sim.isComplete)
    }

    @Test
    fun pressAndAssemblerCraftPlateAndGear() {
        val sim = FactorySimulation(width = 6, height = 5)
        // Two iron ingot spawners -> two presses -> assembler -> gear hatch
        sim.setTile(GridCoord(0, 2), TileEntity.Spawner(ItemType.IRON_INGOT, Direction.EAST, spawnIntervalTicks = 2))
        sim.setTile(GridCoord(1, 2), TileEntity.Press(Direction.WEST, Direction.EAST, processTicks = 2))
        sim.setTile(GridCoord(4, 2), TileEntity.Spawner(ItemType.IRON_INGOT, Direction.WEST, spawnIntervalTicks = 2))
        sim.setTile(GridCoord(3, 2), TileEntity.Press(Direction.EAST, Direction.WEST, processTicks = 2))
        sim.setTile(
            GridCoord(2, 2),
            TileEntity.Assembler(
                inputDirA = Direction.WEST,
                inputDirB = Direction.EAST,
                outputDir = Direction.SOUTH,
                processTicks = 3
            )
        )
        sim.setTile(GridCoord(2, 3), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim.setTile(GridCoord(2, 4), TileEntity.Hatch(ItemType.GEAR, targetCount = 2))

        repeat(30) {
            if (!sim.isComplete) sim.tick()
        }

        val hatch = sim.getTile(GridCoord(2, 4)) as TileEntity.Hatch
        assertEquals(2, hatch.deliveredCount)
        assertTrue(sim.isComplete)
    }

    @Test
    fun starterLevelsAreAllSolvable() {
        // Level 1: First Melt
        val sim1 = FactorySimulation()
        val level1 = LevelRepository.getLevel(1)
        sim1.loadLevel(level1)
        sim1.setTile(GridCoord(2, 3), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim1.setTile(GridCoord(3, 3), TileEntity.Smelter(Direction.WEST, Direction.SOUTH))
        sim1.setTile(GridCoord(3, 4), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim1.setTile(GridCoord(3, 5), TileEntity.Belt(Direction.NORTH, Direction.EAST))
        sim1.setTile(GridCoord(4, 5), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim1.setTile(GridCoord(5, 5), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim1.setTile(GridCoord(6, 5), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        repeat(80) { if (!sim1.isComplete) sim1.tick() }
        assertTrue("Level 1 should complete", sim1.isComplete)
        assertEquals(3, level1.calculateStars(sim1.beltCount))

        // Level 2: Heavy Metal
        val sim2 = FactorySimulation()
        val level2 = LevelRepository.getLevel(2)
        sim2.loadLevel(level2)
        sim2.setTile(GridCoord(1, 2), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim2.setTile(GridCoord(1, 3), TileEntity.Smelter(Direction.NORTH, Direction.SOUTH))
        sim2.setTile(GridCoord(1, 4), TileEntity.Belt(Direction.NORTH, Direction.EAST))
        sim2.setTile(GridCoord(2, 4), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim2.setTile(GridCoord(3, 4), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim2.setTile(GridCoord(4, 4), TileEntity.Press(Direction.WEST, Direction.EAST))
        sim2.setTile(GridCoord(5, 4), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim2.setTile(GridCoord(6, 4), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        sim2.setTile(GridCoord(6, 5), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim2.setTile(GridCoord(6, 6), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim2.setTile(GridCoord(6, 7), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        repeat(150) { if (!sim2.isComplete) sim2.tick() }
        assertTrue("Level 2 should complete", sim2.isComplete)
        assertEquals(3, level2.calculateStars(sim2.beltCount))

        // Level 3: Gearworks
        val sim3 = FactorySimulation()
        val level3 = LevelRepository.getLevel(3)
        sim3.loadLevel(level3)
        sim3.setTile(GridCoord(3, 2), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim3.setTile(GridCoord(3, 3), TileEntity.Splitter(Direction.NORTH, Direction.EAST, Direction.WEST))
        sim3.setTile(GridCoord(4, 3), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        sim3.setTile(GridCoord(4, 4), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim3.setTile(GridCoord(4, 5), TileEntity.Belt(Direction.NORTH, Direction.WEST))
        sim3.setTile(GridCoord(2, 3), TileEntity.Belt(Direction.EAST, Direction.SOUTH))
        sim3.setTile(GridCoord(2, 4), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim3.setTile(GridCoord(2, 5), TileEntity.Belt(Direction.NORTH, Direction.EAST))
        sim3.setTile(GridCoord(3, 5), TileEntity.Assembler(Direction.WEST, Direction.EAST, Direction.SOUTH))
        sim3.setTile(GridCoord(3, 6), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim3.setTile(GridCoord(3, 7), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        repeat(120) { if (!sim3.isComplete) sim3.tick() }
        assertTrue("Level 3 should complete", sim3.isComplete)
        assertEquals(3, level3.calculateStars(sim3.beltCount))
    }
}
