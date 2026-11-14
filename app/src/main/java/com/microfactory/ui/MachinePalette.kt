package com.microfactory.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.microfactory.render.BuildTool
import com.microfactory.render.FactoryColors
import com.microfactory.render.drawGear

@Composable
fun MachinePalette(
    selectedTool: BuildTool,
    onSelectTool: (BuildTool) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(FactoryColors.SurfaceElevated)
            .border(width = 1.dp, color = FactoryColors.GridCellBorder)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = selectedTool.shortHint,
            color = FactoryColors.TextSecondary,
            fontSize = 11.sp,
            maxLines = 1
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (tool in BuildTool.entries) {
                val isSelected = tool == selectedTool
                val accent = toolAccentColor(tool)
                Column(
                    modifier = Modifier
                        .width(66.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) accent.copy(alpha = 0.16f) else FactoryColors.GridCell)
                        .border(
                            width = if (isSelected) 1.8.dp else 1.dp,
                            color = if (isSelected) accent else FactoryColors.GridCellBorder,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectTool(tool) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Canvas(modifier = Modifier.size(26.dp)) {
                        drawToolIcon(tool, accent)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tool.label,
                        color = if (isSelected) FactoryColors.TextPrimary else FactoryColors.TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private fun toolAccentColor(tool: BuildTool): Color = when (tool) {
    BuildTool.BELT -> FactoryColors.BeltChevron
    BuildTool.SPLITTER -> FactoryColors.SplitterAccent
    BuildTool.SMELTER -> FactoryColors.SmelterAccent
    BuildTool.PRESS -> FactoryColors.PressAccent
    BuildTool.ASSEMBLER -> FactoryColors.AssemblerAccent
    BuildTool.BULLDOZE -> FactoryColors.BulldozeAccent
}

private fun DrawScope.drawToolIcon(tool: BuildTool, accent: Color) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f

    when (tool) {
        BuildTool.BELT -> {
            drawRoundRect(
                color = FactoryColors.BeltTrack,
                topLeft = Offset(w * 0.08f, h * 0.24f),
                size = Size(w * 0.84f, h * 0.52f),
                cornerRadius = CornerRadius(4f)
            )
            val chevron = Path().apply {
                moveTo(w * 0.25f, h * 0.34f)
                lineTo(w * 0.45f, cy)
                lineTo(w * 0.25f, h * 0.66f)
                moveTo(w * 0.55f, h * 0.34f)
                lineTo(w * 0.75f, cy)
                lineTo(w * 0.55f, h * 0.66f)
            }
            drawPath(chevron, color = accent, style = Stroke(width = 2.6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }

        BuildTool.SPLITTER -> {
            val path = Path().apply {
                moveTo(cx, h * 0.86f)
                lineTo(cx, cy)
                moveTo(cx, cy)
                lineTo(w * 0.20f, h * 0.22f)
                moveTo(cx, cy)
                lineTo(w * 0.80f, h * 0.22f)
            }
            drawPath(path, color = accent, style = Stroke(width = 2.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawCircle(accent, radius = w * 0.14f, center = Offset(cx, cy))
        }

        BuildTool.SMELTER -> {
            drawRoundRect(
                color = FactoryColors.SmelterBody,
                topLeft = Offset(w * 0.12f, h * 0.12f),
                size = Size(w * 0.76f, h * 0.76f),
                cornerRadius = CornerRadius(6f)
            )
            drawRoundRect(
                color = accent,
                topLeft = Offset(w * 0.12f, h * 0.12f),
                size = Size(w * 0.76f, h * 0.76f),
                cornerRadius = CornerRadius(6f),
                style = Stroke(width = 2.2f)
            )
            drawCircle(FactoryColors.SmelterGlow, radius = w * 0.18f, center = Offset(cx, cy))
        }

        BuildTool.PRESS -> {
            drawRoundRect(
                color = FactoryColors.PressBody,
                topLeft = Offset(w * 0.12f, h * 0.12f),
                size = Size(w * 0.76f, h * 0.76f),
                cornerRadius = CornerRadius(6f)
            )
            drawRoundRect(
                color = accent,
                topLeft = Offset(w * 0.12f, h * 0.12f),
                size = Size(w * 0.76f, h * 0.76f),
                cornerRadius = CornerRadius(6f),
                style = Stroke(width = 2.2f)
            )
            drawRoundRect(
                color = FactoryColors.PressLight,
                topLeft = Offset(w * 0.26f, h * 0.28f),
                size = Size(w * 0.48f, h * 0.14f),
                cornerRadius = CornerRadius(2f)
            )
            drawRoundRect(
                color = FactoryColors.PressLight,
                topLeft = Offset(w * 0.26f, h * 0.58f),
                size = Size(w * 0.48f, h * 0.14f),
                cornerRadius = CornerRadius(2f)
            )
        }

        BuildTool.ASSEMBLER -> {
            drawRoundRect(
                color = FactoryColors.AssemblerBody,
                topLeft = Offset(w * 0.12f, h * 0.12f),
                size = Size(w * 0.76f, h * 0.76f),
                cornerRadius = CornerRadius(6f)
            )
            drawRoundRect(
                color = accent,
                topLeft = Offset(w * 0.12f, h * 0.12f),
                size = Size(w * 0.76f, h * 0.76f),
                cornerRadius = CornerRadius(6f),
                style = Stroke(width = 2.2f)
            )
            drawGear(
                center = Offset(cx, cy),
                outerRadius = w * 0.24f,
                innerRadius = w * 0.17f,
                holeRadius = w * 0.07f,
                teeth = 6,
                fillColor = FactoryColors.AssemblerLight,
                rimColor = FactoryColors.AssemblerBody
            )
        }

        BuildTool.BULLDOZE -> {
            drawLine(
                color = accent,
                start = Offset(w * 0.22f, h * 0.22f),
                end = Offset(w * 0.78f, h * 0.78f),
                strokeWidth = 3.2f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = accent,
                start = Offset(w * 0.78f, h * 0.22f),
                end = Offset(w * 0.22f, h * 0.78f),
                strokeWidth = 3.2f,
                cap = StrokeCap.Round
            )
        }
    }
}
