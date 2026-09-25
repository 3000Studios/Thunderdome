package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine
import com.example.game.model.BossPhase
import com.example.game.model.RoguelitePerk
import com.example.game.render.GameRenderer
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel
import kotlinx.coroutines.isActive
import kotlin.math.hypot

@Composable
fun CombatScreen(
    viewModel: GameViewModel,
    onExitMission: () -> Unit
) {
    val engine = viewModel.gameEngine
    val player = engine.playerState
    val stats = engine.combatStats
    val settings by viewModel.settings.collectAsState()

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var lastNanoTime by remember { mutableLongStateOf(0L) }

    // Primary weapon auto-fire or touch-to-fire
    DisposableEffect(Unit) {
        engine.isFireHeld = true
        onDispose {
            engine.isFireHeld = false
            viewModel.saveMissionFinish()
        }
    }

    // High performance 60-120fps Game Loop
    LaunchedEffect(canvasSize) {
        if (canvasSize.width == 0 || canvasSize.height == 0) return@LaunchedEffect
        lastNanoTime = System.nanoTime()

        while (isActive) {
            withFrameNanos { now ->
                val dt = ((now - lastNanoTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanoTime = now
                engine.update(dt, canvasSize.width.toFloat(), canvasSize.height.toFloat())
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
            .onSizeChanged { canvasSize = it }
    ) {
        // 1. Hardware-Accelerated Combat Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(settings.controlScheme) {
                    when (settings.controlScheme) {
                        "JOYSTICK" -> {
                            detectDragGestures(
                                onDragEnd = {
                                    engine.inputDirX = 0f
                                    engine.inputDirY = 0f
                                },
                                onDragCancel = {
                                    engine.inputDirX = 0f
                                    engine.inputDirY = 0f
                                }
                            ) { change, dragAmount ->
                                change.consume()
                                engine.inputDirX = (dragAmount.x * 0.15f * settings.touchSensitivity).coerceIn(-1f, 1f)
                                engine.inputDirY = (dragAmount.y * 0.15f * settings.touchSensitivity).coerceIn(-1f, 1f)
                            }
                        }
                        else -> {
                            // Touch-follow relative drag
                            detectDragGestures(
                                onDragEnd = {
                                    engine.inputDirX = 0f
                                    engine.inputDirY = 0f
                                }
                            ) { change, dragAmount ->
                                change.consume()
                                engine.inputDirX = (dragAmount.x * 0.18f * settings.touchSensitivity).coerceIn(-1f, 1f)
                                engine.inputDirY = (dragAmount.y * 0.18f * settings.touchSensitivity).coerceIn(-1f, 1f)
                            }
                        }
                    }
                }
        ) {
            GameRenderer.render(this, engine, size.width, size.height)
        }

        // 2. Futuristic Cockpit Holographic HUD
        CombatHudOverlay(
            engine = engine,
            onPauseClick = { engine.isPaused = true },
            onBarrelRoll = { engine.physics.triggerBarrelRoll(player) },
            onSecondaryFire = {
                engine.weaponSystem.fireSecondary(
                    player = player,
                    spec = engine.currentSecondarySpec,
                    enemies = engine.enemySystem.enemies,
                    boss = engine.enemySystem.currentBoss
                )
            },
            onSpecialFire = {
                engine.weaponSystem.activateSpecial(player, engine.currentSpecialSpec)
            },
            onToggleBoost = {
                player.isBoosting = !player.isBoosting
            }
        )

        // 3. Boss Health Bar (When active)
        engine.enemySystem.currentBoss?.let { boss ->
            if (boss.phase != BossPhase.DESTROYED) {
                BossHudBar(
                    boss = boss,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 64.dp)
                )
            }
        }

        // 4. Roguelite Perk Level-Up Selection Modal
        engine.pendingPerkSelection?.let { perks ->
            RoguelitePerkModal(
                perks = perks,
                onSelectPerk = { engine.selectPerk(it) }
            )
        }

        // 5. Pause Menu Modal
        if (engine.isPaused) {
            PauseModal(
                onResume = { engine.isPaused = false },
                onQuit = {
                    viewModel.saveMissionFinish()
                    onExitMission()
                }
            )
        }

        // 6. Game Over / Debriefing Screen
        if (engine.isGameOver) {
            GameOverModal(
                stats = stats,
                onRetry = {
                    val w = canvasSize.width.toFloat()
                    val h = canvasSize.height.toFloat()
                    engine.startMission(
                        aircraft = engine.currentAircraftSpec,
                        primary = engine.currentPrimarySpec,
                        secondary = engine.currentSecondarySpec,
                        special = engine.currentSpecialSpec,
                        biome = engine.currentBiome,
                        paint = engine.currentPaint,
                        exhaust = engine.currentExhaust,
                        screenWidth = w,
                        screenHeight = h
                    )
                },
                onExit = {
                    viewModel.saveMissionFinish()
                    onExitMission()
                }
            )
        }
    }
}

@Composable
fun CombatHudOverlay(
    engine: GameEngine,
    onPauseClick: () -> Unit,
    onBarrelRoll: () -> Unit,
    onSecondaryFire: () -> Unit,
    onSpecialFire: () -> Unit,
    onToggleBoost: () -> Unit
) {
    val player = engine.playerState
    val stats = engine.combatStats

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Status Header: Health, Shield, Score, Combo, Pause
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Player Armor & Shield Vitals
            Column(modifier = Modifier.width(180.dp)) {
                // Shield Bar (Cyan / Blue)
                val shieldRatio = (player.shield / player.maxShield).coerceIn(0f, 1f)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "SHD",
                        color = ShieldBlue,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(30.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSurfaceBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(shieldRatio)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(listOf(ShieldBlue, AeroCyan))
                                )
                        )
                    }
                    Text(
                        "${player.shield.toInt()}",
                        color = ShieldBlue,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Hull Health Bar (Emerald / Red if low)
                val hullRatio = (player.health / player.maxHealth).coerceIn(0f, 1f)
                val hullColor = if (hullRatio < 0.25f) DangerRed else HullGreen
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "HULL",
                        color = hullColor,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(30.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSurfaceBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(hullRatio)
                                .fillMaxHeight()
                                .background(hullColor)
                        )
                    }
                    Text(
                        "${player.health.toInt()}",
                        color = hullColor,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Heat Gauge
                val heatRatio = (player.heat / 100f).coerceIn(0f, 1f)
                val heatColor = if (player.isOverheated) DangerRed else AeroAmber
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (player.isOverheated) "OVR" else "HEAT",
                        color = heatColor,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(30.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(DarkSurfaceBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(heatRatio)
                                .fillMaxHeight()
                                .background(heatColor)
                        )
                    }
                }
            }

            // Score & Combo Counter
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${stats.score}",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace),
                    color = Color.White
                )

                if (stats.comboCount > 1) {
                    Text(
                        text = "COMBO x${stats.comboCount} (+${(stats.comboCount * 10)}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = AeroAmber,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onPauseClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("combat_pause_button")
                ) {
                    Icon(Icons.Default.Pause, contentDescription = "Pause", tint = TextSecondary)
                }
            }
        }

        // Bottom Action Controls: Boost, Barrel Roll, Secondary Missile, Special Ability
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Left Action: Barrel Roll & Boost Toggle
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Boost Bar
                val boostRatio = (player.boost / player.maxBoost).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .width(76.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(DarkSurfaceBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(boostRatio)
                            .fillMaxHeight()
                            .background(AeroCyan)
                    )
                }

                // Afterburner Boost Button
                FloatingActionButton(
                    onClick = onToggleBoost,
                    containerColor = if (player.isBoosting) AeroOrange else DarkSurfaceElevated,
                    contentColor = if (player.isBoosting) DarkVoid else AeroOrange,
                    modifier = Modifier
                        .size(54.dp)
                        .testTag("boost_button"),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Speed, contentDescription = "Afterburner Boost")
                }

                // Evasive Barrel Roll Button
                FloatingActionButton(
                    onClick = onBarrelRoll,
                    containerColor = DarkSurfaceElevated,
                    contentColor = AeroCyan,
                    modifier = Modifier
                        .size(54.dp)
                        .testTag("barrel_roll_button"),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Evasive Barrel Roll")
                }
            }

            // Right Action: Secondary Missile & Special Ability
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Special Ability Button (e.g. Nova, Chrono, Shield)
                val specialReady = player.specialCooldown <= 0f
                FloatingActionButton(
                    onClick = onSpecialFire,
                    containerColor = if (specialReady) AeroViolet else DarkSurfaceElevated,
                    contentColor = if (specialReady) Color.White else TextMuted,
                    modifier = Modifier
                        .size(58.dp)
                        .testTag("special_ability_button"),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = "Special Ability")
                }

                // Secondary Weapon (Swarm Missiles / EMP)
                val secondaryReady = player.secondaryCooldown <= 0f
                FloatingActionButton(
                    onClick = onSecondaryFire,
                    containerColor = if (secondaryReady) AeroCrimson else DarkSurfaceElevated,
                    contentColor = if (secondaryReady) Color.White else TextMuted,
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("secondary_weapon_button"),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = "Secondary Weapon")
                }
            }
        }
    }
}

@Composable
fun BossHudBar(
    boss: com.example.game.model.BossEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .shadow(12.dp, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.92f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = boss.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = DangerRed
                )
                Text(
                    text = boss.phase.name.replace("_", " "),
                    style = MaterialTheme.typography.labelSmall,
                    color = AeroAmber
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Boss Hull Bar
            val hullRatio = (boss.health / boss.maxHealth).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(DarkSurfaceBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(hullRatio)
                        .fillMaxHeight()
                        .background(Brush.horizontalGradient(listOf(Color(0xFFFF2200), Color(0xFFFF7A00))))
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Destructible Sub-Components Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                boss.components.forEach { comp ->
                    val ratio = (comp.health / comp.maxHealth).coerceIn(0f, 1f)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (comp.isDestroyed) "${comp.name} [DESTROYED]" else comp.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = if (comp.isDestroyed) TextMuted else TextSecondary
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(DarkSurfaceBorder)
                        ) {
                            if (!comp.isDestroyed) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(ratio)
                                        .fillMaxHeight()
                                        .background(AeroOrange)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoguelitePerkModal(
    perks: List<RoguelitePerk>,
    onSelectPerk: (RoguelitePerk) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .border(1.5.dp, AeroCyan, RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "SYSTEM OVERCLOCK // LEVEL UP",
                style = MaterialTheme.typography.titleMedium,
                color = AeroCyan
            )
            Text(
                "Select a tactical combat augmentation:",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                perks.forEach { perk ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectPerk(perk) }
                            .testTag("perk_card_${perk.id}"),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, perk.rarity.color.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = perk.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = perk.rarity.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = perk.rarity.color,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = perk.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PauseModal(
    onResume: () -> Unit,
    onQuit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "MISSION SUSPENDED",
                style = MaterialTheme.typography.titleMedium,
                color = AeroCyan
            )

            Button(
                onClick = onResume,
                modifier = Modifier.fillMaxWidth().testTag("pause_resume_button"),
                colors = ButtonDefaults.buttonColors(containerColor = AeroCyan, contentColor = DarkVoid)
            ) {
                Text("RESUME COMBAT", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onQuit,
                modifier = Modifier.fillMaxWidth().testTag("pause_quit_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
            ) {
                Text("ABORT MISSION")
            }
        }
    }
}

@Composable
fun GameOverModal(
    stats: com.example.game.engine.GameCombatStats,
    onRetry: () -> Unit,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .border(1.5.dp, DangerRed, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "MISSION DEBRIEF",
                style = MaterialTheme.typography.titleLarge,
                color = DangerRed
            )
            Text(
                "AIRCRAFT CRITICAL DAMAGE DESTROYED",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Score:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("${stats.score}", color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Hostiles Destroyed:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("${stats.kills}", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Bosses Defeated:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("${stats.bossKills}", color = AeroAmber, style = MaterialTheme.typography.bodyMedium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Credits Earned:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("+${stats.creditsEarned} CR", color = AeroEmerald, style = MaterialTheme.typography.bodyMedium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Plasma Cores:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("+${stats.plasmaCoresEarned}", color = AeroViolet, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onExit,
                    modifier = Modifier.weight(1f).testTag("gameover_exit_button")
                ) {
                    Text("HANGAR")
                }
                Button(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f).testTag("gameover_retry_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AeroCyan, contentColor = DarkVoid)
                ) {
                    Text("RE-DEPLOY", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
