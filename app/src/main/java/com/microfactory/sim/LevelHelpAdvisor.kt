package com.microfactory.sim

/**
 * Generates level-specific factory blueprints, step-by-step recipe chains,
 * and BFS open-route guides for the in-game Help system.
 */
data class LevelHelpBlueprint(
    val summary: String,
    val craftingSteps: List<String>,
    val tips: List<String>,
    val suggestedPathCoords: Set<GridCoord>
)

object LevelHelpAdvisor {
    fun buildBlueprint(level: LevelConfig): LevelHelpBlueprint {
        val steps = linkedSetOf<String>()
        val tips = mutableListOf<String>()

        val spawners = level.spawners.mapNotNull { placed ->
            (placed.tile as? TileEntity.Spawner)?.let { placed to it }
        }
        val hatches = level.hatches.mapNotNull { placed ->
            (placed.tile as? TileEntity.Hatch)?.let { placed to it }
        }

        val targetItems = hatches.map { it.second.requiredItem }.toSet()
        for (item in targetItems) {
            appendRecipesFor(item, steps)
        }

        val mixedSpawners = spawners.filter { it.second.secondaryItemType != null }
        if (mixedSpawners.isNotEmpty()) {
            val pairs = mixedSpawners.joinToString(", ") { (_, s) ->
                "${displayName(s.itemType)} + ${displayName(s.secondaryItemType!!)}"
            }
            steps.add("Mixed Spawner ($pairs): Place a Filter tile right after the spawner, tap it, and choose one item in the bar to separate the two streams.")
        }

        if (level.id >= 10 && (spawners.size >= 2 || hatches.size >= 2)) {
            tips.add("Crossing Paths: Use a Bridge tile to carry one horizontal and one vertical belt across the same square without mixing.")
        }
        if (level.id >= 35 && mixedSpawners.isNotEmpty()) {
            tips.add("Prevent Jams: Connect a Trash (Incinerator) or Overflow valve to bleed off extra items if one line fills up first.")
        }
        if (level.id >= 45) {
            tips.add("Priority Flow: Overflow sends items straight until backed up, then spills sideways — great before secondary hatches or Trash.")
        }
        if (level.requiresPower) {
            steps.add("Power Layer (⚡): Switch to [⚡ Power] in the top bar, build Wind Turbines (2× on Wind Spots) or Hydro Generators (on Water Currents), and route Power Cables under your machines.")
            tips.add("Capacitor & Overclock: Cable runs longer than 8 tiles lose 10% efficiency per 4 extra tiles unless buffered by a Capacitor Bank (40⚡ storage). Tap any powered machine to toggle 2.0× Overclock speed (2.5× ⚡ cost).")
        }
        tips.add("Controls: Drag to lay curved belts, tap any belt to smoothly curve-connect it to neighbors, and tap machines to rotate.")
        tips.add("Target Par: Solve using ${level.parBelts} belts or fewer for 3 stars.")

        val paths = computeSuggestedPaths(level, spawners.map { it.first }, hatches.map { it.first })

        return LevelHelpBlueprint(
            summary = level.hint,
            craftingSteps = steps.toList(),
            tips = tips,
            suggestedPathCoords = paths
        )
    }

    private fun appendRecipesFor(item: ItemType, out: MutableSet<String>) {
        when (item) {
            ItemType.IRON_ORE -> out.add("Route Iron Ore directly from the spawner into the Hatch.")
            ItemType.COPPER_ORE -> out.add("Route Copper Ore directly from the spawner into the Hatch.")
            ItemType.IRON_INGOT -> out.add("Smelter: Feed Iron Ore -> outputs Iron Ingot.")
            ItemType.COPPER_INGOT -> out.add("Smelter: Feed Copper Ore -> outputs Copper Ingot.")
            ItemType.IRON_PLATE -> {
                out.add("Smelter: Iron Ore -> Iron Ingot.")
                out.add("Press: Iron Ingot -> Iron Plate.")
            }
            ItemType.COPPER_WIRE -> {
                out.add("Smelter: Copper Ore -> Copper Ingot.")
                out.add("Press: Copper Ingot -> Copper Wire.")
            }
            ItemType.GEAR -> {
                out.add("Smelter + Press: Turn Iron Ore -> Iron Ingot -> Iron Plate.")
                out.add("Gear Assembler: Feed 2 Iron Plates into two different input sides of an Assembler (use a Splitter if coming from 1 belt) -> Gear.")
            }
            ItemType.CIRCUIT -> {
                out.add("Iron Branch: Iron Ore -> Smelter -> Press -> Iron Plate.")
                out.add("Copper Branch: Copper Ore -> Smelter -> Press -> Copper Wire.")
                out.add("Circuit Assembler: Feed Iron Plate into one input side and Copper Wire into the other input side -> Circuit.")
            }
            ItemType.MOTOR -> {
                out.add("Craft Gears: 2x Iron Plate -> Assembler -> Gear.")
                out.add("Craft Circuits: Iron Plate + Copper Wire -> Assembler -> Circuit.")
                out.add("Motor Assembler: Feed Gear into one input side and Circuit into the other input side -> Motor.")
            }
        }
    }

    private fun displayName(item: ItemType): String = when (item) {
        ItemType.IRON_ORE -> "Iron Ore"
        ItemType.IRON_INGOT -> "Iron Ingot"
        ItemType.IRON_PLATE -> "Iron Plate"
        ItemType.COPPER_ORE -> "Copper Ore"
        ItemType.COPPER_INGOT -> "Copper Ingot"
        ItemType.COPPER_WIRE -> "Copper Wire"
        ItemType.GEAR -> "Gear"
        ItemType.CIRCUIT -> "Circuit"
        ItemType.MOTOR -> "Motor"
    }

    private fun computeSuggestedPaths(
        level: LevelConfig,
        spawners: List<PlacedTile>,
        hatches: List<PlacedTile>
    ): Set<GridCoord> {
        val blocked = HashSet<GridCoord>(level.rocks.size + spawners.size + hatches.size)
        blocked.addAll(level.rocks)
        spawners.forEach { blocked.add(it.coord) }
        val hatchCoords = hatches.map { it.coord }.toSet()
        blocked.addAll(hatchCoords)

        val result = linkedSetOf<GridCoord>()
        for (spawner in spawners) {
            val spTile = spawner.tile as? TileEntity.Spawner ?: continue
            val start = spawner.coord + spTile.outputDir
            if (!inBounds(start, level) || start in blocked) continue

            val goal = findShortestPathToAnyHatch(level, start, hatchCoords, blocked)
            result.addAll(goal)
        }
        return result
    }

    private fun findShortestPathToAnyHatch(
        level: LevelConfig,
        start: GridCoord,
        hatchCoords: Set<GridCoord>,
        blocked: Set<GridCoord>
    ): List<GridCoord> {
        val parent = HashMap<GridCoord, GridCoord?>()
        val queue = ArrayDeque<GridCoord>()
        parent[start] = null
        queue.add(start)

        var adjacentEnd: GridCoord? = null
        while (queue.isNotEmpty()) {
            val cur = queue.removeFirst()
            // Stop at a tile immediately adjacent to any Hatch
            if (Direction.entries.any { cur + it in hatchCoords }) {
                adjacentEnd = cur
                break
            }
            for (dir in Direction.entries) {
                val next = cur + dir
                if (!inBounds(next, level) || next in blocked || parent.containsKey(next)) continue
                parent[next] = cur
                queue.add(next)
            }
        }

        val end = adjacentEnd ?: return emptyList()
        val path = ArrayList<GridCoord>()
        var step: GridCoord? = end
        while (step != null) {
            path.add(step)
            step = parent[step]
        }
        return path
    }

    private fun inBounds(c: GridCoord, level: LevelConfig): Boolean =
        c.x in 0 until level.width && c.y in 0 until level.height
}
