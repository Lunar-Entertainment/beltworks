package com.microfactory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.microfactory.render.FactoryColors
import com.microfactory.sim.LevelConfig
import com.microfactory.sim.LevelHelpBlueprint

@Composable
fun HelpBlueprintDialog(
    level: LevelConfig,
    blueprint: LevelHelpBlueprint,
    statusNote: String?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(FactoryColors.SurfaceElevated)
                .border(1.5.dp, FactoryColors.BeltChevron, RoundedCornerShape(18.dp))
                .padding(18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "BLUEPRINT HELP · LVL ${level.id}",
                color = FactoryColors.BeltChevron,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )
            Text(
                text = level.name,
                color = FactoryColors.TextPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold
            )

            if (statusNote != null) {
                Text(
                    text = statusNote,
                    color = FactoryColors.SpawnerAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(FactoryColors.GridCell)
                    .border(1.dp, FactoryColors.GridCellBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = blueprint.summary,
                    color = FactoryColors.TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }

            Text(
                text = "PRODUCTION CHAIN",
                color = FactoryColors.TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            blueprint.craftingSteps.forEachIndexed { idx, step ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${idx + 1}.",
                        color = FactoryColors.BeltChevron,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = step,
                        color = FactoryColors.TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            if (blueprint.suggestedPathCoords.isNotEmpty()) {
                Text(
                    text = "Cyan guide markers are now lit on open grid lanes from spawners toward hatches.",
                    color = FactoryColors.SpawnerAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "TIPS",
                color = FactoryColors.TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            for (tip in blueprint.tips) {
                Text(
                    text = "• $tip",
                    color = FactoryColors.TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FactoryColors.BeltChevron,
                    contentColor = FactoryColors.Background
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "GOT IT",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
