package com.example.game.multiplayer

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class RemotePlayerState(
    var callsign: String = "VIPER_01",
    var aircraftId: String = "apex_falcon",
    var x: Float = 540f,
    var y: Float = 400f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var bankAngle: Float = 0f,
    var health: Float = 1000f,
    var maxHealth: Float = 1000f,
    var shield: Float = 600f,
    var maxShield: Float = 600f,
    var isFiring: Boolean = false,
    var isBoosting: Boolean = false,
    var primaryColor: Color = Color(0xFFFF2A4D),
    var accentColor: Color = Color(0xFFFF9500)
)

enum class MultiplayerStatus {
    DISCONNECTED,
    CONNECTING,
    SEARCHING_MATCH,
    CONNECTED_DOGFIGHT,
    CONNECTION_ERROR
}

class MultiplayerManager {
    private val client = OkHttpClient.Builder()
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _status = MutableStateFlow(MultiplayerStatus.DISCONNECTED)
    val status: StateFlow<MultiplayerStatus> = _status

    private val _remotePlayer = MutableStateFlow<RemotePlayerState?>(null)
    val remotePlayer: StateFlow<RemotePlayerState?> = _remotePlayer

    private val _latencyMs = MutableStateFlow(0L)
    val latencyMs: StateFlow<Long> = _latencyMs

    var roomId: String = ""
    var isHost: Boolean = false

    fun connectToMatchmaking(
        playerCallsign: String,
        aircraftId: String,
        serverUrl: String = "wss://thunderdome-pvp.3000studios.workers.dev/ws"
    ) {
        _status.value = MultiplayerStatus.CONNECTING
        val request = Request.Builder()
            .url(serverUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _status.value = MultiplayerStatus.SEARCHING_MATCH
                // Send join match request
                val joinMsg = JSONObject().apply {
                    put("action", "join_match")
                    put("callsign", playerCallsign)
                    put("aircraftId", aircraftId)
                }
                webSocket.send(joinMsg.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    when (json.optString("event")) {
                        "match_found" -> {
                            roomId = json.optString("roomId")
                            isHost = json.optBoolean("isHost")
                            _status.value = MultiplayerStatus.CONNECTED_DOGFIGHT
                            
                            val opponent = json.optJSONObject("opponent")
                            if (opponent != null) {
                                _remotePlayer.value = RemotePlayerState(
                                    callsign = opponent.optString("callsign", "RIVAL_PILOT"),
                                    aircraftId = opponent.optString("aircraftId", "valkyrie_phantom")
                                )
                            } else {
                                _remotePlayer.value = RemotePlayerState()
                            }
                        }
                        "state_update" -> {
                            val remote = _remotePlayer.value ?: RemotePlayerState()
                            remote.x = json.optDouble("x", remote.x.toDouble()).toFloat()
                            remote.y = json.optDouble("y", remote.y.toDouble()).toFloat()
                            remote.vx = json.optDouble("vx", remote.vx.toDouble()).toFloat()
                            remote.vy = json.optDouble("vy", remote.vy.toDouble()).toFloat()
                            remote.bankAngle = json.optDouble("bankAngle", remote.bankAngle.toDouble()).toFloat()
                            remote.health = json.optDouble("health", remote.health.toDouble()).toFloat()
                            remote.shield = json.optDouble("shield", remote.shield.toDouble()).toFloat()
                            remote.isFiring = json.optBoolean("isFiring", remote.isFiring)
                            remote.isBoosting = json.optBoolean("isBoosting", remote.isBoosting)
                            _remotePlayer.value = remote
                        }
                        "ping" -> {
                            val clientTime = json.optLong("timestamp", System.currentTimeMillis())
                            _latencyMs.value = System.currentTimeMillis() - clientTime
                        }
                    }
                } catch (_: Exception) {}
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _status.value = MultiplayerStatus.CONNECTION_ERROR
                // Fallback simulation mode for testing multiplayer dogfight offline / when server disconnected
                startSimulatedOpponent(playerCallsign)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _status.value = MultiplayerStatus.DISCONNECTED
            }
        })
    }

    fun sendLocalState(
        x: Float,
        y: Float,
        vx: Float,
        vy: Float,
        bankAngle: Float,
        health: Float,
        shield: Float,
        isFiring: Boolean,
        isBoosting: Boolean
    ) {
        if (_status.value != MultiplayerStatus.CONNECTED_DOGFIGHT) return
        val msg = JSONObject().apply {
            put("action", "state_update")
            put("roomId", roomId)
            put("x", x)
            put("y", y)
            put("vx", vx)
            put("vy", vy)
            put("bankAngle", bankAngle)
            put("health", health)
            put("shield", shield)
            put("isFiring", isFiring)
            put("isBoosting", isBoosting)
            put("timestamp", System.currentTimeMillis())
        }
        webSocket?.send(msg.toString())
    }

    // Offline / Direct P2P fallback bot simulator if server is offline
    fun startSimulatedOpponent(playerCallsign: String) {
        scope.launch {
            _status.value = MultiplayerStatus.CONNECTED_DOGFIGHT
            _remotePlayer.value = RemotePlayerState(
                callsign = "ACE_PHANTOM_AI",
                aircraftId = "valkyrie_phantom",
                x = 540f,
                y = 350f
            )
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "User Disconnected")
        webSocket = null
        _status.value = MultiplayerStatus.DISCONNECTED
        _remotePlayer.value = null
    }
}
