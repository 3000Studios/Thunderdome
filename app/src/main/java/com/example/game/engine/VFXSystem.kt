package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.game.model.*
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class VFXSystem(private val maxParticles: Int = 350) {
    val particles = ArrayList<ParticleEntity>(maxParticles)
    val shockwaves = ArrayList<ShockwaveImpact>(16)
    val floatingTexts = ArrayList<FloatingCombatText>(32)

    fun update(dt: Float) {
        // Update particles
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.rotation += p.rotSpeed * dt
            p.life -= dt

            // Type-specific particle evolution
            when (p.type) {
                ParticleType.FIREBALL -> {
                    p.size += dt * 30f
                    p.alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
                    p.vx *= (1f - dt * 2f)
                    p.vy *= (1f - dt * 2f)
                }
                ParticleType.SMOKE -> {
                    p.size += dt * 45f
                    p.alpha = (p.life / p.maxLife * 0.7f).coerceIn(0f, 1f)
                    p.vx *= (1f - dt * 1.5f)
                    p.vy *= (1f - dt * 1.5f)
                }
                ParticleType.SPARK -> {
                    p.alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
                    p.vx *= (1f - dt * 3.5f)
                    p.vy *= (1f - dt * 3.5f)
                }
                ParticleType.AFTERBURNER -> {
                    p.size *= (1f - dt * 4f)
                    p.alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
                }
                ParticleType.SPEED_STREAK -> {
                    p.alpha = (p.life / p.maxLife * 0.8f).coerceIn(0f, 1f)
                }
                else -> {
                    p.alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
                }
            }

            if (p.life <= 0f) {
                pIter.remove()
            }
        }

        // Update shockwaves
        val sIter = shockwaves.iterator()
        while (sIter.hasNext()) {
            val s = sIter.next()
            s.radius += dt * (s.maxRadius * 4.5f)
            s.alpha = (1f - (s.radius / s.maxRadius)).coerceIn(0f, 1f)
            if (s.radius >= s.maxRadius || s.alpha <= 0.05f) {
                sIter.remove()
            }
        }

        // Update floating combat texts
        val tIter = floatingTexts.iterator()
        while (tIter.hasNext()) {
            val t = tIter.next()
            t.y -= dt * 65f
            t.life -= dt
            t.scale = 1.0f + (0.9f - t.life) * 0.35f
            t.alpha = (t.life / 0.9f).coerceIn(0f, 1f)
            if (t.life <= 0f) {
                tIter.remove()
            }
        }
    }

    // Engine Afterburner Plumes
    fun spawnThrusterParticles(
        leftNozzleX: Float,
        rightNozzleX: Float,
        nozzleY: Float,
        exhaustColor: ExhaustFlame,
        isBoosting: Boolean
    ) {
        val count = if (isBoosting) 4 else 2
        val speedY = if (isBoosting) 420f else 240f
        val size = if (isBoosting) 14f else 8f

        for (i in 0 until count) {
            val xOffset = (Random.nextFloat() - 0.5f) * 6f
            val nozzleX = if (Random.nextBoolean()) leftNozzleX else rightNozzleX

            if (particles.size < maxParticles) {
                particles.add(
                    ParticleEntity(
                        x = nozzleX + xOffset,
                        y = nozzleY,
                        vx = (Random.nextFloat() - 0.5f) * 40f,
                        vy = speedY + Random.nextFloat() * 120f,
                        color = if (Random.nextBoolean()) exhaustColor.coreColor else exhaustColor.outerColor,
                        size = size + Random.nextFloat() * 4f,
                        life = if (isBoosting) 0.28f else 0.16f,
                        maxLife = if (isBoosting) 0.28f else 0.16f,
                        type = ParticleType.AFTERBURNER
                    )
                )
            }
        }
    }

    // Layered Cinematic Explosions
    fun spawnExplosion(x: Float, y: Float, isHeavy: Boolean = false, colorScheme: Color = AeroOrange) {
        val particleMultiplier = if (isHeavy) 2.2f else 1.0f
        val shockRadius = if (isHeavy) 260f else 140f

        // 1. Expanding Shockwave
        shockwaves.add(
            ShockwaveImpact(
                x = x,
                y = y,
                radius = 12f,
                maxRadius = shockRadius,
                color = if (isHeavy) AeroAmber else AeroCyan
            )
        )

        // 2. Initial Blinding Fireball Core
        val fireballCount = (12 * particleMultiplier).toInt()
        for (i in 0 until fireballCount) {
            if (particles.size >= maxParticles) break
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = 80f + Random.nextFloat() * 220f
            particles.add(
                ParticleEntity(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = if (Random.nextBoolean()) Color(0xFFFFF7ED) else colorScheme,
                    size = 18f + Random.nextFloat() * 22f,
                    life = 0.35f + Random.nextFloat() * 0.25f,
                    maxLife = 0.6f,
                    type = ParticleType.FIREBALL
                )
            )
        }

        // 3. Glowing Sparks and Debris
        val sparkCount = (24 * particleMultiplier).toInt()
        for (i in 0 until sparkCount) {
            if (particles.size >= maxParticles) break
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = 180f + Random.nextFloat() * 460f
            particles.add(
                ParticleEntity(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = if (Random.nextBoolean()) Color(0xFFFEF08A) else AeroAmber,
                    size = 4f + Random.nextFloat() * 5f,
                    life = 0.4f + Random.nextFloat() * 0.45f,
                    maxLife = 0.85f,
                    type = ParticleType.SPARK
                )
            )
        }

        // 4. Volumetric Smoke Plumes
        val smokeCount = (10 * particleMultiplier).toInt()
        for (i in 0 until smokeCount) {
            if (particles.size >= maxParticles) break
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = 40f + Random.nextFloat() * 110f
            particles.add(
                ParticleEntity(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = Color(0xFF1E293B),
                    size = 24f + Random.nextFloat() * 28f,
                    life = 0.7f + Random.nextFloat() * 0.5f,
                    maxLife = 1.2f,
                    type = ParticleType.SMOKE
                )
            )
        }
    }

    // Missile Smoke Trail
    fun spawnMissileTrail(x: Float, y: Float) {
        if (particles.size < maxParticles && Random.nextFloat() < 0.65f) {
            particles.add(
                ParticleEntity(
                    x = x + (Random.nextFloat() - 0.5f) * 6f,
                    y = y + 12f,
                    vx = (Random.nextFloat() - 0.5f) * 30f,
                    vy = 60f + Random.nextFloat() * 40f,
                    color = Color(0x9994A3B8),
                    size = 8f + Random.nextFloat() * 6f,
                    life = 0.35f,
                    maxLife = 0.35f,
                    type = ParticleType.SMOKE
                )
            )
        }
    }

    // Speed Streaks when Boosting
    fun spawnSpeedStreaks(screenWidth: Float, screenHeight: Float) {
        for (i in 0 until 3) {
            if (particles.size < maxParticles) {
                particles.add(
                    ParticleEntity(
                        x = Random.nextFloat() * screenWidth,
                        y = -20f,
                        vx = 0f,
                        vy = 1200f + Random.nextFloat() * 400f,
                        color = Color(0x6600F0FF),
                        size = 3f,
                        life = 0.5f,
                        maxLife = 0.5f,
                        type = ParticleType.SPEED_STREAK
                    )
                )
            }
        }
    }

    // Floating text indicator
    fun addText(text: String, x: Float, y: Float, color: Color = Color.White) {
        if (floatingTexts.size < 32) {
            floatingTexts.add(FloatingCombatText(text, x, y, color))
        }
    }

    fun clear() {
        particles.clear()
        shockwaves.clear()
        floatingTexts.clear()
    }
}
