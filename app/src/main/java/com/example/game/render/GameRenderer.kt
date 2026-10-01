package com.example.game.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import com.example.game.engine.*
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

        val scaleFactor = when (engine.screenSizeScale) {
            "COMPACT" -> 0.90f
            "MAX_IMMERSIVE" -> 1.15f
            else -> 1.0f
        }

        val isCockpit = engine.cameraViewMode == "COCKPIT_1ST"
        val isTopDown = engine.cameraViewMode == "TOP_DOWN_CHASE"

        // Apply dynamic camera shake, lag, and display scale
        drawScope.withTransform({
            scale(scaleFactor, scaleFactor, pivot = Offset(width * 0.5f, height * 0.5f))
            if (isCockpit) {
                translate(
                    physics.cameraShakeX * 1.2f + physics.cameraLagX * 0.35f,
                    physics.cameraShakeY * 1.2f + physics.cameraLagY * 0.35f
                )
            } else {
                translate(physics.cameraShakeX + physics.cameraLagX, physics.cameraShakeY + physics.cameraLagY)
            }
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

            // 2b. Layer 2b: Biome-Specific Planetary Textures & Features (All 24 Biomes)
            drawBiomeSpecificFeatures(this, biome, env, width, height)

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
                if (!isCockpit) {
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

                    // Tactical Core Hitbox Center Pip (Precision Flight Indicator)
                    drawCircle(
                        color = AeroCyan.copy(alpha = 0.85f),
                        radius = 3.5f,
                        center = Offset(player.x, player.y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 1.5f,
                        center = Offset(player.x, player.y)
                    )
                } else {
                    // In Cockpit View: draw sleek fighter nose cone at the bottom of the viewport
                    drawScope.withTransform({
                        translate(player.x, player.y + 40f)
                        rotate(player.bankAngle * 0.4f)
                    }) {
                        drawCockpitNoseTip(this, engine.currentAircraftSpec, engine.currentPaint)
                    }
                }
            }

            // 12. Remote Multiplayer Opponent Fighter (Only when explicit 1v1 PvP is active)
            if (engine.isMultiplayerMatchActive) {
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

        // 17. High-Res 1st-Person Cockpit Interior & HUD Glass System
        if (isCockpit && !engine.isGameOver) {
            drawFirstPersonCockpit(drawScope, engine, width, height)
        } else if (isTopDown && !engine.isGameOver) {
            drawTopDownChaseHud(drawScope, engine, width, height)
        }

        // 18. Cinematic Tachyon Warp Drive Drop-In Sequence
        if (engine.isWarpIntroActive && !engine.isGameOver) {
            drawWarpDriveIntro(drawScope, engine, width, height)
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

        when (spec.id) {
            "aircraft_dart" -> {
                // ── DART: RAZOR DELTA INTERCEPTOR ──
                val dartPath = Path().apply {
                    moveTo(0f, -46f) // Needle nose tip
                    lineTo(8f, -22f)
                    lineTo(38f, 10f) // Swept delta wingtip
                    lineTo(34f, 22f)
                    lineTo(16f, 18f)
                    lineTo(14f, 32f) // Twin right engine nozzle
                    lineTo(0f, 24f)
                    lineTo(-14f, 32f) // Twin left engine nozzle
                    lineTo(-16f, 18f)
                    lineTo(-34f, 22f)
                    lineTo(-38f, 10f) // Swept delta left wingtip
                    lineTo(-8f, -22f)
                    close()
                }
                scope.drawPath(dartPath, color = bodyColor)
                scope.drawPath(dartPath, color = trimColor, style = Stroke(width = 2.2f))

                // Carbon Fiber Wing Inset Panels
                val leftPanel = Path().apply {
                    moveTo(-6f, -16f); lineTo(-30f, 8f); lineTo(-18f, 16f); close()
                }
                val rightPanel = Path().apply {
                    moveTo(6f, -16f); lineTo(30f, 8f); lineTo(18f, 16f); close()
                }
                scope.drawPath(leftPanel, color = Color(0x55000000))
                scope.drawPath(rightPanel, color = Color(0x55000000))

                // Dual Forward Plasma Muzzles
                scope.drawLine(color = trimColor, start = Offset(-18f, 6f), end = Offset(-18f, -4f), strokeWidth = 2.5f)
                scope.drawLine(color = trimColor, start = Offset(18f, 6f), end = Offset(18f, -4f), strokeWidth = 2.5f)

                // Canopy
                val canopy = Path().apply {
                    moveTo(0f, -32f); lineTo(5f, -10f); lineTo(0f, 12f); lineTo(-5f, -10f); close()
                }
                scope.drawPath(canopy, brush = Brush.verticalGradient(listOf(AeroCyan, Color(0xFF0284C7))))

                // Dual Ion Thrusters
                val flLen = if (isBoosting) 42f else 20f
                scope.drawOval(brush = Brush.verticalGradient(listOf(Color.White, AeroCyan, Color.Transparent)), topLeft = Offset(-17f, 28f), size = Size(7f, flLen))
                scope.drawOval(brush = Brush.verticalGradient(listOf(Color.White, AeroCyan, Color.Transparent)), topLeft = Offset(10f, 28f), size = Size(7f, flLen))
                return
            }
            "aircraft_valkyrie" -> {
                // ── VALKYRIE: HEAVY TWIN-BOOM STRIKE FIGHTER ──
                val valkPath = Path().apply {
                    moveTo(0f, -40f)
                    lineTo(14f, -24f)
                    lineTo(48f, -4f) // Heavy broad wings
                    lineTo(44f, 20f)
                    lineTo(22f, 24f)
                    lineTo(20f, 38f) // Right engine boom
                    lineTo(8f, 36f)
                    lineTo(0f, 16f) // Center fuselage inset
                    lineTo(-8f, 36f)
                    lineTo(-20f, 38f) // Left engine boom
                    lineTo(-22f, 24f)
                    lineTo(-44f, 20f)
                    lineTo(-48f, -4f)
                    lineTo(-14f, -24f)
                    close()
                }
                scope.drawPath(valkPath, color = bodyColor)
                scope.drawPath(valkPath, color = trimColor, style = Stroke(width = 2.5f))

                // Heavy Titanium Armor Plating Line
                scope.drawLine(color = trimColor, start = Offset(-38f, 4f), end = Offset(38f, 4f), strokeWidth = 2f)

                // Cockpit Glass
                val canopy = Path().apply {
                    moveTo(0f, -28f); lineTo(7f, -8f); lineTo(0f, 10f); lineTo(-7f, -8f); close()
                }
                scope.drawPath(canopy, brush = Brush.verticalGradient(listOf(AeroAmber, AeroOrange)))

                // Heavy Twin Engine Plumes
                val flLen = if (isBoosting) 46f else 24f
                scope.drawOval(brush = Brush.verticalGradient(listOf(Color.White, AeroOrange, Color.Transparent)), topLeft = Offset(-18f, 34f), size = Size(10f, flLen))
                scope.drawOval(brush = Brush.verticalGradient(listOf(Color.White, AeroOrange, Color.Transparent)), topLeft = Offset(8f, 34f), size = Size(10f, flLen))
                return
            }
            "aircraft_phantom" -> {
                // ── PHANTOM: STEALTH VARIABLE-SWEEP FIGHTER ──
                val phantPath = Path().apply {
                    moveTo(0f, -48f)
                    lineTo(10f, -26f)
                    lineTo(44f, 14f)
                    lineTo(28f, 24f)
                    lineTo(12f, 22f)
                    lineTo(10f, 34f)
                    lineTo(0f, 28f)
                    lineTo(-10f, 34f)
                    lineTo(-12f, 22f)
                    lineTo(-28f, 24f)
                    lineTo(-44f, 14f)
                    lineTo(-10f, -26f)
                    close()
                }
                scope.drawPath(phantPath, color = Color(0xFF0F172A))
                scope.drawPath(phantPath, color = trimColor, style = Stroke(width = 2.2f))

                // Stealth Faceted Edge Facets
                scope.drawLine(color = AeroViolet, start = Offset(0f, -48f), end = Offset(0f, 28f), strokeWidth = 2f)
                scope.drawCircle(color = AeroViolet, radius = 5f, center = Offset(0f, -8f))

                // Purple Ion Thruster Plumes
                val flLen = if (isBoosting) 40f else 18f
                scope.drawOval(brush = Brush.verticalGradient(listOf(Color.White, AeroViolet, Color.Transparent)), topLeft = Offset(-14f, 30f), size = Size(7f, flLen))
                scope.drawOval(brush = Brush.verticalGradient(listOf(Color.White, AeroViolet, Color.Transparent)), topLeft = Offset(7f, 30f), size = Size(7f, flLen))
                return
            }
            "aircraft_titan" -> {
                // ── TITAN: ARMORED DREADNOUGHT ASSAULT FIGHTER ──
                val titanPath = Path().apply {
                    moveTo(0f, -38f)
                    lineTo(18f, -28f)
                    lineTo(52f, 0f)
                    lineTo(48f, 28f)
                    lineTo(26f, 28f)
                    lineTo(22f, 38f)
                    lineTo(0f, 32f)
                    lineTo(-22f, 38f)
                    lineTo(-26f, 28f)
                    lineTo(-48f, 28f)
                    lineTo(-52f, 0f)
                    lineTo(-18f, -28f)
                    close()
                }
                scope.drawPath(titanPath, color = bodyColor)
                scope.drawPath(titanPath, color = trimColor, style = Stroke(width = 2.8f))

                // Reinforced Bulkheads & Cockpit
                scope.drawRect(color = Color(0xFF334155), topLeft = Offset(-12f, -10f), size = Size(24f, 20f))
                scope.drawCircle(color = AeroEmerald, radius = 6f, center = Offset(0f, -16f))

                // Triple Heavy Thrusters
                val flLen = if (isBoosting) 50f else 26f
                scope.drawOval(brush = Brush.verticalGradient(listOf(Color.White, AeroEmerald, Color.Transparent)), topLeft = Offset(-20f, 34f), size = Size(8f, flLen))
                scope.drawOval(brush = Brush.verticalGradient(listOf(Color.White, AeroEmerald, Color.Transparent)), topLeft = Offset(-4f, 30f), size = Size(8f, flLen))
                scope.drawOval(brush = Brush.verticalGradient(listOf(Color.White, AeroEmerald, Color.Transparent)), topLeft = Offset(12f, 34f), size = Size(8f, flLen))
                return
            }
            "aircraft_jerica" -> {
                // ── SECRET QUEEN BEE (JERICA) ──
                val beeGold = Color(0xFFFFD700)
                val beeBlack = Color(0xFF18181B)
                val beeAmber = Color(0xFFF59E0B)

                // Translucent Hexagonal Wings
                val wingPath = Path().apply {
                    moveTo(0f, -10f)
                    lineTo(48f, -22f)
                    lineTo(44f, 15f)
                    lineTo(12f, 10f)
                    lineTo(0f, 0f)
                    lineTo(-12f, 10f)
                    lineTo(-44f, 15f)
                    lineTo(-48f, -22f)
                    close()
                }
                scope.drawPath(wingPath, color = Color(0x66FDE047))
                scope.drawPath(wingPath, color = beeGold, style = Stroke(width = 1.8f))

                // Black & Gold Striped Bee Fuselage
                val fuselage = Path().apply {
                    moveTo(0f, -38f)
                    lineTo(16f, -15f)
                    lineTo(18f, 12f)
                    lineTo(8f, 32f)
                    lineTo(0f, 44f) // Sharp Stinger Tip
                    lineTo(-8f, 32f)
                    lineTo(-18f, 12f)
                    lineTo(-16f, -15f)
                    close()
                }
                scope.drawPath(fuselage, color = beeBlack)
                scope.drawPath(fuselage, color = beeGold, style = Stroke(width = 2.4f))

                // Golden Abdomen Stripes
                for (yOff in listOf(-8f, 4f, 16f, 26f)) {
                    scope.drawRoundRect(
                        color = beeGold,
                        topLeft = Offset(-12f, yOff),
                        size = Size(24f, 6f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                    )
                }

                // Stinger Energy Glow & Thruster
                val stingerColor = if (isBoosting) Color(0xFFFF2200) else beeAmber
                scope.drawCircle(stingerColor, radius = if (isBoosting) 10f else 6f, center = Offset(0f, 44f))

                // Cyan Hologram Queen Eyes
                scope.drawOval(color = AeroCyan, topLeft = Offset(-10f, -30f), size = Size(6f, 10f))
                scope.drawOval(color = AeroCyan, topLeft = Offset(4f, -30f), size = Size(6f, 10f))
                return
            }
            "aircraft_jadon" -> {
                // ── SECRET APEX SOVEREIGN (JADON) ──
                val jadonRed = Color(0xFFFF1E56)
                val jadonCyan = Color(0xFF00F0FF)
                val jadonDark = Color(0xFF0A0F1D)

                // Split Dual-Tone Sovereign Wing Geometry
                val sovereignPath = Path().apply {
                    moveTo(0f, -44f)
                    lineTo(14f, -20f)
                    lineTo(50f, 8f)
                    lineTo(34f, 22f)
                    lineTo(18f, 16f)
                    lineTo(12f, 36f)
                    lineTo(0f, 28f)
                    lineTo(-12f, 36f)
                    lineTo(-18f, 16f)
                    lineTo(-34f, 22f)
                    lineTo(-50f, 8f)
                    lineTo(-14f, -20f)
                    close()
                }
                scope.drawPath(sovereignPath, color = jadonDark)

                // Left Wing Crimson Accent, Right Wing Cyan Accent
                val leftWing = Path().apply {
                    moveTo(0f, -44f)
                    lineTo(-14f, -20f)
                    lineTo(-50f, 8f)
                    lineTo(-34f, 22f)
                    lineTo(-18f, 16f)
                    close()
                }
                scope.drawPath(leftWing, color = jadonRed.copy(alpha = 0.85f))
                scope.drawPath(leftWing, color = Color.White, style = Stroke(width = 1.6f))

                val rightWing = Path().apply {
                    moveTo(0f, -44f)
                    lineTo(14f, -20f)
                    lineTo(50f, 8f)
                    lineTo(34f, 22f)
                    lineTo(18f, 16f)
                    close()
                }
                scope.drawPath(rightWing, color = jadonCyan.copy(alpha = 0.85f))
                scope.drawPath(rightWing, color = Color.White, style = Stroke(width = 1.6f))

                // Central Titanium Spine & Canopy
                scope.drawLine(color = Color.White, start = Offset(0f, -44f), end = Offset(0f, 28f), strokeWidth = 3f)
                scope.drawCircle(color = AeroCyan, radius = 6f, center = Offset(0f, -12f))

                // Dual Ion Thruster Plumes
                val flameLen = if (isBoosting) 45f else 22f
                scope.drawOval(brush = Brush.verticalGradient(listOf(Color.White, jadonRed, Color.Transparent)), topLeft = Offset(-16f, 32f), size = Size(8f, flameLen))
                scope.drawOval(brush = Brush.verticalGradient(listOf(Color.White, jadonCyan, Color.Transparent)), topLeft = Offset(8f, 32f), size = Size(8f, flameLen))
                return
            }
        }

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

    private fun drawBiomeSpecificFeatures(
        scope: DrawScope,
        biome: BiomeSpec,
        env: EnvironmentSystem,
        width: Float,
        height: Float
    ) {
        when (biome.id) {
            "asteroid_belt", "junkyard", "space_graveyard" -> {
                // Floating space debris & jagged asteroid boulders
                for (i in 0 until 12) {
                    val astY = ((env.scrollOffset * 0.6f + i * 140f) % (height + 160f)) - 80f
                    val astX = ((i * 197f) % (width - 60f)) + 30f
                    val size = 22f + (i % 5) * 8f
                    val astCol = if (biome.id == "junkyard") Color(0xFF78350F) else Color(0xFF475569)
                    scope.drawCircle(astCol.copy(alpha = 0.55f), size, Offset(astX, astY))
                    scope.drawCircle(Color.Black.copy(alpha = 0.4f), size * 0.6f, Offset(astX - 3f, astY - 3f))
                }
            }
            "crystal_caverns", "dimension_rift", "void_gate" -> {
                // Prismatic crystal refraction shards & dimensional fracture lines
                val pulse = (sin(env.scrollOffset * 0.02) * 0.3f + 0.7f).toFloat()
                for (i in 0 until 8) {
                    val crx = ((i * 220f) % (width - 80f)) + 40f
                    val cry = ((env.scrollOffset * 0.8f + i * 200f) % (height + 200f)) - 100f
                    val shardPath = Path().apply {
                        moveTo(crx, cry - 30f)
                        lineTo(crx + 18f, cry)
                        lineTo(crx, cry + 30f)
                        lineTo(crx - 18f, cry)
                        close()
                    }
                    scope.drawPath(shardPath, color = AeroViolet.copy(alpha = 0.25f * pulse))
                    scope.drawPath(shardPath, color = AeroCyan.copy(alpha = 0.65f * pulse), style = Stroke(width = 1.5f))
                }
            }
            "lava_planet", "solar_core", "volcanic_ridge" -> {
                // Glowing magma cracks & heat fissure veins on terrain
                val lavaCol = Color(0xFFFF5500)
                for (i in 0 until 6) {
                    val lx = ((i * 180f) % (width - 100f)) + 50f
                    val ly = ((env.scrollOffset * 0.5f + i * 240f) % (height + 150f)) - 80f
                    scope.drawLine(
                        color = lavaCol.copy(alpha = 0.45f),
                        start = Offset(lx - 40f, ly),
                        end = Offset(lx + 40f, ly + 60f),
                        strokeWidth = 5f
                    )
                    scope.drawLine(
                        color = Color(0xFFFDE047).copy(alpha = 0.7f),
                        start = Offset(lx - 20f, ly + 15f),
                        end = Offset(lx + 20f, ly + 45f),
                        strokeWidth = 2f
                    )
                }
            }
            "neon_outpost", "cyber_city", "orbital_array" -> {
                // Cyberpunk runway gridlines & high-tech ground telemetry
                val gridAlpha = 0.12f
                for (x in 0..(width / 70f).toInt()) {
                    scope.drawLine(
                        color = AeroCyan.copy(alpha = gridAlpha),
                        start = Offset(x * 70f, 0f),
                        end = Offset(x * 70f, height),
                        strokeWidth = 1f
                    )
                }
            }
            "underwater_ruins" -> {
                // Rising deep-sea bubbles & luminous hydro-currents
                for (i in 0 until 14) {
                    val bx = ((i * 137f) % width)
                    val by = height - ((env.scrollOffset * 1.2f + i * 90f) % (height + 50f))
                    scope.drawCircle(AeroCyan.copy(alpha = 0.35f), 4f + (i % 3) * 3f, Offset(bx, by), style = Stroke(width = 1.5f))
                }
            }
            "thunder_dome", "the_citadel", "final_approach" -> {
                // Apex Colosseum thunder arcs & golden arena floodlights
                val glowAlpha = (sin(env.scrollOffset * 0.04) * 0.2f + 0.35f).toFloat()
                scope.drawLine(
                    color = AeroAmber.copy(alpha = glowAlpha),
                    start = Offset(0f, (env.scrollOffset * 0.7f) % height),
                    end = Offset(width, (env.scrollOffset * 0.7f) % height),
                    strokeWidth = 2.5f
                )
            }
        }
    }

    private fun drawWarpDriveIntro(
        scope: DrawScope,
        engine: GameEngine,
        width: Float,
        height: Float
    ) {
        val timer = engine.warpIntroTimer
        val duration = engine.warpIntroDuration
        val progress = (1.0f - (timer / duration)).coerceIn(0f, 1f) // 0 to 1
        val nativeCanvas = scope.drawContext.canvas.nativeCanvas

        // 1. Hyperspace Tachyon Streak Tunnel
        val cx = width * 0.5f
        val cy = height * 0.45f
        val streakCount = 36
        for (i in 0 until streakCount) {
            val angle = (i * (360f / streakCount) + progress * 720f) * PI.toFloat() / 180f
            val baseLen = (height * 0.7f) * (1.0f - progress * 0.6f)
            val startDist = 30f + progress * 100f
            val endDist = startDist + baseLen
            val col = if (i % 2 == 0) AeroCyan.copy(alpha = (1.0f - progress) * 0.85f) else AeroViolet.copy(alpha = (1.0f - progress) * 0.65f)
            scope.drawLine(
                color = col,
                start = Offset(cx + cos(angle) * startDist, cy + sin(angle) * startDist),
                end = Offset(cx + cos(angle) * endDist, cy + sin(angle) * endDist),
                strokeWidth = 3f * (1.0f - progress)
            )
        }

        // 2. Chromatic Distortion Rings
        val ringRadius = (progress * width * 1.2f)
        scope.drawCircle(
            color = AeroCyan.copy(alpha = (1.0f - progress) * 0.4f),
            radius = ringRadius,
            center = Offset(cx, cy),
            style = Stroke(width = 6f * (1.0f - progress))
        )
        scope.drawCircle(
            color = Color.White.copy(alpha = (1.0f - progress) * 0.6f),
            radius = ringRadius * 0.85f,
            center = Offset(cx, cy),
            style = Stroke(width = 3f * (1.0f - progress))
        )

        // 3. Futuristic Drop-in Holo Banner
        if (progress < 0.85f) {
            val bannerAlpha = if (progress < 0.65f) 1.0f else (0.85f - progress) / 0.2f
            scope.drawRoundRect(
                color = Color(0xFF0F172A).copy(alpha = 0.88f * bannerAlpha),
                topLeft = Offset(cx - 160f, height * 0.28f),
                size = Size(320f, 58f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )
            scope.drawRoundRect(
                brush = Brush.horizontalGradient(listOf(AeroCyan.copy(alpha = bannerAlpha), AeroViolet.copy(alpha = bannerAlpha))),
                topLeft = Offset(cx - 160f, height * 0.28f),
                size = Size(320f, 58f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
                style = Stroke(width = 2f)
            )
            textPaint.textAlign = android.graphics.Paint.Align.CENTER
            textPaint.textSize = 14f
            textPaint.color = android.graphics.Color.argb((255 * bannerAlpha).toInt(), 0, 240, 255)
            nativeCanvas.drawText("⚡ TACHYON HYPERDRIVE EXIT ⚡", cx, height * 0.28f + 24f, textPaint)
            textPaint.textSize = 11f
            textPaint.color = android.graphics.Color.argb((200 * bannerAlpha).toInt(), 255, 255, 255)
            nativeCanvas.drawText("SECTOR: ${engine.currentBiome.name.uppercase()}", cx, height * 0.28f + 44f, textPaint)
        }
    }

    private fun drawEnemyCraft(
        scope: DrawScope,
        enemy: EnemyEntity,
        flash: Boolean,
        alpha: Float
    ) {
        val r = enemy.type.radius
        val baseColor = if (flash) Color.White else (enemy.customColor ?: when (enemy.type) {
            EnemyType.SCOUT_DRONE -> Color(0xFFEF4444)
            EnemyType.FAST_INTERCEPTOR -> Color(0xFFFF5500)
            EnemyType.HEAVY_GUNSHIP -> Color(0xFF8B5CF6)
            EnemyType.BOMBER -> Color(0xFFB45309)
            EnemyType.SWARM_UNIT -> Color(0xFFE11D48)
            EnemyType.SHIELD_UNIT -> Color(0xFF0284C7)
            EnemyType.SNIPER_AIRCRAFT -> Color(0xFF10B981)
            EnemyType.KAMIKAZE_UNIT -> Color(0xFFFF0055)
            EnemyType.STEALTH_RAIDER -> Color(0xFF334155)
            EnemyType.MISSILE_CORVETTE -> Color(0xFFDC2626)
            EnemyType.ELITE_GUARD -> Color(0xFFFFD700)
        }).copy(alpha = alpha)

        val trimColor = if (flash) Color.White else Color(0xFFFFD700).copy(alpha = alpha)

        // 11 Distinct Enemy Geometries
        val path = Path().apply {
            when (enemy.type) {
                EnemyType.SCOUT_DRONE, EnemyType.SWARM_UNIT -> {
                    // Compact agile diamond with sensor prongs
                    moveTo(0f, -r * 1.3f)
                    lineTo(r * 0.8f, 0f)
                    lineTo(r * 0.5f, r * 0.9f)
                    lineTo(0f, r * 0.4f)
                    lineTo(-r * 0.5f, r * 0.9f)
                    lineTo(-r * 0.8f, 0f)
                    close()
                }
                EnemyType.FAST_INTERCEPTOR -> {
                    // Razor forward-swept twin-blade
                    moveTo(0f, -r * 1.4f)
                    lineTo(r * 0.3f, -r * 0.2f)
                    lineTo(r * 1.1f, r * 0.5f)
                    lineTo(r * 0.6f, r)
                    lineTo(0f, r * 0.6f)
                    lineTo(-r * 0.6f, r)
                    lineTo(-r * 1.1f, r * 0.5f)
                    lineTo(-r * 0.3f, -r * 0.2f)
                    close()
                }
                EnemyType.HEAVY_GUNSHIP, EnemyType.ELITE_GUARD -> {
                    // Broad armored flying dreadnought with weapon sponsons
                    moveTo(0f, -r * 1.1f)
                    lineTo(r * 0.6f, -r * 0.5f)
                    lineTo(r * 1.2f, 0f)
                    lineTo(r * 1.1f, r * 0.8f)
                    lineTo(r * 0.4f, r)
                    lineTo(0f, r * 0.7f)
                    lineTo(-r * 0.4f, r)
                    lineTo(-r * 1.1f, r * 0.8f)
                    lineTo(-r * 1.2f, 0f)
                    lineTo(-r * 0.6f, -r * 0.5f)
                    close()
                }
                EnemyType.BOMBER -> {
                    // Heavy diamond delta with ordnance bay
                    moveTo(0f, -r * 1.2f)
                    lineTo(r * 1.3f, r * 0.4f)
                    lineTo(r * 0.7f, r)
                    lineTo(0f, r * 0.5f)
                    lineTo(-r * 0.7f, r)
                    lineTo(-r * 1.3f, r * 0.4f)
                    close()
                }
                EnemyType.SHIELD_UNIT -> {
                    // Hexagonal shield matrix generator
                    for (i in 0 until 6) {
                        val angle = (i * 60f - 30f) * PI.toFloat() / 180f
                        val hx = cos(angle) * r
                        val hy = sin(angle) * r
                        if (i == 0) moveTo(hx, hy) else lineTo(hx, hy)
                    }
                    close()
                }
                EnemyType.SNIPER_AIRCRAFT -> {
                    // Needle-nose railgun platform
                    moveTo(0f, -r * 1.8f)
                    lineTo(r * 0.25f, -r * 0.2f)
                    lineTo(r * 0.9f, r * 0.7f)
                    lineTo(0f, r * 0.4f)
                    lineTo(-r * 0.9f, r * 0.7f)
                    lineTo(-r * 0.25f, -r * 0.2f)
                    close()
                }
                EnemyType.KAMIKAZE_UNIT -> {
                    // Aggressive swept delta with booster tail
                    moveTo(0f, -r * 1.5f)
                    lineTo(r * 0.95f, r * 0.8f)
                    lineTo(0f, r * 0.2f)
                    lineTo(-r * 0.95f, r * 0.8f)
                    close()
                }
                EnemyType.STEALTH_RAIDER -> {
                    // Faceted stealth stealth chevron
                    moveTo(0f, -r * 1.3f)
                    lineTo(r * 0.7f, 0f)
                    lineTo(r * 1.0f, r * 0.9f)
                    lineTo(0f, r * 0.3f)
                    lineTo(-r * 1.0f, r * 0.9f)
                    lineTo(-r * 0.7f, 0f)
                    close()
                }
                EnemyType.MISSILE_CORVETTE -> {
                    // Twin-hull catamaran
                    moveTo(-r * 0.5f, -r * 1.2f)
                    lineTo(-r * 0.2f, r)
                    lineTo(0f, r * 0.5f)
                    lineTo(r * 0.2f, r)
                    lineTo(r * 0.5f, -r * 1.2f)
                    lineTo(r * 0.8f, r * 0.6f)
                    lineTo(0f, r * 0.2f)
                    lineTo(-r * 0.8f, r * 0.6f)
                    close()
                }
            }
        }

        scope.drawPath(path, color = baseColor)
        scope.drawPath(path, color = trimColor, style = Stroke(width = 1.8f))

        // Glowing Cockpit Core
        val eyeColor = if (enemy.type == EnemyType.SHIELD_UNIT) AeroCyan else Color(0xFFFF0055)
        scope.drawCircle(
            color = eyeColor.copy(alpha = alpha),
            radius = enemy.type.radius * 0.25f,
            center = Offset(0f, 0f)
        )

        // ── PROGRESSIVE ENEMY DAMAGE OVERLAYS ──
        if (enemy.isWingDamaged || enemy.damageState == DamageState.CRITICAL) {
            // Broken jagged wing crack
            scope.drawLine(
                color = Color(0xFFFF3366),
                start = Offset(-r * 0.8f, r * 0.2f),
                end = Offset(-r * 0.2f, -r * 0.3f),
                strokeWidth = 2.5f
            )
            scope.drawLine(
                color = Color(0xFFFFD700),
                start = Offset(r * 0.6f, r * 0.3f),
                end = Offset(r * 0.1f, 0f),
                strokeWidth = 2f
            )
        }

        if (enemy.isSparking) {
            // Electrical short-circuit sparks
            val sparkAngle = (System.currentTimeMillis() % 360L) * PI.toFloat() / 180f
            scope.drawLine(
                color = Color(0xFF00F0FF),
                start = Offset(cos(sparkAngle) * r * 0.4f, sin(sparkAngle) * r * 0.4f),
                end = Offset(cos(sparkAngle) * r * 0.9f, sin(sparkAngle) * r * 0.9f),
                strokeWidth = 1.8f
            )
        }

        if (enemy.isSmoking) {
            // Hull scorch mark
            scope.drawCircle(
                color = Color.Black.copy(alpha = 0.6f),
                radius = r * 0.35f,
                center = Offset(r * 0.2f, -r * 0.2f)
            )
        }
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

    private fun drawCockpitNoseTip(
        scope: DrawScope,
        spec: AircraftSpec,
        paint: PaintScheme
    ) {
        val path = Path().apply {
            moveTo(0f, -40f)
            lineTo(22f, 30f)
            lineTo(14f, 45f)
            lineTo(-14f, 45f)
            lineTo(-22f, 30f)
            close()
        }
        scope.drawPath(path, color = paint.bodyColor)
        scope.drawPath(path, color = paint.trimColor, style = Stroke(width = 2.5f))

        // Pitot Probe & Sensor Needle
        scope.drawLine(
            color = Color.White,
            start = Offset(0f, -40f),
            end = Offset(0f, -58f),
            strokeWidth = 2.5f
        )
        scope.drawCircle(color = AeroCyan, radius = 3f, center = Offset(0f, -58f))
    }

    private fun drawFirstPersonCockpit(
        scope: DrawScope,
        engine: GameEngine,
        width: Float,
        height: Float
    ) {
        val player = engine.playerState
        val stats = engine.combatStats
        val nativeCanvas = scope.drawContext.canvas.nativeCanvas

        val cx = width * 0.5f
        val cy = height * 0.48f

        // ── 1. CANOPY STRUCTURAL STRUTS & TITANIUM FRAME ──
        val frameDark = Color(0xFF0D121D)
        val frameBorder = Color(0xFF1E293B)
        val frameHighlight = Color(0xFF334155)

        // Left Canopy Pillar
        val leftPillar = Path().apply {
            moveTo(0f, height)
            lineTo(0f, 0f)
            lineTo(width * 0.16f, 0f)
            lineTo(width * 0.22f, height * 0.38f)
            lineTo(width * 0.10f, height)
            close()
        }
        scope.drawPath(leftPillar, color = frameDark)
        scope.drawPath(leftPillar, color = frameBorder, style = Stroke(width = 3f))

        // Right Canopy Pillar
        val rightPillar = Path().apply {
            moveTo(width, height)
            lineTo(width, 0f)
            lineTo(width * 0.84f, 0f)
            lineTo(width * 0.78f, height * 0.38f)
            lineTo(width * 0.90f, height)
            close()
        }
        scope.drawPath(rightPillar, color = frameDark)
        scope.drawPath(rightPillar, color = frameBorder, style = Stroke(width = 3f))

        // Top Canopy Arch Sill
        val topArch = Path().apply {
            moveTo(width * 0.16f, 0f)
            lineTo(width * 0.84f, 0f)
            lineTo(width * 0.76f, height * 0.08f)
            lineTo(width * 0.24f, height * 0.08f)
            close()
        }
        scope.drawPath(topArch, color = frameDark)
        scope.drawPath(topArch, color = frameHighlight, style = Stroke(width = 2f))

        // Rivets on Canopy Pillars
        for (i in 1..8) {
            val t = i / 9f
            scope.drawCircle(Color(0xFF64748B), 3f, Offset(width * (0.04f + t * 0.10f), height * (0.1f + t * 0.8f)))
            scope.drawCircle(Color(0xFF64748B), 3f, Offset(width * (0.96f - t * 0.10f), height * (0.1f + t * 0.8f)))
        }

        // Canopy Glass Tint & Optical Anti-Glare Sheen
        scope.drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    AeroCyan.copy(alpha = 0.04f),
                    Color.Transparent,
                    AeroViolet.copy(alpha = 0.03f)
                )
            ),
            size = Size(width, height)
        )
        // Diagonal Glare Reflection Streaks
        scope.drawLine(
            color = Color.White.copy(alpha = 0.09f),
            start = Offset(width * 0.25f, height * 0.08f),
            end = Offset(width * 0.40f, height * 0.85f),
            strokeWidth = 35f
        )
        scope.drawLine(
            color = Color.White.copy(alpha = 0.05f),
            start = Offset(width * 0.30f, height * 0.08f),
            end = Offset(width * 0.45f, height * 0.85f),
            strokeWidth = 12f
        )

        // ── 2. COLLIMATED HOLOGRAPHIC HUD COMBINER GLASS ──
        val hudLeft = width * 0.18f
        val hudRight = width * 0.82f
        val hudTop = height * 0.10f
        val hudBottom = height * 0.76f
        val hudW = hudRight - hudLeft
        val hudH = hudBottom - hudTop

        // HUD Glass Bezel
        scope.drawRoundRect(
            color = AeroCyan.copy(alpha = 0.12f),
            topLeft = Offset(hudLeft, hudTop),
            size = Size(hudW, hudH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
            style = Stroke(width = 1.8f)
        )
        // Glass Mount Brackets at corners
        scope.drawRect(Color(0xFF475569), Offset(hudLeft - 4f, hudTop + 20f), Size(8f, 24f))
        scope.drawRect(Color(0xFF475569), Offset(hudRight - 4f, hudTop + 20f), Size(8f, 24f))
        scope.drawRect(Color(0xFF475569), Offset(hudLeft - 4f, hudBottom - 44f), Size(8f, 24f))
        scope.drawRect(Color(0xFF475569), Offset(hudRight - 4f, hudBottom - 44f), Size(8f, 24f))

        val hudColor = AeroEmerald
        val hudGlow = AeroEmerald.copy(alpha = 0.4f)

        // ── 3. ARTIFICIAL HORIZON & PITCH LADDER ──
        scope.withTransform({
            translate(cx, cy)
            rotate(player.bankAngle)
        }) {
            // Main Boresight Horizon Line
            drawLine(
                color = hudColor,
                start = Offset(-70f, 0f),
                end = Offset(-20f, 0f),
                strokeWidth = 2.2f
            )
            drawLine(
                color = hudColor,
                start = Offset(20f, 0f),
                end = Offset(70f, 0f),
                strokeWidth = 2.2f
            )
            // Center Flight Path Marker (Boresight Circle)
            drawCircle(color = hudColor, radius = 6f, center = Offset(0f, 0f), style = Stroke(width = 2f))
            drawLine(color = hudColor, start = Offset(0f, -10f), end = Offset(0f, -6f), strokeWidth = 2f)
            drawLine(color = hudColor, start = Offset(-10f, 0f), end = Offset(-6f, 0f), strokeWidth = 2f)
            drawLine(color = hudColor, start = Offset(6f, 0f), end = Offset(10f, 0f), strokeWidth = 2f)

            // Pitch Rungs (+10°, +20°, -10°, -20°)
            for (p in listOf(-50f to "+10", -100f to "+20", 50f to "-10", 100f to "-20")) {
                val py = p.first
                drawLine(color = hudGlow, start = Offset(-45f, py), end = Offset(-18f, py), strokeWidth = 1.5f)
                drawLine(color = hudGlow, start = Offset(18f, py), end = Offset(45f, py), strokeWidth = 1.5f)
                drawLine(color = hudGlow, start = Offset(-45f, py), end = Offset(-45f, py + 8f), strokeWidth = 1.5f)
                drawLine(color = hudGlow, start = Offset(45f, py), end = Offset(45f, py + 8f), strokeWidth = 1.5f)
            }
        }

        // ── 4. SUBTLE COCKPIT TARGETING HUD (NON-OBSTRUCTIVE) ──
        val closestEnemy = engine.enemySystem.enemies.minByOrNull {
            kotlin.math.hypot(it.x - player.x, it.y - player.y)
        } ?: engine.enemySystem.currentBoss?.let {
            EnemyEntity(id = 8888L, type = EnemyType.HEAVY_GUNSHIP, x = it.x, y = it.y, health = it.health, maxHealth = it.maxHealth)
        }

        if (closestEnemy != null) {
            // Sleek subtle holographic lead pip without obscuring banners or bulky boxes
            scope.drawCircle(
                color = AeroCyan.copy(alpha = 0.65f),
                radius = 7f,
                center = Offset(closestEnemy.x, closestEnemy.y),
                style = Stroke(width = 1.2f)
            )
        }

        // ── 5. LEFT AIRSPEED TAPE & RIGHT ALTIMETER TAPE ──
        // Left Airspeed Indicator
        val speedKts = 750 + (player.vy * -0.2f).toInt()
        val mach = String.format("M %.2f", (speedKts / 660f))
        scope.drawRoundRect(
            color = Color(0xFF0F172A).copy(alpha = 0.7f),
            topLeft = Offset(hudLeft + 8f, cy - 80f),
            size = Size(64f, 160f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
        scope.drawRoundRect(
            color = hudColor.copy(alpha = 0.5f),
            topLeft = Offset(hudLeft + 8f, cy - 80f),
            size = Size(64f, 160f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
            style = Stroke(width = 1.5f)
        )
        textPaint.textSize = 13f
        textPaint.color = android.graphics.Color.argb(255, 74, 222, 128)
        nativeCanvas.drawText("SPD", hudLeft + 40f, cy - 60f, textPaint)
        textPaint.textSize = 16f
        nativeCanvas.drawText("$speedKts", hudLeft + 40f, cy, textPaint)
        textPaint.textSize = 11f
        nativeCanvas.drawText(mach, hudLeft + 40f, cy + 25f, textPaint)

        // Right Altitude Tape
        val altMeters = (2500f + engine.stageDistanceCurrent * 1.5f).toInt()
        scope.drawRoundRect(
            color = Color(0xFF0F172A).copy(alpha = 0.7f),
            topLeft = Offset(hudRight - 72f, cy - 80f),
            size = Size(64f, 160f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
        scope.drawRoundRect(
            color = hudColor.copy(alpha = 0.5f),
            topLeft = Offset(hudRight - 72f, cy - 80f),
            size = Size(64f, 160f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
            style = Stroke(width = 1.5f)
        )
        textPaint.textSize = 13f
        nativeCanvas.drawText("ALT", hudRight - 40f, cy - 60f, textPaint)
        textPaint.textSize = 15f
        nativeCanvas.drawText("$altMeters", hudRight - 40f, cy, textPaint)
        textPaint.textSize = 11f
        nativeCanvas.drawText("R-ALT", hudRight - 40f, cy + 25f, textPaint)

        // ── 6. TOP HEADING COMPASS TAPE ──
        val headingVal = (360 + ((player.x / width) * 60f).toInt()) % 360
        scope.drawRoundRect(
            color = Color(0xFF0F172A).copy(alpha = 0.8f),
            topLeft = Offset(cx - 90f, hudTop + 10f),
            size = Size(180f, 32f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
        scope.drawRoundRect(
            color = hudColor.copy(alpha = 0.6f),
            topLeft = Offset(cx - 90f, hudTop + 10f),
            size = Size(180f, 32f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
            style = Stroke(width = 1.5f)
        )
        textPaint.textSize = 13f
        nativeCanvas.drawText("HDG: ${headingVal}° N", cx, hudTop + 32f, textPaint)

        // ── 7. LOWER COCKPIT MFDs (MULTI-FUNCTION DISPLAYS) ──
        val mfdY = height - 120f
        val mfdSize = 100f

        // Left MFD: 3D Tactical Radar Scanner
        val mfdLeftX = width * 0.08f
        scope.drawRoundRect(
            color = Color(0xFF020617),
            topLeft = Offset(mfdLeftX, mfdY),
            size = Size(mfdSize, mfdSize),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
        )
        scope.drawRoundRect(
            color = AeroCyan,
            topLeft = Offset(mfdLeftX, mfdY),
            size = Size(mfdSize, mfdSize),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
            style = Stroke(width = 2f)
        )
        val radarCenter = Offset(mfdLeftX + mfdSize * 0.5f, mfdY + mfdSize * 0.5f)
        scope.drawCircle(AeroCyan.copy(alpha = 0.3f), 38f, radarCenter, style = Stroke(width = 1f))
        scope.drawCircle(AeroCyan.copy(alpha = 0.5f), 20f, radarCenter, style = Stroke(width = 1f))
        // Rotating Radar Sweep
        val sweepAngle = (System.currentTimeMillis() % 2000L) / 2000f * 2 * PI.toFloat()
        scope.drawLine(
            color = AeroCyan,
            start = radarCenter,
            end = Offset(radarCenter.x + cos(sweepAngle) * 38f, radarCenter.y + sin(sweepAngle) * 38f),
            strokeWidth = 2f
        )
        // Draw enemy radar pings
        for (e in engine.enemySystem.enemies.take(6)) {
            val rx = radarCenter.x + ((e.x - player.x) / width) * 35f
            val ry = radarCenter.y + ((e.y - player.y) / height) * 35f
            scope.drawCircle(DangerRed, 3f, Offset(rx, ry))
        }
        textPaint.textSize = 9f
        textPaint.color = android.graphics.Color.argb(255, 0, 229, 255)
        nativeCanvas.drawText("RADAR TWS", mfdLeftX + mfdSize * 0.5f, mfdY + 14f, textPaint)

        // Right MFD: Systems & Defense Telemetry
        val mfdRightX = width * 0.92f - mfdSize
        scope.drawRoundRect(
            color = Color(0xFF020617),
            topLeft = Offset(mfdRightX, mfdY),
            size = Size(mfdSize, mfdSize),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
        )
        scope.drawRoundRect(
            color = AeroViolet,
            topLeft = Offset(mfdRightX, mfdY),
            size = Size(mfdSize, mfdSize),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
            style = Stroke(width = 2f)
        )
        textPaint.textSize = 9f
        textPaint.color = android.graphics.Color.argb(255, 192, 132, 252)
        nativeCanvas.drawText("DIAGNOSTICS", mfdRightX + mfdSize * 0.5f, mfdY + 14f, textPaint)

        // System Gauges (Shield %, Hull %, Heat %)
        val shdPct = (player.shield / player.maxShield).coerceIn(0f, 1f)
        val hpPct = (player.health / player.maxHealth).coerceIn(0f, 1f)
        val heatPct = (player.heat / 100f).coerceIn(0f, 1f)

        // Shield bar
        scope.drawRect(Color(0xFF1E293B), Offset(mfdRightX + 12f, mfdY + 26f), Size(76f, 8f))
        scope.drawRect(ShieldBlue, Offset(mfdRightX + 12f, mfdY + 26f), Size(76f * shdPct, 8f))

        // Hull bar
        scope.drawRect(Color(0xFF1E293B), Offset(mfdRightX + 12f, mfdY + 42f), Size(76f, 8f))
        scope.drawRect(AeroEmerald, Offset(mfdRightX + 12f, mfdY + 42f), Size(76f * hpPct, 8f))

        // Heat bar
        scope.drawRect(Color(0xFF1E293B), Offset(mfdRightX + 12f, mfdY + 58f), Size(76f, 8f))
        scope.drawRect(if (player.isOverheated) DangerRed else AeroAmber, Offset(mfdRightX + 12f, mfdY + 58f), Size(76f * heatPct, 8f))

        textPaint.textSize = 8f
        textPaint.color = android.graphics.Color.WHITE
        nativeCanvas.drawText("SHD ${(shdPct * 100).toInt()}%", mfdRightX + 50f, mfdY + 34f, textPaint)
        nativeCanvas.drawText("HUL ${(hpPct * 100).toInt()}%", mfdRightX + 50f, mfdY + 50f, textPaint)
        nativeCanvas.drawText("HEAT ${(heatPct * 100).toInt()}%", mfdRightX + 50f, mfdY + 66f, textPaint)

        // Cockpit Master Indicator Badge
        scope.drawRoundRect(
            color = AeroAmber.copy(alpha = 0.85f),
            topLeft = Offset(cx - 65f, height - 42f),
            size = Size(130f, 26f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        textPaint.textSize = 11f
        textPaint.color = android.graphics.Color.BLACK
        nativeCanvas.drawText("COCKPIT 1ST VIEW", cx, height - 25f, textPaint)
    }

    private fun drawTopDownChaseHud(
        scope: DrawScope,
        engine: GameEngine,
        width: Float,
        height: Float
    ) {
        val player = engine.playerState
        val nativeCanvas = scope.drawContext.canvas.nativeCanvas

        // Tactical Airspace Grid
        val gridStep = 80f
        val gridAlpha = 0.08f
        for (x in 0..(width / gridStep).toInt()) {
            scope.drawLine(
                color = AeroCyan.copy(alpha = gridAlpha),
                start = Offset(x * gridStep, 0f),
                end = Offset(x * gridStep, height),
                strokeWidth = 1f
            )
        }
        for (y in 0..(height / gridStep).toInt()) {
            scope.drawLine(
                color = AeroCyan.copy(alpha = gridAlpha),
                start = Offset(0f, y * gridStep),
                end = Offset(width, y * gridStep),
                strokeWidth = 1f
            )
        }

        // Concentric Tactical Distance Rings around Player
        for (r in listOf(120f, 240f, 360f)) {
            scope.drawCircle(
                color = AeroCyan.copy(alpha = 0.18f),
                radius = r,
                center = Offset(player.x, player.y),
                style = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
            )
        }

        // View Mode Indicator Badge
        scope.drawRoundRect(
            color = AeroViolet.copy(alpha = 0.85f),
            topLeft = Offset(width * 0.5f - 65f, height - 42f),
            size = Size(130f, 26f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        textPaint.textSize = 11f
        textPaint.color = android.graphics.Color.WHITE
        nativeCanvas.drawText("TOP-DOWN CHASE", width * 0.5f, height - 25f, textPaint)
    }
}
