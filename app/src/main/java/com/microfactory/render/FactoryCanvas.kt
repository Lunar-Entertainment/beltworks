package com.microfactory.render

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
import com.microfactory.sim.MachineStatus
import com.microfactory.sim.TileEntity
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min

enum class BuildTool(val label: String, val shortHint: String) {
    BELT("Belt", "Drag across cells to build & curve belts • Tap to rotate"),
    SPLITTER("Splitter", "Alternates items 50/50 left & right • Tap to rotate"),
    SMELTER("Smelter", "Smelts Ore → Ingot (2 ticks) • Tap to rotate"),
    PRESS("Press", "Stamps Iron Ingot → Iron Plate (2 ticks) • Tap to rotate"),
    ASSEMBLER("Assembler", "Crafts 2 Iron Plates → 1 Gear (3 ticks) • Tap to rotate"),
    BULLDOZE("Bulldoze", "Tap or drag to remove belts & machines")
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

    fun cellCenter(coord: GridCoord): Offset =
        Offset(originX + (coord.x + 0.5f) * cellSize, originY + (coord.y + 0.5f) * cellSize)
}

private fun computeGridMetrics(canvasSize: Size, gridWidth: Int, gridHeight: Int): GridMetrics {
    val padding = 12f
    val availW = (canvasSize.width - padding * 2f).coerceAtLeast(1f)
    val availH = (canvasSize.height - padding * 2f).coerceAtLeast(1f)
    val cellSize = min(availW / gridWidth, availH / gridHeight)
    val totalW = cellSize * gridWidth
    val totalH = cellSize * gridHeight
    val originX = (canvasSize.width - totalW) / 2f
    val originY = (canvasSize.height - totalH) / 2f
    return GridMetrics(originX, originY, cellSize, gridWidth, gridHeight)
}

@Composable
fun FactoryCanvas(
    simulation: FactorySimulation,
    selectedTool: BuildTool,
    tickAlpha: Float,
    beltPhase: Float,
    renderVersion: Int,
    onBoardChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTool by rememberUpdatedState(selectedTool)
    val currentOnBoardChanged by rememberUpdatedState(onBoardChanged)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(simulation) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val metrics = computeGridMetrics(
                        Size(size.width.toFloat(), size.height.toFloat()),
                        simulation.width,
                        simulation.height
                    )
                    val startCell = metrics.cellAt(down.position) ?: return@awaitEachGesture
                    down.consume()

                    var lastCell = startCell
                    var movedOutOfStart = false
                    var prevBeltCell: GridCoord? = null

                    if (currentTool == BuildTool.BULLDOZE) {
                        if (simulation.clearTile(startCell)) {
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

                        val hovered = metrics.cellAt(change.position)
                        if (hovered != null && hovered != lastCell) {
                            movedOutOfStart = true
                            val steps = orthogonalStepsBetween(lastCell, hovered)
                            for (nextCell in steps) {
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
                                lastCell = nextCell
                            }
                            change.consume()
                        }
                    }

                    // Single tap (did not drag onto another cell)
                    if (!movedOutOfStart && currentTool != BuildTool.BULLDOZE) {
                        val existing = simulation.getTile(startCell)
                        if (existing.isPlayerEditable()) {
                            if (simulation.rotateTile(startCell)) {
                                currentOnBoardChanged()
                            }
                        } else if (existing == TileEntity.Empty) {
                            val placed = createDefaultTileForTool(simulation, startCell, currentTool)
                            if (placed != null) {
                                simulation.setTile(startCell, placed)
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
    ) {
        // Read renderVersion so Canvas invalidates whenever simulation ticks or edits happen.
        @Suppress("UNUSED_EXPRESSION")
        renderVersion

        val metrics = computeGridMetrics(size, simulation.width, simulation.height)

        // Outer industrial board frame
        drawRoundRect(
            color = FactoryColors.SurfaceElevated,
            topLeft = Offset(metrics.originX - 6f, metrics.originY - 6f),
            size = Size(metrics.cellSize * simulation.width + 12f, metrics.cellSize * simulation.height + 12f),
            cornerRadius = CornerRadius(14f)
        )
        drawRoundRect(
            color = FactoryColors.GridOuterFrame,
            topLeft = Offset(metrics.originX - 6f, metrics.originY - 6f),
            size = Size(metrics.cellSize * simulation.width + 12f, metrics.cellSize * simulation.height + 12f),
            cornerRadius = CornerRadius(14f),
            style = Stroke(width = 2f)
        )

        // Pass 1: Grid cells and floor entities
        for (y in 0 until simulation.height) {
            for (x in 0 until simulation.width) {
                val coord = GridCoord(x, y)
                val topLeft = metrics.cellTopLeft(coord)
                val cellPad = metrics.cellSize * 0.045f
                val innerSize = metrics.cellSize - cellPad * 2f

                drawRoundRect(
                    color = FactoryColors.GridCell,
                    topLeft = topLeft + Offset(cellPad, cellPad),
                    size = Size(innerSize, innerSize),
                    cornerRadius = CornerRadius(metrics.cellSize * 0.12f)
                )
                drawRoundRect(
                    color = FactoryColors.GridCellBorder,
                    topLeft = topLeft + Offset(cellPad, cellPad),
                    size = Size(innerSize, innerSize),
                    cornerRadius = CornerRadius(metrics.cellSize * 0.12f),
                    style = Stroke(width = 1.2f)
                )

                when (val tile = simulation.getTile(coord)) {
                    TileEntity.Empty -> Unit
                    TileEntity.Rock -> drawRockTile(topLeft, metrics.cellSize)
                    is TileEntity.Belt -> drawBeltTile(topLeft, metrics.cellSize, tile, beltPhase)
                    is TileEntity.Splitter -> drawSplitterTile(topLeft, metrics.cellSize, tile)
                    is TileEntity.Smelter -> drawSmelterTile(topLeft, metrics.cellSize, tile, simulation.getMachineStatus(coord))
                    is TileEntity.Press -> drawPressTile(topLeft, metrics.cellSize, tile, simulation.getMachineStatus(coord))
                    is TileEntity.Assembler -> drawAssemblerTile(topLeft, metrics.cellSize, tile, simulation.getMachineStatus(coord))
                    is TileEntity.Spawner -> drawSpawnerTile(topLeft, metrics.cellSize, tile)
                    is TileEntity.Hatch -> drawHatchTile(topLeft, metrics.cellSize, tile)
                }
            }
        }

        // Pass 2: Smoothly interpolated moving items (plus items currently gliding into machines/hatches)
        val itemRadius = metrics.cellSize * 0.25f
        for (item in simulation.consumedVisualItems) {
            val ix = lerp(item.prevCoord.x.toFloat(), item.currentCoord.x.toFloat(), tickAlpha)
            val iy = lerp(item.prevCoord.y.toFloat(), item.currentCoord.y.toFloat(), tickAlpha)
            val center = Offset(
                metrics.originX + (ix + 0.5f) * metrics.cellSize,
                metrics.originY + (iy + 0.5f) * metrics.cellSize
            )
            // Shrink slightly as item enters a machine or hatch
            val scale = (1f - tickAlpha * 0.45f).coerceIn(0.4f, 1f)
            drawItemShape(item.type, center, itemRadius * scale)
        }

        for (item in simulation.items) {
            val ix = lerp(item.prevCoord.x.toFloat(), item.currentCoord.x.toFloat(), tickAlpha)
            val iy = lerp(item.prevCoord.y.toFloat(), item.currentCoord.y.toFloat(), tickAlpha)
            val center = Offset(
                metrics.originX + (ix + 0.5f) * metrics.cellSize,
                metrics.originY + (iy + 0.5f) * metrics.cellSize
            )
            drawItemShape(item.type, center, itemRadius)
        }
    }
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
            // Keep existing inputDir unless backtracking directly into it; update outputDir to bend smoothly!
            val newInput = if (fromTile.inputDir == moveDir) moveDir.opposite() else fromTile.inputDir
            if (fromTile.inputDir != newInput || fromTile.outputDir != moveDir) {
                sim.setTile(fromCell, TileEntity.Belt(inputDir = newInput, outputDir = moveDir))
                changed = true
            }
        }
        else -> Unit // Leave Spawner, Hatch, Rock, and Machines intact
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
            else -> false
        }
        if (pointsIntoCell) return dir
    }
    return null
}

private fun createDefaultTileForTool(
    sim: FactorySimulation,
    coord: GridCoord,
    tool: BuildTool
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
        BuildTool.SMELTER -> TileEntity.Smelter(inputDir = defaultInput, outputDir = defaultOutput)
        BuildTool.PRESS -> TileEntity.Press(inputDir = defaultInput, outputDir = defaultOutput)
        BuildTool.ASSEMBLER -> TileEntity.Assembler(
            inputDirA = Direction.WEST,
            inputDirB = Direction.EAST,
            outputDir = Direction.SOUTH
        )
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

private fun DrawScope.drawRockTile(topLeft: Offset, cellSize: Float) {
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
    drawPath(path, color = FactoryColors.RockBase)
    drawPath(path, color = FactoryColors.RockHighlight, style = Stroke(width = 2f, join = StrokeJoin.Round))
    drawLine(
        color = FactoryColors.RockShadow,
        start = Offset(cx - r * 0.3f, cy - r * 0.2f),
        end = Offset(cx + r * 0.35f, cy + r * 0.3f),
        strokeWidth = 2f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = FactoryColors.RockShadow,
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

    // Center diamond divider
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

private fun DrawScope.drawSmelterTile(
    topLeft: Offset,
    cellSize: Float,
    smelter: TileEntity.Smelter,
    status: MachineStatus?
) {
    drawMachineChassis(topLeft, cellSize, FactoryColors.SmelterBody, FactoryColors.SmelterAccent)
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
            color = FactoryColors.GearGold,
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
    drawMachineChassis(topLeft, cellSize, FactoryColors.PressBody, FactoryColors.PressAccent)
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
    drawMachineChassis(topLeft, cellSize, FactoryColors.AssemblerBody, FactoryColors.AssemblerAccent)
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
            color = FactoryColors.GearGold,
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
    drawItemShape(spawner.itemType, center, cellSize * 0.20f, drawShadow = false)
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

    // Progress ring around hatch bay
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
