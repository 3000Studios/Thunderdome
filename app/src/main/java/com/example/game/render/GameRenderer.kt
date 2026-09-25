package com.example.game.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import com.example.game.engine.GameEngine
import com.example.game.model.*
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object GameRenderer {

    fun render(
        drawScope: DrawScope,
        engine: GameEngine,
        width: Float,
        height: Float
    ) {
        val physics = engine.physics
        val vfx = engine.vfx
        val env = engine.environment
        val player = engine.playerState
        val biome = engine.currentBiome

        // Apply dynamic camera shake and lag
        drawScope.withTransform({
            translate(physics.cameraShakeX + physics.cameraLagX, physics.cameraShakeY + physics.cameraLagY)
        }) {
            // 1. Layer 1: Background Gradient & Atmospheric Sky
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(biome.skyColorTop, biome.skyColorBottom)
                ),
                size = Size(width, height)
            )

            // 2. Layer 2: Distant Buildings & Megastructures (Parallax)
            for (b in env.buildings) {
                // Building base silhouette
                drawRect(
                    color = biome.groundColor,
                    topLeft = Offset(b.x, b.y),
                    size = Size(b.width, b.height)
                )
                // Window grid lights
                val winCols = 3
                val winRows = 6
                val wStep = b.width / (winCols + 1)
                val hStep = b.height / (winRows + 1)
                for (r in 1..winRows) {
                    for (c in 1..winCols) {
                        drawRect(
                            color = b.windowGlowColor.copy(alpha = 0.35f),
                            topLeft = Offset(b.x + c * wStep - 2f, b.y + r * hStep - 2f),
                            size = Size(5f, 6f)
                        )
                    }
                }
                // Holographic ad billboard
                if (b.hasHoloAd) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            listOf(b.holoColor.copy(alpha = 0.5f), Color.Transparent)
                        ),
                        topLeft = Offset(b.x + 8f, b.y + 12f),
                        size = Size(b.width - 16f, 22f)
                    )
                }
            }

            // 3. Layer 3: High-Altitude Atmospheric Clouds with Ground Shadows
            for (c in env.clouds) {
                // Cloud shadow
                drawOval(
                    color = Color.Black.copy(alpha = 0.2f),
                    topLeft = Offset(c.x + 30f, c.y + 40f),
                    size = Size(c.width, c.height)
                )
                // Cloud puff
                drawOval(
                    color = Color.White.copy(alpha = c.alpha),
                    topLeft = Offset(c.x, c.y),
                    size = Size(c.width, c.height)
                )
            }

            // 4. Layer 4: Interactive Ground Structures
            for (s in env.structures) {
                val sCol = if (s.isDestroyed) Color(0xFF334155) else Color(0xFF64748B)
                drawRoundRect(
                    color = sCol,
                    topLeft = Offset(s.x - s.width * 0.5f, s.y - s.height * 0.5f),
                    size = Size(s.width, s.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                // Hazard stripe pattern or radar dish
                if (!s.isDestroyed) {
                    drawCircle(
                        color = Color(0xFFFF5500),
                        radius = 6f,
                        center = Offset(s.x, s.y)
                    )
                }
            }

            // 5. Layer 5: Weather Precipitation
            for (w in env.weatherDrops) {
                drawLine(
                    color = Color(0xFF00E5FF).copy(alpha = w.alpha),
                    start = Offset(w.x, w.y),
                    end = Offset(w.x, w.y + w.length),
                    strokeWidth = 1.2f
                )
            }

            // 6. Ground Illumination Rings from Active Shockwaves
            for (sw in vfx.shockwaves) {
                drawCircle(
                    color = sw.color.copy(alpha = sw.alpha * 0.35f),
                    radius = sw.radius,
                    center = Offset(sw.x, sw.y),
                    style = Stroke(width = 4f)
                )
            }

            // 7. Power-up Pickups
            for (pu in engine.weaponSystem.powerUps) {
                val bob = sin(pu.bobTimer * 4f) * 6f
                drawCircle(
                    color = pu.type.color.copy(alpha = 0.35f),
                    radius = 20f,
                    center = Offset(pu.x, pu.y + bob)
                )
                drawCircle(
                    color = pu.type.color,
                    radius = 12f,
                    center = Offset(pu.x, pu.y + bob)
                )
                drawCircle(
                    color = Color.White,
                    radius = 5f,
                    center = Offset(pu.x, pu.y + bob)
                )
            }

            // 8. Normal Enemies
            for (enemy in engine.enemySystem.enemies) {
                val eAlpha = if (enemy.isCloaked) enemy.cloakAlpha else 1.0f
                val flash = enemy.hitFlashTimer > 0f

                drawScope.withTransform({
                    translate(enemy.x, enemy.y)
                    rotate(enemy.angle - 180f)
                }) {
                    drawEnemyCraft(this, enemy, flash, eAlpha)
                }
            }

            // 9. Epic Multi-Phase Boss
            engine.enemySystem.currentBoss?.let { boss ->
                drawBossDreadnought(drawScope, boss)
            }

            // 10. Combat Projectiles & Glowing Trails
            for (p in engine.weaponSystem.projectiles) {
                // Draw trailing path
                if (p.trail.size > 1) {
                    val trailPoints = p.trail.toList()
                    for (i in 0 until trailPoints.size - 1) {
                        val p1 = trailPoints[i]
                        val p2 = trailPoints[i + 1]
                        drawLine(
                            color = p.glowColor.copy(alpha = p1.alpha * 0.7f),
                            start = Offset(p1.x, p1.y),
                            end = Offset(p2.x, p2.y),
                            strokeWidth = p.size * 1.8f * p1.alpha,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Projectile Core
                drawCircle(
                    color = p.glowColor,
                    radius = p.size * 1.6f,
                    center = Offset(p.x, p.y)
                )
                drawCircle(
                    color = p.color,
                    radius = p.size,
                    center = Offset(p.x, p.y)
                )
                drawCircle(
                    color = Color.White,
                    radius = p.size * 0.45f,
                    center = Offset(p.x, p.y)
                )
            }

            // 11. Player Aircraft
            if (!engine.isGameOver) {
                drawScope.withTransform({
                    translate(player.x, player.y)
                    // Visual Banking & Barrel Roll
                    val rollAngle = if (player.barrelRollProgress > 0f) {
                        player.barrelRollProgress * 360f
                    } else {
                        player.bankAngle
                    }
                    rotate(rollAngle)
                    scale(scaleX = cos(player.barrelRollProgress * 2 * PI.toFloat()).coerceAtLeast(0.35f), scaleY = player.pitchScale)
                }) {
                    drawPlayerAircraft(
                        this,
                        engine.currentAircraftSpec,
                        engine.currentPaint,
                        player.isBoosting,
                        player.invulnerableTimer > 0f
                    )
                }

                // Player Shield Shimmer Bubble
                if (player.shield > 0f) {
                    val shieldPercent = player.shield / player.maxShield
                    drawCircle(
                        color = ShieldBlue.copy(alpha = 0.15f * shieldPercent),
                        radius = 42f,
                        center = Offset(player.x, player.y)
                    )
                    drawCircle(
                        color = ShieldBlue.copy(alpha = 0.45f * shieldPercent),
                        radius = 42f,
                        center = Offset(player.x, player.y),
                        style = Stroke(width = 1.8f)
                    )
                }
            }

            // 12. Particles (Explosions, Fireballs, Smoke, Sparks, Streaks)
            for (pt in vfx.particles) {
                when (pt.type) {
                    ParticleType.SPEED_STREAK -> {
                        drawLine(
                            color = pt.color.copy(alpha = pt.alpha),
                            start = Offset(pt.x, pt.y),
                            end = Offset(pt.x, pt.y + 40f),
                            strokeWidth = pt.size
                        )
                    }
                    ParticleType.SMOKE -> {
                        drawCircle(
                            color = pt.color.copy(alpha = pt.alpha),
                            radius = pt.size,
                            center = Offset(pt.x, pt.y)
                        )
                    }
                    ParticleType.FIREBALL, ParticleType.AFTERBURNER -> {
                        drawCircle(
                            color = pt.color.copy(alpha = pt.alpha),
                            radius = pt.size,
                            center = Offset(pt.x, pt.y)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = pt.alpha * 0.7f),
                            radius = pt.size * 0.4f,
                            center = Offset(pt.x, pt.y)
                        )
                    }
                    else -> {
                        drawCircle(
                            color = pt.color.copy(alpha = pt.alpha),
                            radius = pt.size,
                            center = Offset(pt.x, pt.y)
                        )
                    }
                }
            }
        }
    }

    private fun drawPlayerAircraft(
        scope: DrawScope,
        spec: AircraftSpec,
        paint: PaintScheme,
        isBoosting: Boolean,
        isInvulnerable: Boolean
    ) {
        val bodyColor = if (isInvulnerable) Color.White else paint.bodyColor
        val trimColor = paint.trimColor

        // Main Fuselage Path
        val path = Path().apply {
            moveTo(0f, -34f) // Nose
            lineTo(8f, -12f)
            lineTo(18f, 2f)
            lineTo(38f, 18f) // Right wingtip
            lineTo(34f, 25f)
            lineTo(14f, 20f)
            lineTo(10f, 30f) // Right engine
            lineTo(0f, 26f)
            lineTo(-10f, 30f) // Left engine
            lineTo(-14f, 20f)
            lineTo(-34f, 25f)
            lineTo(-38f, 18f) // Left wingtip
            lineTo(-18f, 2f)
            lineTo(-8f, -12f)
            close()
        }

        scope.drawPath(path, color = bodyColor)
        scope.drawPath(path, color = trimColor, style = Stroke(width = 2.2f))

        // Canopy (Glass reflection glow)
        val canopyPath = Path().apply {
            moveTo(0f, -18f)
            lineTo(5f, -2f)
            lineTo(0f, 8f)
            lineTo(-5f, -2f)
            close()
        }
        scope.drawPath(
            canopyPath,
            brush = Brush.verticalGradient(
                listOf(AeroAmber, AeroOrange)
            )
        )

        // Wingtip energy lights
        scope.drawCircle(AeroCyan, radius = 2.5f, center = Offset(-36f, 18f))
        scope.drawCircle(AeroCyan, radius = 2.5f, center = Offset(36f, 18f))
    }

    private fun drawEnemyCraft(
        scope: DrawScope,
        enemy: EnemyEntity,
        flash: Boolean,
        alpha: Float
    ) {
        val baseColor = if (flash) Color.White else when (enemy.type) {
            EnemyType.SCOUT_DRONE -> Color(0xFFEF4444)
            EnemyType.FAST_INTERCEPTOR -> Color(0xFFFF5500)
            EnemyType.HEAVY_GUNSHIP -> Color(0xFF8B5CF6)
            EnemyType.STEALTH_RAIDER -> Color(0xFF334155)
            EnemyType.MISSILE_CORVETTE -> Color(0xFFDC2626)
        }.copy(alpha = alpha)

        val path = Path().apply {
            val r = enemy.type.radius
            moveTo(0f, -r * 1.2f)
            lineTo(r * 0.9f, r)
            lineTo(0f, r * 0.5f)
            lineTo(-r * 0.9f, r)
            close()
        }

        scope.drawPath(path, color = baseColor)
        scope.drawPath(path, color = Color(0xFFFFD700).copy(alpha = alpha), style = Stroke(width = 1.5f))

        // Red cockpit eye
        scope.drawCircle(
            color = Color(0xFFFF0055).copy(alpha = alpha),
            radius = enemy.type.radius * 0.28f,
            center = Offset(0f, 0f)
        )
    }

    private fun drawBossDreadnought(scope: DrawScope, boss: BossEntity) {
        val halfW = boss.width * 0.5f
        val halfH = boss.height * 0.5f
        val flash = boss.hitFlashTimer > 0f
        val hullColor = if (flash) Color.White else Color(0xFF1E293B)
        val armorTrim = if (flash) Color.White else Color(0xFFEF4444)

        // Main Heavy Chassis
        val bossPath = Path().apply {
            moveTo(boss.x, boss.y + halfH * 0.7f) // Front bow
            lineTo(boss.x + halfW * 0.45f, boss.y + halfH * 0.3f)
            lineTo(boss.x + halfW, boss.y - halfH * 0.2f) // Starboard wing
            lineTo(boss.x + halfW * 0.85f, boss.y - halfH)
            lineTo(boss.x - halfW * 0.85f, boss.y - halfH)
            lineTo(boss.x - halfW, boss.y - halfH * 0.2f) // Port wing
            lineTo(boss.x - halfW * 0.45f, boss.y + halfH * 0.3f)
            close()
        }

        scope.drawPath(bossPath, color = hullColor)
        scope.drawPath(bossPath, color = armorTrim, style = Stroke(width = 3.5f))

        // Left Turret Component
        val leftTurret = boss.components.find { it.id == "left_turret" }
        if (leftTurret != null) {
            val lx = boss.x + leftTurret.offsetX
            val ly = boss.y + leftTurret.offsetY
            val tCol = if (leftTurret.isDestroyed) Color(0xFF475569) else Color(0xFFFF7A00)
            scope.drawCircle(tCol, radius = 18f, center = Offset(lx, ly))
            scope.drawLine(
                color = Color.Black,
                start = Offset(lx, ly),
                end = Offset(lx, ly + 25f),
                strokeWidth = 6f
            )
        }

        // Right Missile Component
        val rightTurret = boss.components.find { it.id == "right_missile" }
        if (rightTurret != null) {
            val rx = boss.x + rightTurret.offsetX
            val ry = boss.y + rightTurret.offsetY
            val mCol = if (rightTurret.isDestroyed) Color(0xFF475569) else Color(0xFFFF1744)
            scope.drawRect(
                color = mCol,
                topLeft = Offset(rx - 15f, ry - 15f),
                size = Size(30f, 30f)
            )
        }

        // Core Reactor (Pulsing Energy Core)
        val coreColor = if (boss.phase == BossPhase.PHASE_3_RAGE_OVERDRIVE) Color(0xFFFF0055) else AeroAmber
        scope.drawCircle(
            color = coreColor.copy(alpha = 0.4f),
            radius = 35f,
            center = Offset(boss.x, boss.y)
        )
        scope.drawCircle(
            color = coreColor,
            radius = 20f,
            center = Offset(boss.x, boss.y)
        )
        scope.drawCircle(
            color = Color.White,
            radius = 8f,
            center = Offset(boss.x, boss.y)
        )

        // Sweeping Laser Beam in Rage Phase
        if (boss.isFiringLaser) {
            val rad = boss.laserBeamAngle * PI.toFloat() / 180f
            val endX = boss.x + cos(rad) * 1200f
            val endY = boss.y + sin(rad) * 1200f
            scope.drawLine(
                color = Color(0x66FF0055),
                start = Offset(boss.x, boss.y + 20f),
                end = Offset(endX, endY),
                strokeWidth = 28f
            )
            scope.drawLine(
                color = Color(0xFFFF0055),
                start = Offset(boss.x, boss.y + 20f),
                end = Offset(endX, endY),
                strokeWidth = 14f
            )
            scope.drawLine(
                color = Color.White,
                start = Offset(boss.x, boss.y + 20f),
                end = Offset(endX, endY),
                strokeWidth = 4f
            )
        }
    }
}
