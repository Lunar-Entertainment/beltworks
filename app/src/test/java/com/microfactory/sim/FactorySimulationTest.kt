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
    fun rotatingBeltSmoothlyConnectsToNeighbors() {
        val sim = FactorySimulation(width = 5, height = 5)
        // Spawner at (0,2) points EAST into (1,2)
        sim.setTile(GridCoord(0, 2), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 2))
        sim.setTile(GridCoord(1, 2), TileEntity.Belt(Direction.WEST, Direction.EAST))
        // Standalone vertical belt below at (1,3)
        sim.setTile(GridCoord(1, 3), TileEntity.Belt(Direction.WEST, Direction.SOUTH))

        // Rotating (1,2) keeps inputDir = WEST locked to spawner and curves outputDir -> SOUTH,
        // AND automatically snaps downstream (1,3) inputDir to NORTH!
        assertTrue(sim.rotateTile(GridCoord(1, 2)))
        assertEquals(TileEntity.Belt(Direction.WEST, Direction.SOUTH), sim.getTile(GridCoord(1, 2)))
        assertEquals(TileEntity.Belt(Direction.NORTH, Direction.SOUTH), sim.getTile(GridCoord(1, 3)))

        // Rotating again sweeps outputDir -> NORTH (turning up while keeping inputDir = WEST)
        assertTrue(sim.rotateTile(GridCoord(1, 2)))
        assertEquals(TileEntity.Belt(Direction.WEST, Direction.NORTH), sim.getTile(GridCoord(1, 2)))

        // Rotating a third time returns smoothly to straight (WEST -> EAST)
        assertTrue(sim.rotateTile(GridCoord(1, 2)))
        assertEquals(TileEntity.Belt(Direction.WEST, Direction.EAST), sim.getTile(GridCoord(1, 2)))
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

        sim.tick()
        assertEquals(1, sim.items.size)
        assertEquals(GridCoord(0, 1), sim.items[0].prevCoord)
        assertEquals(GridCoord(1, 1), sim.items[0].currentCoord)

        sim.tick()
        assertEquals(2, sim.items.size)
        assertEquals(listOf(GridCoord(2, 1), GridCoord(1, 1)), sim.items.map { it.currentCoord })

        sim.tick()
        assertEquals(3, sim.items.size)
        assertEquals(
            listOf(GridCoord(3, 1), GridCoord(2, 1), GridCoord(1, 1)),
            sim.items.map { it.currentCoord }
        )

        sim.tick()
        val storedHatch = sim.getTile(GridCoord(4, 1)) as TileEntity.Hatch
        assertEquals(1, storedHatch.deliveredCount)
        assertEquals(3, sim.items.size)

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
        sim.setTile(GridCoord(2, 1), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        sim.setTile(GridCoord(2, 2), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim.setTile(GridCoord(2, 3), TileEntity.Hatch(ItemType.IRON_ORE, targetCount = 1))

        sim.tick()
        assertEquals(GridCoord(1, 1), sim.items.single().currentCoord)

        sim.tick()
        assertEquals(GridCoord(2, 1), sim.items.single().currentCoord)

        sim.tick()
        assertEquals(GridCoord(2, 1), sim.items.single().prevCoord)
        assertEquals(GridCoord(2, 2), sim.items.single().currentCoord)

        sim.tick()
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

        sim.tick()
        sim.tick()
        assertEquals(0, sim.items.size)
        assertTrue(sim.getMachineStatus(GridCoord(2, 1))!!.isProcessing)

        sim.tick()
        assertEquals(0, sim.items.size)

        sim.tick()
        assertEquals(1, sim.items.size)
        assertEquals(ItemType.IRON_INGOT, sim.items.single().type)
        assertEquals(GridCoord(3, 1), sim.items.single().currentCoord)

        sim.tick()
        val hatch = sim.getTile(GridCoord(4, 1)) as TileEntity.Hatch
        assertEquals(1, hatch.deliveredCount)
        assertTrue(sim.isComplete)
    }

    @Test
    fun pressAndAssemblerCraftPlateAndGear() {
        val sim = FactorySimulation(width = 6, height = 5)
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
    fun allEightLevelsAreSolvable() {
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

        // Level 4: Copper Vein
        val sim4 = FactorySimulation()
        val level4 = LevelRepository.getLevel(4)
        sim4.loadLevel(level4)
        sim4.setTile(GridCoord(1, 2), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim4.setTile(GridCoord(1, 3), TileEntity.Smelter(Direction.NORTH, Direction.EAST))
        sim4.setTile(GridCoord(2, 3), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim4.setTile(GridCoord(3, 3), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        sim4.setTile(GridCoord(3, 4), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim4.setTile(GridCoord(3, 5), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim4.setTile(GridCoord(3, 6), TileEntity.Belt(Direction.NORTH, Direction.WEST))
        sim4.setTile(GridCoord(2, 6), TileEntity.Belt(Direction.EAST, Direction.WEST))
        sim4.setTile(GridCoord(1, 6), TileEntity.Belt(Direction.EAST, Direction.SOUTH))
        sim4.setTile(GridCoord(1, 7), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim4.setTile(GridCoord(6, 2), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim4.setTile(GridCoord(6, 3), TileEntity.Smelter(Direction.NORTH, Direction.SOUTH))
        sim4.setTile(GridCoord(6, 4), TileEntity.Belt(Direction.NORTH, Direction.WEST))
        sim4.setTile(GridCoord(5, 4), TileEntity.Belt(Direction.EAST, Direction.WEST))
        sim4.setTile(GridCoord(4, 4), TileEntity.Belt(Direction.EAST, Direction.SOUTH))
        sim4.setTile(GridCoord(4, 5), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim4.setTile(GridCoord(4, 6), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim4.setTile(GridCoord(4, 7), TileEntity.Belt(Direction.NORTH, Direction.EAST))
        sim4.setTile(GridCoord(5, 7), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim4.setTile(GridCoord(6, 7), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        repeat(120) { if (!sim4.isComplete) sim4.tick() }
        assertTrue("Level 4 should complete", sim4.isComplete)
        assertEquals(3, level4.calculateStars(sim4.beltCount))

        // Level 5: Twin Press
        val sim5 = FactorySimulation()
        val level5 = LevelRepository.getLevel(5)
        sim5.loadLevel(level5)
        sim5.setTile(GridCoord(3, 2), TileEntity.Splitter(Direction.NORTH, Direction.EAST, Direction.WEST))
        sim5.setTile(GridCoord(4, 2), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        sim5.setTile(GridCoord(4, 3), TileEntity.Smelter(Direction.NORTH, Direction.SOUTH))
        sim5.setTile(GridCoord(4, 4), TileEntity.Press(Direction.NORTH, Direction.SOUTH))
        sim5.setTile(GridCoord(4, 5), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim5.setTile(GridCoord(4, 6), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim5.setTile(GridCoord(4, 7), TileEntity.Belt(Direction.NORTH, Direction.WEST))
        sim5.setTile(GridCoord(2, 2), TileEntity.Belt(Direction.EAST, Direction.SOUTH))
        sim5.setTile(GridCoord(2, 3), TileEntity.Smelter(Direction.NORTH, Direction.SOUTH))
        sim5.setTile(GridCoord(2, 4), TileEntity.Press(Direction.NORTH, Direction.SOUTH))
        sim5.setTile(GridCoord(2, 5), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim5.setTile(GridCoord(2, 6), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim5.setTile(GridCoord(2, 7), TileEntity.Belt(Direction.NORTH, Direction.EAST))
        sim5.setTile(GridCoord(3, 7), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        repeat(150) { if (!sim5.isComplete) sim5.tick() }
        assertTrue("Level 5 should complete", sim5.isComplete)
        assertEquals(3, level5.calculateStars(sim5.beltCount))

        // Level 6: Full Foundry
        val sim6 = FactorySimulation()
        val level6 = LevelRepository.getLevel(6)
        sim6.loadLevel(level6)
        sim6.setTile(GridCoord(2, 1), TileEntity.Smelter(Direction.WEST, Direction.EAST))
        sim6.setTile(GridCoord(3, 1), TileEntity.Press(Direction.WEST, Direction.EAST))
        sim6.setTile(GridCoord(4, 1), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        sim6.setTile(GridCoord(4, 2), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim6.setTile(GridCoord(4, 3), TileEntity.Splitter(Direction.NORTH, Direction.EAST, Direction.WEST))
        sim6.setTile(GridCoord(5, 3), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        sim6.setTile(GridCoord(5, 4), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim6.setTile(GridCoord(5, 5), TileEntity.Belt(Direction.NORTH, Direction.WEST))
        sim6.setTile(GridCoord(3, 3), TileEntity.Belt(Direction.EAST, Direction.SOUTH))
        sim6.setTile(GridCoord(3, 4), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim6.setTile(GridCoord(3, 5), TileEntity.Belt(Direction.NORTH, Direction.EAST))
        sim6.setTile(GridCoord(4, 5), TileEntity.Assembler(Direction.WEST, Direction.EAST, Direction.SOUTH))
        sim6.setTile(GridCoord(4, 6), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim6.setTile(GridCoord(4, 7), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        repeat(160) { if (!sim6.isComplete) sim6.tick() }
        assertTrue("Level 6 should complete", sim6.isComplete)
        assertEquals(3, level6.calculateStars(sim6.beltCount))

        // Level 7: Dual Alloy (9x11)
        val sim7 = FactorySimulation()
        val level7 = LevelRepository.getLevel(7)
        sim7.loadLevel(level7)
        sim7.setTile(GridCoord(1, 2), TileEntity.Smelter(Direction.NORTH, Direction.SOUTH))
        sim7.setTile(GridCoord(1, 3), TileEntity.Belt(Direction.NORTH, Direction.EAST))
        sim7.setTile(GridCoord(2, 3), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        sim7.setTile(GridCoord(2, 4), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim7.setTile(GridCoord(2, 5), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim7.setTile(GridCoord(2, 6), TileEntity.Belt(Direction.NORTH, Direction.WEST))
        sim7.setTile(GridCoord(1, 6), TileEntity.Belt(Direction.EAST, Direction.SOUTH))
        sim7.setTile(GridCoord(1, 7), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim7.setTile(GridCoord(1, 8), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))

        sim7.setTile(GridCoord(7, 2), TileEntity.Smelter(Direction.NORTH, Direction.SOUTH))
        sim7.setTile(GridCoord(7, 3), TileEntity.Press(Direction.NORTH, Direction.SOUTH))
        sim7.setTile(GridCoord(7, 4), TileEntity.Belt(Direction.NORTH, Direction.WEST))
        sim7.setTile(GridCoord(6, 4), TileEntity.Belt(Direction.EAST, Direction.WEST))
        sim7.setTile(GridCoord(5, 4), TileEntity.Belt(Direction.EAST, Direction.SOUTH))
        sim7.setTile(GridCoord(5, 5), TileEntity.Splitter(Direction.NORTH, Direction.EAST, Direction.WEST))
        sim7.setTile(GridCoord(6, 5), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        sim7.setTile(GridCoord(6, 6), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim7.setTile(GridCoord(6, 7), TileEntity.Belt(Direction.NORTH, Direction.WEST))
        sim7.setTile(GridCoord(4, 5), TileEntity.Belt(Direction.EAST, Direction.SOUTH))
        sim7.setTile(GridCoord(4, 6), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim7.setTile(GridCoord(4, 7), TileEntity.Belt(Direction.NORTH, Direction.EAST))
        sim7.setTile(GridCoord(5, 7), TileEntity.Assembler(Direction.WEST, Direction.EAST, Direction.SOUTH))
        sim7.setTile(GridCoord(5, 8), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim7.setTile(GridCoord(5, 9), TileEntity.Belt(Direction.NORTH, Direction.EAST))
        repeat(160) { if (!sim7.isComplete) sim7.tick() }
        assertTrue("Level 7 should complete", sim7.isComplete)
        assertEquals(3, level7.calculateStars(sim7.beltCount))

        // Level 8: Clockwork Core (9x12)
        val sim8 = FactorySimulation()
        val level8 = LevelRepository.getLevel(8)
        sim8.loadLevel(level8)
        sim8.setTile(GridCoord(2, 1), TileEntity.Smelter(Direction.WEST, Direction.EAST))
        sim8.setTile(GridCoord(3, 1), TileEntity.Press(Direction.WEST, Direction.SOUTH))
        sim8.setTile(GridCoord(3, 2), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim8.setTile(GridCoord(3, 3), TileEntity.Splitter(Direction.NORTH, Direction.EAST, Direction.WEST))
        // West arm to Plate Hatch (1,10)
        sim8.setTile(GridCoord(2, 3), TileEntity.Belt(Direction.EAST, Direction.WEST))
        sim8.setTile(GridCoord(1, 3), TileEntity.Belt(Direction.EAST, Direction.SOUTH))
        for (y in 4..9) {
            sim8.setTile(GridCoord(1, y), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        }
        // East arm into Assembler (4,6)
        sim8.setTile(GridCoord(4, 3), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        sim8.setTile(GridCoord(4, 4), TileEntity.Belt(Direction.NORTH, Direction.WEST))
        sim8.setTile(GridCoord(3, 4), TileEntity.Belt(Direction.EAST, Direction.SOUTH))
        sim8.setTile(GridCoord(3, 5), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim8.setTile(GridCoord(3, 6), TileEntity.Belt(Direction.NORTH, Direction.EAST))

        // Right Spawner B -> Assembler (4,6)
        sim8.setTile(GridCoord(6, 1), TileEntity.Smelter(Direction.EAST, Direction.WEST))
        sim8.setTile(GridCoord(5, 1), TileEntity.Press(Direction.EAST, Direction.SOUTH))
        for (y in 2..5) {
            sim8.setTile(GridCoord(5, y), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        }
        sim8.setTile(GridCoord(5, 6), TileEntity.Belt(Direction.NORTH, Direction.WEST))

        // Assembler -> Gear Hatch (4,10)
        sim8.setTile(GridCoord(4, 6), TileEntity.Assembler(Direction.WEST, Direction.EAST, Direction.SOUTH))
        for (y in 7..9) {
            sim8.setTile(GridCoord(4, y), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        }
        repeat(220) { if (!sim8.isComplete) sim8.tick() }
        assertTrue("Level 8 should complete", sim8.isComplete)
        assertEquals(3, level8.calculateStars(sim8.beltCount))
    }
}
