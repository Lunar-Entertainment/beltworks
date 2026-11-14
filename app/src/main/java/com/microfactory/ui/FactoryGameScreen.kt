package com.microfactory.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.microfactory.render.BuildTool
import com.microfactory.render.FactoryCanvas
import com.microfactory.render.FactoryColors
import com.microfactory.sim.FactorySimulation
import com.microfactory.sim.GridCoord
import com.microfactory.sim.ItemType
import com.microfactory.sim.LevelRepository
import com.microfactory.sim.SimulationSpeed
import com.microfactory.sim.TileEntity

@Composable
fun FactoryGameScreen(
    initialLevelId: Int,
    isMusicEnabled: Boolean,
    onToggleMusic: () -> Unit,
    onNextTrack: () -> Unit,
    onBackToMenu: () -> Unit,
    onTileEdited: () -> Unit,
    onLevelCompleted: (levelId: Int, stars: Int, beltsUsed: Int) -> Unit
) {
    BackHandler(onBack = onBackToMenu)

    val levels = LevelRepository.levels
    var levelIndex by remember(initialLevelId) {
        val idx = levels.indexOfFirst { it.id == initialLevelId }.coerceAtLeast(0)
        mutableIntStateOf(idx)
    }
    val currentLevel = levels[levelIndex]

    val simulation = remember {
        FactorySimulation().apply { loadLevel(currentLevel) }
    }
    var selectedTool by remember { mutableStateOf(BuildTool.BELT) }
    var selectedFilterItem by remember { mutableStateOf(ItemType.IRON_ORE) }
    var activeFilterCoord by remember { mutableStateOf<GridCoord?>(null) }
    var speed by remember { mutableStateOf(SimulationSpeed.X1) }
    var resetNonce by remember { mutableIntStateOf(0) }

    var tickAlpha by remember { mutableFloatStateOf(0f) }
    var beltPhase by remember { mutableFloatStateOf(0f) }
    var renderVersion by remember { mutableIntStateOf(0) }
    var showVictory by remember { mutableStateOf(false) }
    var victoryRecordedForRun by remember { mutableStateOf(false) }

    LaunchedEffect(levelIndex, resetNonce) {
        simulation.loadLevel(currentLevel)
        if (currentLevel.id < selectedTool.unlockedFromLevel) {
            selectedTool = BuildTool.BELT
        }
        activeFilterCoord = null
        tickAlpha = 0f
        beltPhase = 0f
        showVictory = false
        victoryRecordedForRun = false
        if (speed == SimulationSpeed.PAUSED) {
            speed = SimulationSpeed.X1
        }
        renderVersion++
    }

    LaunchedEffect(levelIndex, resetNonce, speed) {
        var lastFrameNanos = withFrameNanos { it }
        var accumulatedScaledNanos = (tickAlpha * FactorySimulation.TICK_DURATION_NANOS).toLong()

        while (true) {
            val frameNanos = withFrameNanos { it }
            val dtNanos = (frameNanos - lastFrameNanos).coerceIn(0L, 100_000_000L)
            lastFrameNanos = frameNanos

            if (speed != SimulationSpeed.PAUSED) {
                accumulatedScaledNanos += dtNanos * speed.multiplier
                var steps = 0
                while (accumulatedScaledNanos >= FactorySimulation.TICK_DURATION_NANOS && steps < 3) {
                    accumulatedScaledNanos -= FactorySimulation.TICK_DURATION_NANOS
                    simulation.tick()
                    steps++
                }
                if (accumulatedScaledNanos >= FactorySimulation.TICK_DURATION_NANOS) {
                    accumulatedScaledNanos %= FactorySimulation.TICK_DURATION_NANOS
                }

                tickAlpha = (accumulatedScaledNanos.toFloat() / FactorySimulation.TICK_DURATION_NANOS).coerceIn(0f, 1f)
                beltPhase = ((simulation.tickCount % 1000L).toFloat() + tickAlpha) % 1f
                renderVersion++

                if (simulation.isComplete && !victoryRecordedForRun) {
                    victoryRecordedForRun = true
                    showVictory = true
                    val belts = simulation.beltCount
                    onLevelCompleted(currentLevel.id, currentLevel.calculateStars(belts), belts)
                }
            }
        }
    }

    @Suppress("UNUSED_EXPRESSION")
    renderVersion
    val hatches: List<TileEntity.Hatch> = simulation.getHatches().mapNotNull { it.tile as? TileEntity.Hatch }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FactoryColors.Background)
            .safeDrawingPadding()
    ) {
        TopHudBar(
            level = currentLevel,
            hasPrevLevel = levelIndex > 0,
            hasNextLevel = levelIndex < levels.lastIndex,
            hatches = hatches,
            rollingIpm = simulation.rollingIpm,
            speed = speed,
            isMusicEnabled = isMusicEnabled,
            onBackToMenu = onBackToMenu,
            onToggleMusic = onToggleMusic,
            onNextTrack = onNextTrack,
            onPrevLevel = {
                if (levelIndex > 0) levelIndex--
            },
            onNextLevel = {
                if (levelIndex < levels.lastIndex) levelIndex++
            },
            onSelectSpeed = { speed = it },
            onResetBoard = { resetNonce++ }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FactoryColors.Background)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val badge = currentLevel.newMechanicBadge
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(FactoryColors.BeltChevron.copy(alpha = 0.18f))
                        .border(1.dp, FactoryColors.BeltChevron.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "NEW: $badge",
                        color = FactoryColors.BeltChevron,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Text(
                text = currentLevel.hint,
                color = FactoryColors.TextMuted,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            FactoryCanvas(
                simulation = simulation,
                selectedTool = selectedTool,
                selectedFilterItem = selectedFilterItem,
                activeFilterCoord = activeFilterCoord,
                tickAlpha = tickAlpha,
                beltPhase = beltPhase,
                renderVersion = renderVersion,
                onBoardChanged = {
                    renderVersion++
                    onTileEdited()
                },
                onFilterSelected = { coord, item ->
                    activeFilterCoord = coord
                    if (item != null) {
                        selectedFilterItem = item
                    }
                }
            )
        }

        MachinePalette(
            levelId = currentLevel.id,
            selectedTool = selectedTool,
            selectedFilterItem = selectedFilterItem,
            showFilterBar = selectedTool == BuildTool.FILTER || activeFilterCoord != null,
            onSelectTool = { tool ->
                selectedTool = tool
                if (tool != BuildTool.FILTER) {
                    activeFilterCoord = null
                }
            },
            onSelectFilterItem = { item ->
                selectedFilterItem = item
                val coord = activeFilterCoord
                if (coord != null && simulation.setFilterItem(coord, item)) {
                    renderVersion++
                    onTileEdited()
                }
            }
        )
    }

    if (showVictory) {
        VictoryDialog(
            level = currentLevel,
            beltsUsed = simulation.beltCount,
            elapsedSeconds = simulation.elapsedSeconds,
            ipm = simulation.rollingIpm,
            hasNextLevel = levelIndex < levels.lastIndex,
            onReplay = {
                showVictory = false
                resetNonce++
            },
            onNextLevel = {
                showVictory = false
                if (levelIndex < levels.lastIndex) {
                    levelIndex++
                } else {
                    levelIndex = 0
                }
            },
            onDismiss = {
                showVictory = false
            }
        )
    }
}
