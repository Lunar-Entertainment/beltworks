package com.microfactory

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.microfactory.ads.HelpAdManager
import com.microfactory.audio.FlowMusicEngine
import com.microfactory.data.ProgressStore
import com.microfactory.sim.LevelRepository
import com.microfactory.ui.AdNoticeOverlay
import com.microfactory.ui.FactoryGameScreen
import com.microfactory.ui.MainMenuScreen
import com.microfactory.ui.MicroFactoryTheme
import com.microfactory.ui.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.parseColor("#12161F")),
            navigationBarStyle = SystemBarStyle.dark(Color.parseColor("#12161F"))
        )
        setContent {
            MicroFactoryTheme {
                val progressStore = remember { ProgressStore(applicationContext) }
                val musicEngine = remember { FlowMusicEngine() }
                val helpAdManager = remember { HelpAdManager(applicationContext) }
                val lifecycleOwner = LocalLifecycleOwner.current

                val currentVersionCode = BuildConfig.VERSION_CODE.toLong()
                val lastSeenNoticeVersion = remember { progressStore.getLastSeenNoticeVersion() }

                var isMusicEnabled by remember { mutableStateOf(progressStore.isMusicEnabled()) }
                var isHelpAdsEnabled by remember { mutableStateOf(progressStore.isHelpAdsEnabled()) }
                var showAdNotice by remember { mutableStateOf(lastSeenNoticeVersion < currentVersionCode) }
                var isSettingsOpen by remember { mutableStateOf(false) }

                var currentTrackTitle by remember { mutableStateOf(musicEngine.getCurrentTrackTitle()) }
                var activeLevelId by remember { mutableStateOf<Int?>(null) }
                var lastPlayedLevelId by remember { mutableIntStateOf(progressStore.getLastPlayedLevelId()) }
                var records by remember {
                    mutableStateOf(
                        LevelRepository.levels.associate { it.id to progressStore.getRecord(it.id) }
                    )
                }

                val completedIds = records.filterValues { it.bestStars > 0 }.keys
                val highestUnlockedStandardId = LevelRepository.highestUnlockedLevelId(completedIds)
                val highestUnlockedExpertId = LevelRepository.highestUnlockedExpertLevelId(completedIds)

                // Initialize AdMob SDK on launch when Help Ads are enabled.
                DisposableEffect(isHelpAdsEnabled) {
                    if (isHelpAdsEnabled) {
                        helpAdManager.initialize()
                    }
                    onDispose {}
                }

                // Keep AudioTrack playback bound to foreground lifecycle.
                DisposableEffect(lifecycleOwner) {
                    val mainHandler = Handler(Looper.getMainLooper())
                    musicEngine.onTrackChanged = { title ->
                        mainHandler.post { currentTrackTitle = title }
                    }
                    musicEngine.setMuted(!isMusicEnabled)
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> musicEngine.start()
                            Lifecycle.Event.ON_PAUSE -> musicEngine.stop()
                            else -> Unit
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        musicEngine.start()
                    }
                    onDispose {
                        musicEngine.onTrackChanged = null
                        lifecycleOwner.lifecycle.removeObserver(observer)
                        musicEngine.stop()
                    }
                }

                val toggleMusic: (Boolean) -> Unit = { enabled ->
                    isMusicEnabled = enabled
                    progressStore.setMusicEnabled(enabled)
                    musicEngine.setMuted(!enabled)
                }

                val toggleHelpAds: (Boolean) -> Unit = { enabled ->
                    isHelpAdsEnabled = enabled
                    progressStore.setHelpAdsEnabled(enabled)
                    if (enabled) {
                        helpAdManager.preload()
                    }
                }

                val currentLevelId = activeLevelId
                when {
                    showAdNotice -> {
                        AdNoticeOverlay(
                            isUpdate = lastSeenNoticeVersion in 1 until currentVersionCode,
                            onContinue = {
                                progressStore.setLastSeenNoticeVersion(currentVersionCode)
                                showAdNotice = false
                            },
                            onOpenSettings = {
                                progressStore.setLastSeenNoticeVersion(currentVersionCode)
                                showAdNotice = false
                                isSettingsOpen = true
                            }
                        )
                    }

                    isSettingsOpen -> {
                        val allLevelIds = LevelRepository.levels.map { it.id }
                        SettingsScreen(
                            isHelpAdsEnabled = isHelpAdsEnabled,
                            isMusicEnabled = isMusicEnabled,
                            currentTrackTitle = currentTrackTitle,
                            completedCount = completedIds.size,
                            totalStarsEarned = records.values.sumOf { it.bestStars },
                            onExportSaveCode = { progressStore.exportSaveCode(allLevelIds) },
                            onExportSaveJson = { progressStore.exportSaveJson(allLevelIds) },
                            onImportSaveData = { raw ->
                                progressStore.importSaveData(raw).also { res ->
                                    if (res.isSuccess) {
                                        records = LevelRepository.levels.associate { it.id to progressStore.getRecord(it.id) }
                                        lastPlayedLevelId = progressStore.getLastPlayedLevelId()
                                        isMusicEnabled = progressStore.isMusicEnabled()
                                        isHelpAdsEnabled = progressStore.isHelpAdsEnabled()
                                        musicEngine.setMuted(!isMusicEnabled)
                                    }
                                }
                            },
                            onToggleHelpAds = toggleHelpAds,
                            onToggleMusic = toggleMusic,
                            onNextTrack = { musicEngine.nextTrack() },
                            onBack = { isSettingsOpen = false }
                        )
                    }

                    currentLevelId == null -> {
                        MainMenuScreen(
                            levels = LevelRepository.levels,
                            records = records,
                            continueLevelId = lastPlayedLevelId,
                            highestUnlockedLevelId = highestUnlockedStandardId,
                            highestUnlockedExpertLevelId = highestUnlockedExpertId,
                            isMusicEnabled = isMusicEnabled,
                            currentTrackTitle = currentTrackTitle,
                            onPlayLevel = { levelId ->
                                if (LevelRepository.isLevelUnlocked(levelId, completedIds)) {
                                    lastPlayedLevelId = levelId
                                    progressStore.setLastPlayedLevelId(levelId)
                                    activeLevelId = levelId
                                }
                            },
                            onToggleMusic = { toggleMusic(!isMusicEnabled) },
                            onNextTrack = { musicEngine.nextTrack() },
                            onOpenSettings = { isSettingsOpen = true }
                        )
                    }

                    else -> {
                        val activeLevel = LevelRepository.getLevel(currentLevelId)
                        FactoryGameScreen(
                            initialLevelId = currentLevelId,
                            highestUnlockedLevelId = if (activeLevel.isExpert) {
                                highestUnlockedExpertId
                            } else {
                                highestUnlockedStandardId
                            },
                            isMusicEnabled = isMusicEnabled,
                            isHelpAdsEnabled = isHelpAdsEnabled,
                            onToggleMusic = { toggleMusic(!isMusicEnabled) },
                            onNextTrack = { musicEngine.nextTrack() },
                            onBackToMenu = {
                                records = LevelRepository.levels.associate { it.id to progressStore.getRecord(it.id) }
                                activeLevelId = null
                            },
                            onTileEdited = {
                                musicEngine.playPlaceSound()
                            },
                            onRequestHelpAd = { onUnlocked, onLoadingChanged, onDenied ->
                                if (!isHelpAdsEnabled) {
                                    onUnlocked(null)
                                } else {
                                    onLoadingChanged(true)
                                    helpAdManager.showHelpAd(
                                        activity = this@MainActivity,
                                        onAdStarted = {
                                            onLoadingChanged(false)
                                            musicEngine.setMuted(true)
                                        },
                                        onAdEnded = {
                                            musicEngine.setMuted(!isMusicEnabled)
                                        },
                                        onHelpUnlocked = {
                                            onLoadingChanged(false)
                                            onUnlocked(null)
                                        },
                                        onSkippedBeforeReward = {
                                            onLoadingChanged(false)
                                            onDenied("Finish the ad to unlock Help, or turn off Help Ads in Settings.")
                                        },
                                        onFallbackUnlock = { note ->
                                            onLoadingChanged(false)
                                            onUnlocked(note)
                                        }
                                    )
                                }
                            },
                            onLevelCompleted = { levelId, stars, beltsUsed ->
                                progressStore.recordCompletion(levelId, stars, beltsUsed)
                                lastPlayedLevelId = levelId
                                records = LevelRepository.levels.associate { it.id to progressStore.getRecord(it.id) }
                                musicEngine.playVictoryChime()
                            }
                        )
                    }
                }
            }
        }
    }
}
