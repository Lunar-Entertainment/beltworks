package com.microfactory.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.microfactory.sim.GridLayer
import com.microfactory.sim.ItemType
import com.microfactory.sim.LevelHelpAdvisor
import com.microfactory.sim.LevelRepository
import com.microfactory.sim.SimulationSpeed
import com.microfactory.sim.TileEntity

@Composable
fun FactoryGameScreen(
    initialLevelId: Int,
    highestUnlockedLevelId: Int,
    isMusicEnabled: Boolean,
    isHelpAdsEnabled: Boolean,
    onToggleMusic: () -> Unit,
    onNextTrack: () -> Unit,
    onBackToMenu: () -> Unit,
    onTileEdited: () -> Unit,
    onRequestHelpAd: (onUnlocked: (String?) -> Unit, onLoadingChanged: (Boolean) -> Unit, onDenied: (String) -> Unit) -> Unit,
    onLevelCompleted: (levelId: Int, stars: Int, beltsUsed: Int) -> Unit
) {
    BackHandler(onBack = onBackToMenu)

    val levels = LevelRepository.allLevels
    var levelIndex by remember(initialLevelId) {
        val idx = levels.indexOfFirst { it.id == initialLevelId }.coerceAtLeast(0)
        mutableIntStateOf(idx)
    }
    val currentLevel = levels[levelIndex]

    val simulation = remember {
        FactorySimulation().apply { loadLevel(currentLevel) }
    }
    var activeLayer by remember { mutableStateOf(GridLayer.FACTORY) }
    var selectedTool by remember { mutableStateOf(BuildTool.BELT) }
    var selectedFilterItem by remember { mutableStateOf(ItemType.IRON_ORE) }
    var activeFilterCoord by remember { mutableStateOf<GridCoord?>(null) }
    var activeMachineCoord by remember { mutableStateOf<GridCoord?>(null) }
    var speed by remember { mutableStateOf(SimulationSpeed.X1) }
    var resetNonce by remember { mutableIntStateOf(0) }

    var tickAlpha by remember { mutableFloatStateOf(0f) }
    var beltPhase by remember { mutableFloatStateOf(0f) }
    var renderVersion by remember { mutableIntStateOf(0) }
    var showVictory by remember { mutableStateOf(false) }
    var victoryRecordedForRun by remember { mutableStateOf(false) }

    var unlockedHelpLevels by remember { mutableStateOf(emptySet<Int>()) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var helpStatusNote by remember { mutableStateOf<String?>(null) }
    var isLoadingHelpAd by remember { mutableStateOf(false) }
    val helpBlueprint = remember(currentLevel.id) { LevelHelpAdvisor.buildBlueprint(currentLevel) }
    val isHelpUnlocked = currentLevel.id in unlockedHelpLevels

    LaunchedEffect(levelIndex, resetNonce) {
        simulation.loadLevel(currentLevel)
        activeLayer = GridLayer.FACTORY
        if (!selectedTool.isVisibleFor(GridLayer.FACTORY, currentLevel.id, currentLevel.requiresPower)) {
            selectedTool = BuildTool.BELT
        }
        activeFilterCoord = null
        activeMachineCoord = null
        tickAlpha = 0f
        beltPhase = 0f
        showVictory = false
        victoryRecordedForRun = false
        showHelpDialog = false
        helpStatusNote = null
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
    val activeMachineTile: TileEntity? = activeMachineCoord?.let { coord ->
        simulation.getTile(coord).takeIf { it.isPowerConsumer() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FactoryColors.Background)
            .safeDrawingPadding()
    ) {
        val nextIdInMode = LevelRepository.nextLevelIdInMode(currentLevel)
        TopHudBar(
            level = currentLevel,
            hasPrevLevel = levelIndex > 0,
            hasNextLevel = nextIdInMode != null && nextIdInMode <= highestUnlockedLevelId,
            hatches = hatches,
            rollingIpm = simulation.rollingIpm,
            speed = speed,
            isMusicEnabled = isMusicEnabled,
            activeLayer = activeLayer,
            generatedPower = simulation.totalGeneratedPower,
            powerDemand = simulation.totalPowerDemand,
            capacitorStored = simulation.totalCapacitorStored,
            capacitorCapacity = simulation.totalCapacitorCapacity,
            powerSatisfaction = simulation.overallPowerSatisfaction,
            onSelectLayer = { targetLayer ->
                activeLayer = targetLayer
                if (!selectedTool.isVisibleFor(targetLayer, currentLevel.id, currentLevel.requiresPower)) {
                    selectedTool = if (targetLayer == GridLayer.POWER) {
                        BuildTool.POWER_CABLE
                    } else {
                        BuildTool.BELT
                    }
                }
            },
            onBackToMenu = onBackToMenu,
            onToggleMusic = onToggleMusic,
            onNextTrack = onNextTrack,
            onPrevLevel = {
                if (levelIndex > 0) levelIndex--
            },
            onNextLevel = {
                if (nextIdInMode != null && nextIdInMode <= highestUnlockedLevelId) {
                    val idx = levels.indexOfFirst { it.id == nextIdInMode }
                    if (idx >= 0) levelIndex = idx
                }
            },
            onSelectSpeed = { speed = it },
            onResetBoard = { resetNonce++ }
        )

        if (currentLevel.isExpert) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FactoryColors.Background)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(FactoryColors.BeltChevron.copy(alpha = 0.16f))
                        .border(1.dp, FactoryColors.BeltChevron.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "EXPERT",
                        color = FactoryColors.BeltChevron,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = "NO HINTS · NO HELP",
                    color = FactoryColors.TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.6.sp
                )
            }
        } else {
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
                    text = helpStatusNote?.takeIf { !showHelpDialog && !isHelpUnlocked } ?: currentLevel.hint,
                    color = if (helpStatusNote != null && !isHelpUnlocked) FactoryColors.BeltChevron else FactoryColors.TextMuted,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            if (isHelpUnlocked) FactoryColors.BeltChevron.copy(alpha = 0.2f)
                            else FactoryColors.SurfaceElevated
                        )
                        .border(
                            1.dp,
                            if (isHelpUnlocked) FactoryColors.BeltChevron else FactoryColors.GridCellBorder,
                            RoundedCornerShape(7.dp)
                        )
                        .clickable(enabled = !isLoadingHelpAd) {
                            if (isHelpUnlocked || !isHelpAdsEnabled) {
                                unlockedHelpLevels = unlockedHelpLevels + currentLevel.id
                                helpStatusNote = null
                                showHelpDialog = true
                            } else {
                                onRequestHelpAd(
                                    { note ->
                                        unlockedHelpLevels = unlockedHelpLevels + currentLevel.id
                                        helpStatusNote = note
                                        showHelpDialog = true
                                    },
                                    { loading -> isLoadingHelpAd = loading },
                                    { deniedMsg -> helpStatusNote = deniedMsg }
                                )
                            }
                        }
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when {
                            isLoadingHelpAd -> "Loading…"
                            isHelpUnlocked -> "Blueprint"
                            else -> "? Help"
                        },
                        color = if (isHelpUnlocked) FactoryColors.BeltChevron else FactoryColors.TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
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
                activeLayer = activeLayer,
                selectedTool = selectedTool,
                selectedFilterItem = selectedFilterItem,
                activeFilterCoord = activeFilterCoord,
                activeMachineCoord = activeMachineCoord,
                helpRouteCoords = if (isHelpUnlocked) helpBlueprint.suggestedPathCoords else emptySet(),
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
                },
                onMachineSelected = { coord ->
                    activeMachineCoord = coord
                }
            )
        }

        MachinePalette(
            levelId = currentLevel.id,
            activeLayer = activeLayer,
            requiresPower = currentLevel.requiresPower,
            selectedTool = selectedTool,
            selectedFilterItem = selectedFilterItem,
            showFilterBar = selectedTool == BuildTool.FILTER || activeFilterCoord != null,
            activeMachineTile = activeMachineTile,
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
            },
            onToggleOverclock = {
                val coord = activeMachineCoord
                if (coord != null && simulation.toggleOverclock(coord)) {
                    renderVersion++
                    onTileEdited()
                }
            }
        )
    }

    if (showVictory) {
        val nextLevelId = LevelRepository.nextLevelIdInMode(currentLevel)
        VictoryDialog(
            level = currentLevel,
            beltsUsed = simulation.beltCount,
            elapsedSeconds = simulation.elapsedSeconds,
            ipm = simulation.rollingIpm,
            hasNextLevel = nextLevelId != null,
            onReplay = {
                showVictory = false
                resetNonce++
            },
            onNextLevel = {
                showVictory = false
                if (nextLevelId != null) {
                    val idx = levels.indexOfFirst { it.id == nextLevelId }
                    if (idx >= 0) levelIndex = idx
                }
            },
            onDismiss = {
                showVictory = false
            }
        )
    }

    if (showHelpDialog && !currentLevel.isExpert) {
        HelpBlueprintDialog(
            level = currentLevel,
            blueprint = helpBlueprint,
            statusNote = helpStatusNote,
            onDismiss = { showHelpDialog = false }
        )
    }
}
