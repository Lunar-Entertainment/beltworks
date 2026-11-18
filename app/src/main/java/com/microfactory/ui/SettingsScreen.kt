package com.microfactory.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
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
    completedCount: Int,
    totalStarsEarned: Int,
    onExportSaveCode: () -> String,
    onExportSaveJson: () -> String,
    onImportSaveData: (String) -> Result<Int>,
    onToggleHelpAds: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onNextTrack: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var backupStatus by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importInput by remember { mutableStateOf("") }

    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(onExportSaveJson().toByteArray(Charsets.UTF_8))
                }
                backupStatus = "Exported save file! Keep it in Downloads or Drive."
            } catch (t: Throwable) {
                backupStatus = "Export failed: ${t.message ?: "unknown error"}"
            }
        }
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.use { input ->
                    input.bufferedReader(Charsets.UTF_8).readText()
                } ?: ""
                val result = onImportSaveData(content)
                backupStatus = result.fold(
                    onSuccess = { count -> "Restored progress for $count levels!" },
                    onFailure = { err -> "Invalid save file: ${err.message ?: "parse error"}" }
                )
            } catch (t: Throwable) {
                backupStatus = "Import failed: ${t.message ?: "unknown error"}"
            }
        }
    }

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
                    text = "Leaving this setting on supports development.",
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

        // Card 3: Save Backup & Restore
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(FactoryColors.SurfaceElevated)
                .border(1.dp, FactoryColors.GridOuterFrame, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SAVE & BACKUP",
                    color = FactoryColors.BeltChevron,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "$completedCount cleared · ★ $totalStarsEarned",
                    color = FactoryColors.GearGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = "Transfer progress between Play Store and Dev builds using a clipboard save code or a .json backup file.",
                color = FactoryColors.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(FactoryColors.BeltChevron)
                        .clickable {
                            val code = onExportSaveCode()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Beltworks Save Code", code))
                            backupStatus = "Copied save code ($completedCount levels) to clipboard!"
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "COPY CODE",
                        color = FactoryColors.Background,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(FactoryColors.GridCell)
                        .border(1.dp, FactoryColors.BeltChevron.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                        .clickable {
                            importInput = ""
                            showImportDialog = true
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RESTORE CODE",
                        color = FactoryColors.BeltChevron,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(FactoryColors.SpawnerBody)
                        .border(1.dp, FactoryColors.SpawnerAccent.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .clickable { exportFileLauncher.launch("beltworks-save.json") }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "EXPORT FILE",
                        color = FactoryColors.SpawnerAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(FactoryColors.GridCell)
                        .border(1.dp, FactoryColors.GridCellBorder, RoundedCornerShape(10.dp))
                        .clickable { importFileLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "IMPORT FILE",
                        color = FactoryColors.TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (backupStatus != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(FactoryColors.GridCell)
                        .border(1.dp, FactoryColors.BeltChevron.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = backupStatus!!,
                        color = FactoryColors.BeltChevron,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Card 4: Info
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

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = FactoryColors.SurfaceElevated,
            titleContentColor = FactoryColors.TextPrimary,
            textContentColor = FactoryColors.TextSecondary,
            title = {
                Text(
                    text = "Restore Save Code",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Paste your BW1:... save code below to merge stars and best belt records.",
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = importInput,
                        onValueChange = { importInput = it },
                        placeholder = { Text("BW1:...", fontSize = 12.sp) },
                        singleLine = false,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(FactoryColors.GridCell)
                            .border(1.dp, FactoryColors.GridCellBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                if (!clipText.isNullOrBlank()) {
                                    importInput = clipText
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "PASTE FROM CLIPBOARD",
                            color = FactoryColors.BeltChevron,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val result = onImportSaveData(importInput)
                        backupStatus = result.fold(
                            onSuccess = { count -> "Restored progress for $count levels!" },
                            onFailure = { err -> "Invalid save code: ${err.message ?: "parse error"}" }
                        )
                        showImportDialog = false
                    }
                ) {
                    Text("RESTORE", color = FactoryColors.BeltChevron, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("CANCEL", color = FactoryColors.TextSecondary)
                }
            }
        )
    }
}
