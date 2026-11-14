package com.microfactory

import android.graphics.Color
import android.os.Bundle
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
import com.microfactory.audio.FlowMusicEngine
import com.microfactory.data.LevelRecord
import com.microfactory.data.ProgressStore
import com.microfactory.sim.LevelRepository
import com.microfactory.ui.FactoryGameScreen
import com.microfactory.ui.MainMenuScreen
import com.microfactory.ui.MicroFactoryTheme

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
                val lifecycleOwner = LocalLifecycleOwner.current

                var isMusicEnabled by remember { mutableStateOf(progressStore.isMusicEnabled()) }
                var activeLevelId by remember { mutableStateOf<Int?>(null) }
                var lastPlayedLevelId by remember { mutableIntStateOf(progressStore.getLastPlayedLevelId()) }
                var records by remember {
                    mutableStateOf(
                        LevelRepository.levels.associate { it.id to progressStore.getRecord(it.id) }
                    )
                }

                // Keep AudioTrack playback bound to foreground lifecycle.
                DisposableEffect(lifecycleOwner) {
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
                        lifecycleOwner.lifecycle.removeObserver(observer)
                        musicEngine.stop()
                    }
                }

                val toggleMusic = {
                    val next = !isMusicEnabled
                    isMusicEnabled = next
                    progressStore.setMusicEnabled(next)
                    musicEngine.setMuted(!next)
                }

                val currentLevelId = activeLevelId
                if (currentLevelId == null) {
                    MainMenuScreen(
                        levels = LevelRepository.levels,
                        records = records,
                        continueLevelId = lastPlayedLevelId,
                        isMusicEnabled = isMusicEnabled,
                        onPlayLevel = { levelId ->
                            lastPlayedLevelId = levelId
                            progressStore.setLastPlayedLevelId(levelId)
                            activeLevelId = levelId
                        },
                        onToggleMusic = toggleMusic
                    )
                } else {
                    FactoryGameScreen(
                        initialLevelId = currentLevelId,
                        isMusicEnabled = isMusicEnabled,
                        onToggleMusic = toggleMusic,
                        onBackToMenu = {
                            records = LevelRepository.levels.associate { it.id to progressStore.getRecord(it.id) }
                            activeLevelId = null
                        },
                        onTileEdited = {
                            musicEngine.playPlaceSound()
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
