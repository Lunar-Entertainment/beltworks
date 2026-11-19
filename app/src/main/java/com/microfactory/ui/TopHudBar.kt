package com.microfactory.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.microfactory.render.FactoryColors
import com.microfactory.render.drawItemShape
import com.microfactory.sim.GridLayer
import com.microfactory.sim.LevelConfig
import com.microfactory.sim.SimulationSpeed
import com.microfactory.sim.TileEntity
import java.util.Locale

@Composable
fun TopHudBar(
    level: LevelConfig,
    hasPrevLevel: Boolean,
    hasNextLevel: Boolean,
    hatches: List<TileEntity.Hatch>,
    rollingIpm: Double,
    speed: SimulationSpeed,
    isMusicEnabled: Boolean,
    activeLayer: GridLayer = GridLayer.FACTORY,
    generatedPower: Float = 0f,
    powerDemand: Float = 0f,
    capacitorStored: Float = 0f,
    capacitorCapacity: Float = 0f,
    powerSatisfaction: Float = 1f,
    onSelectLayer: (GridLayer) -> Unit = {},
    onBackToMenu: () -> Unit,
    onToggleMusic: () -> Unit,
    onNextTrack: () -> Unit,
    onPrevLevel: () -> Unit,
    onNextLevel: () -> Unit,
    onSelectSpeed: (SimulationSpeed) -> Unit,
    onResetBoard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(FactoryColors.SurfaceElevated)
            .border(width = 1.dp, color = FactoryColors.GridCellBorder)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: Menu button + Level switcher + Music toggle + IPM readout
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(FactoryColors.GridCell)
                        .border(1.dp, FactoryColors.GridCellBorder, RoundedCornerShape(6.dp))
                        .clickable(onClick = onBackToMenu)
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Menu",
                        color = FactoryColors.TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                LevelNavButton(label = "<", enabled = hasPrevLevel, onClick = onPrevLevel)
                Spacer(modifier = Modifier.width(5.dp))
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "LVL ${level.id} · ${level.name}",
                        color = FactoryColors.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = level.subtitle,
                        color = FactoryColors.TextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(5.dp))
                LevelNavButton(label = ">", enabled = hasNextLevel, onClick = onNextLevel)
            }

            Spacer(modifier = Modifier.width(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isMusicEnabled) FactoryColors.SpawnerBody else FactoryColors.GridCell)
                        .border(
                            1.dp,
                            if (isMusicEnabled) FactoryColors.SpawnerAccent.copy(alpha = 0.7f) else FactoryColors.GridCellBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable(onClick = onToggleMusic)
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isMusicEnabled) "♪" else "♪̸",
                        color = if (isMusicEnabled) FactoryColors.SpawnerAccent else FactoryColors.TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isMusicEnabled) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(FactoryColors.GridCell)
                            .border(1.dp, FactoryColors.GridCellBorder, RoundedCornerShape(8.dp))
                            .clickable(onClick = onNextTrack)
                            .padding(horizontal = 6.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "››",
                            color = FactoryColors.SpawnerAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Rolling IPM badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FactoryColors.GridCell)
                        .border(1.dp, FactoryColors.GridCellBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f IPM", rollingIpm),
                        color = FactoryColors.BeltChevron,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Row 2: Quota indicators + Simulation speed toggles + Reset
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (hatch in hatches) {
                    val done = hatch.deliveredCount >= hatch.targetCount
                    val accent = if (done) FactoryColors.HatchDoneAccent else FactoryColors.HatchAccent
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (done) FactoryColors.HatchDoneBody else FactoryColors.GridCell)
                            .border(1.dp, accent.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Canvas(modifier = Modifier.size(15.dp)) {
                            drawItemShape(
                                type = hatch.requiredItem,
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = size.minDimension * 0.44f,
                                drawShadow = false
                            )
                        }
                        Text(
                            text = "${hatch.deliveredCount.coerceAtMost(hatch.targetCount)}/${hatch.targetCount}",
                            color = if (done) FactoryColors.HatchDoneAccent else FactoryColors.TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (option in SimulationSpeed.entries) {
                    val selected = speed == option
                    val bg = when {
                        selected && option == SimulationSpeed.PAUSED -> FactoryColors.HatchAccent
                        selected -> FactoryColors.BeltChevron
                        else -> FactoryColors.GridCell
                    }
                    val fg = if (selected) FactoryColors.Background else FactoryColors.TextSecondary
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(bg)
                            .border(
                                1.dp,
                                if (selected) Color.Transparent else FactoryColors.GridCellBorder,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { onSelectSpeed(option) }
                            .padding(horizontal = 7.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (option == SimulationSpeed.PAUSED) "II" else option.label,
                            color = fg,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(2.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(FactoryColors.GridCell)
                        .border(1.dp, FactoryColors.BulldozeAccent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .clickable { onResetBoard() }
                        .padding(horizontal = 7.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Reset",
                        color = FactoryColors.BulldozeAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Row 3 (Power Levels only): Segmented [Factory] / [⚡ Power] layer toggle + live network wattage & capacitor readout
        if (level.requiresPower) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FactoryColors.Background)
                        .border(1.dp, FactoryColors.GridCellBorder, RoundedCornerShape(8.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val factorySelected = activeLayer == GridLayer.FACTORY
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (factorySelected) FactoryColors.BeltChevron else Color.Transparent)
                            .clickable { onSelectLayer(GridLayer.FACTORY) }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Factory",
                            color = if (factorySelected) FactoryColors.Background else FactoryColors.TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    val powerSelected = activeLayer == GridLayer.POWER
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (powerSelected) FactoryColors.CableCore else Color.Transparent)
                            .clickable { onSelectLayer(GridLayer.POWER) }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚡ Power",
                            color = if (powerSelected) FactoryColors.Background else FactoryColors.CableCore,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val wattAccent = when {
                        powerDemand <= 0f && generatedPower <= 0f -> FactoryColors.TextSecondary
                        powerSatisfaction >= 0.99f -> FactoryColors.HatchDoneAccent
                        powerSatisfaction > 0.01f -> FactoryColors.HatchAccent
                        else -> FactoryColors.IncineratorAccent
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(FactoryColors.GridCell)
                            .border(1.dp, wattAccent.copy(alpha = 0.65f), RoundedCornerShape(7.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "⚡ %.1f / %.1fW", generatedPower, powerDemand),
                            color = wattAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(FactoryColors.CapacitorBody)
                            .border(1.dp, FactoryColors.CapacitorAccent.copy(alpha = 0.65f), RoundedCornerShape(7.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = String.format(
                                Locale.US,
                                "CAP %.0f/%.0f⚡",
                                capacitorStored,
                                capacitorCapacity
                            ),
                            color = FactoryColors.CapacitorCharge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelNavButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(25.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(FactoryColors.GridCell)
            .border(1.dp, FactoryColors.GridCellBorder, RoundedCornerShape(6.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (enabled) FactoryColors.TextPrimary else FactoryColors.TextMuted.copy(alpha = 0.4f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
