package com.microfactory.data

import android.content.Context

data class LevelRecord(
    val bestStars: Int = 0,
    val bestBelts: Int? = null
)

class ProgressStore(context: Context) {
    private val prefs = context.getSharedPreferences("micro_factory_progress", Context.MODE_PRIVATE)

    fun getRecord(levelId: Int): LevelRecord {
        val stars = prefs.getInt("stars_$levelId", 0)
        val belts = if (prefs.contains("belts_$levelId")) prefs.getInt("belts_$levelId", 0) else null
        return LevelRecord(bestStars = stars, bestBelts = belts)
    }

    fun recordCompletion(levelId: Int, stars: Int, beltsUsed: Int) {
        val existing = getRecord(levelId)
        val editor = prefs.edit()
        if (stars > existing.bestStars) {
            editor.putInt("stars_$levelId", stars)
        }
        val prevBelts = existing.bestBelts
        if (prevBelts == null || beltsUsed < prevBelts) {
            editor.putInt("belts_$levelId", beltsUsed)
        }
        editor.putInt("last_played_level", levelId)
        editor.apply()
    }

    fun getLastPlayedLevelId(): Int = prefs.getInt("last_played_level", 1)

    fun setLastPlayedLevelId(levelId: Int) {
        prefs.edit().putInt("last_played_level", levelId).apply()
    }

    fun isMusicEnabled(): Boolean = prefs.getBoolean("music_enabled", true)

    fun setMusicEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("music_enabled", enabled).apply()
    }
}
