package com.microfactory.sim

import kotlin.math.min

data class MachineStatus(
    val inputCount: Int,
    val maxInputs: Int,
    val progress: Float,
    val isProcessing: Boolean,
    val hasOutputReady: Boolean,
    val slotAFilled: Boolean = false,
    val slotBFilled: Boolean = false,
    val powerSatisfaction: Float = 1f,
    val isOverclocked: Boolean = false,
    val powerState: MachinePowerState = MachinePowerState.POWERED
)

/**
 * Pure Kotlin factory grid simulation running at a fixed base tick rate (4 ticks/sec).
 * Uses two-pass tick resolution so conveyor trains advance in lockstep without overlaps or teleportation.
 *
 * Supports dual-layer architecture:
 * - Layer 0 (Factory Layer): `width x height` floor for belts, sources, sinks, and processing machines.
 * - Layer 1 (Power Layer): `(width + 4) x (height + 4)` expanded grid with a 2-tile outer perimeter ring
 *   (`powerX = factoryX + 2`, `powerY = factoryY + 2`).
 */
class FactorySimulation(
    width: Int = 8,
    height: Int = 10,
    requiresPower: Boolean = false
) {
    var width: Int = width
        private set
    var height: Int = height
        private set

    var requiresPower: Boolean = requiresPower
        private set

    val powerWidth: Int
        get() = width + POWER_PERIMETER * 2

    val powerHeight: Int
        get() = height + POWER_PERIMETER * 2

    companion object {
        const val BASE_TICKS_PER_SECOND = 4.0
        const val TICK_DURATION_NANOS = 250_000_000L
        private const val IPM_WINDOW_TICKS = 240L // 60 simulated seconds at 4 ticks/sec

        const val POWER_PERIMETER = 2
        const val WIND_TURBINE_BASE_OUTPUT = 3f
        const val WIND_SPOT_MULTIPLIER = 2.0f
        const val HYDRO_GENERATOR_OUTPUT = 8f
        const val CAPACITOR_MAX_CHARGE = 40f
        const val TRANSMISSION_FREE_DISTANCE = 8
        const val TRANSMISSION_LOSS_STEP_TILES = 4f
        const val TRANSMISSION_LOSS_PER_STEP = 0.10f
        const val OVERCLOCK_SPEED_MULTIPLIER = 2.0f
        const val OVERCLOCK_POWER_MULTIPLIER = 2.5f

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

        fun transmissionEfficiencyForDistance(distanceSteps: Int): Float {
            if (distanceSteps <= TRANSMISSION_FREE_DISTANCE) return 1.0f
            val extraTiles = (distanceSteps - TRANSMISSION_FREE_DISTANCE).toFloat()
            val loss = (extraTiles / TRANSMISSION_LOSS_STEP_TILES) * TRANSMISSION_LOSS_PER_STEP
            return (1.0f - loss).coerceIn(0.1f, 1.0f)
        }
    }

    private var grid: Array<Array<TileEntity>> =
        Array(height) { Array(width) { TileEntity.Empty } }

    private var powerGrid: Array<Array<PowerTile>> =
        Array(powerHeight) { Array(powerWidth) { PowerTile.Empty } }

    private var powerTerrainGrid: Array<Array<PowerTerrainTile>> =
        Array(powerHeight) { y ->
            Array(powerWidth) { x ->
                PowerTerrainTile(GridCoord(x, y), PowerTerrainType.NORMAL)
            }
        }

    private val activeItems = mutableListOf<MovingItem>()
    private val recentConsumedItems = mutableListOf<MovingItem>()
    private val bridgeVerticalItems = HashSet<Long>()

    private val spawnerCooldowns = mutableMapOf<GridCoord, Int>()
    private val spawnerSecondaryTurn = mutableMapOf<GridCoord, Boolean>()
    private val singleMachines = mutableMapOf<GridCoord, SingleMachineState>()
    private val assemblers = mutableMapOf<GridCoord, AssemblerState>()
    private val filterWork = mutableMapOf<GridCoord, Float>()
    private val deliveryHistoryTicks = ArrayDeque<Long>()

    private val capacitorCharges = mutableMapOf<GridCoord, Float>()
    private val networkIdByPowerCoord = mutableMapOf<GridCoord, Int>()
    private val networkStatsById = mutableMapOf<Int, PowerNetworkStats>()
    private val tileTransmissionEfficiency = mutableMapOf<GridCoord, Float>()

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

    val totalRawGeneratedPower: Float
        get() = networkStatsById.values.sumOf { it.rawGenerated.toDouble() }.toFloat()

    val totalGeneratedPower: Float
        get() = networkStatsById.values.sumOf { it.effectiveGenerated.toDouble() }.toFloat()

    val totalPowerDemand: Float
        get() {
            var sum = 0f
            for (y in 0 until height) {
                for (x in 0 until width) {
                    sum += grid[y][x].powerDemand()
                }
            }
            return sum
        }

    val totalCapacitorStored: Float
        get() = capacitorCharges.values.sum()

    val totalCapacitorCapacity: Float
        get() = capacitorCharges.size * CAPACITOR_MAX_CHARGE

    val overallPowerSatisfaction: Float
        get() {
            if (!requiresPower) return 1.0f
            var totalDemand = 0f
            var satisfiedDemand = 0f
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val tile = grid[y][x]
                    if (tile.isPowerConsumer()) {
                        val d = tile.powerDemand()
                        totalDemand += d
                        satisfiedDemand += d * getMachinePowerSatisfaction(GridCoord(x, y))
                    }
                }
            }
            if (totalDemand <= 0f) {
                return if (totalGeneratedPower > 0f || totalCapacitorStored > 0f) 1.0f else 0.0f
            }
            return (satisfiedDemand / totalDemand).coerceIn(0f, 1f)
        }

    private data class OccupantSlot(val coord: GridCoord, val lane: Int)

    private data class SingleMachineState(
        var inputItem: ItemType? = null,
        var outputItem: ItemType? = null,
        var workDone: Float = 0f,
        var totalTicks: Int = 2,
        var enteredThisTick: Boolean = false
    )

    private data class AssemblerState(
        var slotAItem: ItemType? = null,
        var slotBItem: ItemType? = null,
        var outputItem: ItemType? = null,
        var workDone: Float = 0f,
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

    fun inPowerBounds(powerCoord: GridCoord): Boolean =
        powerCoord.x in 0 until powerWidth && powerCoord.y in 0 until powerHeight

    fun factoryToPower(factoryCoord: GridCoord): GridCoord =
        GridCoord(factoryCoord.x + POWER_PERIMETER, factoryCoord.y + POWER_PERIMETER)

    fun powerToFactory(powerCoord: GridCoord): GridCoord? {
        val fx = powerCoord.x - POWER_PERIMETER
        val fy = powerCoord.y - POWER_PERIMETER
        return if (fx in 0 until width && fy in 0 until height) GridCoord(fx, fy) else null
    }

    fun isOuterPerimeter(powerCoord: GridCoord): Boolean =
        inPowerBounds(powerCoord) && powerToFactory(powerCoord) == null

    fun setRequiresPower(enabled: Boolean) {
        requiresPower = enabled
        recomputePowerNetworks(advanceTick = false)
    }

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
        filterWork.remove(coord)
        recomputePowerNetworks(advanceTick = false)
    }

    fun getPowerTerrain(powerCoord: GridCoord): PowerTerrainTile =
        if (inPowerBounds(powerCoord)) {
            powerTerrainGrid[powerCoord.y][powerCoord.x]
        } else {
            PowerTerrainTile(powerCoord, PowerTerrainType.ROCK)
        }

    fun setPowerTerrain(
        powerCoord: GridCoord,
        type: PowerTerrainType,
        flowDir: Direction = Direction.SOUTH
    ) {
        if (!inPowerBounds(powerCoord)) return
        if (loadedLevel == null && type != PowerTerrainType.NORMAL) {
            requiresPower = true
        }
        powerTerrainGrid[powerCoord.y][powerCoord.x] = PowerTerrainTile(powerCoord, type, flowDir)
        if (type == PowerTerrainType.ROCK) {
            powerGrid[powerCoord.y][powerCoord.x] = PowerTile.Empty
            capacitorCharges.remove(powerCoord)
        }
        updateCableMasks()
        recomputePowerNetworks(advanceTick = false)
    }

    fun getPowerTile(powerCoord: GridCoord): PowerTile =
        if (inPowerBounds(powerCoord)) powerGrid[powerCoord.y][powerCoord.x] else PowerTile.Empty

    fun canPlacePowerTile(powerCoord: GridCoord, tile: PowerTile): Boolean {
        if (!inPowerBounds(powerCoord)) return false
        if (tile == PowerTile.Empty) return true
        val terrain = powerTerrainGrid[powerCoord.y][powerCoord.x]
        if (terrain.type == PowerTerrainType.ROCK) return false
        if (tile is PowerTile.HydroGenerator && terrain.type != PowerTerrainType.WATER_CURRENT) {
            return false
        }
        return true
    }

    fun setPowerTile(powerCoord: GridCoord, tile: PowerTile): Boolean {
        if (!canPlacePowerTile(powerCoord, tile)) return false
        if (loadedLevel == null && tile != PowerTile.Empty) {
            requiresPower = true
        }
        val terrain = powerTerrainGrid[powerCoord.y][powerCoord.x]
        val resolvedTile = when (tile) {
            is PowerTile.HydroGenerator -> tile.copy(direction = terrain.flowDir)
            else -> tile
        }
        powerGrid[powerCoord.y][powerCoord.x] = resolvedTile
        if (resolvedTile is PowerTile.CapacitorBank) {
            capacitorCharges.putIfAbsent(powerCoord, 0f)
        } else {
            capacitorCharges.remove(powerCoord)
        }
        updateCableMasks()
        recomputePowerNetworks(advanceTick = false)
        return true
    }

    fun clearPowerTile(powerCoord: GridCoord): Boolean {
        if (!inPowerBounds(powerCoord)) return false
        val current = powerGrid[powerCoord.y][powerCoord.x]
        if (!current.isPlayerEditable()) return false
        return setPowerTile(powerCoord, PowerTile.Empty)
    }

    private fun updateCableMasks() {
        for (y in 0 until powerHeight) {
            for (x in 0 until powerWidth) {
                val current = powerGrid[y][x]
                if (current is PowerTile.PowerCable) {
                    var mask = 0
                    val coord = GridCoord(x, y)
                    for (dir in Direction.entries) {
                        val neighbor = coord + dir
                        if (inPowerBounds(neighbor) && powerGrid[neighbor.y][neighbor.x].isConductive()) {
                            mask = mask or PowerTile.PowerCable.maskForDirection(dir)
                        }
                    }
                    powerGrid[y][x] = current.copy(connectionMask = mask)
                }
            }
        }
    }

    fun getGeneratorOutput(powerCoord: GridCoord): Float {
        if (!inPowerBounds(powerCoord)) return 0f
        val terrain = powerTerrainGrid[powerCoord.y][powerCoord.x]
        return when (powerGrid[powerCoord.y][powerCoord.x]) {
            PowerTile.WindTurbine -> {
                val mult = if (terrain.type == PowerTerrainType.WIND_SPOT) WIND_SPOT_MULTIPLIER else 1.0f
                WIND_TURBINE_BASE_OUTPUT * mult
            }
            is PowerTile.HydroGenerator -> {
                if (terrain.type == PowerTerrainType.WATER_CURRENT) HYDRO_GENERATOR_OUTPUT else 0f
            }
            else -> 0f
        }
    }

    fun getCapacitorCharge(powerCoord: GridCoord): Float =
        capacitorCharges[powerCoord] ?: 0f

    fun setCapacitorCharge(powerCoord: GridCoord, charge: Float) {
        if (!inPowerBounds(powerCoord)) return
        if (powerGrid[powerCoord.y][powerCoord.x] is PowerTile.CapacitorBank) {
            capacitorCharges[powerCoord] = charge.coerceIn(0f, CAPACITOR_MAX_CHARGE)
            recomputePowerNetworks(advanceTick = false)
        }
    }

    fun getTransmissionEfficiency(powerCoord: GridCoord): Float =
        tileTransmissionEfficiency[powerCoord] ?: 0f

    fun getPowerNetworkStatsForPowerCoord(powerCoord: GridCoord): PowerNetworkStats? {
        val netId = networkIdByPowerCoord[powerCoord] ?: return null
        return networkStatsById[netId]
    }

    fun getPowerNetworkStatsForMachine(factoryCoord: GridCoord): PowerNetworkStats? {
        if (!inBounds(factoryCoord)) return null
        val powerCoord = factoryToPower(factoryCoord)
        val powerTile = getPowerTile(powerCoord)
        if (!powerTile.canSupplyMachineAbove()) return null
        val netId = networkIdByPowerCoord[powerCoord] ?: return null
        return networkStatsById[netId]
    }

    fun getMachinePowerSatisfaction(factoryCoord: GridCoord): Float {
        if (!requiresPower) return 1.0f
        val tile = getTile(factoryCoord)
        if (!tile.isPowerConsumer()) return 1.0f
        val stats = getPowerNetworkStatsForMachine(factoryCoord) ?: return 0f
        return stats.satisfaction
    }

    fun getMachinePowerState(factoryCoord: GridCoord): MachinePowerState {
        val tile = getTile(factoryCoord)
        if (!requiresPower) {
            return if (tile.isOverclocked()) MachinePowerState.OVERCLOCKED else MachinePowerState.POWERED
        }
        val sat = getMachinePowerSatisfaction(factoryCoord)
        return when {
            sat <= 0.001f -> MachinePowerState.UNPOWERED
            sat < 0.999f -> MachinePowerState.BROWNOUT
            tile.isOverclocked() -> MachinePowerState.OVERCLOCKED
            else -> MachinePowerState.POWERED
        }
    }

    fun toggleOverclock(coord: GridCoord): Boolean {
        if (!inBounds(coord)) return false
        val current = grid[coord.y][coord.x]
        if (!current.isPowerConsumer()) return false
        grid[coord.y][coord.x] = current.withOverclock(!current.isOverclocked())
        recomputePowerNetworks(advanceTick = false)
        return true
    }

    fun setOverclock(coord: GridCoord, enabled: Boolean): Boolean {
        if (!inBounds(coord)) return false
        val current = grid[coord.y][coord.x]
        if (!current.isPowerConsumer()) return false
        grid[coord.y][coord.x] = current.withOverclock(enabled)
        recomputePowerNetworks(advanceTick = false)
        return true
    }

    fun isOverclocked(coord: GridCoord): Boolean =
        getTile(coord).isOverclocked()

    private fun recomputePowerNetworks(advanceTick: Boolean) {
        networkIdByPowerCoord.clear()
        networkStatsById.clear()
        tileTransmissionEfficiency.clear()

        val visited = HashSet<GridCoord>()
        var nextNetworkId = 1

        for (py in 0 until powerHeight) {
            for (px in 0 until powerWidth) {
                val start = GridCoord(px, py)
                if (!powerGrid[py][px].isConductive() || start in visited) continue

                val netId = nextNetworkId++
                val component = mutableListOf<GridCoord>()
                val queue = ArrayDeque<GridCoord>()
                queue.add(start)
                visited.add(start)

                while (queue.isNotEmpty()) {
                    val cur = queue.removeFirst()
                    component.add(cur)
                    networkIdByPowerCoord[cur] = netId
                    for (dir in Direction.entries) {
                        val next = cur + dir
                        if (inPowerBounds(next) && next !in visited && powerGrid[next.y][next.x].isConductive()) {
                            visited.add(next)
                            queue.add(next)
                        }
                    }
                }

                val generators = mutableListOf<GridCoord>()
                val capacitors = mutableListOf<GridCoord>()
                var rawGen = 0f

                for (coord in component) {
                    when (powerGrid[coord.y][coord.x]) {
                        PowerTile.WindTurbine, is PowerTile.HydroGenerator -> {
                            val out = getGeneratorOutput(coord)
                            if (out > 0f) {
                                generators.add(coord)
                                rawGen += out
                            }
                        }
                        PowerTile.CapacitorBank -> capacitors.add(coord)
                        else -> Unit
                    }
                }

                val distMap = HashMap<GridCoord, Int>(component.size * 2)
                val bfsQueue = ArrayDeque<GridCoord>()
                if (generators.isNotEmpty()) {
                    // Generators and any connected CapacitorBank tiles act as distance-0 sources
                    for (g in generators) {
                        distMap[g] = 0
                        bfsQueue.add(g)
                    }
                    for (c in capacitors) {
                        if (c !in distMap) {
                            distMap[c] = 0
                            bfsQueue.add(c)
                        }
                    }
                } else {
                    for (c in capacitors) {
                        if ((capacitorCharges[c] ?: 0f) > 0f) {
                            distMap[c] = 0
                            bfsQueue.add(c)
                        }
                    }
                }

                val componentSet = component.toHashSet()
                while (bfsQueue.isNotEmpty()) {
                    val cur = bfsQueue.removeFirst()
                    val curDist = distMap[cur] ?: 0
                    for (dir in Direction.entries) {
                        val next = cur + dir
                        if (next in componentSet && next !in distMap) {
                            distMap[next] = curDist + 1
                            bfsQueue.add(next)
                        }
                    }
                }

                for (coord in component) {
                    val d = distMap[coord]
                    tileTransmissionEfficiency[coord] = if (d != null) {
                        transmissionEfficiencyForDistance(d)
                    } else {
                        0f
                    }
                }

                // Gather Layer 0 machines drawing from this sub-network
                var demand = 0f
                var weightedEfficiencySum = 0f
                for (coord in component) {
                    if (!powerGrid[coord.y][coord.x].canSupplyMachineAbove()) continue
                    val factoryCoord = powerToFactory(coord) ?: continue
                    val factoryTile = grid[factoryCoord.y][factoryCoord.x]
                    if (factoryTile.isPowerConsumer()) {
                        val mDemand = factoryTile.powerDemand()
                        val eff = tileTransmissionEfficiency[coord] ?: 1.0f
                        demand += mDemand
                        weightedEfficiencySum += mDemand * eff
                    }
                }

                val networkEfficiency = when {
                    generators.isEmpty() -> 1.0f
                    demand > 0f -> (weightedEfficiencySum / demand).coerceIn(0.1f, 1.0f)
                    else -> {
                        component.minOfOrNull { tileTransmissionEfficiency[it] ?: 1.0f } ?: 1.0f
                    }
                }

                val effectiveGen = rawGen * networkEfficiency
                val capacity = capacitors.size * CAPACITOR_MAX_CHARGE
                var storedBefore = capacitors.sumOf { (capacitorCharges[it] ?: 0f).toDouble() }.toFloat()

                val discharge = if (demand > effectiveGen) {
                    min(demand - effectiveGen, storedBefore)
                } else {
                    0f
                }

                if (advanceTick && capacitors.isNotEmpty()) {
                    if (effectiveGen > demand) {
                        var remainingSurplus = effectiveGen - demand
                        for (c in capacitors) {
                            if (remainingSurplus <= 0f) break
                            val cur = capacitorCharges[c] ?: 0f
                            val room = (CAPACITOR_MAX_CHARGE - cur).coerceAtLeast(0f)
                            val added = min(room, remainingSurplus)
                            capacitorCharges[c] = cur + added
                            remainingSurplus -= added
                        }
                    } else if (discharge > 0f) {
                        var remainingDischarge = discharge
                        for (c in capacitors) {
                            if (remainingDischarge <= 0f) break
                            val cur = capacitorCharges[c] ?: 0f
                            val drained = min(cur, remainingDischarge)
                            capacitorCharges[c] = cur - drained
                            remainingDischarge -= drained
                        }
                    }
                    storedBefore = capacitors.sumOf { (capacitorCharges[it] ?: 0f).toDouble() }.toFloat()
                }

                val satisfaction = if (demand <= 0f) {
                    if (effectiveGen > 0f || storedBefore > 0f) 1.0f else 0.0f
                } else {
                    ((effectiveGen + discharge) / demand).coerceIn(0f, 1.0f)
                }

                networkStatsById[netId] = PowerNetworkStats(
                    networkId = netId,
                    rawGenerated = rawGen,
                    effectiveGenerated = effectiveGen,
                    transmissionEfficiency = networkEfficiency,
                    demand = demand,
                    capacitorStored = storedBefore,
                    capacitorCapacity = capacity,
                    capacitorDischarge = discharge,
                    satisfaction = satisfaction
                )
            }
        }
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
            powerGrid = Array(powerHeight) { Array(powerWidth) { PowerTile.Empty } }
            powerTerrainGrid = Array(powerHeight) { y ->
                Array(powerWidth) { x ->
                    PowerTerrainTile(GridCoord(x, y), PowerTerrainType.NORMAL)
                }
            }
        }
        requiresPower = level.requiresPower
        loadedLevel = level
        resetBoard()
    }

    fun resetBoard() {
        for (y in 0 until height) {
            for (x in 0 until width) {
                grid[y][x] = TileEntity.Empty
            }
        }
        for (py in 0 until powerHeight) {
            for (px in 0 until powerWidth) {
                powerGrid[py][px] = PowerTile.Empty
                powerTerrainGrid[py][px] = PowerTerrainTile(GridCoord(px, py), PowerTerrainType.NORMAL)
            }
        }
        activeItems.clear()
        recentConsumedItems.clear()
        bridgeVerticalItems.clear()
        spawnerCooldowns.clear()
        spawnerSecondaryTurn.clear()
        singleMachines.clear()
        assemblers.clear()
        filterWork.clear()
        capacitorCharges.clear()
        networkIdByPowerCoord.clear()
        networkStatsById.clear()
        tileTransmissionEfficiency.clear()
        deliveryHistoryTicks.clear()
        tickCount = 0L
        nextItemId = 1L

        val level = loadedLevel ?: return
        requiresPower = level.requiresPower
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
        for (terrain in level.powerTerrain) {
            if (inPowerBounds(terrain.coord)) {
                powerTerrainGrid[terrain.coord.y][terrain.coord.x] = terrain
            }
        }
        recomputePowerNetworks(advanceTick = false)
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
        val tile = getTile(coord)
        val sat = getMachinePowerSatisfaction(coord)
        val pState = getMachinePowerState(coord)
        return when (tile) {
            is TileEntity.Smelter, is TileEntity.Press -> {
                val state = singleMachines[coord] ?: return MachineStatus(
                    inputCount = 0,
                    maxInputs = 1,
                    progress = 0f,
                    isProcessing = false,
                    hasOutputReady = false,
                    powerSatisfaction = sat,
                    isOverclocked = tile.isOverclocked(),
                    powerState = pState
                )
                val progress = if (state.totalTicks > 0 && state.inputItem != null) {
                    (state.workDone / state.totalTicks).coerceIn(0f, 1f)
                } else 0f
                MachineStatus(
                    inputCount = if (state.inputItem != null) 1 else 0,
                    maxInputs = 1,
                    progress = if (state.outputItem != null) 1f else progress,
                    isProcessing = state.inputItem != null && state.outputItem == null,
                    hasOutputReady = state.outputItem != null,
                    powerSatisfaction = sat,
                    isOverclocked = tile.isOverclocked(),
                    powerState = pState
                )
            }
            is TileEntity.Assembler -> {
                val state = assemblers[coord] ?: return MachineStatus(
                    inputCount = 0,
                    maxInputs = 2,
                    progress = 0f,
                    isProcessing = false,
                    hasOutputReady = false,
                    powerSatisfaction = sat,
                    isOverclocked = tile.overclocked,
                    powerState = pState
                )
                val count = (if (state.slotAItem != null) 1 else 0) + (if (state.slotBItem != null) 1 else 0)
                val progress = if (state.totalTicks > 0 && state.isProcessing) {
                    (state.workDone / state.totalTicks).coerceIn(0f, 1f)
                } else 0f
                MachineStatus(
                    inputCount = count,
                    maxInputs = 2,
                    progress = if (state.outputItem != null) 1f else progress,
                    isProcessing = state.isProcessing,
                    hasOutputReady = state.outputItem != null,
                    slotAFilled = state.slotAItem != null,
                    slotBFilled = state.slotBItem != null,
                    powerSatisfaction = sat,
                    isOverclocked = tile.overclocked,
                    powerState = pState
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
     * Advances the simulation by 1 tick using power grid evaluation, two-pass movement resolution,
     * and machine & spawner updates.
     */
    fun tick() {
        tickCount++
        recentConsumedItems.clear()

        // Evaluate power sub-networks and capacitor bank charge/discharge for this tick.
        recomputePowerNetworks(advanceTick = true)

        // Accumulate Filter work on power levels.
        if (requiresPower) {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val f = grid[y][x] as? TileEntity.Filter ?: continue
                    val coord = GridCoord(x, y)
                    val sat = getMachinePowerSatisfaction(coord)
                    val mult = if (f.overclocked) OVERCLOCK_SPEED_MULTIPLIER else 1.0f
                    val nextWork = ((filterWork[coord] ?: 0f) + sat * mult).coerceAtMost(2.0f)
                    filterWork[coord] = nextWork
                }
            }
        }

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

            if (requiresPower && tile is TileEntity.Filter) {
                val availableWork = filterWork[pos] ?: 0f
                if (availableWork + 1e-5f < 1.0f) {
                    decision[slot] = false
                    return false
                }
            }

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
            val originCoord = item.currentCoord
            if (requiresPower && getTile(originCoord) is TileEntity.Filter) {
                filterWork[originCoord] = ((filterWork[originCoord] ?: 1f) - 1f).coerceAtLeast(0f)
            }
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
                                workDone = 0f,
                                totalTicks = machineTile.processTicks.coerceAtLeast(1),
                                enteredThisTick = true
                            )
                        }
                        is TileEntity.Press -> {
                            singleMachines[action.target] = SingleMachineState(
                                inputItem = item.type,
                                workDone = 0f,
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
                                state.workDone = 0f
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
                            workDone = 0f,
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
                            workDone = 0f,
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
                            state.workDone = 0f
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
                            val sat = getMachinePowerSatisfaction(coord)
                            val mult = if (tile.overclocked) OVERCLOCK_SPEED_MULTIPLIER else 1.0f
                            state.workDone += sat * mult
                            if (state.workDone + 1e-5f >= state.totalTicks) {
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
                            val sat = getMachinePowerSatisfaction(coord)
                            val mult = if (tile.overclocked) OVERCLOCK_SPEED_MULTIPLIER else 1.0f
                            state.workDone += sat * mult
                            if (state.workDone + 1e-5f >= state.totalTicks) {
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
                            val sat = getMachinePowerSatisfaction(coord)
                            val mult = if (tile.overclocked) OVERCLOCK_SPEED_MULTIPLIER else 1.0f
                            state.workDone += sat * mult
                            if (state.workDone + 1e-5f >= state.totalTicks) {
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
