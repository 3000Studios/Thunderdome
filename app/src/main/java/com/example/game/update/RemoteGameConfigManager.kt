package com.example.game.update

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class RemoteGameConfig(
    val contentVersion: Int = 1,
    val dailyChallengeMultiplier: Float = 1.0f,
    val featuredAircraftId: String = "apex_falcon",
    val globalCreditDropMultiplier: Float = 1.0f,
    val activeEventName: String = "VANGUARD INITIATIVE",
    val announcements: List<String> = listOf("Welcome to Thunder Dome // Sorties Ready"),
    val bossTauntOverrides: Map<String, String> = emptyMap()
)

class RemoteGameConfigManager(private val context: Context) {
    companion object {
        private const val TAG = "RemoteGameConfig"
        private const val CONFIG_FILE_NAME = "remote_game_config.json"
        const val REMOTE_CONFIG_URL = "https://3000studios.vip/api/thunderdome/content_config.json"
    }

    private val _config = MutableStateFlow(RemoteGameConfig())
    val config: StateFlow<RemoteGameConfig> = _config

    init {
        loadCachedConfig()
    }

    private fun loadCachedConfig() {
        try {
            val file = File(context.filesDir, CONFIG_FILE_NAME)
            if (file.exists()) {
                val jsonStr = file.readText()
                val parsed = parseJson(jsonStr)
                if (parsed != null) {
                    _config.value = parsed
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error loading cached config: ${e.message}")
        }
    }

    suspend fun fetchLatestConfig() {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(REMOTE_CONFIG_URL)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 4000
                    readTimeout = 4000
                    requestMethod = "GET"
                }

                if (conn.responseCode == 200) {
                    val body = conn.inputStream.bufferedReader().use { it.readText() }
                    val parsed = parseJson(body)
                    if (parsed != null && parsed.contentVersion >= _config.value.contentVersion) {
                        _config.value = parsed
                        // Cache valid config to disk
                        File(context.filesDir, CONFIG_FILE_NAME).writeText(body)
                        Log.i(TAG, "Successfully updated live game content to v${parsed.contentVersion}")
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Live content fetch offline/handled: using cached/local defaults (${e.message})")
            }
        }
    }

    private fun parseJson(jsonStr: String): RemoteGameConfig? {
        return try {
            val obj = JSONObject(jsonStr)
            val ver = obj.optInt("contentVersion", 1)
            val dailyMult = obj.optDouble("dailyChallengeMultiplier", 1.0).toFloat()
            val featured = obj.optString("featuredAircraftId", "apex_falcon")
            val dropMult = obj.optDouble("globalCreditDropMultiplier", 1.0).toFloat()
            val eventName = obj.optString("activeEventName", "VANGUARD INITIATIVE")

            val announcementsList = mutableListOf<String>()
            val annArray = obj.optJSONArray("announcements")
            if (annArray != null) {
                for (i in 0 until annArray.length()) {
                    announcementsList.add(annArray.getString(i))
                }
            } else {
                announcementsList.add("Welcome to Thunder Dome // Sorties Ready")
            }

            val tauntMap = mutableMapOf<String, String>()
            val tauntsObj = obj.optJSONObject("bossTauntOverrides")
            if (tauntsObj != null) {
                for (key in tauntsObj.keys()) {
                    tauntMap[key] = tauntsObj.getString(key)
                }
            }

            RemoteGameConfig(
                contentVersion = ver,
                dailyChallengeMultiplier = dailyMult,
                featuredAircraftId = featured,
                globalCreditDropMultiplier = dropMult,
                activeEventName = eventName,
                announcements = announcementsList,
                bossTauntOverrides = tauntMap
            )
        } catch (e: Exception) {
            Log.w(TAG, "Malformed remote config JSON rejected: ${e.message}")
            null
        }
    }
}
