package com.example.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class AudioHapticSystem(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    var hapticsEnabled: Boolean = true
    var sfxVolume: Float = 0.85f

    enum class SoundType {
        PLASMA_SHOT,
        GATLING_SHOT,
        RAILGUN_SHOT,
        MISSILE_LAUNCH,
        EXPLOSION_LIGHT,
        EXPLOSION_HEAVY,
        SHIELD_HIT,
        SHIELD_BREAK,
        POWERUP,
        WARNING_BEEP,
        BOOST_BURST,
        BOSS_ROAR
    }

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(12)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundIdMap = ConcurrentHashMap<SoundType, Int>()
    private val sampleRate = 22050

    init {
        scope.launch {
            try {
                loadAllSounds()
            } catch (_: Exception) {}
        }
    }

    private fun loadAllSounds() {
        val soundDefs = mapOf(
            SoundType.PLASMA_SHOT to synthesizeChirp(80, 880f, 220f, 18f),
            SoundType.GATLING_SHOT to synthesizeSnappyClick(40, 420f),
            SoundType.RAILGUN_SHOT to synthesizeRailgun(220),
            SoundType.MISSILE_LAUNCH to synthesizeWhoosh(180),
            SoundType.EXPLOSION_LIGHT to synthesizeNoiseExplosion(220, true),
            SoundType.EXPLOSION_HEAVY to synthesizeNoiseExplosion(420, false),
            SoundType.SHIELD_HIT to synthesizeTone(70, 600f),
            SoundType.SHIELD_BREAK to synthesizeTone(180, 280f),
            SoundType.POWERUP to synthesizeArpeggio(),
            SoundType.WARNING_BEEP to synthesizeTone(100, 1200f),
            SoundType.BOOST_BURST to synthesizeWhoosh(250),
            SoundType.BOSS_ROAR to synthesizeBossRoar(500)
        )

        for ((type, pcm) in soundDefs) {
            try {
                val wavFile = File(context.cacheDir, "aerostrike_sfx_${type.name.lowercase()}.wav")
                writeWav(wavFile, pcm, sampleRate)
                val soundId = soundPool.load(wavFile.absolutePath, 1)
                soundIdMap[type] = soundId
            } catch (_: Exception) {}
        }
    }

    fun playSound(type: SoundType, volumeMultiplier: Float = 1.0f) {
        if (sfxVolume <= 0.01f) return
        val soundId = soundIdMap[type] ?: return
        val gain = (sfxVolume * volumeMultiplier).coerceIn(0f, 1f)
        try {
            soundPool.play(soundId, gain, gain, 1, 0, 1.0f)
        } catch (_: Exception) {}
    }

    // Haptic Feedback Implementations
    fun triggerFireHaptic() {
        if (!hapticsEnabled || vibrator == null || !vibrator!!.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(10, 80))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(10)
            }
        } catch (_: Exception) {}
    }

    fun triggerDamageHaptic() {
        if (!hapticsEnabled || vibrator == null || !vibrator!!.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(50, 180))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        } catch (_: Exception) {}
    }

    fun triggerExplosionHaptic(isHeavy: Boolean = false) {
        if (!hapticsEnabled || vibrator == null || !vibrator!!.hasVibrator()) return
        try {
            val duration = if (isHeavy) 120L else 60L
            val amplitude = if (isHeavy) 220 else 120
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(duration, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(duration)
            }
        } catch (_: Exception) {}
    }

    fun triggerBoostHaptic() {
        if (!hapticsEnabled || vibrator == null || !vibrator!!.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, 120))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (_: Exception) {}
    }

    // Synthesizers
    private fun synthesizeChirp(durationMs: Int, startFreq: Float, endFreq: Float, decayRate: Float): ShortArray {
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val progress = i.toFloat() / totalSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            val env = exp(-progress * decayRate)
            phase += 2.0 * PI * currentFreq / sampleRate
            val sample = (sin(phase) * env * 22000).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeSnappyClick(durationMs: Int, pitch: Float): ShortArray {
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val progress = i.toFloat() / totalSamples
            val noise = Random.nextFloat() * 2f - 1f
            val tone = sin(2.0 * PI * pitch * i / sampleRate).toFloat()
            val env = exp(-progress * 25f)
            val sample = ((noise * 0.4f + tone * 0.6f) * env * 24000).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeRailgun(durationMs: Int): ShortArray {
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val progress = i.toFloat() / totalSamples
            val freq = 1200f * (1f - progress * 0.8f)
            phase += 2.0 * PI * freq / sampleRate
            val env = exp(-progress * 9f)
            val hum = sin(phase) * 0.7f + (Random.nextFloat() * 2f - 1f) * 0.3f
            val sample = (hum * env * 26000).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeWhoosh(durationMs: Int): ShortArray {
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)
        var lastNoise = 0f
        for (i in 0 until totalSamples) {
            val progress = i.toFloat() / totalSamples
            val env = sin(progress * PI.toFloat())
            val raw = Random.nextFloat() * 2f - 1f
            lastNoise = (lastNoise * 0.8f) + (raw * 0.2f)
            val sample = (lastNoise * env * 20000).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeNoiseExplosion(durationMs: Int, lowPass: Boolean): ShortArray {
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)
        var filterVal = 0f
        val alpha = if (lowPass) 0.15f else 0.35f
        for (i in 0 until totalSamples) {
            val progress = i.toFloat() / totalSamples
            val raw = Random.nextFloat() * 2f - 1f
            filterVal = (filterVal * (1f - alpha)) + (raw * alpha)
            val env = exp(-progress * 6f)
            val sample = (filterVal * env * 27000).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeTone(durationMs: Int, freq: Float): ShortArray {
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val progress = i.toFloat() / totalSamples
            val env = exp(-progress * 12f)
            val sample = (sin(2.0 * PI * freq * i / sampleRate) * env * 20000).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeArpeggio(): ShortArray {
        val notes = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f)
        val noteDuration = 50
        val totalSamples = (sampleRate * noteDuration * notes.size) / 1000
        val buffer = ShortArray(totalSamples)
        val samplesPerNote = totalSamples / notes.size
        for (n in notes.indices) {
            val freq = notes[n]
            for (i in 0 until samplesPerNote) {
                val overallIdx = n * samplesPerNote + i
                val progress = i.toFloat() / samplesPerNote
                val env = exp(-progress * 5f)
                val sample = (sin(2.0 * PI * freq * i / sampleRate) * env * 22000).toInt()
                buffer[overallIdx] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }
        return buffer
    }

    private fun synthesizeBossRoar(durationMs: Int): ShortArray {
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)
        var phase1 = 0.0
        var phase2 = 0.0
        var noise = 0f
        for (i in 0 until totalSamples) {
            val progress = i.toFloat() / totalSamples
            phase1 += 2.0 * PI * 85.0 / sampleRate
            phase2 += 2.0 * PI * 130.0 / sampleRate
            noise = (noise * 0.9f) + ((Random.nextFloat() * 2f - 1f) * 0.1f)
            val env = sin(progress * PI.toFloat())
            val mix = sin(phase1) * 0.5f + sin(phase2) * 0.3f + noise * 0.4f
            val sample = (mix * env * 25000).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun writeWav(file: File, pcm: ShortArray, sampleRate: Int) {
        val byteData = ByteArray(pcm.size * 2)
        for (i in pcm.indices) {
            val s = pcm[i].toInt()
            byteData[i * 2] = (s and 0xFF).toByte()
            byteData[i * 2 + 1] = ((s shr 8) and 0xFF).toByte()
        }
        val totalDataLen = byteData.size + 36
        val byteRate = sampleRate * 2

        FileOutputStream(file).use { out ->
            out.write("RIFF".toByteArray())
            out.write(intToBytes(totalDataLen))
            out.write("WAVEfmt ".toByteArray())
            out.write(intToBytes(16))
            out.write(shortToBytes(1)) // PCM
            out.write(shortToBytes(1)) // mono
            out.write(intToBytes(sampleRate))
            out.write(intToBytes(byteRate))
            out.write(shortToBytes(2)) // block align
            out.write(shortToBytes(16)) // 16-bit
            out.write("data".toByteArray())
            out.write(intToBytes(byteData.size))
            out.write(byteData)
        }
    }

    private fun intToBytes(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }

    private fun shortToBytes(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte()
        )
    }
}
