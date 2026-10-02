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

    val soundBites: SoundBiteSystem by lazy { SoundBiteSystem(context) }

    fun playSoundBite(bite: SoundBiteSystem.SoundBite, volume: Float = 0.95f, force: Boolean = false) {
        if (sfxVolume > 0.01f) {
            soundBites.play(bite, volume * sfxVolume, force)
        }
    }

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
        BOSS_ROAR,
        BUTTON_CLICK,
        PURCHASE_SUCCESS,
        PURCHASE_FAIL,
        DEV_MODE_UNLOCKED,
        WARP_ENGAGE,
        SECRET_TUNNEL_ENTER,
        ENEMY_ARMOR_CRACK,
        BOSS_LAUGH,
        VOICE_DIE_IN_A_FIRE,
        VOICE_GOT_EM
    }

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(16)
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
            SoundType.BOSS_ROAR to synthesizeBossRoar(500),
            SoundType.BUTTON_CLICK to synthesizeKeyClick(35),
            SoundType.PURCHASE_SUCCESS to synthesizeChaChing(450),
            SoundType.PURCHASE_FAIL to synthesizeErrorBuzz(250),
            SoundType.DEV_MODE_UNLOCKED to synthesizeDevChime(500),
            SoundType.WARP_ENGAGE to synthesizeWarpScream(600),
            SoundType.SECRET_TUNNEL_ENTER to synthesizeSecretChime(550),
            SoundType.ENEMY_ARMOR_CRACK to synthesizeArmorCrack(85),
            SoundType.BOSS_LAUGH to synthesizeBossLaugh(650),
            SoundType.VOICE_DIE_IN_A_FIRE to synthesizeVoiceStinger(450, true),
            SoundType.VOICE_GOT_EM to synthesizeVoiceStinger(350, false)
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

    private fun synthesizeKeyClick(durationMs: Int): ShortArray {
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val progress = i.toFloat() / totalSamples
            val noise = (Random.nextFloat() * 2f - 1f) * 0.7f
            val tone = sin(2.0 * PI * 1800.0 * i / sampleRate).toFloat() * 0.3f
            val env = exp(-progress * 35f)
            val sample = ((noise + tone) * env * 26000).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeChaChing(durationMs: Int): ShortArray {
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)
        val coin1Samples = (sampleRate * 0.12f).toInt()
        val coin2Start = (sampleRate * 0.08f).toInt()
        
        for (i in 0 until totalSamples) {
            var sum = 0.0
            // Coin Hit 1 (987Hz + 1975Hz)
            if (i < coin1Samples) {
                val p = i.toFloat() / coin1Samples
                val env = exp(-p * 8f)
                sum += (sin(2.0 * PI * 987.77 * i / sampleRate) * 0.5 + sin(2.0 * PI * 1975.5 * i / sampleRate) * 0.5) * env
            }
            // Cash Register Bell Hit 2 (1318Hz + 2637Hz + 3951Hz)
            if (i >= coin2Start) {
                val p = (i - coin2Start).toFloat() / (totalSamples - coin2Start)
                val env = exp(-p * 4.5f)
                sum += (sin(2.0 * PI * 1318.5 * i / sampleRate) * 0.4 + 
                        sin(2.0 * PI * 2637.0 * i / sampleRate) * 0.35 + 
                        sin(2.0 * PI * 3951.0 * i / sampleRate) * 0.25) * env
            }
            val sample = (sum * 28000).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeErrorBuzz(durationMs: Int): ShortArray {
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val progress = i.toFloat() / totalSamples
            val square = if ((i * 150 / sampleRate) % 2 == 0) 1f else -1f
            val env = exp(-progress * 6f)
            val sample = (square * env * 20000).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeDevChime(durationMs: Int): ShortArray {
        val notes = floatArrayOf(523.25f, 659.25f, 783.99f, 987.77f, 1318.5f, 1567.98f, 2093.0f)
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)
        val step = totalSamples / notes.size
        for (n in notes.indices) {
            val freq = notes[n]
            for (i in 0 until (totalSamples - n * step)) {
                val idx = n * step + i
                if (idx < totalSamples) {
                    val p = i.toFloat() / (step * 2)
                    val env = exp(-p * 4f)
                    val sample = (sin(2.0 * PI * freq * i / sampleRate) * env * 8000).toInt()
                    val cur = buffer[idx].toInt()
                    buffer[idx] = (cur + sample).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
        }
        return buffer
    }

    private fun synthesizeWarpScream(durationMs: Int): ShortArray {
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val p = i.toFloat() / totalSamples
            // Rising hyper-drive turbine from 300Hz to 2800Hz with sub-bass drop
            val freq = 300.0 + 2500.0 * (p * p)
            phase += 2.0 * PI * freq / sampleRate
            val subBass = sin(2.0 * PI * (80.0 * (1f - p * 0.5f)) * i / sampleRate) * 0.5
            val scream = sin(phase) * 0.5
            val env = sin(p * PI.toFloat())
            val sample = ((scream + subBass) * env * 27000).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeSecretChime(durationMs: Int): ShortArray {
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val p = i.toFloat() / totalSamples
            val f1 = sin(2.0 * PI * 880.0 * i / sampleRate)
            val f2 = sin(2.0 * PI * 1320.0 * i / sampleRate + sin(2.0 * PI * 6.0 * i / sampleRate) * 2.0)
            val env = exp(-p * 3.5f)
            val sample = ((f1 * 0.5 + f2 * 0.5) * env * 24000).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeArmorCrack(durationMs: Int): ShortArray {
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val p = i.toFloat() / totalSamples
            val noise = (Random.nextFloat() * 2f - 1f) * 0.8f
            val snap = sin(2.0 * PI * 2400.0 * i / sampleRate).toFloat() * 0.4f
            val env = exp(-p * 28f)
            val sample = ((noise + snap) * env * 26000).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeBossLaugh(durationMs: Int): ShortArray {
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)
        val pulseCount = 4
        val samplesPerPulse = totalSamples / pulseCount
        for (k in 0 until pulseCount) {
            val pulseFreq = 160.0 - k * 18.0
            for (i in 0 until samplesPerPulse) {
                val idx = k * samplesPerPulse + i
                val p = i.toFloat() / samplesPerPulse
                val env = sin(p * PI.toFloat()) * exp(-p * 2f)
                val tone = sin(2.0 * PI * pulseFreq * i / sampleRate) * 0.7 + (Random.nextFloat() * 2f - 1f) * 0.3
                val sample = (tone * env * 24000).toInt()
                buffer[idx] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }
        return buffer
    }

    private fun synthesizeVoiceStinger(durationMs: Int, isDieInFire: Boolean): ShortArray {
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)
        val baseFreq = if (isDieInFire) 340.0 else 440.0
        for (i in 0 until totalSamples) {
            val p = i.toFloat() / totalSamples
            val formant1 = sin(2.0 * PI * baseFreq * i / sampleRate) * 0.5
            val formant2 = sin(2.0 * PI * (baseFreq * 2.4) * i / sampleRate) * 0.3
            val grit = (Random.nextFloat() * 2f - 1f) * 0.2f
            val env = sin(p * PI.toFloat())
            val sample = ((formant1 + formant2 + grit) * env * 25000).toInt()
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
