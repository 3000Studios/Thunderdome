package com.example.game.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import com.example.game.engine.GameEngine
import com.example.game.model.*
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object GameRenderer {
    private val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        textAlign = android.graphics.Paint.Align.CENTER
    }

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

            // 5. Layer 5: Dynamic Weather Precipitation (Slanted Rain, Blizzard, Green Acid Mist)
            val dropColor = when {
                biome.id in listOf("toxic_sector", "bio_labs", "alien_jungle") -> Color(0xFF4ADE80)
                biome.id in listOf("lava_planet", "machine_world") -> Color(0xFFFF6B00)
                biome.id in listOf("void_gate", "dimension_rift") -> Color(0xFFC084FC)
                biome.id in listOf("ice_fortress") -> Color(0xFFE0F2FE)
                else -> Color(0xFF00E5FF)
            }
            for (w in env.weatherDrops) {
                val slantX = if (w.vx != 0f) w.vx * 0.04f else 0f
                drawLine(
                    color = dropColor.copy(alpha = w.alpha),
                    start = Offset(w.x, w.y),
                    end = Offset(w.x + slantX, w.y + w.length),
                    strokeWidth = 1.4f
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

            // 4b. Speed Gates (3D Glowing Acceleration Rings)
            for (sg in env.speedGates) {
                if (!sg.isTriggered) {
                    val pulse = 0.8f + 0.2f * sin((env.scrollOffset * 0.05f + sg.id).toDouble()).toFloat()
                    drawOval(
                        color = AeroCyan.copy(alpha = 0.35f * pulse),
                        topLeft = Offset(sg.x - sg.width * 0.5f, sg.y - sg.height * 0.5f),
                        size = Size(sg.width, sg.height)
                    )
                    drawOval(
                        color = AeroCyan,
                        topLeft = Offset(sg.x - sg.width * 0.5f, sg.y - sg.height * 0.5f),
                        size = Size(sg.width, sg.height),
                        style = Stroke(width = 3.5f)
                    )
                    // Inner energy pulse
                    drawOval(
                        color = AeroAmber,
                        topLeft = Offset(sg.x - sg.width * 0.3f, sg.y - sg.height * 0.3f),
                        size = Size(sg.width * 0.6f, sg.height * 0.6f),
                        style = Stroke(width = 1.8f)
                    )
                }
            }

            // 4c. Course Obstacles (3D Laser Barricades, Asteroid Pillars, Green Goo, Wind Gusts)
            for (co in env.courseObstacles) {
                if (!co.isDestroyed) {
                    when (co.type) {
                        "LASER_BARRIER" -> {
                            // Left & Right Emitter Posts
                            drawRect(Color(0xFF64748B), Offset(co.x - co.width * 0.5f, co.y - 15f), Size(18f, 30f))
                            drawRect(Color(0xFF64748B), Offset(co.x + co.width * 0.5f - 18f, co.y - 15f), Size(18f, 30f))
                            // Pulsating Laser Beam
                            drawLine(
                                color = DangerRed.copy(alpha = 0.85f),
                                start = Offset(co.x - co.width * 0.5f + 18f, co.y),
                                end = Offset(co.x + co.width * 0.5f - 18f, co.y),
                                strokeWidth = 6f
                            )
                            drawLine(
                                color = Color.White,
                                start = Offset(co.x - co.width * 0.5f + 18f, co.y),
                                end = Offset(co.x + co.width * 0.5f - 18f, co.y),
                                strokeWidth = 2f
                            )
                        }
                        "GREEN_GOO" -> {
                            // Viscous slime puddle with bubbling core
                            drawOval(
                                color = Color(0x7722C55E),
                                topLeft = Offset(co.x - co.width * 0.5f, co.y - co.height * 0.5f),
                                size = Size(co.width, co.height)
                            )
                            drawOval(
                                color = Color(0xFF15803D),
                                topLeft = Offset(co.x - co.width * 0.38f, co.y - co.height * 0.38f),
                                size = Size(co.width * 0.76f, co.height * 0.76f)
                            )
                            // Glowing bio bubbles
                            drawCircle(Color(0xFF4ADE80), 9f, Offset(co.x - 22f, co.y - 12f))
                            drawCircle(Color(0xFF86EFAC), 6f, Offset(co.x + 28f, co.y + 14f))
                            drawCircle(Color(0xFF22C55E), 12f, Offset(co.x + 6f, co.y - 4f))
                        }
                        "WIND_GUST" -> {
                            // High-speed wind current streamlines
                            val windAlpha = 0.45f + 0.3f * sin((env.scrollOffset * 0.08f + co.id).toDouble()).toFloat()
                            val windCol = Color(0xFF93C5FD).copy(alpha = windAlpha)
                            for (row in listOf(-20f, 0f, 20f)) {
                                drawLine(
                                    color = windCol,
                                    start = Offset(co.x - co.width * 0.45f, co.y + row),
                                    end = Offset(co.x + co.width * 0.45f, co.y + row),
                                    strokeWidth = 3f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                        else -> {
                            // Asteroid Pillar
                            drawCircle(Color(0xFF475569), co.width * 0.45f, Offset(co.x, co.y))
                            drawCircle(Color(0xFF334155), co.width * 0.40f, Offset(co.x + 3f, co.y + 3f))
                            drawCircle(Color(0xFF1E293B), co.width * 0.20f, Offset(co.x - 5f, co.y - 5f))
                        }
                    }
                }
            }

            // 10. Combat Projectiles & Upgraded Evolving Bullet Graphics
            for (p in engine.weaponSystem.projectiles) {
                if (p.trail.size > 1) {
                    val trailPoints = p.trail.toList()
                    for (i in 0 until trailPoints.size - 1) {
                        val p1 = trailPoints[i]
                        val p2 = trailPoints[i + 1]
                        drawLine(
                            color = p.glowColor.copy(alpha = p1.alpha * 0.7f),
                            start = Offset(p1.x, p1.y),
                            end = Offset(p2.x, p2.y),
                            strokeWidth = p.size * 2.2f * p1.alpha,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Upgraded Evolving Bullet Graphics
                val level = engine.combatStats.currentLevel
                if (p.isPlayer && level >= 3) {
                    // Level 3+: Multi-Ring Energy Plasma Orb
                    drawCircle(color = AeroCyan.copy(alpha = 0.4f), radius = p.size * 2.5f, center = Offset(p.x, p.y))
                    drawCircle(color = p.glowColor, radius = p.size * 1.8f, center = Offset(p.x, p.y))
                    drawCircle(color = p.color, radius = p.size * 1.2f, center = Offset(p.x, p.y))
                    drawCircle(color = Color.White, radius = p.size * 0.5f, center = Offset(p.x, p.y))
                } else if (p.isPlayer && level >= 2) {
                    // Level 2: Dual Energy Arc Bolt
                    drawCircle(color = p.glowColor, radius = p.size * 1.8f, center = Offset(p.x, p.y))
                    drawCircle(color = p.color, radius = p.size * 1.1f, center = Offset(p.x, p.y))
                    drawCircle(color = Color.White, radius = p.size * 0.45f, center = Offset(p.x, p.y))
                } else {
                    // Level 1: Standard Kinetic Plasma Bolt
                    drawCircle(color = p.glowColor, radius = p.size * 1.6f, center = Offset(p.x, p.y))
                    drawCircle(color = p.color, radius = p.size, center = Offset(p.x, p.y))
                    drawCircle(color = Color.White, radius = p.size * 0.45f, center = Offset(p.x, p.y))
                }
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

            // 12. Remote Multiplayer Opponent Fighter (Real-Time 1v1 PvP)
            engine.multiplayerManager.remotePlayer.value?.let { remote ->
                drawScope.withTransform({
                    translate(remote.x, remote.y)
                    rotate(remote.bankAngle)
                }) {
                    drawEnemyCraft(
                        scope = this,
                        enemy = EnemyEntity(
                            id = 999999L,
                            type = EnemyType.FAST_INTERCEPTOR,
                            x = remote.x,
                            y = remote.y,
                            health = remote.health,
                            maxHealth = remote.maxHealth
                        ),
                        flash = false,
                        alpha = 1.0f
                    )
                }

                // Opponent Radar Target Lock Box & Callsign Text
                drawRect(
                    color = AeroCrimson,
                    topLeft = Offset(remote.x - 30f, remote.y - 30f),
                    size = androidx.compose.ui.geometry.Size(60f, 60f),
                    style = Stroke(width = 1.5f)
                )
            }

            // 13. Bonus Vortex Black Hole Event Horizon (When Active)
            if (engine.bonusVortexActive) {
                drawBonusVortex(drawScope, engine.vortexX, engine.vortexY, engine.vortexRadius, engine.vortexRotation)
            }

            // 14. Glowing Warp Tunnel Overlay (When In Tunnel)
            if (engine.isInBonusTunnel) {
                drawBonusWarpTunnel(drawScope, engine, width, height)
            }

            // 15. Particle Engine (Fireballs, Exhaust, Sparks, Speed Streaks)
            for (pt in vfx.particles) {
                val pColor = pt.color.copy(alpha = pt.alpha)
                when (pt.type) {
                    ParticleType.FIREBALL -> {
                        drawCircle(color = pColor, radius = pt.size * 0.5f, center = Offset(pt.x, pt.y))
                        drawCircle(color = Color.White.copy(alpha = pt.alpha * 0.8f), radius = pt.size * 0.22f, center = Offset(pt.x, pt.y))
                    }
                    ParticleType.SPARK -> {
                        drawCircle(color = pColor, radius = pt.size * 0.4f, center = Offset(pt.x, pt.y))
                    }
                    ParticleType.AFTERBURNER -> {
                        drawCircle(color = pColor, radius = pt.size * 0.6f, center = Offset(pt.x, pt.y))
                    }
                    ParticleType.SMOKE -> {
                        drawCircle(color = pColor, radius = pt.size * 0.5f, center = Offset(pt.x, pt.y))
                    }
                    ParticleType.SPEED_STREAK -> {
                        drawLine(
                            color = pColor,
                            start = Offset(pt.x, pt.y),
                            end = Offset(pt.x, pt.y + pt.size * 2f),
                            strokeWidth = 2.5f
                        )
                    }
                    else -> {
                        drawCircle(color = pColor, radius = pt.size * 0.5f, center = Offset(pt.x, pt.y))
                    }
                }
            }

            // 16. Floating Combat Text (Damage numbers, radio announcements, powerups)
            if (vfx.floatingTexts.isNotEmpty()) {
                val nativeCanvas = drawContext.canvas.nativeCanvas
                for (ft in vfx.floatingTexts) {
                    textPaint.textSize = (14f * ft.scale).coerceAtLeast(10f)
                    val r = (ft.color.red * 255).toInt()
                    val g = (ft.color.green * 255).toInt()
                    val b = (ft.color.blue * 255).toInt()
                    val a = (ft.alpha.coerceIn(0f, 1f) * 255).toInt()
                    textPaint.setARGB(a, r, g, b)
                    nativeCanvas.drawText(ft.text, ft.x, ft.y, textPaint)
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

        val hash = spec.id.hashCode()
        val wingSpan = 32f + (abs(hash % 24))
        val noseLen = 32f + (abs(hash % 16))
        val swept = (hash and 0x4) != 0

        val path = Path().apply {
            moveTo(0f, -noseLen)
            if (swept) {
                lineTo(8f, -noseLen * 0.35f)
                lineTo(wingSpan, 4f)
                lineTo(wingSpan * 0.45f, 18f)
                lineTo(18f, 26f)
                lineTo(14f, 32f) // Right engine
                lineTo(0f, 24f)
                lineTo(-14f, 32f) // Left engine
                lineTo(-18f, 26f)
                lineTo(-wingSpan * 0.45f, 18f)
                lineTo(-wingSpan, 4f)
                lineTo(-8f, -noseLen * 0.35f)
            } else {
                lineTo(7f, -noseLen * 0.4f)
                lineTo(wingSpan, -4f)
                lineTo(wingSpan * 0.9f, 8f)
                lineTo(14f, 14f)
                lineTo(16f, 30f) // Right engine
                lineTo(0f, 24f)
                lineTo(-16f, 30f) // Left engine
                lineTo(-14f, 14f)
                lineTo(-wingSpan * 0.9f, 8f)
                lineTo(-wingSpan, -4f)
                lineTo(-7f, -noseLen * 0.4f)
            }
            close()
        }

        // Draw dynamic afterburner plume from engine nozzles
        val flameLen = if (isBoosting) 38f else 18f
        val flameWidth = if (isBoosting) 12f else 6f
        val flameColor = if (isBoosting) AeroOrange else AeroCyan
        scope.drawOval(
            brush = Brush.verticalGradient(listOf(Color.White, flameColor, Color.Transparent)),
            topLeft = Offset(-flameWidth * 0.5f - 14f, 28f),
            size = Size(flameWidth, flameLen)
        )
        scope.drawOval(
            brush = Brush.verticalGradient(listOf(Color.White, flameColor, Color.Transparent)),
            topLeft = Offset(-flameWidth * 0.5f + 14f, 28f),
            size = Size(flameWidth, flameLen)
        )

        scope.drawPath(path, color = bodyColor)
        scope.drawPath(path, color = trimColor, style = Stroke(width = 2.4f))

        // Canopy (Glass reflection glow)
        val canopyPath = Path().apply {
            moveTo(0f, -noseLen * 0.55f)
            lineTo(5f, -noseLen * 0.1f)
            lineTo(0f, noseLen * 0.2f)
            lineTo(-5f, -noseLen * 0.1f)
            close()
        }
        scope.drawPath(
            canopyPath,
            brush = Brush.verticalGradient(
                listOf(AeroAmber, AeroOrange)
            )
        )

        // Wingtip navigation energy lights
        val tipY = if (swept) 4f else -4f
        scope.drawCircle(color = trimColor, radius = 3f, center = Offset(-wingSpan, tipY))
        scope.drawCircle(color = trimColor, radius = 3f, center = Offset(wingSpan, tipY))
    }

    private fun drawEnemyCraft(
        scope: DrawScope,
        enemy: EnemyEntity,
        flash: Boolean,
        alpha: Float
    ) {
        val baseColor = if (flash) Color.White else (enemy.customColor ?: when (enemy.type) {
            EnemyType.SCOUT_DRONE -> Color(0xFFEF4444)
            EnemyType.FAST_INTERCEPTOR -> Color(0xFFFF5500)
            EnemyType.HEAVY_GUNSHIP -> Color(0xFF8B5CF6)
            EnemyType.STEALTH_RAIDER -> Color(0xFF334155)
            EnemyType.MISSILE_CORVETTE -> Color(0xFFDC2626)
        }).copy(alpha = alpha)

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
        val prof = boss.profile
        val basePColor = prof?.primaryColor ?: Color(0xFFEF4444)
        val baseAColor = prof?.accentColor ?: Color(0xFFFF9500)
        val hullColor = if (flash) Color.White else Color(0xFF0F172A)
        val armorTrim = if (flash) Color.White else basePColor

        // Concept Art Custom Wing Silhouette Geometry
        val bossPath = Path().apply {
            when (prof?.id) {
                "toxin_haze" -> {
                    // Swept bio-organic razor wings with needle nose
                    moveTo(boss.x, boss.y + halfH * 0.85f)
                    lineTo(boss.x + halfW * 0.25f, boss.y + halfH * 0.35f)
                    lineTo(boss.x + halfW * 1.1f, boss.y + halfH * 0.15f) // Forward swept tip
                    lineTo(boss.x + halfW * 0.7f, boss.y - halfH * 0.5f)
                    lineTo(boss.x + halfW * 0.2f, boss.y - halfH * 0.85f)
                    lineTo(boss.x - halfW * 0.2f, boss.y - halfH * 0.85f)
                    lineTo(boss.x - halfW * 0.7f, boss.y - halfH * 0.5f)
                    lineTo(boss.x - halfW * 1.1f, boss.y + halfH * 0.15f)
                    lineTo(boss.x - halfW * 0.25f, boss.y + halfH * 0.35f)
                    close()
                }
                "frost_nova" -> {
                    // Angular crystalline shard geometry
                    moveTo(boss.x, boss.y + halfH * 0.95f)
                    lineTo(boss.x + halfW * 0.4f, boss.y + halfH * 0.2f)
                    lineTo(boss.x + halfW * 1.05f, boss.y - halfH * 0.1f)
                    lineTo(boss.x + halfW * 0.85f, boss.y - halfH * 0.7f)
                    lineTo(boss.x + halfW * 0.35f, boss.y - halfH * 0.4f)
                    lineTo(boss.x, boss.y - halfH * 0.8f)
                    lineTo(boss.x - halfW * 0.35f, boss.y - halfH * 0.4f)
                    lineTo(boss.x - halfW * 0.85f, boss.y - halfH * 0.7f)
                    lineTo(boss.x - halfW * 1.05f, boss.y - halfH * 0.1f)
                    lineTo(boss.x - halfW * 0.4f, boss.y + halfH * 0.2f)
                    close()
                }
                "solar_flare", "magma_brute" -> {
                    // Aggressive heavy chevron spike frame
                    moveTo(boss.x, boss.y + halfH * 0.7f)
                    lineTo(boss.x + halfW * 0.5f, boss.y + halfH * 0.5f)
                    lineTo(boss.x + halfW * 1.15f, boss.y - halfH * 0.3f)
                    lineTo(boss.x + halfW * 0.6f, boss.y - halfH * 0.9f)
                    lineTo(boss.x, boss.y - halfH * 0.35f)
                    lineTo(boss.x - halfW * 0.6f, boss.y - halfH * 0.9f)
                    lineTo(boss.x - halfW * 1.15f, boss.y - halfH * 0.3f)
                    lineTo(boss.x - halfW * 0.5f, boss.y + halfH * 0.5f)
                    close()
                }
                "void_reaper", "neon_phantom", "quantum_shift" -> {
                    // Triple-spike phantom delta frame
                    moveTo(boss.x, boss.y + halfH * 0.9f)
                    lineTo(boss.x + halfW * 0.3f, boss.y + halfH * 0.1f)
                    lineTo(boss.x + halfW * 0.95f, boss.y + halfH * 0.4f)
                    lineTo(boss.x + halfW * 0.85f, boss.y - halfH * 0.8f)
                    lineTo(boss.x, boss.y - halfH * 0.2f)
                    lineTo(boss.x - halfW * 0.85f, boss.y - halfH * 0.8f)
                    lineTo(boss.x - halfW * 0.95f, boss.y + halfH * 0.4f)
                    lineTo(boss.x - halfW * 0.3f, boss.y + halfH * 0.1f)
                    close()
                }
                else -> {
                    // Standard heavy dreadnought chassis
                    moveTo(boss.x, boss.y + halfH * 0.7f)
                    lineTo(boss.x + halfW * 0.45f, boss.y + halfH * 0.3f)
                    lineTo(boss.x + halfW, boss.y - halfH * 0.2f)
                    lineTo(boss.x + halfW * 0.85f, boss.y - halfH)
                    lineTo(boss.x - halfW * 0.85f, boss.y - halfH)
                    lineTo(boss.x - halfW, boss.y - halfH * 0.2f)
                    lineTo(boss.x - halfW * 0.45f, boss.y + halfH * 0.3f)
                    close()
                }
            }
        }

        // Emissive Under-Glow / Ambient Shield Halo
        scope.drawPath(bossPath, color = basePColor.copy(alpha = 0.25f))
        scope.drawPath(bossPath, color = hullColor)
        scope.drawPath(bossPath, color = armorTrim, style = Stroke(width = 3.5f))

        // Left Turret Component
        val leftTurret = boss.components.find { it.id == "left_turret" }
        if (leftTurret != null) {
            val lx = boss.x + leftTurret.offsetX
            val ly = boss.y + leftTurret.offsetY
            val tCol = if (leftTurret.isDestroyed) Color(0xFF475569) else basePColor
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
            val mCol = if (rightTurret.isDestroyed) Color(0xFF475569) else baseAColor
            scope.drawRect(
                color = mCol,
                topLeft = Offset(rx - 15f, ry - 15f),
                size = Size(30f, 30f)
            )
        }

        // Core Reactor (Pulsing Energy Core)
        val coreColor = if (boss.phase == BossPhase.PHASE_3_RAGE_OVERDRIVE) Color(0xFFFF0055) else basePColor
        scope.drawCircle(
            color = coreColor.copy(alpha = 0.45f),
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

    private fun drawBonusVortex(
        scope: DrawScope,
        vx: Float,
        vy: Float,
        radius: Float,
        rotation: Float
    ) {
        // Outer Pulsating Accretion Glow Disk
        scope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    AeroCyan.copy(alpha = 0.85f),
                    AeroViolet.copy(alpha = 0.5f),
                    AeroAmber.copy(alpha = 0.2f),
                    Color.Transparent
                ),
                center = Offset(vx, vy),
                radius = radius * 2.8f
            ),
            radius = radius * 2.8f,
            center = Offset(vx, vy)
        )

        // Swirling Spiral Arms
        scope.withTransform({
            translate(vx, vy)
            rotate(rotation)
        }) {
            for (i in 0 until 6) {
                val angle = i * 60f
                val armRad = angle * PI.toFloat() / 180f
                val armX = cos(armRad) * radius * 1.5f
                val armY = sin(armRad) * radius * 1.5f
                drawLine(
                    color = AeroCyan,
                    start = Offset(0f, 0f),
                    end = Offset(armX, armY),
                    strokeWidth = 3.5f
                )
                drawCircle(
                    color = AeroAmber,
                    radius = 5f,
                    center = Offset(armX, armY)
                )
            }
        }

        // Event Horizon Black Core
        scope.drawCircle(
            color = Color.Black,
            radius = radius * 0.75f,
            center = Offset(vx, vy)
        )
        scope.drawCircle(
            color = AeroCyan,
            radius = radius * 0.75f,
            center = Offset(vx, vy),
            style = Stroke(width = 3f)
        )
        scope.drawCircle(
            color = Color.White,
            radius = radius * 0.25f,
            center = Offset(vx, vy)
        )
    }

    private fun drawBonusWarpTunnel(
        scope: DrawScope,
        engine: GameEngine,
        width: Float,
        height: Float
    ) {
        val cx = width * 0.5f
        val cy = height * 0.5f
        val timer = engine.tunnelTimer
        val speedMult = engine.tunnelSpeedMultiplier

        // Neon Warp Tunnel Rings zooming toward screen
        val ringCount = 12
        for (i in 0 until ringCount) {
            val z = ((timer * 1.8f * speedMult + i * (1.0f / ringCount)) % 1.0f)
            val ringRadius = z * (width * 0.75f)
            val alpha = (z * 1.2f).coerceIn(0f, 0.9f)

            scope.drawCircle(
                color = if (i % 2 == 0) AeroCyan.copy(alpha = alpha) else AeroViolet.copy(alpha = alpha),
                radius = ringRadius.coerceAtLeast(10f),
                center = Offset(cx, cy),
                style = Stroke(width = (2f + z * 8f))
            )
        }

        // Radial Hyper Streaks
        val streakCount = 16
        for (i in 0 until streakCount) {
            val angle = i * (360f / streakCount) + timer * 90f
            val rad = angle * PI.toFloat() / 180f
            val innerR = 40f
            val outerR = width * 0.8f
            scope.drawLine(
                color = ShieldBlue.copy(alpha = 0.45f),
                start = Offset(cx + cos(rad) * innerR, cy + sin(rad) * innerR),
                end = Offset(cx + cos(rad) * outerR, cy + sin(rad) * outerR),
                strokeWidth = 2.5f
            )
        }

        // Floating Golden Tunnel Bonus Coins
        for (k in 0..4) {
            val coinY = cy - 250f + k * 100f + ((timer * 400f * speedMult) % 500f)
            val coinX = cx + sin((timer * 3f + k).toDouble()).toFloat() * 140f
            scope.drawCircle(
                color = Color(0xFFFFD700).copy(alpha = 0.9f),
                radius = 14f,
                center = Offset(coinX, coinY)
            )
            scope.drawCircle(
                color = Color.White,
                radius = 5f,
                center = Offset(coinX, coinY)
            )
        }
    }
}
