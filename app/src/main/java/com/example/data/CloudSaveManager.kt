package com.example.data

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

data class CloudSaveSnapshot(
    val version: Int,
    val timestamp: Long,
    val profile: PlayerProfileEntity,
    val aircraft: List<AircraftSaveEntity>
)

object CloudSaveManager {
    private const val TAG = "CloudSaveManager"

    fun exportSaveToJson(profile: PlayerProfileEntity, aircraftList: List<AircraftSaveEntity>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())

        val pObj = JSONObject().apply {
            put("id", profile.id)
            put("callsign", profile.callsign)
            put("level", profile.level)
            put("xp", profile.xp)
            put("credits", profile.credits)
            put("plasmaCores", profile.plasmaCores)
            put("selectedAircraftId", profile.selectedAircraftId)
            put("highScore", profile.highScore)
            put("totalKills", profile.totalKills)
            put("bossesDefeated", profile.bossesDefeated)
            put("missionsCompleted", profile.missionsCompleted)
            put("vanguardPassTier", profile.vanguardPassTier)
            put("vanguardPassXp", profile.vanguardPassXp)
            put("claimedPassTiers", profile.claimedPassTiers)
            put("isAdsRemoved", profile.isAdsRemoved)
            put("hasFounderPack", profile.hasFounderPack)
            put("hasStarterPack", profile.hasStarterPack)
            put("purchasedProductIds", profile.purchasedProductIds)
        }
        root.put("profile", pObj)

        val aArray = JSONArray()
        for (a in aircraftList) {
            val aObj = JSONObject().apply {
                put("aircraftId", a.aircraftId)
                put("isUnlocked", a.isUnlocked)
                put("level", a.level)
                put("overclockLevel", a.overclockLevel)
                put("paintSchemeId", a.paintSchemeId)
                put("exhaustColorId", a.exhaustColorId)
                put("primaryWeaponId", a.primaryWeaponId)
                put("secondaryWeaponId", a.secondaryWeaponId)
                put("specialAbilityId", a.specialAbilityId)
                put("engineUpgradeLevel", a.engineUpgradeLevel)
                put("weaponUpgradeLevel", a.weaponUpgradeLevel)
                put("armorUpgradeLevel", a.armorUpgradeLevel)
                put("shieldUpgradeLevel", a.shieldUpgradeLevel)
                put("avionicsUpgradeLevel", a.avionicsUpgradeLevel)
            }
            aArray.put(aObj)
        }
        root.put("aircraft", aArray)

        return root.toString(2)
    }

    fun parseSaveFromJson(jsonStr: String): CloudSaveSnapshot? {
        return try {
            val root = JSONObject(jsonStr)
            val version = root.optInt("version", 1)
            val timestamp = root.optLong("timestamp", 0L)

            val pObj = root.getJSONObject("profile")
            val profile = PlayerProfileEntity(
                id = pObj.optInt("id", 1),
                callsign = pObj.optString("callsign", "VANGUARD-01"),
                level = pObj.optInt("level", 1),
                xp = pObj.optLong("xp", 0L),
                credits = pObj.optLong("credits", 2500L),
                plasmaCores = pObj.optInt("plasmaCores", 15),
                selectedAircraftId = pObj.optString("selectedAircraftId", "apex_falcon"),
                highScore = pObj.optLong("highScore", 0L),
                totalKills = pObj.optInt("totalKills", 0),
                bossesDefeated = pObj.optInt("bossesDefeated", 0),
                missionsCompleted = pObj.optInt("missionsCompleted", 0),
                vanguardPassTier = pObj.optInt("vanguardPassTier", 1),
                vanguardPassXp = pObj.optInt("vanguardPassXp", 0),
                claimedPassTiers = pObj.optString("claimedPassTiers", "1,2"),
                isAdsRemoved = pObj.optBoolean("isAdsRemoved", false),
                hasFounderPack = pObj.optBoolean("hasFounderPack", false),
                hasStarterPack = pObj.optBoolean("hasStarterPack", false),
                purchasedProductIds = pObj.optString("purchasedProductIds", ""),
                lastSyncTimestamp = timestamp
            )

            val aircraftList = mutableListOf<AircraftSaveEntity>()
            val aArray = root.optJSONArray("aircraft")
            if (aArray != null) {
                for (i in 0 until aArray.length()) {
                    val aObj = aArray.getJSONObject(i)
                    aircraftList.add(
                        AircraftSaveEntity(
                            aircraftId = aObj.getString("aircraftId"),
                            isUnlocked = aObj.optBoolean("isUnlocked", false),
                            level = aObj.optInt("level", 1),
                            overclockLevel = aObj.optInt("overclockLevel", 0),
                            paintSchemeId = aObj.optString("paintSchemeId", "stealth_black"),
                            exhaustColorId = aObj.optString("exhaustColorId", "cyan_flame"),
                            primaryWeaponId = aObj.optString("primaryWeaponId", "plasma_gatling"),
                            secondaryWeaponId = aObj.optString("secondaryWeaponId", "swarm_missiles"),
                            specialAbilityId = aObj.optString("specialAbilityId", "chrono_overdrive"),
                            engineUpgradeLevel = aObj.optInt("engineUpgradeLevel", 0),
                            weaponUpgradeLevel = aObj.optInt("weaponUpgradeLevel", 0),
                            armorUpgradeLevel = aObj.optInt("armorUpgradeLevel", 0),
                            shieldUpgradeLevel = aObj.optInt("shieldUpgradeLevel", 0),
                            avionicsUpgradeLevel = aObj.optInt("avionicsUpgradeLevel", 0)
                        )
                    )
                }
            }

            CloudSaveSnapshot(version, timestamp, profile, aircraftList)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse save JSON: ${e.message}")
            null
        }
    }
}
