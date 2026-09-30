package com.example.game.update

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed class AppUpdateStatus {
    object UpToDate : AppUpdateStatus()
    data class UpdateAvailable(val latestVersion: String, val updateMessage: String, val storeUrl: String) : AppUpdateStatus()
    data class UpdateRequired(val minimumVersion: String, val updateMessage: String, val storeUrl: String) : AppUpdateStatus()
}

class AppUpdateManager(private val context: Context) {
    companion object {
        private const val TAG = "AppUpdateManager"
        // Remote version endpoint URL (or official fallback endpoint)
        const val VERSION_CHECK_URL = "https://3000studios.vip/api/thunderdome/version.json"
    }

    private val _updateStatus = MutableStateFlow<AppUpdateStatus>(AppUpdateStatus.UpToDate)
    val updateStatus: StateFlow<AppUpdateStatus> = _updateStatus

    suspend fun checkForUpdates(currentVersionCode: Int, currentVersionName: String) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(VERSION_CHECK_URL)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 4000
                    readTimeout = 4000
                    requestMethod = "GET"
                }

                if (conn.responseCode == 200) {
                    val body = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(body)

                    val latestVersionCode = json.optInt("latestVersionCode", currentVersionCode)
                    val latestVersionName = json.optString("latestVersionName", currentVersionName)
                    val minimumVersionCode = json.optInt("minimumVersionCode", 1)
                    val updateMessage = json.optString("updateMessage", "A new version of Thunder Dome is available with new warbirds and stages!")
                    val storeUrl = json.optString("storeUrl", "https://play.google.com/store/apps/details?id=com.aistudio.aerostrike.xrkfpz")

                    if (currentVersionCode < minimumVersionCode) {
                        _updateStatus.value = AppUpdateStatus.UpdateRequired(
                            minimumVersion = "v$minimumVersionCode",
                            updateMessage = updateMessage,
                            storeUrl = storeUrl
                        )
                    } else if (currentVersionCode < latestVersionCode) {
                        _updateStatus.value = AppUpdateStatus.UpdateAvailable(
                            latestVersion = latestVersionName,
                            updateMessage = updateMessage,
                            storeUrl = storeUrl
                        )
                    } else {
                        _updateStatus.value = AppUpdateStatus.UpToDate
                    }
                } else {
                    _updateStatus.value = AppUpdateStatus.UpToDate
                }
            } catch (e: Exception) {
                Log.d(TAG, "Version check handled: offline or endpoint unreachable (${e.message})")
                _updateStatus.value = AppUpdateStatus.UpToDate
            }
        }
    }
}
