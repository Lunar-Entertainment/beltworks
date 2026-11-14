package com.microfactory.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.microfactory.render.FactoryColors
import com.microfactory.sim.LevelConfig
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VictoryDialog(
    level: LevelConfig,
    beltsUsed: Int,
    elapsedSeconds: Double,
    ipm: Double,
    hasNextLevel: Boolean,
    onReplay: () -> Unit,
    onNextLevel: () -> Unit,
    onDismiss: () -> Unit
) {
    val stars = level.calculateStars(beltsUsed)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(FactoryColors.SurfaceElevated)
                .border(1.5.dp, FactoryColors.HatchDoneAccent, RoundedCornerShape(18.dp))
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "QUOTA COMPLETE",
                color = FactoryColors.HatchDoneAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Level ${level.id}: ${level.name}",
                color = FactoryColors.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1-3 Star Rating based on belt count used
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..3) {
                    val earned = i <= stars
                    Canvas(modifier = Modifier.size(36.dp)) {
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
                            style = Stroke(width = 2f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metrics summary
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(FactoryColors.GridCell)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricRow("Belts Used", "$beltsUsed (Par ${level.parBelts})")
                MetricRow("Throughput", String.format(Locale.US, "%.1f IPM", ipm))
                MetricRow("Sim Time", String.format(Locale.US, "%.1fs", elapsedSeconds))
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onReplay,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FactoryColors.TextPrimary)
                ) {
                    Text("Replay")
                }

                Button(
                    onClick = if (hasNextLevel) onNextLevel else onReplay,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FactoryColors.HatchDoneAccent,
                        contentColor = FactoryColors.Background
                    )
                ) {
                    Text(
                        text = if (hasNextLevel) "Next Level" else "Play Again",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = FactoryColors.TextSecondary, fontSize = 13.sp)
        Text(
            text = value,
            color = FactoryColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
