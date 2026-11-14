package com.microfactory.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.microfactory.data.LevelRecord
import com.microfactory.render.FactoryColors
import com.microfactory.render.drawItemShape
import com.microfactory.sim.ItemType
import com.microfactory.sim.LevelConfig
import com.microfactory.sim.TileEntity
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class TierFilter(val label: String, val range: IntRange)

private val TIER_FILTERS = listOf(
    TierFilter("All (80)", 1..80),
    TierFilter("1–8 Core", 1..8),
    TierFilter("9–18 Bridge", 9..18),
    TierFilter("19–30 Circuit", 19..30),
    TierFilter("31–44 Filter", 31..44),
    TierFilter("45–62 Motor", 45..62),
    TierFilter("63–80 Apex", 63..80)
)

@Composable
fun MainMenuScreen(
    levels: List<LevelConfig>,
    records: Map<Int, LevelRecord>,
    continueLevelId: Int,
    isMusicEnabled: Boolean,
    onPlayLevel: (Int) -> Unit,
    onToggleMusic: () -> Unit
) {
    var animPhase by remember { mutableFloatStateOf(0f) }
    var selectedTierIdx by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        var lastNanos = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dtSec = ((now - lastNanos).coerceIn(0L, 100_000_000L)) / 1_000_000_000f
            lastNanos = now
            animPhase = (animPhase + dtSec * 0.55f) % 1f
        }
    }

    val totalStarsEarned = records.values.sumOf { it.bestStars }
    val maxPossibleStars = levels.size * 3
    val continueLevel = levels.firstOrNull { it.id == continueLevelId } ?: levels.first()
    val filteredLevels = levels.filter { it.id in TIER_FILTERS[selectedTierIdx].range }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FactoryColors.Background)
            .safeDrawingPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(FactoryColors.SurfaceElevated)
                .border(1.dp, FactoryColors.GridOuterFrame, RoundedCornerShape(18.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PORTRAIT AUTOMATION PUZZLER",
                color = FactoryColors.BeltChevron,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "BELTWORKS",
                color = FactoryColors.TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.4.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                val trackH = size.height * 0.62f
                val railH = trackH + 6f
                val cy = size.height / 2f

                drawRoundRect(
                    color = FactoryColors.BeltRail,
                    topLeft = Offset(0f, cy - railH / 2f),
                    size = Size(size.width, railH),
                    cornerRadius = CornerRadius(10f)
                )
                drawRoundRect(
                    color = FactoryColors.BeltTrack,
                    topLeft = Offset(3f, cy - trackH / 2f),
                    size = Size(size.width - 6f, trackH),
                    cornerRadius = CornerRadius(8f)
                )

                val chevronCount = 9
                val spacing = size.width / chevronCount
                val chevSize = trackH * 0.28f
                for (i in 0..chevronCount) {
                    val x = ((i + animPhase) * spacing) % (size.width + spacing) - spacing * 0.5f
                    val path = Path().apply {
                        moveTo(x - chevSize * 0.5f, cy - chevSize * 0.7f)
                        lineTo(x + chevSize * 0.5f, cy)
                        lineTo(x - chevSize * 0.5f, cy + chevSize * 0.7f)
                    }
                    drawPath(
                        path = path,
                        color = FactoryColors.BeltChevron.copy(alpha = 0.75f),
                        style = Stroke(width = 2.6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                val showcaseItems = listOf(
                    ItemType.IRON_ORE,
                    ItemType.IRON_PLATE,
                    ItemType.COPPER_WIRE,
                    ItemType.CIRCUIT,
                    ItemType.MOTOR
                )
                val itemSpacing = size.width / showcaseItems.size
                showcaseItems.forEachIndexed { idx, itemType ->
                    val x = ((idx + animPhase) * itemSpacing) % (size.width + itemSpacing * 0.5f) - itemSpacing * 0.25f
                    if (x in -20f..(size.width + 20f)) {
                        drawItemShape(itemType, Offset(x, cy), trackH * 0.34f)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onPlayLevel(continueLevel.id) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FactoryColors.BeltChevron,
                        contentColor = FactoryColors.Background
                    )
                ) {
                    Text(
                        text = "PLAY LVL ${continueLevel.id}: ${continueLevel.name.uppercase()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isMusicEnabled) FactoryColors.SpawnerBody else FactoryColors.GridCell)
                        .border(
                            1.2.dp,
                            if (isMusicEnabled) FactoryColors.SpawnerAccent else FactoryColors.GridCellBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(onClick = onToggleMusic)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isMusicEnabled) "♪ ON" else "♪ OFF",
                        color = if (isMusicEnabled) FactoryColors.SpawnerAccent else FactoryColors.TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FACTORY CONTRACTS (${levels.size})",
                color = FactoryColors.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "★ $totalStarsEarned / $maxPossibleStars",
                color = FactoryColors.GearGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tier jump filter bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TIER_FILTERS.forEachIndexed { idx, tier ->
                val selected = idx == selectedTierIdx
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) FactoryColors.BeltChevron else FactoryColors.SurfaceElevated)
                        .border(
                            1.dp,
                            if (selected) FactoryColors.BeltChevron else FactoryColors.GridCellBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedTierIdx = idx }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tier.label,
                        color = if (selected) FactoryColors.Background else FactoryColors.TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(filteredLevels, key = { it.id }) { level ->
                val record = records[level.id] ?: LevelRecord()
                LevelCard(
                    level = level,
                    record = record,
                    onClick = { onPlayLevel(level.id) }
                )
            }
        }
    }
}

@Composable
private fun LevelCard(
    level: LevelConfig,
    record: LevelRecord,
    onClick: () -> Unit
) {
    val hatches = level.hatches.mapNotNull { it.tile as? TileEntity.Hatch }
    val completed = record.bestStars > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FactoryColors.SurfaceElevated)
            .border(
                width = 1.dp,
                color = if (completed) FactoryColors.HatchDoneAccent.copy(alpha = 0.55f) else FactoryColors.GridCellBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(FactoryColors.GridCell)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${level.id}",
                        color = FactoryColors.BeltChevron,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = level.name,
                    color = FactoryColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${level.width}×${level.height}",
                    color = FactoryColors.TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                val badge = level.newMechanicBadge
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(FactoryColors.AssemblerBody)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = badge,
                            color = FactoryColors.AssemblerLight,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = level.subtitle,
                color = FactoryColors.TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (hatch in hatches) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(FactoryColors.GridCell)
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Canvas(modifier = Modifier.size(13.dp)) {
                            drawItemShape(
                                type = hatch.requiredItem,
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = size.minDimension * 0.44f,
                                drawShadow = false
                            )
                        }
                        Text(
                            text = "×${hatch.targetCount}",
                            color = FactoryColors.TextPrimary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Text(
                    text = if (record.bestBelts != null) "Best ${record.bestBelts}b (Par ${level.parBelts})" else "Par ${level.parBelts}b",
                    color = FactoryColors.TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            for (s in 1..3) {
                val earned = s <= record.bestStars
                Canvas(modifier = Modifier.size(17.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val outerR = size.minDimension * 0.46f
                    val innerR = outerR * 0.44f
                    val path = Path()
                    for (p in 0 until 10) {
                        val angle = -PI.toFloat() / 2f + p * (PI.toFloat() / 5f)
                        val r = if (p % 2 == 0) outerR else innerR
                        val px = cx + cos(angle) * r
                        val py = cy + sin(angle) * r
                        if (p == 0) path.moveTo(px, py) else path.lineTo(px, py)
                    }
                    path.close()
                    drawPath(
                        path = path,
                        color = if (earned) FactoryColors.GearGold else FactoryColors.GridCell
                    )
                    drawPath(
                        path = path,
                        color = if (earned) FactoryColors.GearDark else FactoryColors.GridCellBorder,
                        style = Stroke(width = 1.4f)
                    )
                }
            }
        }
    }
}
