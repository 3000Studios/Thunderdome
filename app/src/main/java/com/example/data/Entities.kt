package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val callsign: String = "VANGUARD-01",
    val level: Int = 1,
    val xp: Long = 0L,
    val credits: Long = 2500L,
    val plasmaCores: Int = 15,
    val selectedAircraftId: String = "apex_falcon",
    val highScore: Long = 0L,
    val totalKills: Int = 0,
    val bossesDefeated: Int = 0,
    val missionsCompleted: Int = 0,
    val vanguardPassTier: Int = 1,
    val vanguardPassXp: Int = 0
)

@Entity(tableName = "aircraft_saves")
data class AircraftSaveEntity(
    @PrimaryKey val aircraftId: String,
    val isUnlocked: Boolean = false,
    val level: Int = 1,
    val paintSchemeId: String = "stealth_black",
    val exhaustColorId: String = "cyan_flame",
    val primaryWeaponId: String = "plasma_gatling",
    val secondaryWeaponId: String = "swarm_missiles",
    val specialAbilityId: String = "chrono_overdrive",
    val engineUpgradeLevel: Int = 0,
    val weaponUpgradeLevel: Int = 0,
    val armorUpgradeLevel: Int = 0,
    val shieldUpgradeLevel: Int = 0,
    val avionicsUpgradeLevel: Int = 0
)

@Entity(tableName = "game_settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val graphicsPreset: String = "ULTRA",
    val targetFps: Int = 60,
    val controlScheme: String = "JOYSTICK",
    val touchSensitivity: Float = 1.0f,
    val hapticsEnabled: Boolean = true,
    val sfxVolume: Float = 0.9f,
    val musicVolume: Float = 0.8f
)
