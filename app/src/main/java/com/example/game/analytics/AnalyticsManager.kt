package com.example.game.analytics

import android.content.Context
import android.util.Log

object AnalyticsManager {
    private const val TAG = "AnalyticsManager"

    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        val paramStr = if (params.isNotEmpty()) {
            params.entries.joinToString(", ") { "${it.key}=${it.value}" }
        } else {
            "none"
        }
        Log.i(TAG, "[EVENT] $eventName -> $paramStr")
    }

    fun logGameOpen() = logEvent("game_open")

    fun logSortieStart(theaterId: String, aircraftId: String) =
        logEvent("level_start", mapOf("theater_id" to theaterId, "aircraft_id" to aircraftId))

    fun logSortieComplete(theaterId: String, score: Long, kills: Int, bossKills: Int) =
        logEvent("level_complete", mapOf("theater_id" to theaterId, "score" to score, "kills" to kills, "boss_kills" to bossKills))

    fun logSortieFail(theaterId: String, distanceMeters: Float) =
        logEvent("level_fail", mapOf("theater_id" to theaterId, "distance_reached" to distanceMeters))

    fun logBossReached(bossId: String, theaterId: String) =
        logEvent("boss_reached", mapOf("boss_id" to bossId, "theater_id" to theaterId))

    fun logBossDefeated(bossId: String, timeTakenSec: Float) =
        logEvent("boss_defeated", mapOf("boss_id" to bossId, "time_sec" to timeTakenSec))

    fun logPlayerDeath(theaterId: String, score: Long) =
        logEvent("player_death", mapOf("theater_id" to theaterId, "score" to score))

    fun logContinueUsed(method: String) =
        logEvent("continue_used", mapOf("method" to method))

    fun logRewardedAdStarted(placement: String) =
        logEvent("rewarded_ad_started", mapOf("placement" to placement))

    fun logRewardedAdCompleted(placement: String, rewardType: String) =
        logEvent("rewarded_ad_completed", mapOf("placement" to placement, "reward_type" to rewardType))

    fun logPurchaseStarted(productId: String) =
        logEvent("purchase_started", mapOf("product_id" to productId))

    fun logPurchaseCompleted(productId: String) =
        logEvent("purchase_completed", mapOf("product_id" to productId))

    fun logPurchaseFailed(productId: String, error: String) =
        logEvent("purchase_failed", mapOf("product_id" to productId, "error" to error))

    fun logPlaneSelected(aircraftId: String) =
        logEvent("plane_selected", mapOf("aircraft_id" to aircraftId))

    fun logPowerupUsed(powerupType: String) =
        logEvent("powerup_used", mapOf("type" to powerupType))

    fun logShareClicked(score: Long, stageName: String) =
        logEvent("share_clicked", mapOf("score" to score, "stage" to stageName))
}
