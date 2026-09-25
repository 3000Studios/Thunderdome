package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.game.audio.AudioHapticSystem
import com.example.game.engine.GameEngine
import com.example.game.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.max

data class LeaderboardEntry(
    val rank: Int,
    val callsign: String,
    val score: Long,
    val aircraft: String,
    val tier: String
)

data class WeeklyChallenge(
    val id: String,
    val title: String,
    val description: String,
    val rewardCredits: Long,
    val rewardCores: Int,
    var progress: Int,
    val maxProgress: Int,
    val isCompleted: Boolean
)

data class BattlePassTier(
    val tier: Int,
    val requiredXp: Int,
    val rewardTitle: String,
    val rewardType: String, // CREDITS, CORES, PAINT, WEAPON
    val rewardAmount: Int,
    val isClaimed: Boolean
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AeroStrikeDatabase.getDatabase(application, viewModelScope)
    private val repository = GameRepository(database.dao())

    val audioHaptics = AudioHapticSystem(application)
    val gameEngine = GameEngine(application, audioHaptics)

    val playerProfile: StateFlow<PlayerProfileEntity> = repository.playerProfile
        .filterNotNull()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            PlayerProfileEntity()
        )

    val allAircraft: StateFlow<List<AircraftSaveEntity>> = repository.allAircraft
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    val settings: StateFlow<SettingsEntity> = repository.settings
        .filterNotNull()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            SettingsEntity()
        )

    // Weekly Simulated Community Leaderboard
    val leaderboards = MutableStateFlow(
        listOf(
            LeaderboardEntry(1, "CYBER_VALKYRIE", 482900L, "Valkyrie Phantom", "APEX GRANDMASTER"),
            LeaderboardEntry(2, "TITAN_CRUSHER", 412500L, "Titan Dread", "DIAMOND I"),
            LeaderboardEntry(3, "GHOST_RUNNER", 389400L, "Solaris Specter", "DIAMOND II"),
            LeaderboardEntry(4, "VANGUARD-01 (YOU)", 214800L, "Apex Falcon", "PLATINUM I"),
            LeaderboardEntry(5, "STORM_STRIKER", 198200L, "Apex Falcon", "PLATINUM II"),
            LeaderboardEntry(6, "HYPERION_ACE", 164000L, "Valkyrie Phantom", "GOLD I"),
            LeaderboardEntry(7, "NEO_PILOT_99", 142100L, "Titan Dread", "GOLD II")
        )
    )

    // Weekly Challenges
    val weeklyChallenges = MutableStateFlow(
        listOf(
            WeeklyChallenge("c1", "Dreadnought Slayer", "Defeat 3 Bosses in Campaign or Boss Rush", 3500L, 15, 1, 3, false),
            WeeklyChallenge("c2", "Kinetic Ace", "Destroy 150 Interceptors using Primary weapons", 2000L, 8, 85, 150, false),
            WeeklyChallenge("c3", "Hyper Boost Master", "Perform 25 Evasive Barrel Rolls in dogfights", 1500L, 5, 18, 25, false),
            WeeklyChallenge("c4", "High-Score Prodigy", "Achieve a score exceeding 100,000 in a single sortie", 5000L, 25, 1, 1, true)
        )
    )

    // Vanguard Seasonal Battle Pass Tiers
    val battlePassTiers = MutableStateFlow(
        (1..15).map { tier ->
            BattlePassTier(
                tier = tier,
                requiredXp = tier * 300,
                rewardTitle = when (tier) {
                    1 -> "1,000 Credits"
                    3 -> "5 Plasma Cores"
                    5 -> "Midnight Stealth Paint"
                    7 -> "Antimatter Violet Exhaust"
                    10 -> "Chrono Laser Prototype"
                    15 -> "Apex Master Golden Decal"
                    else -> "${tier * 500} Credits"
                },
                rewardType = when (tier) {
                    3 -> "CORES"
                    5 -> "PAINT"
                    7 -> "EXHAUST"
                    10 -> "WEAPON"
                    else -> "CREDITS"
                },
                rewardAmount = tier * 500,
                isClaimed = tier <= 2
            )
        }
    )

    init {
        viewModelScope.launch {
            settings.collect { s ->
                audioHaptics.hapticsEnabled = s.hapticsEnabled
                audioHaptics.sfxVolume = s.sfxVolume
                gameEngine.graphicsPreset = s.graphicsPreset
            }
        }
    }

    fun selectAircraft(aircraftId: String) {
        viewModelScope.launch {
            val prof = playerProfile.value.copy(selectedAircraftId = aircraftId)
            repository.updateProfile(prof)
        }
    }

    fun unlockAircraft(spec: AircraftSpec) {
        val prof = playerProfile.value
        if (prof.credits >= spec.unlockCostCredits && prof.plasmaCores >= spec.unlockCostCores) {
            viewModelScope.launch {
                val updatedProf = prof.copy(
                    credits = prof.credits - spec.unlockCostCredits,
                    plasmaCores = prof.plasmaCores - spec.unlockCostCores
                )
                repository.updateProfile(updatedProf)
                val craft = repository.getAircraftById(spec.id) ?: AircraftSaveEntity(aircraftId = spec.id)
                repository.updateAircraft(craft.copy(isUnlocked = true))
                audioHaptics.playSound(AudioHapticSystem.SoundType.POWERUP)
            }
        }
    }

    fun upgradeAircraftAttribute(aircraftId: String, attribute: String) {
        val prof = playerProfile.value
        val craft = allAircraft.value.find { it.aircraftId == aircraftId } ?: return
        val currentLevel = when (attribute) {
            "engine" -> craft.engineUpgradeLevel
            "weapon" -> craft.weaponUpgradeLevel
            "armor" -> craft.armorUpgradeLevel
            "shield" -> craft.shieldUpgradeLevel
            else -> craft.avionicsUpgradeLevel
        }

        if (currentLevel >= 10) return
        val cost = (currentLevel + 1) * 600L
        if (prof.credits >= cost) {
            viewModelScope.launch {
                repository.updateProfile(prof.copy(credits = prof.credits - cost))
                val updatedCraft = when (attribute) {
                    "engine" -> craft.copy(engineUpgradeLevel = currentLevel + 1)
                    "weapon" -> craft.copy(weaponUpgradeLevel = currentLevel + 1)
                    "armor" -> craft.copy(armorUpgradeLevel = currentLevel + 1)
                    "shield" -> craft.copy(shieldUpgradeLevel = currentLevel + 1)
                    else -> craft.copy(avionicsUpgradeLevel = currentLevel + 1)
                }
                repository.updateAircraft(updatedCraft)
                audioHaptics.playSound(AudioHapticSystem.SoundType.POWERUP)
            }
        }
    }

    fun equipCustomization(
        aircraftId: String,
        paintId: String? = null,
        exhaustId: String? = null,
        primaryId: String? = null,
        secondaryId: String? = null,
        specialId: String? = null
    ) {
        val craft = allAircraft.value.find { it.aircraftId == aircraftId } ?: return
        viewModelScope.launch {
            val updated = craft.copy(
                paintSchemeId = paintId ?: craft.paintSchemeId,
                exhaustColorId = exhaustId ?: craft.exhaustColorId,
                primaryWeaponId = primaryId ?: craft.primaryWeaponId,
                secondaryWeaponId = secondaryId ?: craft.secondaryWeaponId,
                specialAbilityId = specialId ?: craft.specialAbilityId
            )
            repository.updateAircraft(updated)
        }
    }

    fun saveMissionFinish() {
        val stats = gameEngine.combatStats
        val prof = playerProfile.value
        viewModelScope.launch {
            val newScore = max(prof.highScore, stats.score)
            val updatedProfile = prof.copy(
                credits = prof.credits + stats.creditsEarned,
                plasmaCores = prof.plasmaCores + stats.plasmaCoresEarned,
                highScore = newScore,
                totalKills = prof.totalKills + stats.kills,
                bossesDefeated = prof.bossesDefeated + stats.bossKills,
                missionsCompleted = prof.missionsCompleted + 1,
                xp = prof.xp + (stats.score / 10),
                level = 1 + ((prof.xp + (stats.score / 10)) / 2500).toInt()
            )
            repository.updateProfile(updatedProfile)
        }
    }

    fun updateSettings(
        preset: String? = null,
        fps: Int? = null,
        scheme: String? = null,
        haptics: Boolean? = null,
        sfx: Float? = null,
        music: Float? = null
    ) {
        val current = settings.value
        viewModelScope.launch {
            val updated = current.copy(
                graphicsPreset = preset ?: current.graphicsPreset,
                targetFps = fps ?: current.targetFps,
                controlScheme = scheme ?: current.controlScheme,
                hapticsEnabled = haptics ?: current.hapticsEnabled,
                sfxVolume = sfx ?: current.sfxVolume,
                musicVolume = music ?: current.musicVolume
            )
            repository.updateSettings(updated)
        }
    }
}
