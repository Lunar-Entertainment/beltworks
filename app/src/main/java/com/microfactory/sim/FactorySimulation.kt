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
    val width: Int = 8,
    val height: Int = 10
) {
    companion object {
        const val BASE_TICKS_PER_SECOND = 4.0
        const val TICK_DURATION_NANOS = 250_000_000L
        private const val IPM_WINDOW_TICKS = 240L // 60 simulated seconds at 4 ticks/sec
    }

    private val grid: Array<Array<TileEntity>> =
        Array(height) { Array(width) { TileEntity.Empty } }

    private val activeItems = mutableListOf<MovingItem>()
    private val recentConsumedItems = mutableListOf<MovingItem>()

    private val spawnerCooldowns = mutableMapOf<GridCoord, Int>()
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

    private data class SingleMachineState(
        var inputItem: ItemType? = null,
        var outputItem: ItemType? = null,
        var ticksDone: Int = 0,
        var totalTicks: Int = 2,
        var enteredThisTick: Boolean = false
    )

    private data class AssemblerState(
        var slotAFilled: Boolean = false,
        var slotBFilled: Boolean = false,
        var outputItem: ItemType? = null,
        var ticksDone: Int = 0,
        var totalTicks: Int = 3,
        var isProcessing: Boolean = false,
        var enteredThisTick: Boolean = false
    )

    private sealed interface PlannedAction {
        data class MoveToTile(val target: GridCoord, val splitterUsedDir: Direction?) : PlannedAction
        data class EnterHatch(val target: GridCoord) : PlannedAction
        data class EnterMachine(val target: GridCoord, val slot: Int) : PlannedAction
    }

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
        activeItems.removeAll { it.currentCoord == coord }
        recentConsumedItems.removeAll { it.currentCoord == coord }
        spawnerCooldowns.remove(coord)
        singleMachines.remove(coord)
        assemblers.remove(coord)
    }

    fun rotateTile(coord: GridCoord): Boolean {
        if (!inBounds(coord)) return false
        val current = grid[coord.y][coord.x]
        if (!current.isPlayerEditable()) return false
        grid[coord.y][coord.x] = current.rotatedClockwise()
        singleMachines.remove(coord)
        assemblers.remove(coord)
        return true
    }

    fun clearTile(coord: GridCoord): Boolean {
        if (!inBounds(coord)) return false
        val current = grid[coord.y][coord.x]
        if (!current.isPlayerEditable()) return false
        setTile(coord, TileEntity.Empty)
        return true
    }

    fun loadLevel(level: LevelConfig) {
        require(level.width == width && level.height == height) {
            "Level dimensions (${level.width}x${level.height}) must match simulation (${width}x${height})"
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
        spawnerCooldowns.clear()
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
        return when (val tile = getTile(coord)) {
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
                val count = (if (state.slotAFilled) 1 else 0) + (if (state.slotBFilled) 1 else 0)
                val progress = if (state.totalTicks > 0 && state.isProcessing) {
                    (state.ticksDone.toFloat() / state.totalTicks).coerceIn(0f, 1f)
                } else 0f
                MachineStatus(
                    inputCount = count,
                    maxInputs = 2,
                    progress = if (state.outputItem != null) 1f else progress,
                    isProcessing = state.isProcessing,
                    hasOutputReady = state.outputItem != null,
                    slotAFilled = state.slotAFilled,
                    slotBFilled = state.slotBFilled
                )
            }
            else -> null
        }
    }

    /**
     * Advances the simulation by 1 tick using two-pass movement resolution followed by machine & spawner updates.
     */
    fun tick() {
        tickCount++
        recentConsumedItems.clear()

        // Snapshot previous coordinates for smooth frame interpolation.
        for (item in activeItems) {
            item.prevCoord = item.currentCoord
        }

        // Index initial occupancy by coordinate.
        val occupantAt = HashMap<GridCoord, MovingItem>(activeItems.size * 2)
        for (item in activeItems) {
            occupantAt[item.currentCoord] = item
        }

        // Pass 1: Back-to-front recursive planning & cell reservation.
        val plannedMoves = HashMap<Long, PlannedAction>()
        val reservedTileTargets = HashSet<GridCoord>()
        val reservedMachineSlots = HashSet<Pair<GridCoord, Int>>()
        val decision = HashMap<GridCoord, Boolean>()
        val visiting = HashSet<GridCoord>()

        fun canSmelterAccept(coord: GridCoord, type: ItemType): Boolean {
            if (type != ItemType.IRON_ORE && type != ItemType.COPPER_ORE) return false
            val state = singleMachines[coord] ?: return true
            return state.inputItem == null && state.outputItem == null
        }

        fun canPressAccept(coord: GridCoord, type: ItemType): Boolean {
            if (type != ItemType.IRON_INGOT) return false
            val state = singleMachines[coord] ?: return true
            return state.inputItem == null && state.outputItem == null
        }

        fun canAssemblerAccept(coord: GridCoord, fromCoord: GridCoord, tile: TileEntity.Assembler, type: ItemType): Int? {
            if (type != ItemType.IRON_PLATE) return null
            val state = assemblers[coord] ?: AssemblerState(totalTicks = tile.processTicks)
            if (state.outputItem != null || state.isProcessing) return null
            if (fromCoord == coord + tile.inputDirA && !state.slotAFilled && (coord to 0) !in reservedMachineSlots) {
                return 0
            }
            if (fromCoord == coord + tile.inputDirB && !state.slotBFilled && (coord to 1) !in reservedMachineSlots) {
                return 1
            }
            return null
        }

        fun resolveMove(pos: GridCoord): Boolean {
            decision[pos]?.let { return it }
            if (pos in visiting) return false

            val item = occupantAt[pos] ?: return true
            val tile = getTile(pos)
            val candidateDirs = when (tile) {
                is TileEntity.Belt -> listOf(tile.outputDir)
                is TileEntity.Splitter -> if (tile.toggleLeft) {
                    listOf(tile.outputLeft, tile.outputRight)
                } else {
                    listOf(tile.outputRight, tile.outputLeft)
                }
                else -> {
                    decision[pos] = false
                    return false
                }
            }

            visiting.add(pos)

            for (outDir in candidateDirs) {
                val target = pos + outDir
                if (!inBounds(target)) continue
                val targetTile = getTile(target)

                when (targetTile) {
                    is TileEntity.Hatch -> {
                        if (item.type == targetTile.requiredItem) {
                            plannedMoves[item.id] = PlannedAction.EnterHatch(target)
                            visiting.remove(pos)
                            decision[pos] = true
                            return true
                        }
                    }

                    is TileEntity.Smelter -> {
                        if (pos == target + targetTile.inputDir &&
                            canSmelterAccept(target, item.type) &&
                            (target to 0) !in reservedMachineSlots
                        ) {
                            reservedMachineSlots.add(target to 0)
                            plannedMoves[item.id] = PlannedAction.EnterMachine(target, 0)
                            visiting.remove(pos)
                            decision[pos] = true
                            return true
                        }
                    }

                    is TileEntity.Press -> {
                        if (pos == target + targetTile.inputDir &&
                            canPressAccept(target, item.type) &&
                            (target to 0) !in reservedMachineSlots
                        ) {
                            reservedMachineSlots.add(target to 0)
                            plannedMoves[item.id] = PlannedAction.EnterMachine(target, 0)
                            visiting.remove(pos)
                            decision[pos] = true
                            return true
                        }
                    }

                    is TileEntity.Assembler -> {
                        val slot = canAssemblerAccept(target, pos, targetTile, item.type)
                        if (slot != null) {
                            reservedMachineSlots.add(target to slot)
                            plannedMoves[item.id] = PlannedAction.EnterMachine(target, slot)
                            visiting.remove(pos)
                            decision[pos] = true
                            return true
                        }
                    }

                    is TileEntity.Belt -> {
                        val canEnter = pos != target + targetTile.outputDir
                        if (canEnter && target !in reservedTileTargets) {
                            val targetFree = (target !in occupantAt) || resolveMove(target)
                            if (targetFree) {
                                reservedTileTargets.add(target)
                                val splitterDir = if (tile is TileEntity.Splitter) outDir else null
                                plannedMoves[item.id] = PlannedAction.MoveToTile(target, splitterDir)
                                visiting.remove(pos)
                                decision[pos] = true
                                return true
                            }
                        }
                    }

                    is TileEntity.Splitter -> {
                        val canEnter = pos == target + targetTile.inputDir
                        if (canEnter && target !in reservedTileTargets) {
                            val targetFree = (target !in occupantAt) || resolveMove(target)
                            if (targetFree) {
                                reservedTileTargets.add(target)
                                val splitterDir = if (tile is TileEntity.Splitter) outDir else null
                                plannedMoves[item.id] = PlannedAction.MoveToTile(target, splitterDir)
                                visiting.remove(pos)
                                decision[pos] = true
                                return true
                            }
                        }
                    }

                    TileEntity.Empty, TileEntity.Rock, is TileEntity.Spawner -> Unit
                }
            }

            visiting.remove(pos)
            decision[pos] = false
            return false
        }

        // Deterministic coordinate order for planning.
        val sortedPositions = occupantAt.keys.sortedWith(compareBy({ it.y }, { it.x }))
        for (pos in sortedPositions) {
            resolveMove(pos)
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
                    item.currentCoord = action.target
                }

                is PlannedAction.EnterHatch -> {
                    val hatch = getTile(action.target) as? TileEntity.Hatch
                    if (hatch != null) {
                        hatch.deliveredCount++
                        deliveryHistoryTicks.addLast(tickCount)
                    }
                    recentConsumedItems.add(
                        item.copy(prevCoord = item.currentCoord, currentCoord = action.target)
                    )
                    iterator.remove()
                }

                is PlannedAction.EnterMachine -> {
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
                            if (action.slot == 0) state.slotAFilled = true else state.slotBFilled = true
                            state.enteredThisTick = true
                            if (state.slotAFilled && state.slotBFilled) {
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
        val occupiedAfterMoves = HashSet<GridCoord>(activeItems.size * 2)
        for (item in activeItems) {
            occupiedAfterMoves.add(item.currentCoord)
        }

        fun tryEmit(fromCoord: GridCoord, outDir: Direction, itemType: ItemType): Boolean {
            val target = fromCoord + outDir
            if (!inBounds(target)) return false
            val targetTile = getTile(target)
            return when (targetTile) {
                is TileEntity.Belt -> {
                    val canEnter = fromCoord != target + targetTile.outputDir
                    if (canEnter && target !in occupiedAfterMoves) {
                        activeItems.add(MovingItem(nextItemId++, itemType, fromCoord, target))
                        occupiedAfterMoves.add(target)
                        true
                    } else false
                }
                is TileEntity.Splitter -> {
                    val canEnter = fromCoord == target + targetTile.inputDir
                    if (canEnter && target !in occupiedAfterMoves) {
                        activeItems.add(MovingItem(nextItemId++, itemType, fromCoord, target))
                        occupiedAfterMoves.add(target)
                        true
                    } else false
                }
                is TileEntity.Hatch -> {
                    if (itemType == targetTile.requiredItem) {
                        targetTile.deliveredCount++
                        deliveryHistoryTicks.addLast(tickCount)
                        recentConsumedItems.add(MovingItem(nextItemId++, itemType, fromCoord, target))
                        true
                    } else false
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
                    val slot = canAssemblerAccept(target, fromCoord, targetTile, itemType)
                    if (slot != null) {
                        val state = assemblers.getOrPut(target) {
                            AssemblerState(totalTicks = targetTile.processTicks.coerceAtLeast(1))
                        }
                        if (slot == 0) state.slotAFilled = true else state.slotBFilled = true
                        state.enteredThisTick = true
                        if (state.slotAFilled && state.slotBFilled) {
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
                                state.outputItem = when (state.inputItem) {
                                    ItemType.IRON_INGOT -> ItemType.IRON_PLATE
                                    else -> null
                                }
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
                                state.slotAFilled = false
                                state.slotBFilled = false
                                state.isProcessing = false
                                state.outputItem = ItemType.GEAR
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
                            if (tryEmit(coord, tile.outputDir, tile.itemType)) {
                                spawnerCooldowns[coord] = tile.spawnIntervalTicks.coerceAtLeast(1)
                            }
                        }
                    }

                    else -> Unit
                }
            }
        }
    }
}
