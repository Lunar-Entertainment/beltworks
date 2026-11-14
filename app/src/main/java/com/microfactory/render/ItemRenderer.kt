package com.microfactory.render

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.microfactory.sim.ItemType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun DrawScope.drawItemShape(
    type: ItemType,
    center: Offset,
    radius: Float,
    drawShadow: Boolean = true
) {
    if (drawShadow) {
        drawCircle(
            color = Color.Black.copy(alpha = 0.4f),
            radius = radius * 1.05f,
            center = center + Offset(0f, radius * 0.14f)
        )
    }

    when (type) {
        ItemType.IRON_ORE -> {
            drawOreChunk(
                center = center,
                radius = radius,
                baseColor = FactoryColors.IronOre,
                speckColor = FactoryColors.IronOreSpeck
            )
        }

        ItemType.COPPER_ORE -> {
            drawOreChunk(
                center = center,
                radius = radius,
                baseColor = FactoryColors.CopperOre,
                speckColor = FactoryColors.CopperOreSpeck
            )
        }

        ItemType.IRON_INGOT -> {
            drawIngotBar(
                center = center,
                radius = radius,
                baseColor = FactoryColors.IronIngot,
                highlightColor = FactoryColors.IronIngotHighlight
            )
        }

        ItemType.COPPER_INGOT -> {
            drawIngotBar(
                center = center,
                radius = radius,
                baseColor = FactoryColors.CopperIngot,
                highlightColor = FactoryColors.CopperIngotHighlight
            )
        }

        ItemType.IRON_PLATE -> {
            val side = radius * 1.56f
            val topLeft = Offset(center.x - side / 2f, center.y - side / 2f)
            drawRoundRect(
                color = FactoryColors.IronPlate,
                topLeft = topLeft,
                size = Size(side, side),
                cornerRadius = CornerRadius(radius * 0.28f)
            )
            val innerSide = side * 0.62f
            drawRoundRect(
                color = FactoryColors.IronPlateInner,
                topLeft = Offset(center.x - innerSide / 2f, center.y - innerSide / 2f),
                size = Size(innerSide, innerSide),
                cornerRadius = CornerRadius(radius * 0.14f),
                style = Stroke(width = (radius * 0.14f).coerceAtLeast(1.2f))
            )
            // Corner rivets
            val rivetOffset = side * 0.30f
            val rivetRadius = (radius * 0.09f).coerceAtLeast(1f)
            drawCircle(Color.White.copy(alpha = 0.7f), rivetRadius, center + Offset(-rivetOffset, -rivetOffset))
            drawCircle(Color.White.copy(alpha = 0.7f), rivetRadius, center + Offset(rivetOffset, -rivetOffset))
            drawCircle(Color.White.copy(alpha = 0.7f), rivetRadius, center + Offset(-rivetOffset, rivetOffset))
            drawCircle(Color.White.copy(alpha = 0.7f), rivetRadius, center + Offset(rivetOffset, rivetOffset))
        }

        ItemType.GEAR -> {
            drawGear(
                center = center,
                outerRadius = radius * 1.05f,
                innerRadius = radius * 0.72f,
                holeRadius = radius * 0.28f,
                teeth = 8,
                fillColor = FactoryColors.GearGold,
                rimColor = FactoryColors.GearDark
            )
        }
    }
}

private fun DrawScope.drawOreChunk(
    center: Offset,
    radius: Float,
    baseColor: Color,
    speckColor: Color
) {
    val angles = floatArrayOf(0.1f, 1.05f, 2.0f, 3.05f, 4.15f, 5.2f)
    val scales = floatArrayOf(0.95f, 0.80f, 1.0f, 0.85f, 0.98f, 0.76f)
    val path = Path().apply {
        for (i in angles.indices) {
            val r = radius * scales[i]
            val px = center.x + cos(angles[i]) * r
            val py = center.y + sin(angles[i]) * r
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }
    drawPath(path, color = baseColor)
    drawPath(path, color = Color.White.copy(alpha = 0.22f), style = Stroke(width = (radius * 0.1f).coerceAtLeast(1f)))

    // Metallic flecks
    drawCircle(speckColor, radius * 0.18f, center + Offset(-radius * 0.25f, -radius * 0.18f))
    drawCircle(speckColor, radius * 0.14f, center + Offset(radius * 0.22f, radius * 0.20f))
    drawCircle(speckColor, radius * 0.11f, center + Offset(radius * 0.15f, -radius * 0.30f))
}

private fun DrawScope.drawIngotBar(
    center: Offset,
    radius: Float,
    baseColor: Color,
    highlightColor: Color
) {
    val w = radius * 1.75f
    val h = radius * 1.05f
    val topLeft = Offset(center.x - w / 2f, center.y - h / 2f)
    drawRoundRect(
        color = baseColor,
        topLeft = topLeft,
        size = Size(w, h),
        cornerRadius = CornerRadius(radius * 0.25f)
    )
    // Top bevel highlight
    val insetW = w * 0.68f
    val insetH = h * 0.34f
    drawRoundRect(
        color = highlightColor,
        topLeft = Offset(center.x - insetW / 2f, center.y - insetH * 0.85f),
        size = Size(insetW, insetH),
        cornerRadius = CornerRadius(radius * 0.12f)
    )
}

fun DrawScope.drawGear(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    holeRadius: Float,
    teeth: Int,
    fillColor: Color,
    rimColor: Color
) {
    val path = Path()
    val steps = teeth * 4
    for (i in 0 until steps) {
        val angle = (i.toFloat() / steps) * (2f * PI.toFloat())
        val toothPhase = i % 4
        val r = if (toothPhase == 0 || toothPhase == 1) outerRadius else innerRadius
        val px = center.x + cos(angle) * r
        val py = center.y + sin(angle) * r
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path, color = fillColor)
    drawPath(path, color = rimColor, style = Stroke(width = (outerRadius * 0.10f).coerceAtLeast(1f)))
    drawCircle(color = FactoryColors.GridCell, radius = holeRadius, center = center)
    drawCircle(color = rimColor, radius = holeRadius, center = center, style = Stroke(width = (outerRadius * 0.10f).coerceAtLeast(1f)))
}
