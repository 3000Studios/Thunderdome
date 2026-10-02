package com.example.game.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * High-performance Sound Bite System for Thunder Dome by 3000 Studios.
 * Loads and plays all 65 separated voice lines, tactical alerts, hero lines,
 * villain taunts, and announcer comedy callouts directly from assets/sound_bites.
 */
class SoundBiteSystem(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO)

    enum class SoundBite(val relativePath: String) {
        // 01 Intro
        INTRO_WELCOME("08_ANNOUNCER_COMEDY/002_INTRO_ANNOUNCER__WELCOME_TO_THE_THUNDERDOME.wav"),

        // 02 System Alerts
        SYSTEM_DETECTED("02_SYSTEM_ALERTS/003_SYSTEM_ALERT__DETECTED.wav"),
        SYSTEM_OVERLOAD("02_SYSTEM_ALERTS/004_SYSTEM_ALERT__SYSTEM_OVERLOAD.wav"),
        SYSTEM_CRITICAL_DAMAGE("02_SYSTEM_ALERTS/021_SYSTEM_ALERT__ARMOR_FAILING_CONTROLS_UNSTABLE_CRITICAL_DAMAGE.wav"),
        SYSTEM_RADAR_JAMMED("02_SYSTEM_ALERTS/030_SYSTEM_ALERT__RADAR_JAMMED.wav"),
        SYSTEM_REACTOR_UNSTABLE("02_SYSTEM_ALERTS/031_SYSTEM_ALERT__REACTOR_UNSTABLE.wav"),
        SYSTEM_BRACE_FOR_IMPACT("02_SYSTEM_ALERTS/032_SYSTEM_ALERT__BRACE_FOR_IMPACT.wav"),

        // 03 Villain Taunts
        VILLAIN_BEAT_ME("03_VILLAIN_TAUNTS/007_VILLAIN_TAUNT__YOU_ACTUALLY_THOUGHT_YOU_COULD_BEAT_ME.wav"),
        VILLAIN_FINAL_FLIGHT("03_VILLAIN_TAUNTS/008_VILLAIN_TAUNT__WELCOME_TO_YOUR_FINAL_FLIGHT.wav"),
        VILLAIN_OWN_AIRSPACE("03_VILLAIN_TAUNTS/009_VILLAIN_TAUNT__I_OWN_THIS_AIRSPACE.wav"),
        VILLAIN_TOO_SLOW("03_VILLAIN_TAUNTS/017_VILLAIN_TAUNT__TOO_SLOW.wav"),
        VILLAIN_BEST_SHOT("03_VILLAIN_TAUNTS/018_VILLAIN_TAUNT__THAT_WAS_YOUR_BEST_SHOT.wav"),
        VILLAIN_ENGINES_SCREAMING("03_VILLAIN_TAUNTS/019_VILLAIN_TAUNT__I_CAN_HEAR_YOUR_ENGINES_SCREAMING.wav"),
        VILLAIN_RUNNING_OUT_OF_SKY("03_VILLAIN_TAUNTS/020_VILLAIN_TAUNT__YOURE_RUNNING_OUT_OF_SKY.wav"),
        VILLAIN_GOING_DOWN("03_VILLAIN_TAUNTS/022_VILLAIN_TAUNT__YOURE_GOING_DOWN.wav"),
        VILLAIN_WEAPONS_MEAN_NOTHING("03_VILLAIN_TAUNTS/024_VILLAIN_TAUNT__WEAPONS_MEAN_NOTHING.wav"),
        VILLAIN_CANNOT_ESCAPE("03_VILLAIN_TAUNTS/025_VILLAIN_TAUNT__YOU_CANNOT_ESCAPE.wav"),
        VILLAIN_DOME_BELONGS_TO_ME("03_VILLAIN_TAUNTS/026_VILLAIN_TAUNT__THE_DOME_BELONGS_TO_ME.wav"),
        VILLAIN_LUCK_ENDS_HERE("03_VILLAIN_TAUNTS/033_VILLAIN_TAUNT__YOUR_LUCK_ENDS_HERE.wav"),
        VILLAIN_EVERY_PATH_LEADS_TO_ME("03_VILLAIN_TAUNTS/034_VILLAIN_TAUNT__EVERY_PATH_LEADS_TO_ME.wav"),

        // 04 Hero Lines
        HERO_NOT_TODAY("04_HERO_LINES/005_HERO_TAUNT__NOT_TODAY.wav"),
        HERO_WRONG_PILOT("04_HERO_LINES/006_HERO_TAUNT__YOU_PICKED_THE_WRONG_PILOT.wav"),
        HERO_THATS_HOW_DONE("04_HERO_LINES/014_HERO_TAUNT__THATS_HOW_ITS_DONE.wav"),
        HERO_NOBODY_OWNS_SKIES("04_HERO_LINES/027_HERO_TAUNT__NOBODY_OWNS_THESE_SKIES.wav"),
        HERO_LOCKED_AND_LOADED("04_HERO_LINES/028_HERO_CALLOUT__LOCKED_AND_LOADED.wav"),
        HERO_LETS_LIGHT_THEM_UP("04_HERO_LINES/029_HERO_CALLOUT__LETS_LIGHT_THEM_UP.wav"),
        HERO_HERE_WE_GO("04_HERO_LINES/036_HERO_CALLOUT__HERE_WE_GO.wav"),
        HERO_TAKE_THAT("04_HERO_LINES/037_HERO_TAUNT__TAKE_THAT.wav"),
        HERO_READY_FOR_THAT("04_HERO_LINES/045_HERO_TAUNT__READY_FOR_THAT.wav"),

        // 05 Combat Callouts
        COMBAT_TARGET_LOCKED("05_COMBAT_CALLOUTS/010_COMBAT_CALLOUT__TARGET_LOCKED.wav"),
        COMBAT_FOX_ONE("05_COMBAT_CALLOUTS/011_COMBAT_CALLOUT__FOX_ONE.wav"),
        COMBAT_MISSILE_AWAY("05_COMBAT_CALLOUTS/012_COMBAT_CALLOUT__MISSILE_AWAY.wav"),

        // 06 Kill Confirmations
        KILL_TARGET_DESTROYED("06_KILL_CONFIRMATIONS/015_KILL_CONFIRMATION__TARGET_DESTROYED.wav"),
        KILL_CLEAN_KILL("06_KILL_CONFIRMATIONS/016_KILL_CONFIRMATION__CLEAN_KILL.wav"),
        KILL_FLATLINED("06_KILL_CONFIRMATIONS/044_KILL_CONFIRMATION__FLATLINED.wav"),

        // 07 Powerups
        POWERUP_UNSTOPPABLE("07_POWERUPS/046_POWER_UP_ANNOUNCER__UNSTOPPABLE.wav"),
        POWERUP_MAX_OVERDRIVE("07_POWERUPS/061_POWER_UP_ANNOUNCER__MAXIMUM_OVERDRIVE.wav"),

        // 08 Announcer Comedy & Impact
        ANNOUNCER_BOOM("08_ANNOUNCER_COMEDY/013_IMPACT_ANNOUNCER__BOOM.wav"),
        ANNOUNCER_BOOM_SHAKA_LAKA("08_ANNOUNCER_COMEDY/038_ANNOUNCER_COMEDY__BOOM_SHAKA_LAKA.wav"),
        ANNOUNCER_A_COFFIN("08_ANNOUNCER_COMEDY/039_ANNOUNCER_COMEDY__A_COFFIN.wav"),
        ANNOUNCER_NOT_READY("08_ANNOUNCER_COMEDY/040_ANNOUNCER_COMEDY__I_BET_HE_WASNT_READY_FOR_THAT.wav"),
        ANNOUNCER_CRITICAL_HIT("08_ANNOUNCER_COMEDY/043_COMBAT_ANNOUNCER__CRITICAL_HIT.wav"),
        ANNOUNCER_KNUCKLE_SANDWICH("08_ANNOUNCER_COMEDY/047_ANNOUNCER_COMEDY__DID_SOMEONE_ORDER_A_KNUCKLE_SANDWICH_WITH_EXTRA_CHEESE.wav"),
        ANNOUNCER_ORDER_UP("08_ANNOUNCER_COMEDY/048_ANNOUNCER_COMEDY__ORDER_UP.wav"),
        ANNOUNCER_EMOTIONAL_DAMAGE("08_ANNOUNCER_COMEDY/051_ANNOUNCER_COMEDY__EMOTIONAL_DAMAGE.wav"),
        ANNOUNCER_LEAVE_A_MARK("08_ANNOUNCER_COMEDY/052_ANNOUNCER_COMEDY__THATS_GONNA_LEAVE_A_MARK.wav"),
        ANNOUNCER_CALL_AMBULANCE("08_ANNOUNCER_COMEDY/053_ANNOUNCER_COMEDY__CALL_THE_AMBULANCE_BUT_NOT_FOR_ME.wav"),
        ANNOUNCER_HOLY_CANNOLI("08_ANNOUNCER_COMEDY/056_ANNOUNCER_COMEDY__HOLY_CANNOLI_HE_TOOK_THE_WHOLE_SCREEN_WITH_HIM.wav"),
        ANNOUNCER_ABSOLUTE_PERFECTION("08_ANNOUNCER_COMEDY/057_ANNOUNCER_COMEDY__OH_BABY_ABSOLUTE_PERFECTION.wav"),
        ANNOUNCER_BLINKED("08_ANNOUNCER_COMEDY/058_ANNOUNCER_COMEDY__BLINKED.wav"),
        ANNOUNCER_NEVER_BLINK("08_ANNOUNCER_COMEDY/059_ANNOUNCER_COMEDY__RULE_NUMBER_ONE_NEVER_BLINK.wav"),
        ANNOUNCER_DIRT_OFFENDED("08_ANNOUNCER_COMEDY/062_ANNOUNCER_COMEDY__I_DONT_KNOW_WHAT_YOU_WERE_AIMING_FOR_BUT_THE_DIRT_IS_V.wav"),
        ANNOUNCER_FROM_DOWNTOWN("08_ANNOUNCER_COMEDY/063_ANNOUNCER_COMEDY__FROM_DOWNTOWN_HE_PUT_HIM_ON_A_POSTER.wav"),
        ANNOUNCER_GOOD_NIGHT("08_ANNOUNCER_COMEDY/064_ANNOUNCER_COMEDY__GOOD_NIGHT_SWEET_PRINCE.wav"),

        // 09 Game State
        GAME_STATE_GAME_OVER("09_GAME_STATE/065_GAME_STATE__GAME_OVER.wav"),

        // 10 Misc Taunts
        TAUNT_IS_THAT_ALL("10_MISC_REVIEW/041_TAUNT__IS_THAT_ALL_YOU_GOT.wav"),
        TAUNT_SIT_DOWN_JUNIOR("10_MISC_REVIEW/060_TAUNT__SIT_DOWN_JUNIOR.wav")
    }

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(10)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        .build()

    private val soundIdMap = ConcurrentHashMap<SoundBite, Int>()
    private var lastPlayTimestamp = 0L
    private val minIntervalMs = 800L // Prevent voice line overlapping/spamming

    init {
        scope.launch {
            loadSoundBites()
        }
    }

    private fun loadSoundBites() {
        SoundBite.values().forEach { bite ->
            try {
                val assetPath = "sound_bites/${bite.relativePath}"
                val afd: AssetFileDescriptor = context.assets.openFd(assetPath)
                val id = soundPool.load(afd, 1)
                soundIdMap[bite] = id
            } catch (_: Exception) {}
        }
    }

    fun play(bite: SoundBite, volume: Float = 0.95f, force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && (now - lastPlayTimestamp < minIntervalMs)) return

        val soundId = soundIdMap[bite] ?: return
        val gain = volume.coerceIn(0f, 1f)
        try {
            soundPool.play(soundId, gain, gain, 2, 0, 1.0f)
            lastPlayTimestamp = now
        } catch (_: Exception) {}
    }

    // High-Level Gameplay Audio Hooks
    fun playIntroWelcome() = play(SoundBite.INTRO_WELCOME, force = true)
    
    fun playStageStart() {
        val starters = listOf(SoundBite.HERO_LOCKED_AND_LOADED, SoundBite.HERO_LETS_LIGHT_THEM_UP, SoundBite.HERO_WRONG_PILOT)
        play(starters.random(), force = true)
    }

    fun playMissileLaunch() {
        val calls = listOf(SoundBite.COMBAT_FOX_ONE, SoundBite.COMBAT_MISSILE_AWAY)
        play(calls.random(), volume = 0.85f)
    }

    fun playKillReaction(isHeavy: Boolean = false, comboCount: Int = 0) {
        if (comboCount >= 20) {
            val bigCombos = listOf(
                SoundBite.ANNOUNCER_EMOTIONAL_DAMAGE,
                SoundBite.ANNOUNCER_HOLY_CANNOLI,
                SoundBite.ANNOUNCER_BOOM_SHAKA_LAKA
            )
            play(bigCombos.random(), force = true)
        } else if (comboCount >= 10) {
            val midCombos = listOf(
                SoundBite.POWERUP_UNSTOPPABLE,
                SoundBite.ANNOUNCER_KNUCKLE_SANDWICH,
                SoundBite.ANNOUNCER_FROM_DOWNTOWN
            )
            play(midCombos.random())
        } else if (isHeavy) {
            val heavyKills = listOf(
                SoundBite.KILL_FLATLINED,
                SoundBite.ANNOUNCER_LEAVE_A_MARK,
                SoundBite.HERO_THATS_HOW_DONE,
                SoundBite.TAUNT_SIT_DOWN_JUNIOR
            )
            play(heavyKills.random())
        } else if (Random.nextFloat() < 0.25f) {
            val lightKills = listOf(
                SoundBite.KILL_TARGET_DESTROYED,
                SoundBite.KILL_CLEAN_KILL,
                SoundBite.ANNOUNCER_BOOM,
                SoundBite.ANNOUNCER_BLINKED
            )
            play(lightKills.random())
        }
    }

    fun playPlayerDodge() {
        val dodges = listOf(SoundBite.HERO_NOT_TODAY, SoundBite.HERO_TAKE_THAT)
        play(dodges.random())
    }

    fun playOverdriveBoost() {
        val boosts = listOf(SoundBite.POWERUP_MAX_OVERDRIVE, SoundBite.HERO_HERE_WE_GO)
        play(boosts.random(), force = true)
    }

    fun playBossSpawn(stageIndex: Int) {
        if (stageIndex == 24) {
            play(SoundBite.VILLAIN_DOME_BELONGS_TO_ME, force = true)
        } else {
            val bossIntros = listOf(
                SoundBite.VILLAIN_FINAL_FLIGHT,
                SoundBite.VILLAIN_OWN_AIRSPACE,
                SoundBite.VILLAIN_BEAT_ME,
                SoundBite.VILLAIN_LUCK_ENDS_HERE
            )
            play(bossIntros.random(), force = true)
        }
    }

    fun playBossDefeated() {
        val victoryLines = listOf(
            SoundBite.ANNOUNCER_GOOD_NIGHT,
            SoundBite.ANNOUNCER_HOLY_CANNOLI,
            SoundBite.HERO_NOBODY_OWNS_SKIES
        )
        play(victoryLines.random(), force = true)
    }

    fun playWormholePerfection() {
        play(SoundBite.ANNOUNCER_ABSOLUTE_PERFECTION, force = true)
    }

    fun playCriticalHullAlert() {
        play(SoundBite.SYSTEM_CRITICAL_DAMAGE, force = true)
    }

    fun playGameOver() {
        play(SoundBite.GAME_STATE_GAME_OVER, force = true)
    }

    fun release() {
        try {
            soundPool.release()
        } catch (_: Exception) {}
    }
}
