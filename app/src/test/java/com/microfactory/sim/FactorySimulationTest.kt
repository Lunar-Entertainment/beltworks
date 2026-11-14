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
        val bridge = TileEntity.Bridge(Direction.EAST, Direction.SOUTH)
        assertEquals(
            TileEntity.Bridge(Direction.WEST, Direction.SOUTH),
            bridge.rotatedClockwise()
        )
        assertFalse(TileEntity.Rock.isPlayerEditable())
        assertFalse(TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 2).isPlayerEditable())
        assertFalse(TileEntity.Hatch(ItemType.IRON_INGOT, 5).isPlayerEditable())
        assertTrue(belt.isPlayerEditable())
        assertTrue(bridge.isPlayerEditable())
        assertTrue(TileEntity.Incinerator.isPlayerEditable())
    }

    @Test
    fun rotatingBeltSmoothlyConnectsToNeighbors() {
        val sim = FactorySimulation(width = 5, height = 5)
        sim.setTile(GridCoord(0, 2), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 2))
        sim.setTile(GridCoord(1, 2), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim.setTile(GridCoord(1, 3), TileEntity.Belt(Direction.WEST, Direction.SOUTH))

        assertTrue(sim.rotateTile(GridCoord(1, 2)))
        assertEquals(TileEntity.Belt(Direction.WEST, Direction.SOUTH), sim.getTile(GridCoord(1, 2)))
        assertEquals(TileEntity.Belt(Direction.NORTH, Direction.SOUTH), sim.getTile(GridCoord(1, 3)))

        assertTrue(sim.rotateTile(GridCoord(1, 2)))
        assertEquals(TileEntity.Belt(Direction.WEST, Direction.NORTH), sim.getTile(GridCoord(1, 2)))

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
        sim.tick()
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

        repeat(20) { if (!sim.isComplete) sim.tick() }

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
        sim.tick()
        assertEquals(1, sim.items.size)
        assertEquals(ItemType.IRON_INGOT, sim.items.single().type)

        sim.tick()
        val hatch = sim.getTile(GridCoord(4, 1)) as TileEntity.Hatch
        assertEquals(1, hatch.deliveredCount)
        assertTrue(sim.isComplete)
    }

    @Test
    fun pressAndAssemblerCraftPlateWireCircuitGearAndMotor() {
        val sim = FactorySimulation(width = 7, height = 7)
        // Top left: IRON_INGOT -> Press -> IRON_PLATE -> Splitter -> Gear Assembler
        sim.setTile(GridCoord(0, 1), TileEntity.Spawner(ItemType.IRON_INGOT, Direction.EAST, 1))
        sim.setTile(GridCoord(1, 1), TileEntity.Press(Direction.WEST, Direction.EAST))
        sim.setTile(GridCoord(2, 1), TileEntity.Splitter(Direction.WEST, Direction.NORTH, Direction.SOUTH))
        sim.setTile(GridCoord(2, 0), TileEntity.Belt(Direction.SOUTH, Direction.EAST))
        sim.setTile(GridCoord(3, 0), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        sim.setTile(GridCoord(2, 2), TileEntity.Belt(Direction.NORTH, Direction.EAST))
        sim.setTile(GridCoord(3, 2), TileEntity.Belt(Direction.WEST, Direction.NORTH))
        sim.setTile(GridCoord(3, 1), TileEntity.Assembler(Direction.NORTH, Direction.SOUTH, Direction.EAST, processTicks = 2))
        sim.setTile(GridCoord(4, 1), TileEntity.Belt(Direction.WEST, Direction.SOUTH))

        // Bottom: IRON_PLATE + COPPER_INGOT->Press->COPPER_WIRE -> Circuit Assembler
        sim.setTile(GridCoord(0, 4), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.EAST, 1))
        sim.setTile(GridCoord(1, 4), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim.setTile(GridCoord(2, 4), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim.setTile(GridCoord(6, 4), TileEntity.Spawner(ItemType.COPPER_INGOT, Direction.WEST, 1))
        sim.setTile(GridCoord(5, 4), TileEntity.Press(Direction.EAST, Direction.WEST))
        sim.setTile(GridCoord(4, 4), TileEntity.Belt(Direction.EAST, Direction.WEST))
        sim.setTile(GridCoord(3, 4), TileEntity.Assembler(Direction.WEST, Direction.EAST, Direction.NORTH, processTicks = 2))
        sim.setTile(GridCoord(3, 3), TileEntity.Belt(Direction.SOUTH, Direction.EAST))
        sim.setTile(GridCoord(4, 3), TileEntity.Belt(Direction.WEST, Direction.NORTH))

        // Motor Assembler at (4,2) receiving GEAR from NORTH and CIRCUIT from SOUTH -> EAST
        sim.setTile(GridCoord(4, 2), TileEntity.Assembler(Direction.NORTH, Direction.SOUTH, Direction.EAST, processTicks = 2))
        sim.setTile(GridCoord(5, 2), TileEntity.Hatch(ItemType.MOTOR, 3))

        repeat(60) { if (!sim.isComplete) sim.tick() }
        assertTrue(sim.isComplete)
    }

    @Test
    fun bridgeCrossesTwoLanesWithoutMixing() {
        val sim = FactorySimulation(width = 5, height = 5)
        sim.setTile(GridCoord(0, 2), TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1))
        sim.setTile(GridCoord(1, 2), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim.setTile(GridCoord(2, 0), TileEntity.Spawner(ItemType.COPPER_WIRE, Direction.SOUTH, 1))
        sim.setTile(GridCoord(2, 1), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))

        sim.setTile(GridCoord(2, 2), TileEntity.Bridge(horizDir = Direction.EAST, vertDir = Direction.SOUTH))

        sim.setTile(GridCoord(3, 2), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim.setTile(GridCoord(4, 2), TileEntity.Hatch(ItemType.IRON_ORE, 6))
        sim.setTile(GridCoord(2, 3), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim.setTile(GridCoord(2, 4), TileEntity.Hatch(ItemType.COPPER_WIRE, 6))

        // On Tick 2, both items enter (2,2) simultaneously on orthogonal lanes
        sim.tick()
        sim.tick()
        assertEquals(2, sim.items.count { it.currentCoord == GridCoord(2, 2) })

        repeat(20) { if (!sim.isComplete) sim.tick() }
        assertTrue(sim.isComplete)
    }

    @Test
    fun filterAndIncineratorSeparateMixedStreamCleanly() {
        val sim = FactorySimulation(width = 6, height = 5)
        sim.setTile(
            GridCoord(0, 2),
            TileEntity.Spawner(ItemType.IRON_ORE, Direction.EAST, 1, secondaryItemType = ItemType.COPPER_ORE)
        )
        sim.setTile(GridCoord(1, 2), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim.setTile(
            GridCoord(2, 2),
            TileEntity.Filter(
                inputDir = Direction.WEST,
                matchDir = Direction.EAST,
                otherDir = Direction.SOUTH,
                filterItem = ItemType.IRON_ORE
            )
        )
        sim.setTile(GridCoord(3, 2), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim.setTile(GridCoord(4, 2), TileEntity.Hatch(ItemType.IRON_ORE, 6))
        sim.setTile(GridCoord(2, 3), TileEntity.Incinerator)

        repeat(25) { if (!sim.isComplete) sim.tick() }
        assertTrue(sim.isComplete)
    }

    @Test
    fun overflowPrioritizesPrimaryThenSpillsExcess() {
        val sim = FactorySimulation(width = 6, height = 5)
        sim.setTile(GridCoord(0, 2), TileEntity.Spawner(ItemType.IRON_PLATE, Direction.EAST, 1))
        sim.setTile(GridCoord(1, 2), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim.setTile(
            GridCoord(2, 2),
            TileEntity.Overflow(
                inputDir = Direction.WEST,
                primaryDir = Direction.NORTH,
                overflowDir = Direction.EAST
            )
        )
        val primaryHatch = TileEntity.Hatch(ItemType.IRON_PLATE, 4)
        val secondaryHatch = TileEntity.Hatch(ItemType.IRON_PLATE, 4)
        sim.setTile(GridCoord(2, 1), primaryHatch)
        sim.setTile(GridCoord(3, 2), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim.setTile(GridCoord(4, 2), secondaryHatch)

        // First 4 plates must fill primaryHatch completely before secondaryHatch starts receiving
        repeat(6) { sim.tick() }
        val p = sim.getTile(GridCoord(2, 1)) as TileEntity.Hatch
        val s = sim.getTile(GridCoord(4, 2)) as TileEntity.Hatch
        assertEquals(4, p.deliveredCount)
        assertTrue("Secondary hatch shouldn't fill before primary", s.deliveredCount < 4)

        repeat(15) { if (!sim.isComplete) sim.tick() }
        assertTrue(sim.isComplete)
        assertEquals(4, p.deliveredCount)
        assertEquals(4, s.deliveredCount)
    }

    @Test
    fun allOneHundredFortyLevelsHaveValidOpenGeometryAndIncreasingProgression() {
        val levels = LevelRepository.levels
        assertEquals(140, levels.size)
        assertEquals((1..140).toList(), levels.map { it.id })
        assertEquals(140, levels.map { it.name }.toSet().size)

        for (level in levels) {
            assertTrue("Level ${level.id} width >= 8", level.width >= 8)
            assertTrue("Level ${level.id} height >= 10", level.height >= 10)
            assertTrue("Level ${level.id} needs spawners", level.spawners.isNotEmpty())
            assertTrue("Level ${level.id} needs hatches", level.hatches.isNotEmpty())

            val occupied = mutableSetOf<GridCoord>()
            for (s in level.spawners) {
                assertTrue("Spawner in bounds lv ${level.id}", s.coord.x in 0 until level.width && s.coord.y in 0 until level.height)
                assertTrue("Unique tile lv ${level.id}", occupied.add(s.coord))
            }
            for (h in level.hatches) {
                assertTrue("Hatch in bounds lv ${level.id}", h.coord.x in 0 until level.width && h.coord.y in 0 until level.height)
                assertTrue("Unique tile lv ${level.id}", occupied.add(h.coord))
            }
            for (r in level.rocks) {
                assertTrue("Rock in bounds lv ${level.id}", r.x in 0 until level.width && r.y in 0 until level.height)
                assertTrue("Rock doesn't overwrite spawner/hatch lv ${level.id}", occupied.add(r))
            }

            val rockSet = level.rocks.toSet()
            val fixedBlockingSet = level.spawners.map { it.coord }.toSet() + rockSet + level.hatches.map { it.coord }.toSet()
            val hatchCoords = level.hatches.map { it.coord }.toSet()

            // Verify every spawner's output port is open and can reach all hatches via BFS around rocks/fixed tiles
            for (s in level.spawners) {
                val spawner = s.tile as TileEntity.Spawner
                val start = s.coord + spawner.outputDir
                assertTrue("Spawner output in bounds lv ${level.id}", start.x in 0 until level.width && start.y in 0 until level.height)
                assertFalse("Spawner output blocked by rock lv ${level.id}", start in rockSet)

                val visited = HashSet<GridCoord>()
                val queue = ArrayDeque<GridCoord>()
                queue.add(start)
                visited.add(start)
                val reachedHatches = HashSet<GridCoord>()

                while (queue.isNotEmpty()) {
                    val cur = queue.removeFirst()
                    if (cur in hatchCoords) {
                        reachedHatches.add(cur)
                        continue
                    }
                    for (dir in Direction.entries) {
                        val next = cur + dir
                        if (next.x !in 0 until level.width || next.y !in 0 until level.height) continue
                        if (next in visited) continue
                        if (next in hatchCoords) {
                            reachedHatches.add(next)
                            visited.add(next)
                            continue
                        }
                        if (next !in fixedBlockingSet) {
                            visited.add(next)
                            queue.add(next)
                        }
                    }
                }
                assertEquals("Spawner ${s.coord} on lv ${level.id} should reach all hatches", hatchCoords, reachedHatches)
            }
        }
    }

    @Test
    fun milestoneLevelsAcrossAllTiersAreSolvable() {
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

        // Level 9: Copper Spool (COPPER_WIRE)
        val sim9 = FactorySimulation()
        sim9.loadLevel(LevelRepository.getLevel(9))
        sim9.setTile(GridCoord(2, 2), TileEntity.Smelter(Direction.WEST, Direction.SOUTH))
        sim9.setTile(GridCoord(2, 3), TileEntity.Press(Direction.NORTH, Direction.SOUTH))
        sim9.setTile(GridCoord(2, 4), TileEntity.Belt(Direction.NORTH, Direction.EAST))
        sim9.setTile(GridCoord(3, 4), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim9.setTile(GridCoord(4, 4), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim9.setTile(GridCoord(5, 4), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim9.setTile(GridCoord(6, 4), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        sim9.setTile(GridCoord(6, 5), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim9.setTile(GridCoord(6, 6), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        repeat(120) { if (!sim9.isComplete) sim9.tick() }
        assertTrue("Level 9 should complete", sim9.isComplete)

        // Level 10: Overpass (Bridge)
        val sim10 = FactorySimulation()
        sim10.loadLevel(LevelRepository.getLevel(10))
        sim10.setTile(GridCoord(2, 4), TileEntity.Smelter(Direction.WEST, Direction.EAST))
        sim10.setTile(GridCoord(3, 4), TileEntity.Bridge(Direction.EAST, Direction.SOUTH))
        sim10.setTile(GridCoord(4, 4), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim10.setTile(GridCoord(5, 4), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim10.setTile(GridCoord(3, 2), TileEntity.Smelter(Direction.NORTH, Direction.SOUTH))
        sim10.setTile(GridCoord(3, 3), TileEntity.Press(Direction.NORTH, Direction.SOUTH))
        sim10.setTile(GridCoord(3, 5), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim10.setTile(GridCoord(3, 6), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim10.setTile(GridCoord(3, 7), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        repeat(120) { if (!sim10.isComplete) sim10.tick() }
        assertTrue("Level 10 should complete", sim10.isComplete)

        // Level 19: First Circuit (CIRCUIT)
        val sim19 = FactorySimulation()
        sim19.loadLevel(LevelRepository.getLevel(19))
        sim19.setTile(GridCoord(2, 4), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim19.setTile(GridCoord(3, 4), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        sim19.setTile(GridCoord(5, 4), TileEntity.Belt(Direction.EAST, Direction.SOUTH))
        sim19.setTile(GridCoord(5, 5), TileEntity.Belt(Direction.NORTH, Direction.WEST))
        sim19.setTile(GridCoord(4, 5), TileEntity.Belt(Direction.EAST, Direction.WEST))
        sim19.setTile(GridCoord(3, 5), TileEntity.Assembler(Direction.NORTH, Direction.EAST, Direction.SOUTH))
        sim19.setTile(GridCoord(3, 6), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim19.setTile(GridCoord(3, 7), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        repeat(120) { if (!sim19.isComplete) sim19.tick() }
        assertTrue("Level 19 should complete", sim19.isComplete)

        // Level 31: First Sort (Mixed Spawner + Filter)
        val sim31 = FactorySimulation()
        sim31.loadLevel(LevelRepository.getLevel(31))
        sim31.setTile(GridCoord(3, 2), TileEntity.Filter(Direction.NORTH, Direction.WEST, Direction.EAST, ItemType.IRON_ORE))
        sim31.setTile(GridCoord(2, 2), TileEntity.Belt(Direction.EAST, Direction.WEST))
        sim31.setTile(GridCoord(1, 2), TileEntity.Belt(Direction.EAST, Direction.SOUTH))
        sim31.setTile(GridCoord(1, 3), TileEntity.Smelter(Direction.NORTH, Direction.SOUTH))
        for (y in 4..7) sim31.setTile(GridCoord(1, y), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim31.setTile(GridCoord(4, 2), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim31.setTile(GridCoord(5, 2), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim31.setTile(GridCoord(6, 2), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        sim31.setTile(GridCoord(6, 3), TileEntity.Smelter(Direction.NORTH, Direction.SOUTH))
        for (y in 4..7) sim31.setTile(GridCoord(6, y), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        repeat(120) { if (!sim31.isComplete) sim31.tick() }
        assertTrue("Level 31 should complete", sim31.isComplete)

        // Level 35: Clean Burn (Incinerator)
        val sim35 = FactorySimulation()
        sim35.loadLevel(LevelRepository.getLevel(35))
        sim35.setTile(GridCoord(2, 4), TileEntity.Filter(Direction.WEST, Direction.EAST, Direction.SOUTH, ItemType.IRON_ORE))
        sim35.setTile(GridCoord(2, 5), TileEntity.Incinerator)
        sim35.setTile(GridCoord(3, 4), TileEntity.Smelter(Direction.WEST, Direction.EAST))
        sim35.setTile(GridCoord(4, 4), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim35.setTile(GridCoord(5, 4), TileEntity.Belt(Direction.WEST, Direction.EAST))
        repeat(120) { if (!sim35.isComplete) sim35.tick() }
        assertTrue("Level 35 should complete", sim35.isComplete)

        // Level 45: Spillway (Overflow)
        val sim45 = FactorySimulation()
        sim45.loadLevel(LevelRepository.getLevel(45))
        sim45.setTile(GridCoord(2, 4), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim45.setTile(GridCoord(3, 4), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim45.setTile(GridCoord(4, 4), TileEntity.Overflow(Direction.WEST, Direction.NORTH, Direction.EAST))
        sim45.setTile(GridCoord(4, 3), TileEntity.Belt(Direction.SOUTH, Direction.NORTH))
        sim45.setTile(GridCoord(5, 4), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim45.setTile(GridCoord(6, 4), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        sim45.setTile(GridCoord(6, 5), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim45.setTile(GridCoord(6, 6), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        repeat(80) { if (!sim45.isComplete) sim45.tick() }
        assertTrue("Level 45 should complete", sim45.isComplete)

        // Level 48: Electric Motor (MOTOR)
        val sim48 = FactorySimulation()
        sim48.loadLevel(LevelRepository.getLevel(48))
        sim48.setTile(GridCoord(2, 4), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim48.setTile(GridCoord(3, 4), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim48.setTile(GridCoord(6, 4), TileEntity.Belt(Direction.EAST, Direction.WEST))
        sim48.setTile(GridCoord(5, 4), TileEntity.Belt(Direction.EAST, Direction.WEST))
        sim48.setTile(GridCoord(4, 4), TileEntity.Assembler(Direction.WEST, Direction.EAST, Direction.SOUTH))
        for (y in 5..8) sim48.setTile(GridCoord(4, y), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        repeat(100) { if (!sim48.isComplete) sim48.tick() }
        assertTrue("Level 48 should complete", sim48.isComplete)

        // Level 80: Apex Automation (Grand Finale)
        val sim80 = FactorySimulation()
        sim80.loadLevel(LevelRepository.getLevel(80))
        // Left mixed spawner (1,1) -> Filter at (1,2): IRON_ORE -> SOUTH (to Gear line), COPPER_ORE -> EAST (to Circuit line)
        sim80.setTile(GridCoord(1, 2), TileEntity.Filter(Direction.NORTH, Direction.SOUTH, Direction.EAST, ItemType.IRON_ORE))
        sim80.setTile(GridCoord(1, 3), TileEntity.Smelter(Direction.NORTH, Direction.SOUTH))
        sim80.setTile(GridCoord(1, 4), TileEntity.Press(Direction.NORTH, Direction.SOUTH))
        sim80.setTile(GridCoord(1, 5), TileEntity.Splitter(Direction.NORTH, Direction.EAST, Direction.SOUTH))
        // Note: (1,5) Splitter EAST -> (2,5) Belt(W->S) -> (2,6) Assembler(N,W->S)? Let's route neatly:
        sim80.setTile(GridCoord(1, 6), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim80.setTile(GridCoord(1, 7), TileEntity.Belt(Direction.NORTH, Direction.EAST))
        sim80.setTile(GridCoord(2, 5), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        sim80.setTile(GridCoord(2, 6), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim80.setTile(GridCoord(2, 7), TileEntity.Assembler(Direction.WEST, Direction.NORTH, Direction.SOUTH))
        sim80.setTile(GridCoord(2, 8), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim80.setTile(GridCoord(2, 9), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim80.setTile(GridCoord(2, 10), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))

        // Copper side from Filter (1,2) EAST -> (2,2) -> (3,2) -> (4,2) -> (4,3) -> ... wait (5,1) is IRON_ORE, (8,1) is COPPER_ORE!
        // Let's feed Motor Assembler at (5,9) from (4,9) Gear and (6,9) Circuit, and also supply Circuit Hatch (8,11)
        // Left Filter COPPER_ORE -> (2,2) Smelter -> (3,2) Press -> (4,2) Belt(W->S) -> (4,3) Belt(N->S) -> (4,4) Belt(N->S)
        // Wait: let's just burn left COPPER_ORE at (2,2) Incinerator or use it! Actually (2,2) Smelter -> (2,3) Press -> (2,4) Belt(N->E)->(3,4) Rock! So (2,2) Belt(W->E)->(3,2) Smelter -> (4,2) Press -> (4,3) Belt(N->S) -> (4,4) Belt(N->S) -> (4,5) Belt(N->S) -> (4,6) Assembler(N,E->S) -> Circuit!
        sim80.setTile(GridCoord(2, 2), TileEntity.Belt(Direction.WEST, Direction.EAST))
        sim80.setTile(GridCoord(3, 2), TileEntity.Smelter(Direction.WEST, Direction.EAST))
        sim80.setTile(GridCoord(4, 2), TileEntity.Press(Direction.WEST, Direction.SOUTH))
        sim80.setTile(GridCoord(4, 3), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim80.setTile(GridCoord(4, 4), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim80.setTile(GridCoord(4, 5), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))

        // Center spawner (5,1) IRON_ORE -> (5,2) Smelter -> (5,3) Press -> (5,4) Splitter(N->E,W):
        // West -> (4,4)? Wait (4,4) is occupied! Put Splitter at (5,5) -> West to (4,5)? Wait put Circuit Assembler at (4,6), Motor at (5,9)!
        sim80.setTile(GridCoord(5, 2), TileEntity.Smelter(Direction.NORTH, Direction.SOUTH))
        sim80.setTile(GridCoord(5, 3), TileEntity.Press(Direction.NORTH, Direction.SOUTH))
        sim80.setTile(GridCoord(5, 4), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim80.setTile(GridCoord(5, 5), TileEntity.Splitter(Direction.NORTH, Direction.EAST, Direction.WEST))
        // West of (5,5) -> (4,5)? Wait (4,5) is Copper Wire! Use Bridge at (4,5) OR put Circuit Assembler at (4,5) and route (4,4) Wire into NORTH, (5,5) Plate into EAST!
        sim80.setTile(GridCoord(4, 5), TileEntity.Assembler(Direction.NORTH, Direction.EAST, Direction.SOUTH))
        // Circuit at (4,5) outputs SOUTH -> (4,6)->(4,7)->(4,8)->(4,9)->(5,9) Motor Assembler!
        sim80.setTile(GridCoord(4, 6), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim80.setTile(GridCoord(4, 7), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim80.setTile(GridCoord(4, 8), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim80.setTile(GridCoord(4, 9), TileEntity.Belt(Direction.NORTH, Direction.EAST))

        // East of (5,5) -> (6,5) Belt(W->S) -> (6,6) Belt(N->S) -> (6,7) Splitter(N->E,S)? No, we need GEAR for Motor Assembler (5,9)!
        // Split (6,5) -> (7,5) Belt(W->S) -> (7,6) Belt(N->S) -> (7,7) Belt(N->W) and (6,5)->(6,6)->(6,7) Assembler(N,E->S) -> GEAR!
        sim80.setTile(GridCoord(6, 5), TileEntity.Splitter(Direction.WEST, Direction.EAST, Direction.SOUTH))
        sim80.setTile(GridCoord(7, 5), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        sim80.setTile(GridCoord(7, 6), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim80.setTile(GridCoord(7, 7), TileEntity.Belt(Direction.NORTH, Direction.WEST))
        sim80.setTile(GridCoord(6, 6), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim80.setTile(GridCoord(6, 7), TileEntity.Assembler(Direction.NORTH, Direction.EAST, Direction.SOUTH))
        sim80.setTile(GridCoord(6, 8), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim80.setTile(GridCoord(6, 9), TileEntity.Belt(Direction.NORTH, Direction.WEST))

        // Motor Assembler at (5,9) -> (5,10) -> (5,11) Motor Hatch!
        sim80.setTile(GridCoord(5, 9), TileEntity.Assembler(Direction.WEST, Direction.EAST, Direction.SOUTH))
        sim80.setTile(GridCoord(5, 10), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))

        // Right Spawner (8,1) COPPER_ORE -> (8,2) Smelter -> (8,3) Press -> (8,4) Belt(N->S) -> (8,5) Belt(N->S) -> (8,6) Belt(N->S) -> (8,7) Belt(N->S) -> (8,8) Assembler? Wait we need IRON_PLATE + COPPER_WIRE for Circuit Hatch (8,11)!
        // Let's split Center Plate at (5,4) EAST to (6,4) -> (7,4) -> (7,3) ... or simpler: (5,5) Splitter sends Plate EAST to (6,5) -> (7,5) -> (7,6) -> (7,7) -> (8,7)!!
        // Wait: (8,1) Copper -> (8,2) Smelter -> (8,3) Press -> (8,4) -> (8,5) -> (8,6) -> (8,7) -> (8,8)?
        // Actually look at (5,4): instead of Belt, put Splitter(N->E,S) at (5,4)!
        // (5,4) EAST -> (6,4) is Rock! Put Splitter at (5,3) -> (6,3) Belt(W->E) -> (7,3) Belt(W->S) -> (7,4) Belt(N->S) -> (7,5) ...
        // Let's route (5,4) Plate: use (5,3) Splitter(N->E,S) -> (6,3) Belt(W->E) -> (7,3) Belt(W->S) -> (7,4) Belt(N->E) -> (8,4)? Wait (8,4) has Copper Wire!
        // Put Circuit Assembler at (8,7) with NORTH = Copper Wire and WEST = Iron Plate from (7,7)!
        // Wait: (7,5) and (7,6) were used for Gear arm! Let's shift Gear arm one column left? No, (6,6) & (7,6). Instead route Plate for (8,7) along row 3->4->5? Wait (7,4) -> (7,5) Bridge!
        // Let's check: (5,2) Smelter -> (5,3) Press -> (5,4) Belt(N->S) -> (5,5) Splitter(N->E,W).
        // Put (5,3) Press, (5,4) Splitter(N->E? (6,4) is Rock!). Put (5,2) Press? No, (5,2) Smelter -> (5,3) Splitter? No, must Press first: (5,2) Smelter, (5,3) Press(N->E) -> wait we need BOTH South and East from (5,3)!
        // So (5,2) Smelter(N->S) -> (5,3) Press(N->S) -> wait (5,4) has (6,4) Rock on East, (5,3) has (6,3) OPEN on East!
        // Can (5,3) be Press and (6,3) be Belt? Only if Press outputs East, not both!
        // Look at (6,2): OPEN! Put (5,2) Smelter(N->S), (5,3) Press(N->S), (5,4) Belt(N->S), (5,5) Splitter(N->E,W) -> East (6,5) -> (7,5) -> (8,5)!!
        // Wait: if (8,7) is Circuit Assembler, and (7,7) is Gear belt, use a Bridge at (7,5) or route Circuit Assembler at (7,10)?
        // Even cleaner: use (7,8) for (8,7) -> (8,8) -> (8,9)? Remember (8,9) is Rock! Look at Level 80 rocks: `(3,4), (6,4), (3,8), (6,8), (1,9), (8,9)`.
        // Aha! (8,9) is a Rock, so (8,11) Circuit Hatch must be entered from (7,11) WEST or (8,10) NORTH -> (8,10) must be entered from (7,10) WEST!
        // Look how good that is that we tested Level 80! Let's route the Circuit Assembler cleanly at (7,10) outputting EAST -> (8,10) -> (8,11)!
        // Re-check East side cleanly:
        // (8,1) COPPER_ORE -> (8,2) Smelter(N->S) -> (8,3) Press(N->S) -> (8,4) -> (8,5) -> (8,6) -> (8,7) -> (8,8) -> (7,8)?
        // And Plate from (5,5) Splitter EAST -> (6,5) Splitter(W->E,S):
        //   - SOUTH of (6,5): Gear arm (6,6) Splitter(N->E,S) -> East (7,6)->(7,7)->(6,7) Gear Assembler, South (6,7) -> wait (6,8) is Rock!
        //   Aha! (6,8) is Rock too! That's why Lv 80 is a real puzzle!
        //   Let's route Gear Assembler at (6,6) or (7,7) outputting WEST to (6,7) -> (5,7) -> (5,8) -> (5,9) Motor Assembler!
        //   Let's trace:
        //   - (5,5) Splitter(N -> E, W):
        //     - W -> (4,5) Circuit Assembler (outputs SOUTH -> (4,6) -> (4,7) -> (4,8) -> (4,9) -> (5,9) Motor!)
        //     - E -> (6,5) Belt(W->E) -> (7,5) Splitter(W -> N? No, S & E!):
        //       - Wait: from (7,5) Splitter(W -> S, E):
        //         - E -> (8,5) -> wait (8,4) is Copper Wire! Let's use (7,5) Bridge? Or put Copper Wire down col 8:
        //           (8,2) Smelter -> (8,3) Press -> (8,4) Belt(N->S) -> (8,5) Bridge(E, S) -> (8,6) -> (8,7) -> (8,8) Belt(N->W) -> (7,8) Belt(E->S) -> (7,9) Belt(N->S) -> (7,10) Circuit Assembler(N, W -> E) -> (8,10) Belt(W->S) -> (8,11) Hatch!
        //           Wait: (8,5) Bridge(E, S) receives Copper Wire from NORTH -> goes SOUTH to (8,6)! Where does EAST go? Nowhere unless (9,5) is open! Yes, col 9 IS open (`width = 10`, cols `0..9`)!
        //           So (9,5) -> (9,6) -> (9,7) -> (9,8) -> (9,9) -> (9,10) -> wait (8,10) is output, so (9,7)->(9,8)->(9,9)->(8,9 Rock)->(7,9)!
        //           Even simpler: keep Gear arm at cols 6..7 and put Circuit at (7,10) by splitting (5,5) at (6,5) NORTH? Wait (6,4) is Rock.
        //           What about (6,5) Splitter(W -> E, S):
        //           - S -> (6,6) Belt(N->E) -> (7,6) Belt(W->S) -> (7,7) Splitter(N -> E, W):
        //             - W -> (6,7) Belt(E->S? No, 6,8 is Rock!) -> wait (6,7) -> (5,7) Belt(E->S)!
        //             Let's place Gear Assembler at (5,7) (`inputDirA = NORTH? No, 5,6 is open!`, `inputDirB = EAST` from (6,7))!
        //             Look! (6,5) Splitter(W -> E, S):
        //             - S -> (6,6) Belt(N->S) -> (6,7) Belt(N->W) -> (5,7) Assembler EAST input!
        //             - E -> (7,5) Belt(W->S) -> (7,6) Belt(N->W) -> wait (6,6) is taken, so (7,6)->(7,7) -> or (6,5) Belt(W->S) -> (6,6) Splitter(N->W,S) -> W (5,6)->(5,7) North input, S -> (6,7)->(5,7) East input!
        //             WAIT: (6,6) Splitter(N -> W, S) -> W=(5,6)->(5,7) AND S=(6,7)->(5,7) -> Gear Assembler at (5,7) -> (5,8) -> (5,9) Motor Assembler!
        //             AND we STILL need Iron Plate at (7,10) for Circuit Hatch (8,11)!
        //             Take it from (6,5) Splitter(W -> E, S):
        //             - S goes to (6,6) Gear Splitter!
        //             - E goes to (7,5) -> (7,6) -> (7,7) -> (7,8) -> (7,9) -> (7,10) Circuit Assembler(N, E -> S? No, 7,10->8,10->8,11)!
        //               Put Circuit Assembler at (7,10) with `N` = Iron Plate from (7,9), `E` = Copper Wire from (8,8)->(9,8)->(9,9)->(9,10)->(8,10)? No, (8,10) is Hatch output!
        //               Put `E` = Copper Wire entering (7,10) via (8,8) -> (7,8) -> wait (7,8) is Plate! Use Bridge at (7,8) (`horizDir = WEST, vertDir = SOUTH`):
        //               - Plate goes vertically (7,7)->(7,8)->(7,9)->(7,10) North input!
        //               - Wire goes from (8,8) WEST -> (7,8) Bridge -> (6,8 is Rock!)... Oops (6,8) is Rock!
        //               Put Bridge at (7,9) (`horizDir = WEST, vertDir = SOUTH`):
        //               - Wait (7,9) -> (6,9) is open! Wire goes (8,8)->(8,7)... wait (8,8)->(7,8)? Let's put Plate at (7,8)->(7,9)->(7,10), and Wire from (8,8) -> (9,8) -> (9,9) -> (9,10) -> (9,11) -> (8,11)? No, Circuit Assembler needs to be BEFORE (8,11)!
        //               Put Circuit Assembler at (8,10) (`N`? 8,9 is Rock! `W` = (7,10), `E` = (9,10), `S` = (8,11) Hatch!!)!
        //               LOOK HOW CLEAN THAT IS!!
        //               Circuit Assembler at (8,10) (`inputDirA = WEST`, `inputDirB = EAST`, `outputDir = SOUTH`) outputs DIRECTLY into (8,11) Hatch!!
        //               - Plate goes (7,5)->(7,6)->(7,7)->(7,8)->(7,9)->(7,10) -> WEST input of (8,10)!
        //               - Wire goes (8,2) Smelter -> (8,3) Press -> (8,4)->(8,5)->(8,6)->(8,7)->(8,8)->(9,8)->(9,9)->(9,10) -> EAST input of (8,10)!
        sim80.setTile(GridCoord(6, 5), TileEntity.Splitter(Direction.WEST, Direction.SOUTH, Direction.EAST))
        sim80.setTile(GridCoord(6, 6), TileEntity.Splitter(Direction.NORTH, Direction.WEST, Direction.SOUTH))
        sim80.setTile(GridCoord(5, 6), TileEntity.Belt(Direction.EAST, Direction.SOUTH))
        sim80.setTile(GridCoord(6, 7), TileEntity.Belt(Direction.NORTH, Direction.WEST))
        sim80.setTile(GridCoord(5, 7), TileEntity.Assembler(Direction.NORTH, Direction.EAST, Direction.SOUTH))
        sim80.setTile(GridCoord(5, 8), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))

        sim80.setTile(GridCoord(7, 5), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        for (y in 6..9) sim80.setTile(GridCoord(7, y), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim80.setTile(GridCoord(7, 10), TileEntity.Belt(Direction.NORTH, Direction.EAST))

        sim80.setTile(GridCoord(8, 2), TileEntity.Smelter(Direction.NORTH, Direction.SOUTH))
        sim80.setTile(GridCoord(8, 3), TileEntity.Press(Direction.NORTH, Direction.SOUTH))
        for (y in 4..7) sim80.setTile(GridCoord(8, y), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim80.setTile(GridCoord(8, 8), TileEntity.Belt(Direction.NORTH, Direction.EAST))
        sim80.setTile(GridCoord(9, 8), TileEntity.Belt(Direction.WEST, Direction.SOUTH))
        sim80.setTile(GridCoord(9, 9), TileEntity.Belt(Direction.NORTH, Direction.SOUTH))
        sim80.setTile(GridCoord(9, 10), TileEntity.Belt(Direction.NORTH, Direction.WEST))
        sim80.setTile(GridCoord(8, 10), TileEntity.Assembler(Direction.WEST, Direction.EAST, Direction.SOUTH))

        // Wait: Motor Assembler at (5,9) receives Circuit from (4,9) WEST, Gear from (5,8) NORTH -> wait Motor Assembler (5,9) had (WEST, EAST, SOUTH)!
        // Let's update (5,9) Assembler to (WEST, NORTH, SOUTH) so it takes Circuit from (4,9) WEST and Gear from (5,8) NORTH!
        sim80.setTile(GridCoord(5, 9), TileEntity.Assembler(Direction.WEST, Direction.NORTH, Direction.SOUTH))

        repeat(400) { if (!sim80.isComplete) sim80.tick() }
        assertTrue("Level 80 should complete", sim80.isComplete)
    }

    @Test
    fun levelProgressionUnlocksSequentiallyAndBlocksLockedLevels() {
        assertEquals(1, LevelRepository.highestUnlockedLevelId(emptySet()))
        assertTrue(LevelRepository.isLevelUnlocked(1, emptySet()))
        assertFalse(LevelRepository.isLevelUnlocked(2, emptySet()))

        val completed1And2 = setOf(1, 2)
        assertEquals(3, LevelRepository.highestUnlockedLevelId(completed1And2))
        assertTrue(LevelRepository.isLevelUnlocked(3, completed1And2))
        assertFalse(LevelRepository.isLevelUnlocked(4, completed1And2))

        // Skipping ahead does not unlock levels past an incomplete predecessor
        val skipped = setOf(1, 5)
        assertEquals(2, LevelRepository.highestUnlockedLevelId(skipped))
        assertFalse(LevelRepository.isLevelUnlocked(5, skipped))

        val allStandardCompleted = (1..80).toSet()
        assertEquals(80, LevelRepository.highestUnlockedLevelId(allStandardCompleted))
        assertTrue(LevelRepository.isLevelUnlocked(80, allStandardCompleted))

        // Expert Mode unlocks independently starting at Level 81
        assertEquals(81, LevelRepository.highestUnlockedExpertLevelId(emptySet()))
        assertTrue(LevelRepository.isLevelUnlocked(81, emptySet()))
        assertFalse(LevelRepository.isLevelUnlocked(82, emptySet()))

        val completedExpert = setOf(81, 82)
        assertEquals(83, LevelRepository.highestUnlockedExpertLevelId(completedExpert))
        assertTrue(LevelRepository.isLevelUnlocked(83, completedExpert))
        assertFalse(LevelRepository.isLevelUnlocked(84, completedExpert))

        val allExpertCompleted = (81..140).toSet()
        assertEquals(140, LevelRepository.highestUnlockedExpertLevelId(allExpertCompleted))
        assertTrue(LevelRepository.isLevelUnlocked(140, allExpertCompleted))

        assertEquals(null, LevelRepository.nextLevelIdInMode(LevelRepository.getLevel(80)))
        assertEquals(82, LevelRepository.nextLevelIdInMode(LevelRepository.getLevel(81)))
        assertEquals(null, LevelRepository.nextLevelIdInMode(LevelRepository.getLevel(140)))
    }

    @Test
    fun expertModeHas60LevelsWithNoHintsOrHelp() {
        assertEquals(80, LevelRepository.standardLevels.size)
        assertEquals(60, LevelRepository.expertLevels.size)
        assertEquals(140, LevelRepository.levels.size)

        for (i in 0 until 60) {
            val level = LevelRepository.expertLevels[i]
            assertEquals(81 + i, level.id)
            assertTrue("Level ${level.id} must be expert", level.isExpert)
            assertEquals("Level ${level.id} must have no hint", "", level.hint)
            assertEquals("Level ${level.id} must have no mechanic badge", null, level.newMechanicBadge)
        }
    }

    @Test
    fun helpAdvisorProducesValidStepsAndOpenPathBlueprints() {
        for (levelId in listOf(1, 10, 31, 48, 80)) {
            val level = LevelRepository.getLevel(levelId)
            val blueprint = LevelHelpAdvisor.buildBlueprint(level)
            assertTrue("Level $levelId should produce crafting steps", blueprint.craftingSteps.isNotEmpty())
            assertTrue("Level $levelId should produce suggested path", blueprint.suggestedPathCoords.isNotEmpty())
            for (coord in blueprint.suggestedPathCoords) {
                assertTrue(coord.x in 0 until level.width && coord.y in 0 until level.height)
                assertFalse(coord in level.rocks)
            }
        }
    }
}
