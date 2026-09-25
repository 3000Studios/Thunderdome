package com.example.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class AudioHapticSystem(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)

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

    // Cached pre-rendered sound buffers for zero-latency instant playback
    private val soundBuffers = ConcurrentHashMap<SoundType, ShortArray>()
    private val sampleRate = 22050

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

    init {
        // Pre-generate procedural sound waveforms on background thread
        scope.launch {
            generateAllBuffers()
        }
    }

    private fun generateAllBuffers() {
        soundBuffers[SoundType.PLASMA_SHOT] = synthesizeChirp(
            durationMs = 90,
            startFreq = 880f,
            endFreq = 220f,
            decayRate = 18f
        )
        soundBuffers[SoundType.GATLING_SHOT] = synthesizeSnappyClick(durationMs = 45, pitch = 400f)
        soundBuffers[SoundType.RAILGUN_SHOT] = synthesizeRailgun(durationMs = 280)
        soundBuffers[SoundType.MISSILE_LAUNCH] = synthesizeWhoosh(durationMs = 200)
        soundBuffers[SoundType.EXPLOSION_LIGHT] = synthesizeNoiseExplosion(durationMs = 250, lowPass = true)
        soundBuffers[SoundType.EXPLOSION_HEAVY] = synthesizeNoiseExplosion(durationMs = 550, lowPass = false)
        soundBuffers[SoundType.SHIELD_HIT] = synthesizeTone(durationMs = 80, freq = 600f)
        soundBuffers[SoundType.SHIELD_BREAK] = synthesizeTone(durationMs = 220, freq = 280f)
        soundBuffers[SoundType.POWERUP] = synthesizeArpeggio()
        soundBuffers[SoundType.WARNING_BEEP] = synthesizeTone(durationMs = 120, freq = 1200f)
        soundBuffers[SoundType.BOOST_BURST] = synthesizeWhoosh(durationMs = 350)
        soundBuffers[SoundType.BOSS_ROAR] = synthesizeBossRoar(durationMs = 700)
    }

    fun playSound(type: SoundType, volumeMultiplier: Float = 1.0f) {
        if (sfxVolume <= 0.01f) return
        val buffer = soundBuffers[type] ?: return
        val gain = (sfxVolume * volumeMultiplier).coerceIn(0f, 1f)

        scope.launch {
            try {
                val scaledBuffer = ShortArray(buffer.size) { i ->
                    (buffer[i] * gain).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(scaledBuffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(scaledBuffer, 0, scaledBuffer.size)
                audioTrack.play()
                // Auto-release after playing
                launch {
                    val waitTime = (scaledBuffer.size * 1000L / sampleRate) + 50L
                    kotlinx.coroutines.delay(waitTime)
                    audioTrack.stop()
                    audioTrack.release()
                }
            } catch (_: Exception) {
                // Fallback safe catch for devices with limited audio track instances
            }
        }
    }

    // Haptic Feedback Implementations
    fun triggerFireHaptic() {
        if (!hapticsEnabled || vibrator == null || !vibrator!!.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(12, 100))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(12)
            }
        } catch (_: Exception) {}
    }

    fun triggerDamageHaptic() {
        if (!hapticsEnabled || vibrator == null || !vibrator!!.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(60, 220))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(60)
            }
        } catch (_: Exception) {}
    }

    fun triggerExplosionHaptic(isHeavy: Boolean = false) {
        if (!hapticsEnabled || vibrator == null || !vibrator!!.hasVibrator()) return
        try {
            val duration = if (isHeavy) 180L else 90L
            val amplitude = if (isHeavy) 255 else 160
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
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 20, 20, 20), intArrayOf(0, 180, 0, 140), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(30)
            }
        } catch (_: Exception) {}
    }

    fun triggerWarningHaptic() {
        if (!hapticsEnabled || vibrator == null || !vibrator!!.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 40), intArrayOf(0, 200, 0, 200), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 40, 60, 40), -1)
            }
        } catch (_: Exception) {}
    }

    // Audio waveform synthesizers
    private fun synthesizeChirp(durationMs: Int, startFreq: Float, endFreq: Float, decayRate: Float): ShortArray {
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val progress = i.toFloat() / totalSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            val env = exp(-progress * decayRate)
            phase += 2.0 * PI * currentFreq / sampleRate
            val sample = (sin(phase) * env * 24000).toInt()
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
            val sample = ((noise * 0.4f + tone * 0.6f) * env * 26000).toInt()
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
            val sample = (hum * env * 28000).toInt()
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
            val sample = (lastNoise * env * 22000).toInt()
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
            val sample = (filterVal * env * 29000).toInt()
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
            val sample = (sin(2.0 * PI * freq * i / sampleRate) * env * 22000).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeArpeggio(): ShortArray {
        val notes = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f)
        val noteDuration = 55
        val totalSamples = (sampleRate * noteDuration * notes.size) / 1000
        val buffer = ShortArray(totalSamples)
        val samplesPerNote = totalSamples / notes.size
        for (n in notes.indices) {
            val freq = notes[n]
            for (i in 0 until samplesPerNote) {
                val overallIdx = n * samplesPerNote + i
                val progress = i.toFloat() / samplesPerNote
                val env = exp(-progress * 5f)
                val sample = (sin(2.0 * PI * freq * i / sampleRate) * env * 24000).toInt()
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
            val sample = (mix * env * 27000).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }
}
