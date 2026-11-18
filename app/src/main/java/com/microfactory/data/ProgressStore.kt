package com.microfactory.data

import android.content.Context
import android.util.Base64
import org.json.JSONObject

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

    fun isHelpAdsEnabled(): Boolean = prefs.getBoolean("help_ads_enabled", true)

    fun setHelpAdsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("help_ads_enabled", enabled).apply()
    }

    fun getLastSeenNoticeVersion(): Long = prefs.getLong("last_seen_notice_version", -1L)

    fun setLastSeenNoticeVersion(versionCode: Long) {
        prefs.edit().putLong("last_seen_notice_version", versionCode).apply()
    }

    fun exportSaveJson(levelIds: List<Int>): String {
        val recordsObj = JSONObject()
        for (id in levelIds) {
            val rec = getRecord(id)
            if (rec.bestStars > 0 || rec.bestBelts != null) {
                val entry = JSONObject().apply {
                    put("s", rec.bestStars)
                    if (rec.bestBelts != null) put("b", rec.bestBelts)
                }
                recordsObj.put(id.toString(), entry)
            }
        }
        return JSONObject().apply {
            put("version", 1)
            put("lastPlayedLevel", getLastPlayedLevelId())
            put("musicEnabled", isMusicEnabled())
            put("helpAdsEnabled", isHelpAdsEnabled())
            put("records", recordsObj)
        }.toString()
    }

    fun exportSaveCode(levelIds: List<Int>): String {
        val jsonBytes = exportSaveJson(levelIds).toByteArray(Charsets.UTF_8)
        val encoded = Base64.encodeToString(jsonBytes, Base64.NO_WRAP or Base64.URL_SAFE)
        return SAVE_CODE_PREFIX + encoded
    }

    fun importSaveData(rawInput: String): Result<Int> {
        return try {
            val trimmed = rawInput.trim()
            val jsonText = if (trimmed.startsWith(SAVE_CODE_PREFIX)) {
                val payload = trimmed.removePrefix(SAVE_CODE_PREFIX)
                String(Base64.decode(payload, Base64.NO_WRAP or Base64.URL_SAFE), Charsets.UTF_8)
            } else {
                trimmed
            }

            val root = JSONObject(jsonText)
            val recordsObj = root.optJSONObject("records")
                ?: return Result.failure(IllegalArgumentException("Missing records in save data"))

            val editor = prefs.edit()
            var restoredCount = 0
            val keys = recordsObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val levelId = key.toIntOrNull() ?: continue
                val entry = recordsObj.optJSONObject(key) ?: continue
                val stars = entry.optInt("s", 0).coerceIn(0, 3)
                val belts = if (entry.has("b")) entry.optInt("b", -1).takeIf { it > 0 } else null

                val existing = getRecord(levelId)
                if (stars > existing.bestStars) {
                    editor.putInt("stars_$levelId", stars)
                }
                if (belts != null && (existing.bestBelts == null || belts < existing.bestBelts)) {
                    editor.putInt("belts_$levelId", belts)
                }
                if (stars > 0 || belts != null) {
                    restoredCount++
                }
            }

            if (root.has("lastPlayedLevel")) {
                val importedLast = root.optInt("lastPlayedLevel", 1).coerceAtLeast(1)
                if (importedLast > getLastPlayedLevelId() || getLastPlayedLevelId() == 1) {
                    editor.putInt("last_played_level", importedLast)
                }
            }
            if (root.has("musicEnabled")) {
                editor.putBoolean("music_enabled", root.optBoolean("musicEnabled", true))
            }
            if (root.has("helpAdsEnabled")) {
                editor.putBoolean("help_ads_enabled", root.optBoolean("helpAdsEnabled", true))
            }
            editor.apply()
            Result.success(restoredCount)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    companion object {
        private const val SAVE_CODE_PREFIX = "BW1:"
    }
}
