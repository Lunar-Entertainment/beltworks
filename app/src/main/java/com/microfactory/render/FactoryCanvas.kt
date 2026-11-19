package com.microfactory.render

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import com.microfactory.sim.Direction
import com.microfactory.sim.FactorySimulation
import com.microfactory.sim.GridCoord
import com.microfactory.sim.GridLayer
import com.microfactory.sim.ItemType
import com.microfactory.sim.MachinePowerState
import com.microfactory.sim.MachineStatus
import com.microfactory.sim.PowerTerrainTile
import com.microfactory.sim.PowerTerrainType
import com.microfactory.sim.PowerTile
import com.microfactory.sim.TileEntity
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

enum class BuildTool(
    val label: String,
    val shortHint: String,
    val unlockedFromLevel: Int = 1,
    val isPowerLayerTool: Boolean = false,
    val requiresPowerLevel: Boolean = false
) {
    BELT("Belt", "Drag to route & curve belts • Tap to rotate & connect", 1),
    SPLITTER("Splitter", "Alternates items 50/50 left & right • Tap to rotate", 1),
    BRIDGE("Bridge", "Crosses horizontal & vertical lanes without mixing • Tap to rotate", 9),
    SMELTER("Smelter", "Smelts Iron/Copper Ore → Ingot (2t) • Tap to rotate", 1),
    PRESS("Press", "Stamps Iron → Plate or Copper → Wire (2t) • Tap to rotate", 1),
    ASSEMBLER("Assembler", "Crafts Gear, Circuit, or Motor • Tap to rotate", 1),
    FILTER("Filter", "Routes matching item to green port • Pick item below", 31),
    OVERFLOW("Overflow", "Prioritizes main port; spills excess when full • Tap to rotate", 45),
    INCINERATOR("Trash", "Destroys unwanted items on contact to prevent jams", 35),
    OVERCLOCK(
        label = "Overclock",
        shortHint = "Tap Smelter, Press, Assembler, or Filter to toggle 2.0× speed (2.5× ⚡)",
        unlockedFromLevel = 1,
        isPowerLayerTool = false,
        requiresPowerLevel = true
    ),
    POWER_CABLE(
        label = "Cable",
        shortHint = "Drag or tap to route 4-way cables • >8 tiles loses 10% per 4 tiles",
        unlockedFromLevel = 1,
        isPowerLayerTool = true
    ),
    WIND_TURBINE(
        label = "Wind",
        shortHint = "Generates +3⚡ (+6⚡ on Wind Spot tiles on the perimeter)",
        unlockedFromLevel = 1,
        isPowerLayerTool = true
    ),
    HYDRO_GENERATOR(
        label = "Hydro",
        shortHint = "Generates +8⚡ steady • Must be placed on Water Current river tiles",
        unlockedFromLevel = 1,
        isPowerLayerTool = true
    ),
    CAPACITOR_BANK(
        label = "Capacitor",
        shortHint = "Stores 40⚡ surplus, discharges on deficit & resets cable distance loss",
        unlockedFromLevel = 1,
        isPowerLayerTool = true
    ),
    BULLDOZE("Bulldoze", "Tap or drag to remove components on active layer", 1);

    fun isVisibleFor(layer: GridLayer, levelId: Int, requiresPower: Boolean): Boolean {
        if (this == BULLDOZE) return true
        return when (layer) {
            GridLayer.FACTORY -> {
                !isPowerLayerTool &&
                    levelId >= unlockedFromLevel &&
                    (!requiresPowerLevel || requiresPower)
            }
            GridLayer.POWER -> isPowerLayerTool
        }
    }
}

private data class GridMetrics(
    val originX: Float,
    val originY: Float,
    val cellSize: Float,
    val width: Int,
    val height: Int
) {
    fun cellAt(offset: Offset): GridCoord? {
        val x = ((offset.x - originX) / cellSize).toInt()
        val y = ((offset.y - originY) / cellSize).toInt()
        if (offset.x < originX || offset.y < originY) return null
        if (x !in 0 until width || y !in 0 until height) return null
        return GridCoord(x, y)
    }

    fun cellTopLeft(coord: GridCoord): Offset =
        Offset(originX + coord.x * cellSize, originY + coord.y * cellSize)
}

private fun computeDualLayerMetrics(
    canvasSize: Size,
    factoryWidth: Int,
    factoryHeight: Int,
    layerTransition: Float,
    requiresPower: Boolean
): Pair<GridMetrics, GridMetrics> {
    val padding = 10f
    val availW = (canvasSize.width - padding * 2f).coerceAtLeast(1f)
    val availH = (canvasSize.height - padding * 2f).coerceAtLeast(1f)

    val powerW = factoryWidth + FactorySimulation.POWER_PERIMETER * 2
    val powerH = factoryHeight + FactorySimulation.POWER_PERIMETER * 2

    val baseW = if (requiresPower) factoryWidth + 0.9f else factoryWidth.toFloat()
    val baseH = if (requiresPower) factoryHeight + 0.9f else factoryHeight.toFloat()

    val effectiveW = lerp(baseW, powerW.toFloat(), layerTransition)
    val effectiveH = lerp(baseH, powerH.toFloat(), layerTransition)

    val cellSize = min(availW / effectiveW, availH / effectiveH)
    val factoryTotalW = cellSize * factoryWidth
    val factoryTotalH = cellSize * factoryHeight
    val factoryOriginX = (canvasSize.width - factoryTotalW) / 2f
    val factoryOriginY = (canvasSize.height - factoryTotalH) / 2f

    val powerOriginX = factoryOriginX - FactorySimulation.POWER_PERIMETER * cellSize
    val powerOriginY = factoryOriginY - FactorySimulation.POWER_PERIMETER * cellSize

    val factoryMetrics = GridMetrics(factoryOriginX, factoryOriginY, cellSize, factoryWidth, factoryHeight)
    val powerMetrics = GridMetrics(powerOriginX, powerOriginY, cellSize, powerW, powerH)
    return factoryMetrics to powerMetrics
}

@Composable
fun FactoryCanvas(
    simulation: FactorySimulation,
    activeLayer: GridLayer = GridLayer.FACTORY,
    selectedTool: BuildTool,
    selectedFilterItem: ItemType,
    activeFilterCoord: GridCoord?,
    activeMachineCoord: GridCoord? = null,
    helpRouteCoords: Set<GridCoord> = emptySet(),
    tickAlpha: Float,
    beltPhase: Float,
    renderVersion: Int,
    onBoardChanged: () -> Unit,
    onFilterSelected: (GridCoord?, ItemType?) -> Unit,
    onMachineSelected: (GridCoord?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentLayer by rememberUpdatedState(activeLayer)
    val currentTool by rememberUpdatedState(selectedTool)
    val currentFilterItem by rememberUpdatedState(selectedFilterItem)
    val currentOnBoardChanged by rememberUpdatedState(onBoardChanged)
    val currentOnFilterSelected by rememberUpdatedState(onFilterSelected)
    val currentOnMachineSelected by rememberUpdatedState(onMachineSelected)

    val targetZoom = if (simulation.requiresPower && activeLayer == GridLayer.POWER) 1f else 0f
    val layerTransition by animateFloatAsState(
        targetValue = targetZoom,
        animationSpec = tween(durationMillis = 260),
        label = "powerLayerZoom"
    )
    val currentTransition by rememberUpdatedState(layerTransition)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(simulation) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val (factoryMetrics, powerMetrics) = computeDualLayerMetrics(
                        canvasSize = Size(size.width.toFloat(), size.height.toFloat()),
                        factoryWidth = simulation.width,
                        factoryHeight = simulation.height,
                        layerTransition = currentTransition,
                        requiresPower = simulation.requiresPower
                    )
                    val isPowerLayer = simulation.requiresPower && currentLayer == GridLayer.POWER
                    val activeMetrics = if (isPowerLayer) powerMetrics else factoryMetrics
                    val startCell = activeMetrics.cellAt(down.position) ?: return@awaitEachGesture
                    down.consume()

                    var lastCell = startCell
                    var movedOutOfStart = false
                    var prevBeltCell: GridCoord? = null

                    if (currentTool == BuildTool.BULLDOZE) {
                        val cleared = if (isPowerLayer) {
                            simulation.clearPowerTile(startCell)
                        } else {
                            simulation.clearTile(startCell)
                        }
                        if (cleared) {
                            currentOnBoardChanged()
                        }
                    }

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id }
                            ?: event.changes.firstOrNull()
                            ?: break

                        if (!change.pressed) {
                            change.consume()
                            break
                        }

                        val hovered = activeMetrics.cellAt(change.position)
                        if (hovered != null && hovered != lastCell) {
                            movedOutOfStart = true
                            val steps = orthogonalStepsBetween(lastCell, hovered)
                            for (nextCell in steps) {
                                if (isPowerLayer) {
                                    when (currentTool) {
                                        BuildTool.POWER_CABLE -> {
                                            var changed = false
                                            if (simulation.getPowerTile(lastCell) == PowerTile.Empty) {
                                                changed = simulation.setPowerTile(lastCell, PowerTile.PowerCable()) || changed
                                            }
                                            if (simulation.getPowerTile(nextCell) == PowerTile.Empty) {
                                                changed = simulation.setPowerTile(nextCell, PowerTile.PowerCable()) || changed
                                            }
                                            if (changed) currentOnBoardChanged()
                                        }
                                        BuildTool.BULLDOZE -> {
                                            if (simulation.clearPowerTile(nextCell)) {
                                                currentOnBoardChanged()
                                            }
                                        }
                                        else -> Unit
                                    }
                                } else {
                                    when (currentTool) {
                                        BuildTool.BELT -> {
                                            val changed = handleBeltDragStep(
                                                sim = simulation,
                                                fromCell = prevBeltCell ?: lastCell,
                                                toCell = nextCell
                                            )
                                            if (changed) {
                                                currentOnBoardChanged()
                                            }
                                            prevBeltCell = nextCell
                                        }

                                        BuildTool.BULLDOZE -> {
                                            if (simulation.clearTile(nextCell)) {
                                                currentOnBoardChanged()
                                            }
                                        }

                                        else -> Unit
                                    }
                                }
                                lastCell = nextCell
                            }
                            change.consume()
                        }
                    }

                    if (!movedOutOfStart && currentTool != BuildTool.BULLDOZE) {
                        if (isPowerLayer) {
                            val powerTile = createPowerTileForTool(currentTool)
                            if (powerTile != null) {
                                val existingPower = simulation.getPowerTile(startCell)
                                if (existingPower == PowerTile.Empty || existingPower::class != powerTile::class) {
                                    if (simulation.setPowerTile(startCell, powerTile)) {
                                        currentOnBoardChanged()
                                    }
                                }
                            }
                        } else {
                            val existing = simulation.getTile(startCell)
                            if (currentTool == BuildTool.OVERCLOCK) {
                                if (existing.isPowerConsumer() && simulation.toggleOverclock(startCell)) {
                                    currentOnMachineSelected(startCell)
                                    currentOnBoardChanged()
                                }
                            } else if (existing.isPlayerEditable()) {
                                val cellTopLeft = factoryMetrics.cellTopLeft(startCell)
                                val localX = down.position.x - cellTopLeft.x
                                val localY = down.position.y - cellTopLeft.y
                                val tappedOverclockBadge = simulation.requiresPower &&
                                    existing.isPowerConsumer() &&
                                    localX >= factoryMetrics.cellSize * 0.56f &&
                                    localY <= factoryMetrics.cellSize * 0.44f

                                if (tappedOverclockBadge) {
                                    if (simulation.toggleOverclock(startCell)) {
                                        currentOnMachineSelected(startCell)
                                        currentOnBoardChanged()
                                    }
                                } else if (simulation.rotateTile(startCell)) {
                                    val updated = simulation.getTile(startCell)
                                    if (updated is TileEntity.Filter) {
                                        currentOnFilterSelected(startCell, updated.filterItem)
                                    } else {
                                        currentOnFilterSelected(null, null)
                                    }
                                    if (simulation.requiresPower && updated.isPowerConsumer()) {
                                        currentOnMachineSelected(startCell)
                                    } else {
                                        currentOnMachineSelected(null)
                                    }
                                    currentOnBoardChanged()
                                }
                            } else if (existing == TileEntity.Empty) {
                                val placed = createDefaultTileForTool(
                                    sim = simulation,
                                    coord = startCell,
                                    tool = currentTool,
                                    filterItem = currentFilterItem
                                )
                                if (placed != null) {
                                    simulation.setTile(startCell, placed)
                                    if (placed is TileEntity.Filter) {
                                        currentOnFilterSelected(startCell, placed.filterItem)
                                    } else {
                                        currentOnFilterSelected(null, null)
                                    }
                                    if (simulation.requiresPower && placed.isPowerConsumer()) {
                                        currentOnMachineSelected(startCell)
                                    } else {
                                        currentOnMachineSelected(null)
                                    }
                                    val outDir = when (placed) {
                                        is TileEntity.Belt -> placed.outputDir
                                        is TileEntity.Smelter -> placed.outputDir
                                        is TileEntity.Press -> placed.outputDir
                                        is TileEntity.Assembler -> placed.outputDir
                                        else -> null
                                    }
                                    if (outDir != null) {
                                        simulation.autoConnectDownstreamBelt(startCell, outDir)
                                    }
                                    currentOnBoardChanged()
                                }
                            }
                        }
                    }
                }
            }
    ) {
        @Suppress("UNUSED_EXPRESSION")
        renderVersion

        val (factoryMetrics, powerMetrics) = computeDualLayerMetrics(
            canvasSize = size,
            factoryWidth = simulation.width,
            factoryHeight = simulation.height,
            layerTransition = layerTransition,
            requiresPower = simulation.requiresPower
        )

        val viewingPowerLayer = simulation.requiresPower && (activeLayer == GridLayer.POWER || layerTransition > 0.5f)

        // When level requires power, render the outer/underneath Power Layer context
        if (simulation.requiresPower) {
            val powerFrameAlpha = lerp(0.32f, 1.0f, layerTransition)
            drawRoundRect(
                color = FactoryColors.PowerPerimeterCell.copy(alpha = powerFrameAlpha),
                topLeft = Offset(powerMetrics.originX - 6f, powerMetrics.originY - 6f),
                size = Size(
                    powerMetrics.cellSize * simulation.powerWidth + 12f,
                    powerMetrics.cellSize * simulation.powerHeight + 12f
                ),
                cornerRadius = CornerRadius(14f)
            )
            drawRoundRect(
                color = FactoryColors.PowerGridBorder.copy(alpha = powerFrameAlpha),
                topLeft = Offset(powerMetrics.originX - 6f, powerMetrics.originY - 6f),
                size = Size(
                    powerMetrics.cellSize * simulation.powerWidth + 12f,
                    powerMetrics.cellSize * simulation.powerHeight + 12f
                ),
                cornerRadius = CornerRadius(14f),
                style = Stroke(width = 2f)
            )

            for (py in 0 until simulation.powerHeight) {
                for (px in 0 until simulation.powerWidth) {
                    val pCoord = GridCoord(px, py)
                    val isPerimeter = simulation.isOuterPerimeter(pCoord)
                    val cellAlpha = if (viewingPowerLayer) {
                        1.0f
                    } else if (isPerimeter) {
                        0.38f
                    } else {
                        0.22f
                    }
                    val topLeft = powerMetrics.cellTopLeft(pCoord)
                    val terrain = simulation.getPowerTerrain(pCoord)
                    drawPowerCellBackground(
                        topLeft = topLeft,
                        cellSize = powerMetrics.cellSize,
                        terrain = terrain,
                        isPerimeter = isPerimeter,
                        beltPhase = beltPhase,
                        alpha = cellAlpha
                    )
                }
            }
        }

        if (!viewingPowerLayer) {
            // Factory Layer primary rendering (100% opacity)
            drawRoundRect(
                color = FactoryColors.SurfaceElevated,
                topLeft = Offset(factoryMetrics.originX - 6f, factoryMetrics.originY - 6f),
                size = Size(
                    factoryMetrics.cellSize * simulation.width + 12f,
                    factoryMetrics.cellSize * simulation.height + 12f
                ),
                cornerRadius = CornerRadius(14f)
            )
            drawRoundRect(
                color = FactoryColors.GridOuterFrame,
                topLeft = Offset(factoryMetrics.originX - 6f, factoryMetrics.originY - 6f),
                size = Size(
                    factoryMetrics.cellSize * simulation.width + 12f,
                    factoryMetrics.cellSize * simulation.height + 12f
                ),
                cornerRadius = CornerRadius(14f),
                style = Stroke(width = 2f)
            )

            // Draw perimeter power tiles dimmed around the Factory frame when on Factory Layer
            if (simulation.requiresPower) {
                for (py in 0 until simulation.powerHeight) {
                    for (px in 0 until simulation.powerWidth) {
                        val pCoord = GridCoord(px, py)
                        if (simulation.isOuterPerimeter(pCoord)) {
                            val pTile = simulation.getPowerTile(pCoord)
                            if (pTile != PowerTile.Empty) {
                                drawPowerTileEntity(
                                    topLeft = powerMetrics.cellTopLeft(pCoord),
                                    cellSize = powerMetrics.cellSize,
                                    tile = pTile,
                                    terrain = simulation.getPowerTerrain(pCoord),
                                    capacitorCharge = simulation.getCapacitorCharge(pCoord),
                                    transmissionEfficiency = simulation.getTransmissionEfficiency(pCoord),
                                    hasMachineAbove = false,
                                    beltPhase = beltPhase,
                                    alpha = 0.45f
                                )
                            }
                        }
                    }
                }
            }

            // Pass 1: Grid cells, floor machines, and Bridge lower horizontal tracks
            val bridges = mutableListOf<Pair<GridCoord, TileEntity.Bridge>>()
            for (y in 0 until simulation.height) {
                for (x in 0 until simulation.width) {
                    val coord = GridCoord(x, y)
                    val topLeft = factoryMetrics.cellTopLeft(coord)
                    val cellPad = factoryMetrics.cellSize * 0.045f
                    val innerSize = factoryMetrics.cellSize - cellPad * 2f

                    drawRoundRect(
                        color = FactoryColors.GridCell,
                        topLeft = topLeft + Offset(cellPad, cellPad),
                        size = Size(innerSize, innerSize),
                        cornerRadius = CornerRadius(factoryMetrics.cellSize * 0.12f)
                    )
                    val isHighlighted = coord == activeFilterCoord || coord == activeMachineCoord
                    val highlightColor = when (coord) {
                        activeFilterCoord -> FactoryColors.FilterAccent
                        activeMachineCoord -> FactoryColors.OverclockCyan
                        else -> FactoryColors.GridCellBorder
                    }
                    drawRoundRect(
                        color = highlightColor,
                        topLeft = topLeft + Offset(cellPad, cellPad),
                        size = Size(innerSize, innerSize),
                        cornerRadius = CornerRadius(factoryMetrics.cellSize * 0.12f),
                        style = Stroke(width = if (isHighlighted) 2.2f else 1.2f)
                    )

                    // Subtle under-floor power indicator when a cable/capacitor sits underneath this cell
                    if (simulation.requiresPower) {
                        val underPowerCoord = simulation.factoryToPower(coord)
                        val underPowerTile = simulation.getPowerTile(underPowerCoord)
                        if (underPowerTile != PowerTile.Empty) {
                            drawPowerTileEntity(
                                topLeft = topLeft,
                                cellSize = factoryMetrics.cellSize,
                                tile = underPowerTile,
                                terrain = simulation.getPowerTerrain(underPowerCoord),
                                capacitorCharge = simulation.getCapacitorCharge(underPowerCoord),
                                transmissionEfficiency = simulation.getTransmissionEfficiency(underPowerCoord),
                                hasMachineAbove = simulation.getTile(coord).isPowerConsumer(),
                                beltPhase = beltPhase,
                                alpha = 0.25f
                            )
                        }
                    }

                    val tile = simulation.getTile(coord)
                    when (tile) {
                        TileEntity.Empty -> {
                            if (coord in helpRouteCoords) {
                                val center = topLeft + Offset(factoryMetrics.cellSize * 0.5f, factoryMetrics.cellSize * 0.5f)
                                drawCircle(
                                    color = FactoryColors.BeltChevron.copy(alpha = 0.22f),
                                    radius = factoryMetrics.cellSize * 0.14f,
                                    center = center
                                )
                                drawCircle(
                                    color = FactoryColors.BeltChevron.copy(alpha = 0.65f),
                                    radius = factoryMetrics.cellSize * 0.065f,
                                    center = center
                                )
                            }
                        }
                        TileEntity.Rock -> drawRockTile(topLeft, factoryMetrics.cellSize)
                        is TileEntity.Belt -> drawBeltTile(topLeft, factoryMetrics.cellSize, tile, beltPhase)
                        is TileEntity.Splitter -> drawSplitterTile(topLeft, factoryMetrics.cellSize, tile)
                        is TileEntity.Bridge -> {
                            drawBridgeUnderpass(topLeft, factoryMetrics.cellSize, tile, beltPhase)
                            bridges.add(coord to tile)
                        }
                        is TileEntity.Filter -> drawFilterTile(topLeft, factoryMetrics.cellSize, tile)
                        is TileEntity.Overflow -> drawOverflowTile(topLeft, factoryMetrics.cellSize, tile)
                        TileEntity.Incinerator -> drawIncineratorTile(topLeft, factoryMetrics.cellSize)
                        is TileEntity.Smelter -> drawSmelterTile(topLeft, factoryMetrics.cellSize, tile, simulation.getMachineStatus(coord))
                        is TileEntity.Press -> drawPressTile(topLeft, factoryMetrics.cellSize, tile, simulation.getMachineStatus(coord))
                        is TileEntity.Assembler -> drawAssemblerTile(topLeft, factoryMetrics.cellSize, tile, simulation.getMachineStatus(coord))
                        is TileEntity.Spawner -> drawSpawnerTile(topLeft, factoryMetrics.cellSize, tile)
                        is TileEntity.Hatch -> drawHatchTile(topLeft, factoryMetrics.cellSize, tile)
                    }

                    if ((simulation.requiresPower || tile.isOverclocked()) && tile.isPowerConsumer()) {
                        drawMachinePowerBadge(
                            topLeft = topLeft,
                            cellSize = factoryMetrics.cellSize,
                            powerState = simulation.getMachinePowerState(coord),
                            isOverclocked = tile.isOverclocked()
                        )
                    }
                }
            }

            val itemRadius = factoryMetrics.cellSize * 0.25f
            val underpassItems = mutableListOf<com.microfactory.sim.MovingItem>()
            val normalOrOverpassItems = mutableListOf<com.microfactory.sim.MovingItem>()
            for (item in simulation.items) {
                val tile = simulation.getTile(item.currentCoord)
                if (tile is TileEntity.Bridge && !simulation.isItemOnVerticalBridgeLane(item.id)) {
                    underpassItems.add(item)
                } else {
                    normalOrOverpassItems.add(item)
                }
            }

            // Pass 2: Underpass items passing beneath Bridge decks
            for (item in underpassItems) {
                val ix = lerp(item.prevCoord.x.toFloat(), item.currentCoord.x.toFloat(), tickAlpha)
                val iy = lerp(item.prevCoord.y.toFloat(), item.currentCoord.y.toFloat(), tickAlpha)
                val center = Offset(
                    factoryMetrics.originX + (ix + 0.5f) * factoryMetrics.cellSize,
                    factoryMetrics.originY + (iy + 0.5f) * factoryMetrics.cellSize
                )
                drawItemShape(item.type, center, itemRadius * 0.9f)
            }

            // Pass 3: Elevated Bridge vertical overpass decks
            for ((coord, bridge) in bridges) {
                drawBridgeOverpass(factoryMetrics.cellTopLeft(coord), factoryMetrics.cellSize, bridge, beltPhase)
            }

            // Pass 4: Consumed glide-in items + active belt/overpass items
            for (item in simulation.consumedVisualItems) {
                val ix = lerp(item.prevCoord.x.toFloat(), item.currentCoord.x.toFloat(), tickAlpha)
                val iy = lerp(item.prevCoord.y.toFloat(), item.currentCoord.y.toFloat(), tickAlpha)
                val center = Offset(
                    factoryMetrics.originX + (ix + 0.5f) * factoryMetrics.cellSize,
                    factoryMetrics.originY + (iy + 0.5f) * factoryMetrics.cellSize
                )
                val scale = (1f - tickAlpha * 0.45f).coerceIn(0.4f, 1f)
                drawItemShape(item.type, center, itemRadius * scale)
            }

            for (item in normalOrOverpassItems) {
                val ix = lerp(item.prevCoord.x.toFloat(), item.currentCoord.x.toFloat(), tickAlpha)
                val iy = lerp(item.prevCoord.y.toFloat(), item.currentCoord.y.toFloat(), tickAlpha)
                val center = Offset(
                    factoryMetrics.originX + (ix + 0.5f) * factoryMetrics.cellSize,
                    factoryMetrics.originY + (iy + 0.5f) * factoryMetrics.cellSize
                )
                drawItemShape(item.type, center, itemRadius)
            }
        } else {
            // Power Layer primary rendering:
            // 1. Inner factory floor boundary outline
            drawRoundRect(
                color = FactoryColors.BlueprintSilhouette.copy(alpha = 0.45f),
                topLeft = Offset(factoryMetrics.originX - 2f, factoryMetrics.originY - 2f),
                size = Size(
                    factoryMetrics.cellSize * simulation.width + 4f,
                    factoryMetrics.cellSize * simulation.height + 4f
                ),
                cornerRadius = CornerRadius(10f),
                style = Stroke(width = 1.8f)
            )

            // 2. Semi-transparent Factory Layer blueprint silhouettes showing ⚡ draw
            for (fy in 0 until simulation.height) {
                for (fx in 0 until simulation.width) {
                    val fCoord = GridCoord(fx, fy)
                    val fTile = simulation.getTile(fCoord)
                    if (fTile != TileEntity.Empty) {
                        val topLeft = factoryMetrics.cellTopLeft(fCoord)
                        drawFactoryBlueprintSilhouette(
                            topLeft = topLeft,
                            cellSize = factoryMetrics.cellSize,
                            tile = fTile,
                            powerState = simulation.getMachinePowerState(fCoord)
                        )
                    }
                }
            }

            // 3. Full-opacity Power Layer tiles (WindTurbine, HydroGenerator, PowerCable, CapacitorBank)
            for (py in 0 until simulation.powerHeight) {
                for (px in 0 until simulation.powerWidth) {
                    val pCoord = GridCoord(px, py)
                    val pTile = simulation.getPowerTile(pCoord)
                    if (pTile != PowerTile.Empty) {
                        val fCoord = simulation.powerToFactory(pCoord)
                        val hasMachineAbove = fCoord != null && simulation.getTile(fCoord).isPowerConsumer()
                        drawPowerTileEntity(
                            topLeft = powerMetrics.cellTopLeft(pCoord),
                            cellSize = powerMetrics.cellSize,
                            tile = pTile,
                            terrain = simulation.getPowerTerrain(pCoord),
                            capacitorCharge = simulation.getCapacitorCharge(pCoord),
                            transmissionEfficiency = simulation.getTransmissionEfficiency(pCoord),
                            hasMachineAbove = hasMachineAbove,
                            beltPhase = beltPhase,
                            alpha = 1.0f
                        )
                    }
                }
            }
        }
    }
}

private fun createPowerTileForTool(tool: BuildTool): PowerTile? = when (tool) {
    BuildTool.POWER_CABLE -> PowerTile.PowerCable()
    BuildTool.WIND_TURBINE -> PowerTile.WindTurbine
    BuildTool.HYDRO_GENERATOR -> PowerTile.HydroGenerator()
    BuildTool.CAPACITOR_BANK -> PowerTile.CapacitorBank
    else -> null
}

private fun orthogonalStepsBetween(from: GridCoord, to: GridCoord): List<GridCoord> {
    val steps = mutableListOf<GridCoord>()
    var cx = from.x
    var cy = from.y
    while (cx != to.x || cy != to.y) {
        if (abs(to.x - cx) >= abs(to.y - cy)) {
            cx += if (to.x > cx) 1 else -1
        } else {
            cy += if (to.y > cy) 1 else -1
        }
        steps.add(GridCoord(cx, cy))
    }
    return steps
}

private fun handleBeltDragStep(
    sim: FactorySimulation,
    fromCell: GridCoord,
    toCell: GridCoord
): Boolean {
    val moveDir = fromCell.directionTo(toCell) ?: return false
    var changed = false

    val fromTile = sim.getTile(fromCell)
    when (fromTile) {
        TileEntity.Empty -> {
            val inferredInput = findIncomingDirection(sim, fromCell, exclude = moveDir) ?: moveDir.opposite()
            sim.setTile(fromCell, TileEntity.Belt(inputDir = inferredInput, outputDir = moveDir))
            changed = true
        }
        is TileEntity.Belt -> {
            val newInput = if (fromTile.inputDir == moveDir) moveDir.opposite() else fromTile.inputDir
            if (fromTile.inputDir != newInput || fromTile.outputDir != moveDir) {
                sim.setTile(fromCell, TileEntity.Belt(inputDir = newInput, outputDir = moveDir))
                changed = true
            }
        }
        else -> Unit
    }

    val toTile = sim.getTile(toCell)
    if (toTile == TileEntity.Empty || toTile is TileEntity.Belt) {
        val newBelt = TileEntity.Belt(inputDir = moveDir.opposite(), outputDir = moveDir)
        if (toTile != newBelt) {
            sim.setTile(toCell, newBelt)
            sim.autoConnectDownstreamBelt(toCell, moveDir)
            changed = true
        }
    }

    return changed
}

private fun findIncomingDirection(
    sim: FactorySimulation,
    coord: GridCoord,
    exclude: Direction? = null
): Direction? {
    for (dir in Direction.entries) {
        if (dir == exclude) continue
        val neighbor = coord + dir
        val expectedOutput = dir.opposite()
        val pointsIntoCell = when (val t = sim.getTile(neighbor)) {
            is TileEntity.Spawner -> t.outputDir == expectedOutput
            is TileEntity.Belt -> t.outputDir == expectedOutput
            is TileEntity.Smelter -> t.outputDir == expectedOutput
            is TileEntity.Press -> t.outputDir == expectedOutput
            is TileEntity.Assembler -> t.outputDir == expectedOutput
            is TileEntity.Splitter -> t.outputLeft == expectedOutput || t.outputRight == expectedOutput
            is TileEntity.Filter -> t.matchDir == expectedOutput || t.otherDir == expectedOutput
            is TileEntity.Overflow -> t.primaryDir == expectedOutput || t.overflowDir == expectedOutput
            is TileEntity.Bridge -> t.horizDir == expectedOutput || t.vertDir == expectedOutput
            else -> false
        }
        if (pointsIntoCell) return dir
    }
    return null
}

private fun createDefaultTileForTool(
    sim: FactorySimulation,
    coord: GridCoord,
    tool: BuildTool,
    filterItem: ItemType
): TileEntity? {
    val incoming = findIncomingDirection(sim, coord)
    val defaultInput = incoming ?: Direction.WEST
    val defaultOutput = defaultInput.opposite()
    return when (tool) {
        BuildTool.BELT -> TileEntity.Belt(inputDir = defaultInput, outputDir = defaultOutput)
        BuildTool.SPLITTER -> {
            val inDir = incoming ?: Direction.NORTH
            TileEntity.Splitter(
                inputDir = inDir,
                outputLeft = inDir.rotateCounterClockwise(),
                outputRight = inDir.rotateClockwise()
            )
        }
        BuildTool.BRIDGE -> TileEntity.Bridge(horizDir = Direction.EAST, vertDir = Direction.SOUTH)
        BuildTool.FILTER -> {
            val inDir = incoming ?: Direction.NORTH
            TileEntity.Filter(
                inputDir = inDir,
                matchDir = inDir.rotateCounterClockwise(),
                otherDir = inDir.rotateClockwise(),
                filterItem = filterItem
            )
        }
        BuildTool.OVERFLOW -> {
            val inDir = incoming ?: Direction.NORTH
            TileEntity.Overflow(
                inputDir = inDir,
                primaryDir = inDir.opposite(),
                overflowDir = inDir.rotateClockwise()
            )
        }
        BuildTool.INCINERATOR -> TileEntity.Incinerator
        BuildTool.SMELTER -> TileEntity.Smelter(inputDir = defaultInput, outputDir = defaultOutput)
        BuildTool.PRESS -> TileEntity.Press(inputDir = defaultInput, outputDir = defaultOutput)
        BuildTool.ASSEMBLER -> TileEntity.Assembler(
            inputDirA = Direction.WEST,
            inputDirB = Direction.EAST,
            outputDir = Direction.SOUTH
        )
        BuildTool.OVERCLOCK,
        BuildTool.POWER_CABLE,
        BuildTool.WIND_TURBINE,
        BuildTool.HYDRO_GENERATOR,
        BuildTool.CAPACITOR_BANK,
        BuildTool.BULLDOZE -> null
    }
}

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

private fun edgeMidpoint(topLeft: Offset, cellSize: Float, dir: Direction): Offset {
    val half = cellSize / 2f
    return when (dir) {
        Direction.NORTH -> Offset(topLeft.x + half, topLeft.y)
        Direction.EAST -> Offset(topLeft.x + cellSize, topLeft.y + half)
        Direction.SOUTH -> Offset(topLeft.x + half, topLeft.y + cellSize)
        Direction.WEST -> Offset(topLeft.x, topLeft.y + half)
    }
}

private fun directionAngleDeg(dir: Direction): Float = when (dir) {
    Direction.EAST -> 0f
    Direction.SOUTH -> 90f
    Direction.WEST -> 180f
    Direction.NORTH -> 270f
}

private fun DrawScope.drawPowerCellBackground(
    topLeft: Offset,
    cellSize: Float,
    terrain: PowerTerrainTile,
    isPerimeter: Boolean,
    beltPhase: Float,
    alpha: Float
) {
    val cellPad = cellSize * 0.045f
    val innerSize = cellSize - cellPad * 2f
    val baseColor = when (terrain.type) {
        PowerTerrainType.WIND_SPOT -> FactoryColors.WindSpotBg
        PowerTerrainType.WATER_CURRENT -> FactoryColors.WaterCurrentBg
        PowerTerrainType.ROCK -> FactoryColors.PowerGridCell
        PowerTerrainType.NORMAL -> if (isPerimeter) FactoryColors.PowerPerimeterCell else FactoryColors.PowerGridCell
    }
    val borderColor = when (terrain.type) {
        PowerTerrainType.WIND_SPOT -> FactoryColors.WindSpotAccent.copy(alpha = 0.65f * alpha)
        PowerTerrainType.WATER_CURRENT -> FactoryColors.WaterCurrentFlow.copy(alpha = 0.7f * alpha)
        else -> FactoryColors.PowerGridBorder.copy(alpha = alpha)
    }

    drawRoundRect(
        color = baseColor.copy(alpha = alpha),
        topLeft = topLeft + Offset(cellPad, cellPad),
        size = Size(innerSize, innerSize),
        cornerRadius = CornerRadius(cellSize * 0.12f)
    )
    drawRoundRect(
        color = borderColor,
        topLeft = topLeft + Offset(cellPad, cellPad),
        size = Size(innerSize, innerSize),
        cornerRadius = CornerRadius(cellSize * 0.12f),
        style = Stroke(width = 1.1f)
    )

    when (terrain.type) {
        PowerTerrainType.WIND_SPOT -> {
            val cx = topLeft.x + cellSize * 0.5f
            val cy = topLeft.y + cellSize * 0.5f
            for (i in -1..1) {
                val yOff = i * cellSize * 0.20f
                val slide = ((beltPhase + (i + 1) * 0.33f) % 1f) * 2f - 1f
                val startX = cx - cellSize * 0.28f + slide * cellSize * 0.06f
                val endX = cx + cellSize * 0.28f + slide * cellSize * 0.06f
                drawLine(
                    color = FactoryColors.WindSpotAccent.copy(alpha = 0.55f * alpha),
                    start = Offset(startX, cy + yOff),
                    end = Offset(endX, cy + yOff),
                    strokeWidth = (cellSize * 0.045f).coerceAtLeast(1.5f),
                    cap = StrokeCap.Round
                )
            }
        }

        PowerTerrainType.WATER_CURRENT -> {
            val start = edgeMidpoint(topLeft, cellSize, terrain.flowDir.opposite())
            val end = edgeMidpoint(topLeft, cellSize, terrain.flowDir)
            val angle = directionAngleDeg(terrain.flowDir)
            val dx = end.x - start.x
            val dy = end.y - start.y
            for (i in 0 until 2) {
                val t = (i / 2f + beltPhase) % 1f
                val pos = Offset(
                    lerp(start.x, end.x, 0.2f + 0.6f * t),
                    lerp(start.y, end.y, 0.2f + 0.6f * t)
                )
                drawChevron(
                    center = pos,
                    angleDeg = angle,
                    size = cellSize * 0.13f,
                    color = FactoryColors.WaterCurrentFoam.copy(alpha = 0.65f * alpha)
                )
            }
            // Subtle river wave lines
            drawLine(
                color = FactoryColors.WaterCurrentFlow.copy(alpha = 0.35f * alpha),
                start = Offset(start.x + dx * 0.15f, start.y + dy * 0.15f),
                end = Offset(start.x + dx * 0.85f, start.y + dy * 0.85f),
                strokeWidth = cellSize * 0.22f,
                cap = StrokeCap.Round
            )
        }

        PowerTerrainType.ROCK -> {
            drawRockTile(topLeft, cellSize, alpha = alpha)
        }

        PowerTerrainType.NORMAL -> Unit
    }
}

private fun DrawScope.drawFactoryBlueprintSilhouette(
    topLeft: Offset,
    cellSize: Float,
    tile: TileEntity,
    powerState: MachinePowerState
) {
    val pad = cellSize * 0.13f
    val inner = cellSize - pad * 2f
    val center = Offset(topLeft.x + cellSize * 0.5f, topLeft.y + cellSize * 0.5f)

    when (tile) {
        is TileEntity.Belt, is TileEntity.Bridge -> {
            drawCircle(
                color = FactoryColors.BlueprintSilhouette.copy(alpha = 0.16f),
                radius = cellSize * 0.12f,
                center = center
            )
        }
        TileEntity.Rock -> {
            drawRoundRect(
                color = FactoryColors.RockBase.copy(alpha = 0.22f),
                topLeft = topLeft + Offset(pad, pad),
                size = Size(inner, inner),
                cornerRadius = CornerRadius(cellSize * 0.14f)
            )
        }
        else -> {
            val isConsumer = tile.isPowerConsumer()
            val outlineColor = if (isConsumer) {
                when (powerState) {
                    MachinePowerState.OVERCLOCKED -> FactoryColors.OverclockCyan
                    MachinePowerState.POWERED -> FactoryColors.HatchDoneAccent
                    MachinePowerState.BROWNOUT -> FactoryColors.HatchAccent
                    MachinePowerState.UNPOWERED -> FactoryColors.BlueprintSilhouette
                }
            } else {
                FactoryColors.TextMuted
            }
            drawRoundRect(
                color = outlineColor.copy(alpha = if (isConsumer) 0.16f else 0.08f),
                topLeft = topLeft + Offset(pad, pad),
                size = Size(inner, inner),
                cornerRadius = CornerRadius(cellSize * 0.16f)
            )
            drawRoundRect(
                color = outlineColor.copy(alpha = if (isConsumer) 0.55f else 0.28f),
                topLeft = topLeft + Offset(pad, pad),
                size = Size(inner, inner),
                cornerRadius = CornerRadius(cellSize * 0.16f),
                style = Stroke(width = 1.5f)
            )
            if (isConsumer) {
                drawLightningBolt(
                    center = center,
                    size = cellSize * 0.24f,
                    color = outlineColor.copy(alpha = 0.85f)
                )
            }
        }
    }
}

private fun DrawScope.drawPowerTileEntity(
    topLeft: Offset,
    cellSize: Float,
    tile: PowerTile,
    terrain: PowerTerrainTile,
    capacitorCharge: Float,
    transmissionEfficiency: Float,
    hasMachineAbove: Boolean,
    beltPhase: Float,
    alpha: Float
) {
    val center = Offset(topLeft.x + cellSize * 0.5f, topLeft.y + cellSize * 0.5f)

    when (tile) {
        PowerTile.Empty -> Unit

        is PowerTile.PowerCable -> {
            val trackW = cellSize * 0.26f
            val coreW = cellSize * 0.11f
            val hasLoss = transmissionEfficiency in 0.01f..0.99f
            val coreColor = when {
                transmissionEfficiency <= 0.001f -> FactoryColors.TextMuted
                hasLoss -> FactoryColors.CableLossAmber
                else -> FactoryColors.CableCore
            }.copy(alpha = alpha)

            // Draw connected arms for each of the 4 directions
            for (dir in Direction.entries) {
                if (tile.isConnected(dir)) {
                    val edge = edgeMidpoint(topLeft, cellSize, dir)
                    drawLine(
                        color = FactoryColors.CableTrack.copy(alpha = alpha),
                        start = center,
                        end = edge,
                        strokeWidth = trackW,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = coreColor,
                        start = center,
                        end = edge,
                        strokeWidth = coreW,
                        cap = StrokeCap.Round
                    )
                    if (transmissionEfficiency > 0f) {
                        val pulsePos = Offset(
                            lerp(center.x, edge.x, beltPhase),
                            lerp(center.y, edge.y, beltPhase)
                        )
                        drawCircle(
                            color = FactoryColors.CablePulse.copy(alpha = alpha),
                            radius = cellSize * 0.065f,
                            center = pulsePos
                        )
                    }
                }
            }

            // Center cable hub node
            drawCircle(
                color = FactoryColors.CableTrack.copy(alpha = alpha),
                radius = cellSize * 0.18f,
                center = center
            )
            drawCircle(
                color = coreColor,
                radius = cellSize * 0.10f,
                center = center
            )

            // Vertical power feed socket when a Layer 0 machine sits directly above this cable
            if (hasMachineAbove) {
                drawCircle(
                    color = FactoryColors.OverclockCyan.copy(alpha = 0.85f * alpha),
                    radius = cellSize * 0.23f,
                    center = center,
                    style = Stroke(width = (cellSize * 0.045f).coerceAtLeast(1.6f))
                )
            }
        }

        PowerTile.WindTurbine -> {
            val isBoosted = terrain.type == PowerTerrainType.WIND_SPOT
            val accent = if (isBoosted) FactoryColors.OverclockCyan else FactoryColors.WindTurbineAccent
            drawMachineChassis(
                topLeft = topLeft,
                cellSize = cellSize,
                bodyColor = FactoryColors.WindTurbineBody.copy(alpha = alpha),
                accentColor = accent.copy(alpha = alpha)
            )
            if (isBoosted) {
                drawCircle(
                    color = FactoryColors.OverclockCyan.copy(alpha = 0.22f * alpha),
                    radius = cellSize * 0.34f,
                    center = center
                )
            }
            val spinDeg = (beltPhase * 360f * if (isBoosted) 2f else 1f) % 360f
            val bladeLen = cellSize * 0.32f
            rotate(degrees = spinDeg, pivot = center) {
                for (b in 0 until 3) {
                    val rad = (b * 120.0) * PI / 180.0
                    val tip = Offset(
                        center.x + cos(rad).toFloat() * bladeLen,
                        center.y + sin(rad).toFloat() * bladeLen
                    )
                    drawLine(
                        color = FactoryColors.WindTurbineBlade.copy(alpha = alpha),
                        start = center,
                        end = tip,
                        strokeWidth = (cellSize * 0.075f).coerceAtLeast(2.2f),
                        cap = StrokeCap.Round
                    )
                }
            }
            drawCircle(
                color = accent.copy(alpha = alpha),
                radius = cellSize * 0.10f,
                center = center
            )
        }

        is PowerTile.HydroGenerator -> {
            drawMachineChassis(
                topLeft = topLeft,
                cellSize = cellSize,
                bodyColor = FactoryColors.HydroBody.copy(alpha = alpha),
                accentColor = FactoryColors.HydroAccent.copy(alpha = alpha)
            )
            val spinDeg = (beltPhase * 360f) % 360f
            val wheelR = cellSize * 0.26f
            drawCircle(
                color = FactoryColors.HydroAccent.copy(alpha = 0.85f * alpha),
                radius = wheelR,
                center = center,
                style = Stroke(width = (cellSize * 0.055f).coerceAtLeast(2f))
            )
            rotate(degrees = spinDeg, pivot = center) {
                for (p in 0 until 4) {
                    val rad = (p * 90.0) * PI / 180.0
                    val p1 = Offset(
                        center.x - cos(rad).toFloat() * wheelR,
                        center.y - sin(rad).toFloat() * wheelR
                    )
                    val p2 = Offset(
                        center.x + cos(rad).toFloat() * wheelR,
                        center.y + sin(rad).toFloat() * wheelR
                    )
                    drawLine(
                        color = FactoryColors.HydroRotor.copy(alpha = alpha),
                        start = p1,
                        end = p2,
                        strokeWidth = (cellSize * 0.055f).coerceAtLeast(1.8f),
                        cap = StrokeCap.Round
                    )
                }
            }
            drawCircle(
                color = FactoryColors.CableCore.copy(alpha = alpha),
                radius = cellSize * 0.085f,
                center = center
            )
        }

        PowerTile.CapacitorBank -> {
            drawMachineChassis(
                topLeft = topLeft,
                cellSize = cellSize,
                bodyColor = FactoryColors.CapacitorBody.copy(alpha = alpha),
                accentColor = FactoryColors.CapacitorAccent.copy(alpha = alpha)
            )
            val fillRatio = (capacitorCharge / FactorySimulation.CAPACITOR_MAX_CHARGE).coerceIn(0f, 1f)
            val barW = cellSize * 0.48f
            val barH = cellSize * 0.52f
            val barLeft = center.x - barW * 0.5f
            val barTop = center.y - barH * 0.5f
            drawRoundRect(
                color = FactoryColors.Background.copy(alpha = 0.8f * alpha),
                topLeft = Offset(barLeft, barTop),
                size = Size(barW, barH),
                cornerRadius = CornerRadius(4f)
            )
            if (fillRatio > 0f) {
                val fillH = barH * fillRatio
                drawRoundRect(
                    color = FactoryColors.CapacitorCharge.copy(alpha = alpha),
                    topLeft = Offset(barLeft, barTop + barH - fillH),
                    size = Size(barW, fillH),
                    cornerRadius = CornerRadius(4f)
                )
            }
            drawLightningBolt(
                center = center,
                size = cellSize * 0.22f,
                color = FactoryColors.CableCore.copy(alpha = alpha)
            )
        }

        PowerTile.Substation -> {
            drawMachineChassis(
                topLeft = topLeft,
                cellSize = cellSize,
                bodyColor = FactoryColors.PowerGridCell.copy(alpha = alpha),
                accentColor = FactoryColors.CableCore.copy(alpha = alpha)
            )
            drawLightningBolt(
                center = center,
                size = cellSize * 0.26f,
                color = FactoryColors.CableCore.copy(alpha = alpha)
            )
        }
    }
}

private fun DrawScope.drawMachinePowerBadge(
    topLeft: Offset,
    cellSize: Float,
    powerState: MachinePowerState,
    isOverclocked: Boolean
) {
    val badgeColor = when (powerState) {
        MachinePowerState.OVERCLOCKED -> FactoryColors.OverclockCyan
        MachinePowerState.POWERED -> FactoryColors.HatchDoneAccent
        MachinePowerState.BROWNOUT -> FactoryColors.HatchAccent
        MachinePowerState.UNPOWERED -> FactoryColors.IncineratorAccent
    }
    val badgeCenter = Offset(
        topLeft.x + cellSize * 0.78f,
        topLeft.y + cellSize * 0.22f
    )
    val badgeRadius = cellSize * 0.14f
    drawCircle(
        color = FactoryColors.Background.copy(alpha = 0.92f),
        radius = badgeRadius,
        center = badgeCenter
    )
    drawCircle(
        color = if (isOverclocked) FactoryColors.OverclockCyan else badgeColor,
        radius = badgeRadius,
        center = badgeCenter,
        style = Stroke(width = (cellSize * 0.035f).coerceAtLeast(1.4f))
    )
    drawLightningBolt(
        center = badgeCenter,
        size = badgeRadius * 1.15f,
        color = badgeColor
    )
}

private fun DrawScope.drawLightningBolt(
    center: Offset,
    size: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x + size * 0.12f, center.y - size * 0.65f)
        lineTo(center.x - size * 0.36f, center.y + size * 0.06f)
        lineTo(center.x + size * 0.04f, center.y + size * 0.06f)
        lineTo(center.x - size * 0.12f, center.y + size * 0.65f)
        lineTo(center.x + size * 0.36f, center.y - size * 0.06f)
        lineTo(center.x - size * 0.04f, center.y - size * 0.06f)
        close()
    }
    drawPath(path = path, color = color)
}

private fun DrawScope.drawRockTile(topLeft: Offset, cellSize: Float, alpha: Float = 1.0f) {
    val pad = cellSize * 0.14f
    val cx = topLeft.x + cellSize / 2f
    val cy = topLeft.y + cellSize / 2f
    val r = cellSize / 2f - pad
    val path = Path().apply {
        moveTo(cx - r * 0.85f, cy - r * 0.25f)
        lineTo(cx - r * 0.30f, cy - r * 0.88f)
        lineTo(cx + r * 0.65f, cy - r * 0.70f)
        lineTo(cx + r * 0.92f, cy + r * 0.15f)
        lineTo(cx + r * 0.42f, cy + r * 0.85f)
        lineTo(cx - r * 0.62f, cy + r * 0.72f)
        close()
    }
    drawPath(path, color = FactoryColors.RockBase.copy(alpha = alpha))
    drawPath(
        path,
        color = FactoryColors.RockHighlight.copy(alpha = alpha),
        style = Stroke(width = 2f, join = StrokeJoin.Round)
    )
    drawLine(
        color = FactoryColors.RockShadow.copy(alpha = alpha),
        start = Offset(cx - r * 0.3f, cy - r * 0.2f),
        end = Offset(cx + r * 0.35f, cy + r * 0.3f),
        strokeWidth = 2f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = FactoryColors.RockShadow.copy(alpha = alpha),
        start = Offset(cx + r * 0.05f, cy + r * 0.05f),
        end = Offset(cx - r * 0.25f, cy + r * 0.45f),
        strokeWidth = 1.6f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawBeltTile(
    topLeft: Offset,
    cellSize: Float,
    belt: TileEntity.Belt,
    beltPhase: Float
) {
    val start = edgeMidpoint(topLeft, cellSize, belt.inputDir)
    val center = Offset(topLeft.x + cellSize / 2f, topLeft.y + cellSize / 2f)
    val end = edgeMidpoint(topLeft, cellSize, belt.outputDir)
    val trackWidth = cellSize * 0.56f
    val railWidth = trackWidth + cellSize * 0.10f

    val isStraight = belt.inputDir == belt.outputDir.opposite()

    if (isStraight) {
        drawLine(
            color = FactoryColors.BeltRail,
            start = start,
            end = end,
            strokeWidth = railWidth,
            cap = StrokeCap.Butt
        )
        drawLine(
            color = FactoryColors.BeltTrack,
            start = start,
            end = end,
            strokeWidth = trackWidth,
            cap = StrokeCap.Butt
        )
        val angle = directionAngleDeg(belt.outputDir)
        val dx = end.x - start.x
        val dy = end.y - start.y
        for (i in 0 until 3) {
            val t = (i / 3f + beltPhase) % 1f
            val pos = Offset(start.x + dx * t, start.y + dy * t)
            drawChevron(pos, angle, cellSize * 0.14f, FactoryColors.BeltChevron)
        }
    } else {
        val path = Path().apply {
            moveTo(start.x, start.y)
            quadraticTo(center.x, center.y, end.x, end.y)
        }
        drawPath(
            path = path,
            color = FactoryColors.BeltRail,
            style = Stroke(width = railWidth, cap = StrokeCap.Butt, join = StrokeJoin.Round)
        )
        drawPath(
            path = path,
            color = FactoryColors.BeltTrack,
            style = Stroke(width = trackWidth, cap = StrokeCap.Butt, join = StrokeJoin.Round)
        )
        for (i in 0 until 3) {
            val t = (i / 3f + beltPhase) % 1f
            val omt = 1f - t
            val px = omt * omt * start.x + 2f * omt * t * center.x + t * t * end.x
            val py = omt * omt * start.y + 2f * omt * t * center.y + t * t * end.y
            val tx = 2f * omt * (center.x - start.x) + 2f * t * (end.x - center.x)
            val ty = 2f * omt * (center.y - start.y) + 2f * t * (end.y - center.y)
            val angleDeg = (atan2(ty, tx) * (180.0 / PI)).toFloat()
            drawChevron(Offset(px, py), angleDeg, cellSize * 0.14f, FactoryColors.BeltChevron)
        }
    }
}

private fun DrawScope.drawBridgeUnderpass(
    topLeft: Offset,
    cellSize: Float,
    bridge: TileEntity.Bridge,
    beltPhase: Float
) {
    val start = edgeMidpoint(topLeft, cellSize, bridge.horizDir.opposite())
    val end = edgeMidpoint(topLeft, cellSize, bridge.horizDir)
    val trackWidth = cellSize * 0.48f
    drawLine(
        color = FactoryColors.BridgeUnderpass,
        start = start,
        end = end,
        strokeWidth = trackWidth + cellSize * 0.08f,
        cap = StrokeCap.Butt
    )
    drawLine(
        color = FactoryColors.BeltTrack,
        start = start,
        end = end,
        strokeWidth = trackWidth,
        cap = StrokeCap.Butt
    )
    val angle = directionAngleDeg(bridge.horizDir)
    val dx = end.x - start.x
    val dy = end.y - start.y
    for (i in 0 until 2) {
        val t = (i / 2f + beltPhase) % 1f
        drawChevron(Offset(start.x + dx * t, start.y + dy * t), angle, cellSize * 0.11f, FactoryColors.BeltChevronDim)
    }
}

private fun DrawScope.drawBridgeOverpass(
    topLeft: Offset,
    cellSize: Float,
    bridge: TileEntity.Bridge,
    beltPhase: Float
) {
    val start = edgeMidpoint(topLeft, cellSize, bridge.vertDir.opposite())
    val end = edgeMidpoint(topLeft, cellSize, bridge.vertDir)
    val deckW = cellSize * 0.56f
    // Subtle bridge shadow on underpass
    drawLine(
        color = Color.Black.copy(alpha = 0.45f),
        start = start + Offset(3f, 0f),
        end = end + Offset(3f, 0f),
        strokeWidth = deckW + cellSize * 0.14f,
        cap = StrokeCap.Butt
    )
    // Metallic bridge rails
    drawLine(
        color = FactoryColors.BridgeAccent,
        start = start,
        end = end,
        strokeWidth = deckW + cellSize * 0.12f,
        cap = StrokeCap.Butt
    )
    // Bridge deck
    drawLine(
        color = FactoryColors.BridgeDeck,
        start = start,
        end = end,
        strokeWidth = deckW,
        cap = StrokeCap.Butt
    )
    val angle = directionAngleDeg(bridge.vertDir)
    val dx = end.x - start.x
    val dy = end.y - start.y
    for (i in 0 until 3) {
        val t = (i / 3f + beltPhase) % 1f
        drawChevron(Offset(start.x + dx * t, start.y + dy * t), angle, cellSize * 0.12f, FactoryColors.BeltChevron)
    }
}

private fun DrawScope.drawChevron(
    center: Offset,
    angleDeg: Float,
    size: Float,
    color: Color
) {
    rotate(degrees = angleDeg, pivot = center) {
        val path = Path().apply {
            moveTo(center.x - size * 0.55f, center.y - size * 0.78f)
            lineTo(center.x + size * 0.55f, center.y)
            lineTo(center.x - size * 0.55f, center.y + size * 0.78f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = (size * 0.34f).coerceAtLeast(2f), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

private fun DrawScope.drawPortArrow(
    topLeft: Offset,
    cellSize: Float,
    side: Direction,
    isOutput: Boolean,
    color: Color
) {
    val edge = edgeMidpoint(topLeft, cellSize, side)
    val center = Offset(topLeft.x + cellSize / 2f, topLeft.y + cellSize / 2f)
    val pos = Offset(lerp(edge.x, center.x, 0.22f), lerp(edge.y, center.y, 0.22f))
    val arrowAngle = directionAngleDeg(if (isOutput) side else side.opposite())
    drawChevron(pos, arrowAngle, cellSize * 0.12f, color)
}

private fun DrawScope.drawMachineChassis(
    topLeft: Offset,
    cellSize: Float,
    bodyColor: Color,
    accentColor: Color
) {
    val pad = cellSize * 0.10f
    val inner = cellSize - pad * 2f
    drawRoundRect(
        color = bodyColor,
        topLeft = topLeft + Offset(pad, pad),
        size = Size(inner, inner),
        cornerRadius = CornerRadius(cellSize * 0.18f)
    )
    drawRoundRect(
        color = accentColor,
        topLeft = topLeft + Offset(pad, pad),
        size = Size(inner, inner),
        cornerRadius = CornerRadius(cellSize * 0.18f),
        style = Stroke(width = (cellSize * 0.045f).coerceAtLeast(1.8f))
    )
}

private fun DrawScope.drawSplitterTile(
    topLeft: Offset,
    cellSize: Float,
    splitter: TileEntity.Splitter
) {
    drawMachineChassis(topLeft, cellSize, FactoryColors.SplitterBody, FactoryColors.SplitterAccent)
    val center = Offset(topLeft.x + cellSize / 2f, topLeft.y + cellSize / 2f)

    drawPortArrow(topLeft, cellSize, splitter.inputDir, isOutput = false, color = FactoryColors.TextPrimary)
    drawPortArrow(
        topLeft, cellSize, splitter.outputLeft, isOutput = true,
        color = if (splitter.toggleLeft) FactoryColors.SplitterAccent else FactoryColors.TextMuted
    )
    drawPortArrow(
        topLeft, cellSize, splitter.outputRight, isOutput = true,
        color = if (!splitter.toggleLeft) FactoryColors.SplitterAccent else FactoryColors.TextMuted
    )

    val r = cellSize * 0.16f
    val diamond = Path().apply {
        moveTo(center.x, center.y - r)
        lineTo(center.x + r, center.y)
        lineTo(center.x, center.y + r)
        lineTo(center.x - r, center.y)
        close()
    }
    drawPath(diamond, color = FactoryColors.SplitterAccent)
}

private fun DrawScope.drawFilterTile(
    topLeft: Offset,
    cellSize: Float,
    filter: TileEntity.Filter
) {
    val accent = if (filter.overclocked) FactoryColors.OverclockCyan else FactoryColors.FilterAccent
    drawMachineChassis(topLeft, cellSize, FactoryColors.FilterBody, accent)
    drawPortArrow(topLeft, cellSize, filter.inputDir, isOutput = false, color = FactoryColors.TextPrimary)
    drawPortArrow(topLeft, cellSize, filter.matchDir, isOutput = true, color = FactoryColors.HatchDoneAccent)
    drawPortArrow(topLeft, cellSize, filter.otherDir, isOutput = true, color = FactoryColors.HatchAccent)

    val center = Offset(topLeft.x + cellSize / 2f, topLeft.y + cellSize / 2f)
    drawCircle(FactoryColors.Background.copy(alpha = 0.75f), cellSize * 0.22f, center)
    drawItemShape(filter.filterItem, center, cellSize * 0.15f, drawShadow = false)
}

private fun DrawScope.drawOverflowTile(
    topLeft: Offset,
    cellSize: Float,
    overflow: TileEntity.Overflow
) {
    drawMachineChassis(topLeft, cellSize, FactoryColors.OverflowBody, FactoryColors.OverflowAccent)
    drawPortArrow(topLeft, cellSize, overflow.inputDir, isOutput = false, color = FactoryColors.TextPrimary)
    drawPortArrow(topLeft, cellSize, overflow.primaryDir, isOutput = true, color = FactoryColors.BeltChevron)
    drawPortArrow(topLeft, cellSize, overflow.overflowDir, isOutput = true, color = FactoryColors.OverflowAccent)

    val center = Offset(topLeft.x + cellSize / 2f, topLeft.y + cellSize / 2f)
    drawCircle(
        color = FactoryColors.OverflowAccent,
        radius = cellSize * 0.14f,
        center = center,
        style = Stroke(width = (cellSize * 0.05f).coerceAtLeast(2f))
    )
}

private fun DrawScope.drawIncineratorTile(topLeft: Offset, cellSize: Float) {
    drawMachineChassis(topLeft, cellSize, FactoryColors.IncineratorBody, FactoryColors.IncineratorAccent)
    val center = Offset(topLeft.x + cellSize / 2f, topLeft.y + cellSize / 2f)
    val r = cellSize * 0.22f
    drawLine(
        color = FactoryColors.IncineratorAccent,
        start = center + Offset(-r, -r),
        end = center + Offset(r, r),
        strokeWidth = (cellSize * 0.07f).coerceAtLeast(2.2f),
        cap = StrokeCap.Round
    )
    drawLine(
        color = FactoryColors.IncineratorAccent,
        start = center + Offset(r, -r),
        end = center + Offset(-r, r),
        strokeWidth = (cellSize * 0.07f).coerceAtLeast(2.2f),
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawSmelterTile(
    topLeft: Offset,
    cellSize: Float,
    smelter: TileEntity.Smelter,
    status: MachineStatus?
) {
    val accent = if (smelter.overclocked) FactoryColors.OverclockCyan else FactoryColors.SmelterAccent
    drawMachineChassis(topLeft, cellSize, FactoryColors.SmelterBody, accent)
    drawPortArrow(topLeft, cellSize, smelter.inputDir, isOutput = false, color = FactoryColors.TextSecondary)
    drawPortArrow(topLeft, cellSize, smelter.outputDir, isOutput = true, color = FactoryColors.SmelterAccent)

    val center = Offset(topLeft.x + cellSize / 2f, topLeft.y + cellSize / 2f)
    val coreRadius = cellSize * 0.19f
    val active = status?.isProcessing == true || status?.hasOutputReady == true
    drawCircle(
        color = if (active) FactoryColors.SmelterGlow else FactoryColors.SmelterAccent.copy(alpha = 0.4f),
        radius = coreRadius,
        center = center
    )
    if (status != null && status.progress > 0f) {
        drawArc(
            color = if (smelter.overclocked) FactoryColors.OverclockCyan else FactoryColors.GearGold,
            startAngle = -90f,
            sweepAngle = 360f * status.progress,
            useCenter = false,
            topLeft = Offset(center.x - coreRadius * 1.35f, center.y - coreRadius * 1.35f),
            size = Size(coreRadius * 2.7f, coreRadius * 2.7f),
            style = Stroke(width = (cellSize * 0.05f).coerceAtLeast(2f), cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawPressTile(
    topLeft: Offset,
    cellSize: Float,
    press: TileEntity.Press,
    status: MachineStatus?
) {
    val accent = if (press.overclocked) FactoryColors.OverclockCyan else FactoryColors.PressAccent
    drawMachineChassis(topLeft, cellSize, FactoryColors.PressBody, accent)
    drawPortArrow(topLeft, cellSize, press.inputDir, isOutput = false, color = FactoryColors.TextSecondary)
    drawPortArrow(topLeft, cellSize, press.outputDir, isOutput = true, color = FactoryColors.PressLight)

    val center = Offset(topLeft.x + cellSize / 2f, topLeft.y + cellSize / 2f)
    val p = status?.progress ?: 0f
    val gap = cellSize * (0.14f - 0.09f * p)
    val plateW = cellSize * 0.34f
    val plateH = cellSize * 0.09f
    drawRoundRect(
        color = FactoryColors.PressLight,
        topLeft = Offset(center.x - plateW / 2f, center.y - gap - plateH),
        size = Size(plateW, plateH),
        cornerRadius = CornerRadius(3f)
    )
    drawRoundRect(
        color = FactoryColors.PressLight,
        topLeft = Offset(center.x - plateW / 2f, center.y + gap),
        size = Size(plateW, plateH),
        cornerRadius = CornerRadius(3f)
    )
}

private fun DrawScope.drawAssemblerTile(
    topLeft: Offset,
    cellSize: Float,
    assembler: TileEntity.Assembler,
    status: MachineStatus?
) {
    val accent = if (assembler.overclocked) FactoryColors.OverclockCyan else FactoryColors.AssemblerAccent
    drawMachineChassis(topLeft, cellSize, FactoryColors.AssemblerBody, accent)
    drawPortArrow(
        topLeft, cellSize, assembler.inputDirA, isOutput = false,
        color = if (status?.slotAFilled == true) FactoryColors.IronPlate else FactoryColors.TextSecondary
    )
    drawPortArrow(
        topLeft, cellSize, assembler.inputDirB, isOutput = false,
        color = if (status?.slotBFilled == true) FactoryColors.IronPlate else FactoryColors.TextSecondary
    )
    drawPortArrow(topLeft, cellSize, assembler.outputDir, isOutput = true, color = FactoryColors.GearGold)

    val center = Offset(topLeft.x + cellSize / 2f, topLeft.y + cellSize / 2f)
    val gearR = cellSize * 0.18f
    drawGear(
        center = center,
        outerRadius = gearR,
        innerRadius = gearR * 0.72f,
        holeRadius = gearR * 0.28f,
        teeth = 6,
        fillColor = FactoryColors.AssemblerLight.copy(alpha = if (status?.isProcessing == true) 1f else 0.55f),
        rimColor = FactoryColors.AssemblerBody
    )
    if (status != null && status.progress > 0f) {
        val ringR = cellSize * 0.25f
        drawArc(
            color = if (assembler.overclocked) FactoryColors.OverclockCyan else FactoryColors.GearGold,
            startAngle = -90f,
            sweepAngle = 360f * status.progress,
            useCenter = false,
            topLeft = Offset(center.x - ringR, center.y - ringR),
            size = Size(ringR * 2f, ringR * 2f),
            style = Stroke(width = (cellSize * 0.05f).coerceAtLeast(2f), cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawSpawnerTile(
    topLeft: Offset,
    cellSize: Float,
    spawner: TileEntity.Spawner
) {
    drawMachineChassis(topLeft, cellSize, FactoryColors.SpawnerBody, FactoryColors.SpawnerAccent)
    drawPortArrow(topLeft, cellSize, spawner.outputDir, isOutput = true, color = FactoryColors.SpawnerAccent)
    val center = Offset(topLeft.x + cellSize / 2f, topLeft.y + cellSize / 2f)
    val secondary = spawner.secondaryItemType
    if (secondary == null) {
        drawItemShape(spawner.itemType, center, cellSize * 0.20f, drawShadow = false)
    } else {
        drawItemShape(spawner.itemType, center + Offset(-cellSize * 0.13f, -cellSize * 0.06f), cellSize * 0.13f, drawShadow = false)
        drawItemShape(secondary, center + Offset(cellSize * 0.13f, cellSize * 0.08f), cellSize * 0.13f, drawShadow = false)
    }
}

private fun DrawScope.drawHatchTile(
    topLeft: Offset,
    cellSize: Float,
    hatch: TileEntity.Hatch
) {
    val done = hatch.deliveredCount >= hatch.targetCount
    val body = if (done) FactoryColors.HatchDoneBody else FactoryColors.HatchBody
    val accent = if (done) FactoryColors.HatchDoneAccent else FactoryColors.HatchAccent
    drawMachineChassis(topLeft, cellSize, body, accent)

    val center = Offset(topLeft.x + cellSize / 2f, topLeft.y + cellSize / 2f)
    val bayRadius = cellSize * 0.29f
    drawCircle(
        color = FactoryColors.Background.copy(alpha = 0.7f),
        radius = bayRadius,
        center = center
    )
    drawItemShape(hatch.requiredItem, center, cellSize * 0.19f, drawShadow = false)

    val ratio = (hatch.deliveredCount.toFloat() / hatch.targetCount.coerceAtLeast(1)).coerceIn(0f, 1f)
    if (ratio > 0f) {
        drawArc(
            color = accent,
            startAngle = -90f,
            sweepAngle = 360f * ratio,
            useCenter = false,
            topLeft = Offset(center.x - bayRadius, center.y - bayRadius),
            size = Size(bayRadius * 2f, bayRadius * 2f),
            style = Stroke(width = (cellSize * 0.06f).coerceAtLeast(2.2f), cap = StrokeCap.Round)
        )
    }
}
