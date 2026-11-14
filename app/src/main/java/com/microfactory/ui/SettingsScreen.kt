package com.microfactory.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.microfactory.BuildConfig
import com.microfactory.render.FactoryColors

@Composable
fun SettingsScreen(
    isHelpAdsEnabled: Boolean,
    isMusicEnabled: Boolean,
    currentTrackTitle: String,
    onToggleHelpAds: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onNextTrack: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FactoryColors.Background)
            .safeDrawingPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(FactoryColors.SurfaceElevated)
                    .border(1.dp, FactoryColors.GridCellBorder, RoundedCornerShape(10.dp))
                    .clickable(onClick = onBack)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "← BACK",
                    color = FactoryColors.TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Column {
                Text(
                    text = "SETTINGS",
                    color = FactoryColors.TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Customize gameplay help & audio",
                    color = FactoryColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Card 1: Help Ads
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(FactoryColors.SurfaceElevated)
                .border(1.dp, FactoryColors.GridOuterFrame, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "HELP & ADS",
                color = FactoryColors.BeltChevron,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Help Ad Before Hint",
                        color = FactoryColors.TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Play a short (~15s) skippable ad when unlocking level Help.",
                        color = FactoryColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = isHelpAdsEnabled,
                    onCheckedChange = onToggleHelpAds,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = FactoryColors.Background,
                        checkedTrackColor = FactoryColors.BeltChevron,
                        uncheckedThumbColor = FactoryColors.TextSecondary,
                        uncheckedTrackColor = FactoryColors.GridCell
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(FactoryColors.GridCell)
                    .border(1.dp, FactoryColors.GridCellBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Leaving this setting on supports the game's development. I strongly appreciate if you leave this setting on, but I also understand turning it off.",
                    color = FactoryColors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        // Card 2: Audio
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(FactoryColors.SurfaceElevated)
                .border(1.dp, FactoryColors.GridOuterFrame, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "AUDIO",
                color = FactoryColors.SpawnerAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Flow Soundtrack",
                        color = FactoryColors.TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "5 procedural ambient tracks & tactile sound effects.",
                        color = FactoryColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = isMusicEnabled,
                    onCheckedChange = onToggleMusic,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = FactoryColors.Background,
                        checkedTrackColor = FactoryColors.SpawnerAccent,
                        uncheckedThumbColor = FactoryColors.TextSecondary,
                        uncheckedTrackColor = FactoryColors.GridCell
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(FactoryColors.GridCell)
                    .border(1.dp, FactoryColors.GridCellBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isMusicEnabled) "♪ Now Playing: $currentTrackTitle" else "♪ Soundtrack Muted",
                    color = if (isMusicEnabled) FactoryColors.TextPrimary else FactoryColors.TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (isMusicEnabled) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(FactoryColors.SpawnerBody)
                            .border(1.dp, FactoryColors.SpawnerAccent.copy(alpha = 0.6f), RoundedCornerShape(7.dp))
                            .clickable(onClick = onNextTrack)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "NEXT ▸",
                            color = FactoryColors.SpawnerAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Card 3: Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(FactoryColors.SurfaceElevated)
                .border(1.dp, FactoryColors.GridCellBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "BELTWORKS · v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                color = FactoryColors.TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Package: ${BuildConfig.APPLICATION_ID} · Lunar Entertainment",
                color = FactoryColors.TextMuted,
                fontSize = 11.sp
            )
        }
    }
}
