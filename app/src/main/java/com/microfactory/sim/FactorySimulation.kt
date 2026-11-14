package com.microfactory.sim

data class MachineStatus(
    val inputCount: Int,
    val maxInputs: Int,
    val progress: Float,
    val isProcessing: Boolean,
    val hasOutputReady: Boolean,
    val slotAFilled: Boolean = false,
    val slotBFilled: Boolean = false
)

/**
 * Pure Kotlin factory grid simulation running at a fixed base tick rate (4 ticks/sec).
 * Uses two-pass tick resolution so conveyor trains advance in lockstep without overlaps or teleportation.
 */
class FactorySimulation(
    width: Int = 8,
    height: Int = 10
) {
    var width: Int = width
        private set
    var height: Int = height
        private set

    companion object {
        const val BASE_TICKS_PER_SECOND = 4.0
        const val TICK_DURATION_NANOS = 250_000_000L
        private const val IPM_WINDOW_TICKS = 240L // 60 simulated seconds at 4 ticks/sec

        private const val LANE_NORMAL = 0
        private const val LANE_BRIDGE_HORIZ = 1
        private const val LANE_BRIDGE_VERT = 2

        fun pressOutputFor(input: ItemType): ItemType? = when (input) {
            ItemType.IRON_INGOT -> ItemType.IRON_PLATE
            ItemType.COPPER_INGOT -> ItemType.COPPER_WIRE
            else -> null
        }

        fun assemblerOutputFor(a: ItemType, b: ItemType): ItemType? = when {
            a == ItemType.IRON_PLATE && b == ItemType.IRON_PLATE -> ItemType.GEAR
            (a == ItemType.IRON_PLATE && b == ItemType.COPPER_WIRE) ||
                (a == ItemType.COPPER_WIRE && b == ItemType.IRON_PLATE) -> ItemType.CIRCUIT
            (a == ItemType.GEAR && b == ItemType.CIRCUIT) ||
                (a == ItemType.CIRCUIT && b == ItemType.GEAR) -> ItemType.MOTOR
            else -> null
        }

        fun isAssemblerIngredient(type: ItemType): Boolean = when (type) {
            ItemType.IRON_PLATE, ItemType.COPPER_WIRE, ItemType.GEAR, ItemType.CIRCUIT -> true
            else -> false
        }
    }

    private var grid: Array<Array<TileEntity>> =
        Array(height) { Array(width) { TileEntity.Empty } }

    private val activeItems = mutableListOf<MovingItem>()
    private val recentConsumedItems = mutableListOf<MovingItem>()
    private val bridgeVerticalItems = HashSet<Long>()

    private val spawnerCooldowns = mutableMapOf<GridCoord, Int>()
    private val spawnerSecondaryTurn = mutableMapOf<GridCoord, Boolean>()
    private val singleMachines = mutableMapOf<GridCoord, SingleMachineState>()
    private val assemblers = mutableMapOf<GridCoord, AssemblerState>()
    private val deliveryHistoryTicks = ArrayDeque<Long>()

    private var nextItemId = 1L
    private var loadedLevel: LevelConfig? = null

    var tickCount: Long = 0L
        private set

    val items: List<MovingItem>
        get() = activeItems

    val consumedVisualItems: List<MovingItem>
        get() = recentConsumedItems

    val elapsedSeconds: Double
        get() = tickCount / BASE_TICKS_PER_SECOND

    val rollingIpm: Double
        get() {
            if (tickCount <= 0L) return 0.0
            val windowTicks = tickCount.coerceIn(4L, IPM_WINDOW_TICKS)
            val windowSeconds = windowTicks / BASE_TICKS_PER_SECOND
            val cutoff = tickCount - IPM_WINDOW_TICKS
            while (deliveryHistoryTicks.isNotEmpty() && deliveryHistoryTicks.first() <= cutoff) {
                deliveryHistoryTicks.removeFirst()
            }
            return deliveryHistoryTicks.size * (60.0 / windowSeconds)
        }

    val beltCount: Int
        get() {
            var count = 0
            for (y in 0 until height) {
                for (x in 0 until width) {
                    if (grid[y][x] is TileEntity.Belt) count++
                }
            }
            return count
        }

    val isComplete: Boolean
        get() {
            var hasHatch = false
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val tile = grid[y][x]
                    if (tile is TileEntity.Hatch) {
                        hasHatch = true
                        if (tile.deliveredCount < tile.targetCount) return false
                    }
                }
            }
            return hasHatch
        }

    private data class OccupantSlot(val coord: GridCoord, val lane: Int)

    private data class SingleMachineState(
        var inputItem: ItemType? = null,
        var outputItem: ItemType? = null,
        var ticksDone: Int = 0,
        var totalTicks: Int = 2,
        var enteredThisTick: Boolean = false
    )

    private data class AssemblerState(
        var slotAItem: ItemType? = null,
        var slotBItem: ItemType? = null,
        var outputItem: ItemType? = null,
        var ticksDone: Int = 0,
        var totalTicks: Int = 3,
        var isProcessing: Boolean = false,
        var enteredThisTick: Boolean = false
    )

    private sealed interface PlannedAction {
        data class MoveToTile(
            val target: GridCoord,
            val targetLane: Int,
            val splitterUsedDir: Direction?
        ) : PlannedAction

        data class EnterHatch(val target: GridCoord) : PlannedAction
        data class EnterIncinerator(val target: GridCoord) : PlannedAction
        data class EnterMachine(val target: GridCoord, val slot: Int) : PlannedAction
    }

    fun isItemOnVerticalBridgeLane(itemId: Long): Boolean = itemId in bridgeVerticalItems

    fun inBounds(coord: GridCoord): Boolean =
        coord.x in 0 until width && coord.y in 0 until height

    fun getTile(coord: GridCoord): TileEntity =
        if (inBounds(coord)) grid[coord.y][coord.x] else TileEntity.Rock

    fun setTile(coord: GridCoord, tile: TileEntity) {
        if (!inBounds(coord)) return
        grid[coord.y][coord.x] = when (tile) {
            is TileEntity.Hatch -> tile.copy()
            is TileEntity.Splitter -> tile.copy()
            else -> tile
        }
        val removedIds = activeItems.filter { it.currentCoord == coord }.map { it.id }
        bridgeVerticalItems.removeAll(removedIds.toSet())
        activeItems.removeAll { it.currentCoord == coord }
        recentConsumedItems.removeAll { it.currentCoord == coord }
        spawnerCooldowns.remove(coord)
        spawnerSecondaryTurn.remove(coord)
        singleMachines.remove(coord)
        assemblers.remove(coord)
    }

    fun setFilterItem(coord: GridCoord, itemType: ItemType): Boolean {
        if (!inBounds(coord)) return false
        val current = grid[coord.y][coord.x] as? TileEntity.Filter ?: return false
        grid[coord.y][coord.x] = current.copy(filterItem = itemType)
        return true
    }

    fun rotateTile(coord: GridCoord): Boolean {
        if (!inBounds(coord)) return false
        val current = grid[coord.y][coord.x]
        if (!current.isPlayerEditable()) return false

        val rotated = when (current) {
            is TileEntity.Belt -> computeSmoothBeltRotation(coord, current)
            else -> current.rotatedClockwise()
        }
        grid[coord.y][coord.x] = rotated
        singleMachines.remove(coord)
        assemblers.remove(coord)

        val outDir = when (rotated) {
            is TileEntity.Belt -> rotated.outputDir
            is TileEntity.Smelter -> rotated.outputDir
            is TileEntity.Press -> rotated.outputDir
            is TileEntity.Assembler -> rotated.outputDir
            else -> null
        }
        if (outDir != null) {
            autoConnectDownstreamBelt(coord, outDir)
        }
        return true
    }

    /**
     * Smoothly snaps a downstream Belt's inputDir to receive from [fromCoord] if that belt's
     * current input side is not already fed by another neighbor.
     */
    fun autoConnectDownstreamBelt(fromCoord: GridCoord, outDir: Direction) {
        val nextCoord = fromCoord + outDir
        if (!inBounds(nextCoord)) return
        val nextTile = grid[nextCoord.y][nextCoord.x] as? TileEntity.Belt ?: return
        val incomingSide = outDir.opposite()
        if (nextTile.inputDir == incomingSide || nextTile.outputDir == incomingSide) return
        if (!hasOutputToward(nextCoord + nextTile.inputDir, nextCoord)) {
            grid[nextCoord.y][nextCoord.x] = nextTile.copy(inputDir = incomingSide)
        }
    }

    private fun computeSmoothBeltRotation(coord: GridCoord, current: TileEntity.Belt): TileEntity.Belt {
        val incomingFeeders = Direction.entries.filter { dir ->
            hasOutputToward(coord + dir, coord)
        }
        val outgoingTargets = Direction.entries.filter { dir ->
            canAcceptOutputFrom(coord + dir, coord)
        }

        if (incomingFeeders.isNotEmpty()) {
            val candidates = buildList {
                for (inDir in incomingFeeders) {
                    add(TileEntity.Belt(inDir, inDir.rotateClockwise()))
                    add(TileEntity.Belt(inDir, inDir.opposite()))
                    add(TileEntity.Belt(inDir, inDir.rotateCounterClockwise()))
                }
            }
            val idx = candidates.indexOf(current)
            if (idx >= 0) {
                return candidates[(idx + 1) % candidates.size]
            }
            val primaryIn = incomingFeeders.first()
            val preferredOut = outgoingTargets.firstOrNull { it != primaryIn } ?: primaryIn.opposite()
            return TileEntity.Belt(primaryIn, preferredOut)
        }

        if (outgoingTargets.isNotEmpty()) {
            val candidates = buildList {
                for (outDir in outgoingTargets) {
                    add(TileEntity.Belt(outDir.rotateClockwise(), outDir))
                    add(TileEntity.Belt(outDir.opposite(), outDir))
                    add(TileEntity.Belt(outDir.rotateCounterClockwise(), outDir))
                }
            }
            val idx = candidates.indexOf(current)
            if (idx >= 0) {
                return candidates[(idx + 1) % candidates.size]
            }
            val primaryOut = outgoingTargets.first()
            return TileEntity.Belt(primaryOut.opposite(), primaryOut)
        }

        return current.rotatedClockwise() as TileEntity.Belt
    }

    private fun hasOutputToward(neighborCoord: GridCoord, targetCoord: GridCoord): Boolean {
        if (!inBounds(neighborCoord)) return false
        val expectedDir = neighborCoord.directionTo(targetCoord) ?: return false
        return when (val t = grid[neighborCoord.y][neighborCoord.x]) {
            is TileEntity.Spawner -> t.outputDir == expectedDir
            is TileEntity.Belt -> t.outputDir == expectedDir
            is TileEntity.Smelter -> t.outputDir == expectedDir
            is TileEntity.Press -> t.outputDir == expectedDir
            is TileEntity.Assembler -> t.outputDir == expectedDir
            is TileEntity.Splitter -> t.outputLeft == expectedDir || t.outputRight == expectedDir
            is TileEntity.Filter -> t.matchDir == expectedDir || t.otherDir == expectedDir
            is TileEntity.Overflow -> t.primaryDir == expectedDir || t.overflowDir == expectedDir
            is TileEntity.Bridge -> t.horizDir == expectedDir || t.vertDir == expectedDir
            else -> false
        }
    }

    private fun canAcceptOutputFrom(targetCoord: GridCoord, sourceCoord: GridCoord): Boolean {
        if (!inBounds(targetCoord)) return false
        val sideFromSource = targetCoord.directionTo(sourceCoord) ?: return false
        val moveDir = sideFromSource.opposite()
        return when (val t = grid[targetCoord.y][targetCoord.x]) {
            is TileEntity.Hatch, TileEntity.Incinerator -> true
            is TileEntity.Belt -> t.outputDir != sideFromSource
            is TileEntity.Splitter -> t.inputDir == sideFromSource
            is TileEntity.Filter -> t.inputDir == sideFromSource
            is TileEntity.Overflow -> t.inputDir == sideFromSource
            is TileEntity.Bridge -> t.horizDir == moveDir || t.vertDir == moveDir
            is TileEntity.Smelter -> t.inputDir == sideFromSource
            is TileEntity.Press -> t.inputDir == sideFromSource
            is TileEntity.Assembler -> t.inputDirA == sideFromSource || t.inputDirB == sideFromSource
            else -> false
        }
    }

    fun clearTile(coord: GridCoord): Boolean {
        if (!inBounds(coord)) return false
        val current = grid[coord.y][coord.x]
        if (!current.isPlayerEditable()) return false
        setTile(coord, TileEntity.Empty)
        return true
    }

    fun loadLevel(level: LevelConfig) {
        if (level.width != width || level.height != height) {
            width = level.width
            height = level.height
            grid = Array(height) { Array(width) { TileEntity.Empty } }
        }
        loadedLevel = level
        resetBoard()
    }

    fun resetBoard() {
        for (y in 0 until height) {
            for (x in 0 until width) {
                grid[y][x] = TileEntity.Empty
            }
        }
        activeItems.clear()
        recentConsumedItems.clear()
        bridgeVerticalItems.clear()
        spawnerCooldowns.clear()
        spawnerSecondaryTurn.clear()
        singleMachines.clear()
        assemblers.clear()
        deliveryHistoryTicks.clear()
        tickCount = 0L
        nextItemId = 1L

        val level = loadedLevel ?: return
        for (rock in level.rocks) {
            if (inBounds(rock)) grid[rock.y][rock.x] = TileEntity.Rock
        }
        for (spawner in level.spawners) {
            if (inBounds(spawner.coord)) {
                grid[spawner.coord.y][spawner.coord.x] = spawner.tile
            }
        }
        for (hatch in level.hatches) {
            if (inBounds(hatch.coord)) {
                val h = hatch.tile as? TileEntity.Hatch
                if (h != null) {
                    grid[hatch.coord.y][hatch.coord.x] = h.copy(deliveredCount = 0)
                }
            }
        }
    }

    fun getHatches(): List<PlacedTile> {
        val result = mutableListOf<PlacedTile>()
        for (y in 0 until height) {
            for (x in 0 until width) {
                val tile = grid[y][x]
                if (tile is TileEntity.Hatch) {
                    result.add(PlacedTile(GridCoord(x, y), tile))
                }
            }
        }
        return result
    }

    fun getMachineStatus(coord: GridCoord): MachineStatus? {
        return when (getTile(coord)) {
            is TileEntity.Smelter, is TileEntity.Press -> {
                val state = singleMachines[coord] ?: return MachineStatus(0, 1, 0f, false, false)
                val progress = if (state.totalTicks > 0 && state.inputItem != null) {
                    (state.ticksDone.toFloat() / state.totalTicks).coerceIn(0f, 1f)
                } else 0f
                MachineStatus(
                    inputCount = if (state.inputItem != null) 1 else 0,
                    maxInputs = 1,
                    progress = if (state.outputItem != null) 1f else progress,
                    isProcessing = state.inputItem != null && state.outputItem == null,
                    hasOutputReady = state.outputItem != null
                )
            }
            is TileEntity.Assembler -> {
                val state = assemblers[coord] ?: return MachineStatus(0, 2, 0f, false, false)
                val count = (if (state.slotAItem != null) 1 else 0) + (if (state.slotBItem != null) 1 else 0)
                val progress = if (state.totalTicks > 0 && state.isProcessing) {
                    (state.ticksDone.toFloat() / state.totalTicks).coerceIn(0f, 1f)
                } else 0f
                MachineStatus(
                    inputCount = count,
                    maxInputs = 2,
                    progress = if (state.outputItem != null) 1f else progress,
                    isProcessing = state.isProcessing,
                    hasOutputReady = state.outputItem != null,
                    slotAFilled = state.slotAItem != null,
                    slotBFilled = state.slotBItem != null
                )
            }
            else -> null
        }
    }

    private fun slotForItem(item: MovingItem): OccupantSlot {
        val lane = if (getTile(item.currentCoord) is TileEntity.Bridge) {
            if (item.id in bridgeVerticalItems) LANE_BRIDGE_VERT else LANE_BRIDGE_HORIZ
        } else {
            LANE_NORMAL
        }
        return OccupantSlot(item.currentCoord, lane)
    }

    /**
     * Advances the simulation by 1 tick using two-pass movement resolution followed by machine & spawner updates.
     */
    fun tick() {
        tickCount++
        recentConsumedItems.clear()

        for (item in activeItems) {
            item.prevCoord = item.currentCoord
        }

        val occupantAt = HashMap<OccupantSlot, MovingItem>(activeItems.size * 2)
        for (item in activeItems) {
            occupantAt[slotForItem(item)] = item
        }

        // Pass 1: Back-to-front recursive planning & cell/lane reservation.
        val plannedMoves = HashMap<Long, PlannedAction>()
        val reservedTileSlots = HashSet<OccupantSlot>()
        val reservedMachineSlots = HashMap<Pair<GridCoord, Int>, ItemType>()
        val reservedHatchDeliveries = HashMap<GridCoord, Int>()
        val decision = HashMap<OccupantSlot, Boolean>()
        val visiting = HashSet<OccupantSlot>()

        fun canSmelterAccept(coord: GridCoord, type: ItemType): Boolean {
            if (type != ItemType.IRON_ORE && type != ItemType.COPPER_ORE) return false
            val state = singleMachines[coord] ?: return true
            return state.inputItem == null && state.outputItem == null
        }

        fun canPressAccept(coord: GridCoord, type: ItemType): Boolean {
            if (pressOutputFor(type) == null) return false
            val state = singleMachines[coord] ?: return true
            return state.inputItem == null && state.outputItem == null
        }

        fun canAssemblerAccept(coord: GridCoord, fromCoord: GridCoord, tile: TileEntity.Assembler, type: ItemType): Int? {
            if (!isAssemblerIngredient(type)) return null
            val state = assemblers[coord] ?: AssemblerState(totalTicks = tile.processTicks)
            if (state.outputItem != null || state.isProcessing) return null

            val effectiveA = state.slotAItem ?: reservedMachineSlots[coord to 0]
            val effectiveB = state.slotBItem ?: reservedMachineSlots[coord to 1]

            if (fromCoord == coord + tile.inputDirA && effectiveA == null) {
                if (effectiveB == null || assemblerOutputFor(type, effectiveB) != null) {
                    return 0
                }
            }
            if (fromCoord == coord + tile.inputDirB && effectiveB == null) {
                if (effectiveA == null || assemblerOutputFor(effectiveA, type) != null) {
                    return 1
                }
            }
            return null
        }

        fun resolveMove(slot: OccupantSlot): Boolean {
            decision[slot]?.let { return it }
            if (slot in visiting) return false

            val item = occupantAt[slot] ?: return true
            val pos = slot.coord
            val tile = getTile(pos)
            val candidateDirs = when (tile) {
                is TileEntity.Belt -> listOf(tile.outputDir)
                is TileEntity.Splitter -> if (tile.toggleLeft) {
                    listOf(tile.outputLeft, tile.outputRight)
                } else {
                    listOf(tile.outputRight, tile.outputLeft)
                }
                is TileEntity.Filter -> if (item.type == tile.filterItem) {
                    listOf(tile.matchDir)
                } else {
                    listOf(tile.otherDir)
                }
                is TileEntity.Overflow -> listOf(tile.primaryDir, tile.overflowDir)
                is TileEntity.Bridge -> if (slot.lane == LANE_BRIDGE_VERT) {
                    listOf(tile.vertDir)
                } else {
                    listOf(tile.horizDir)
                }
                else -> {
                    decision[slot] = false
                    return false
                }
            }

            visiting.add(slot)

            for (outDir in candidateDirs) {
                val target = pos + outDir
                if (!inBounds(target)) continue
                val targetTile = getTile(target)

                when (targetTile) {
                    is TileEntity.Hatch -> {
                        val reserved = reservedHatchDeliveries[target] ?: 0
                        if (item.type == targetTile.requiredItem &&
                            targetTile.deliveredCount + reserved < targetTile.targetCount
                        ) {
                            reservedHatchDeliveries[target] = reserved + 1
                            plannedMoves[item.id] = PlannedAction.EnterHatch(target)
                            visiting.remove(slot)
                            decision[slot] = true
                            return true
                        }
                    }

                    TileEntity.Incinerator -> {
                        plannedMoves[item.id] = PlannedAction.EnterIncinerator(target)
                        visiting.remove(slot)
                        decision[slot] = true
                        return true
                    }

                    is TileEntity.Smelter -> {
                        if (pos == target + targetTile.inputDir &&
                            canSmelterAccept(target, item.type) &&
                            (target to 0) !in reservedMachineSlots
                        ) {
                            reservedMachineSlots[target to 0] = item.type
                            plannedMoves[item.id] = PlannedAction.EnterMachine(target, 0)
                            visiting.remove(slot)
                            decision[slot] = true
                            return true
                        }
                    }

                    is TileEntity.Press -> {
                        if (pos == target + targetTile.inputDir &&
                            canPressAccept(target, item.type) &&
                            (target to 0) !in reservedMachineSlots
                        ) {
                            reservedMachineSlots[target to 0] = item.type
                            plannedMoves[item.id] = PlannedAction.EnterMachine(target, 0)
                            visiting.remove(slot)
                            decision[slot] = true
                            return true
                        }
                    }

                    is TileEntity.Assembler -> {
                        val machineSlot = canAssemblerAccept(target, pos, targetTile, item.type)
                        if (machineSlot != null) {
                            reservedMachineSlots[target to machineSlot] = item.type
                            plannedMoves[item.id] = PlannedAction.EnterMachine(target, machineSlot)
                            visiting.remove(slot)
                            decision[slot] = true
                            return true
                        }
                    }

                    is TileEntity.Belt -> {
                        val canEnter = pos != target + targetTile.outputDir
                        val targetSlot = OccupantSlot(target, LANE_NORMAL)
                        if (canEnter && targetSlot !in reservedTileSlots) {
                            val targetFree = (targetSlot !in occupantAt) || resolveMove(targetSlot)
                            if (targetFree) {
                                reservedTileSlots.add(targetSlot)
                                val splitterDir = if (tile is TileEntity.Splitter) outDir else null
                                plannedMoves[item.id] = PlannedAction.MoveToTile(target, LANE_NORMAL, splitterDir)
                                visiting.remove(slot)
                                decision[slot] = true
                                return true
                            }
                        }
                    }

                    is TileEntity.Splitter -> {
                        val canEnter = pos == target + targetTile.inputDir
                        val targetSlot = OccupantSlot(target, LANE_NORMAL)
                        if (canEnter && targetSlot !in reservedTileSlots) {
                            val targetFree = (targetSlot !in occupantAt) || resolveMove(targetSlot)
                            if (targetFree) {
                                reservedTileSlots.add(targetSlot)
                                val splitterDir = if (tile is TileEntity.Splitter) outDir else null
                                plannedMoves[item.id] = PlannedAction.MoveToTile(target, LANE_NORMAL, splitterDir)
                                visiting.remove(slot)
                                decision[slot] = true
                                return true
                            }
                        }
                    }

                    is TileEntity.Filter -> {
                        val canEnter = pos == target + targetTile.inputDir
                        val targetSlot = OccupantSlot(target, LANE_NORMAL)
                        if (canEnter && targetSlot !in reservedTileSlots) {
                            val targetFree = (targetSlot !in occupantAt) || resolveMove(targetSlot)
                            if (targetFree) {
                                reservedTileSlots.add(targetSlot)
                                val splitterDir = if (tile is TileEntity.Splitter) outDir else null
                                plannedMoves[item.id] = PlannedAction.MoveToTile(target, LANE_NORMAL, splitterDir)
                                visiting.remove(slot)
                                decision[slot] = true
                                return true
                            }
                        }
                    }

                    is TileEntity.Overflow -> {
                        val canEnter = pos == target + targetTile.inputDir
                        val targetSlot = OccupantSlot(target, LANE_NORMAL)
                        if (canEnter && targetSlot !in reservedTileSlots) {
                            val targetFree = (targetSlot !in occupantAt) || resolveMove(targetSlot)
                            if (targetFree) {
                                reservedTileSlots.add(targetSlot)
                                val splitterDir = if (tile is TileEntity.Splitter) outDir else null
                                plannedMoves[item.id] = PlannedAction.MoveToTile(target, LANE_NORMAL, splitterDir)
                                visiting.remove(slot)
                                decision[slot] = true
                                return true
                            }
                        }
                    }

                    is TileEntity.Bridge -> {
                        val targetLane = when (outDir) {
                            targetTile.horizDir -> LANE_BRIDGE_HORIZ
                            targetTile.vertDir -> LANE_BRIDGE_VERT
                            else -> -1
                        }
                        if (targetLane >= 0) {
                            val targetSlot = OccupantSlot(target, targetLane)
                            if (targetSlot !in reservedTileSlots) {
                                val targetFree = (targetSlot !in occupantAt) || resolveMove(targetSlot)
                                if (targetFree) {
                                    reservedTileSlots.add(targetSlot)
                                    val splitterDir = if (tile is TileEntity.Splitter) outDir else null
                                    plannedMoves[item.id] = PlannedAction.MoveToTile(target, targetLane, splitterDir)
                                    visiting.remove(slot)
                                    decision[slot] = true
                                    return true
                                }
                            }
                        }
                    }

                    TileEntity.Empty, TileEntity.Rock, is TileEntity.Spawner -> Unit
                }
            }

            visiting.remove(slot)
            decision[slot] = false
            return false
        }

        val sortedSlots = occupantAt.keys.sortedWith(compareBy({ it.coord.y }, { it.coord.x }, { it.lane }))
        for (slot in sortedSlots) {
            resolveMove(slot)
        }

        // Pass 2: Apply all planned moves simultaneously.
        val iterator = activeItems.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            val action = plannedMoves[item.id] ?: continue
            when (action) {
                is PlannedAction.MoveToTile -> {
                    val currentTile = getTile(item.currentCoord)
                    if (currentTile is TileEntity.Splitter && action.splitterUsedDir != null) {
                        currentTile.toggleLeft = (action.splitterUsedDir != currentTile.outputLeft)
                    }
                    if (action.targetLane == LANE_BRIDGE_VERT) {
                        bridgeVerticalItems.add(item.id)
                    } else {
                        bridgeVerticalItems.remove(item.id)
                    }
                    item.currentCoord = action.target
                }

                is PlannedAction.EnterHatch -> {
                    val hatch = getTile(action.target) as? TileEntity.Hatch
                    if (hatch != null) {
                        hatch.deliveredCount++
                        deliveryHistoryTicks.addLast(tickCount)
                    }
                    bridgeVerticalItems.remove(item.id)
                    recentConsumedItems.add(
                        item.copy(prevCoord = item.currentCoord, currentCoord = action.target)
                    )
                    iterator.remove()
                }

                is PlannedAction.EnterIncinerator -> {
                    bridgeVerticalItems.remove(item.id)
                    recentConsumedItems.add(
                        item.copy(prevCoord = item.currentCoord, currentCoord = action.target)
                    )
                    iterator.remove()
                }

                is PlannedAction.EnterMachine -> {
                    bridgeVerticalItems.remove(item.id)
                    when (val machineTile = getTile(action.target)) {
                        is TileEntity.Smelter -> {
                            singleMachines[action.target] = SingleMachineState(
                                inputItem = item.type,
                                ticksDone = 0,
                                totalTicks = machineTile.processTicks.coerceAtLeast(1),
                                enteredThisTick = true
                            )
                        }
                        is TileEntity.Press -> {
                            singleMachines[action.target] = SingleMachineState(
                                inputItem = item.type,
                                ticksDone = 0,
                                totalTicks = machineTile.processTicks.coerceAtLeast(1),
                                enteredThisTick = true
                            )
                        }
                        is TileEntity.Assembler -> {
                            val state = assemblers.getOrPut(action.target) {
                                AssemblerState(totalTicks = machineTile.processTicks.coerceAtLeast(1))
                            }
                            if (action.slot == 0) state.slotAItem = item.type else state.slotBItem = item.type
                            state.enteredThisTick = true
                            if (state.slotAItem != null && state.slotBItem != null) {
                                state.isProcessing = true
                                state.ticksDone = 0
                            }
                        }
                        else -> Unit
                    }
                    recentConsumedItems.add(
                        item.copy(prevCoord = item.currentCoord, currentCoord = action.target)
                    )
                    iterator.remove()
                }
            }
        }

        // Pass 3: Advance machines & spawners, emitting onto unoccupied output tiles.
        val occupiedAfterMoves = HashSet<OccupantSlot>(activeItems.size * 2)
        for (item in activeItems) {
            occupiedAfterMoves.add(slotForItem(item))
        }

        fun tryEmit(fromCoord: GridCoord, outDir: Direction, itemType: ItemType): Boolean {
            val target = fromCoord + outDir
            if (!inBounds(target)) return false
            val targetTile = getTile(target)
            return when (targetTile) {
                is TileEntity.Belt -> {
                    val canEnter = fromCoord != target + targetTile.outputDir
                    val targetSlot = OccupantSlot(target, LANE_NORMAL)
                    if (canEnter && targetSlot !in occupiedAfterMoves) {
                        activeItems.add(MovingItem(nextItemId++, itemType, fromCoord, target))
                        occupiedAfterMoves.add(targetSlot)
                        true
                    } else false
                }
                is TileEntity.Splitter -> {
                    val canEnter = fromCoord == target + targetTile.inputDir
                    val targetSlot = OccupantSlot(target, LANE_NORMAL)
                    if (canEnter && targetSlot !in occupiedAfterMoves) {
                        activeItems.add(MovingItem(nextItemId++, itemType, fromCoord, target))
                        occupiedAfterMoves.add(targetSlot)
                        true
                    } else false
                }
                is TileEntity.Filter -> {
                    val canEnter = fromCoord == target + targetTile.inputDir
                    val targetSlot = OccupantSlot(target, LANE_NORMAL)
                    if (canEnter && targetSlot !in occupiedAfterMoves) {
                        activeItems.add(MovingItem(nextItemId++, itemType, fromCoord, target))
                        occupiedAfterMoves.add(targetSlot)
                        true
                    } else false
                }
                is TileEntity.Overflow -> {
                    val canEnter = fromCoord == target + targetTile.inputDir
                    val targetSlot = OccupantSlot(target, LANE_NORMAL)
                    if (canEnter && targetSlot !in occupiedAfterMoves) {
                        activeItems.add(MovingItem(nextItemId++, itemType, fromCoord, target))
                        occupiedAfterMoves.add(targetSlot)
                        true
                    } else false
                }
                is TileEntity.Bridge -> {
                    val targetLane = when (outDir) {
                        targetTile.horizDir -> LANE_BRIDGE_HORIZ
                        targetTile.vertDir -> LANE_BRIDGE_VERT
                        else -> -1
                    }
                    if (targetLane >= 0) {
                        val targetSlot = OccupantSlot(target, targetLane)
                        if (targetSlot !in occupiedAfterMoves) {
                            val newId = nextItemId++
                            if (targetLane == LANE_BRIDGE_VERT) {
                                bridgeVerticalItems.add(newId)
                            }
                            activeItems.add(MovingItem(newId, itemType, fromCoord, target))
                            occupiedAfterMoves.add(targetSlot)
                            true
                        } else false
                    } else false
                }
                is TileEntity.Hatch -> {
                    if (itemType == targetTile.requiredItem &&
                        targetTile.deliveredCount < targetTile.targetCount
                    ) {
                        targetTile.deliveredCount++
                        deliveryHistoryTicks.addLast(tickCount)
                        recentConsumedItems.add(MovingItem(nextItemId++, itemType, fromCoord, target))
                        true
                    } else false
                }
                TileEntity.Incinerator -> {
                    recentConsumedItems.add(MovingItem(nextItemId++, itemType, fromCoord, target))
                    true
                }
                is TileEntity.Smelter -> {
                    if (fromCoord == target + targetTile.inputDir && canSmelterAccept(target, itemType)) {
                        singleMachines[target] = SingleMachineState(
                            inputItem = itemType,
                            ticksDone = 0,
                            totalTicks = targetTile.processTicks.coerceAtLeast(1),
                            enteredThisTick = true
                        )
                        recentConsumedItems.add(MovingItem(nextItemId++, itemType, fromCoord, target))
                        true
                    } else false
                }
                is TileEntity.Press -> {
                    if (fromCoord == target + targetTile.inputDir && canPressAccept(target, itemType)) {
                        singleMachines[target] = SingleMachineState(
                            inputItem = itemType,
                            ticksDone = 0,
                            totalTicks = targetTile.processTicks.coerceAtLeast(1),
                            enteredThisTick = true
                        )
                        recentConsumedItems.add(MovingItem(nextItemId++, itemType, fromCoord, target))
                        true
                    } else false
                }
                is TileEntity.Assembler -> {
                    val machineSlot = canAssemblerAccept(target, fromCoord, targetTile, itemType)
                    if (machineSlot != null) {
                        val state = assemblers.getOrPut(target) {
                            AssemblerState(totalTicks = targetTile.processTicks.coerceAtLeast(1))
                        }
                        if (machineSlot == 0) state.slotAItem = itemType else state.slotBItem = itemType
                        state.enteredThisTick = true
                        if (state.slotAItem != null && state.slotBItem != null) {
                            state.isProcessing = true
                            state.ticksDone = 0
                        }
                        recentConsumedItems.add(MovingItem(nextItemId++, itemType, fromCoord, target))
                        true
                    } else false
                }
                else -> false
            }
        }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val coord = GridCoord(x, y)
                when (val tile = grid[y][x]) {
                    is TileEntity.Smelter -> {
                        val state = singleMachines[coord] ?: continue
                        if (state.enteredThisTick) {
                            state.enteredThisTick = false
                            continue
                        }
                        if (state.outputItem == null && state.inputItem != null) {
                            state.ticksDone++
                            if (state.ticksDone >= state.totalTicks) {
                                state.outputItem = when (state.inputItem) {
                                    ItemType.IRON_ORE -> ItemType.IRON_INGOT
                                    ItemType.COPPER_ORE -> ItemType.COPPER_INGOT
                                    else -> null
                                }
                            }
                        }
                        val out = state.outputItem
                        if (out != null && tryEmit(coord, tile.outputDir, out)) {
                            singleMachines.remove(coord)
                        }
                    }

                    is TileEntity.Press -> {
                        val state = singleMachines[coord] ?: continue
                        if (state.enteredThisTick) {
                            state.enteredThisTick = false
                            continue
                        }
                        if (state.outputItem == null && state.inputItem != null) {
                            state.ticksDone++
                            if (state.ticksDone >= state.totalTicks) {
                                state.outputItem = state.inputItem?.let(::pressOutputFor)
                            }
                        }
                        val out = state.outputItem
                        if (out != null && tryEmit(coord, tile.outputDir, out)) {
                            singleMachines.remove(coord)
                        }
                    }

                    is TileEntity.Assembler -> {
                        val state = assemblers[coord] ?: continue
                        if (state.enteredThisTick) {
                            state.enteredThisTick = false
                            continue
                        }
                        if (state.outputItem == null && state.isProcessing) {
                            state.ticksDone++
                            if (state.ticksDone >= state.totalTicks) {
                                val a = state.slotAItem
                                val b = state.slotBItem
                                state.slotAItem = null
                                state.slotBItem = null
                                state.isProcessing = false
                                state.outputItem = if (a != null && b != null) assemblerOutputFor(a, b) else null
                            }
                        }
                        val out = state.outputItem
                        if (out != null && tryEmit(coord, tile.outputDir, out)) {
                            assemblers.remove(coord)
                        }
                    }

                    is TileEntity.Spawner -> {
                        val currentCooldown = spawnerCooldowns[coord] ?: 0
                        if (currentCooldown > 0) {
                            spawnerCooldowns[coord] = currentCooldown - 1
                        }
                        if ((spawnerCooldowns[coord] ?: 0) == 0) {
                            val secondary = tile.secondaryItemType
                            val useSecondary = secondary != null && (spawnerSecondaryTurn[coord] == true)
                            val emitType = if (useSecondary) secondary!! else tile.itemType
                            if (tryEmit(coord, tile.outputDir, emitType)) {
                                spawnerCooldowns[coord] = tile.spawnIntervalTicks.coerceAtLeast(1)
                                if (secondary != null) {
                                    spawnerSecondaryTurn[coord] = !useSecondary
                                }
                            }
                        }
                    }

                    else -> Unit
                }
            }
        }
    }
}
