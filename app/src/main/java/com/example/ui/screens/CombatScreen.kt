package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
    var frameTick by remember { mutableLongStateOf(0L) }
    var hudTick by remember { mutableLongStateOf(0L) }

    // Throttle HUD recompositions to 15 Hz for blazing UI performance
    LaunchedEffect(Unit) {
        while (isActive) {
            kotlinx.coroutines.delay(66)
            hudTick = System.currentTimeMillis()
        }
    }

    // Primary weapon auto-fire or touch-to-fire
    DisposableEffect(Unit) {
        engine.isFireHeld = true
        onDispose {
            engine.isFireHeld = false
            engine.stopTunnelMusic()
            engine.stopRadioMusic()
            viewModel.saveMissionFinish()
        }
    }

    // High performance 60-120fps Game Loop
    LaunchedEffect(canvasSize) {
        if (canvasSize.width == 0 || canvasSize.height == 0) return@LaunchedEffect
        if (player.x <= 0f || player.x > canvasSize.width) {
            player.x = canvasSize.width * 0.5f
            player.y = canvasSize.height * 0.78f
        }
        lastNanoTime = System.nanoTime()

        while (isActive) {
            withFrameNanos { now ->
                val dt = ((now - lastNanoTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanoTime = now
                engine.update(dt, canvasSize.width.toFloat(), canvasSize.height.toFloat())
                frameTick = now
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
            .onSizeChanged { canvasSize = it }
    ) {
        // 1. Hardware-Accelerated Combat Canvas with High-Speed Touch Follow
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(settings.controlScheme, settings.touchSensitivity, settings.touchInputEnabled, settings.touchOffsetY) {
                    if (!settings.touchInputEnabled) return@pointerInput
                    if (settings.controlScheme == "JOYSTICK") {
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
                            engine.inputDirX = (dragAmount.x * 0.2f * settings.touchSensitivity).coerceIn(-1f, 1f)
                            engine.inputDirY = (dragAmount.y * 0.2f * settings.touchSensitivity).coerceIn(-1f, 1f)
                        }
                    } else {
                        // Direct Finger Tracking: ship moves with finger wherever finger goes
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            engine.onDirectTouchDown(
                                touchX = down.position.x,
                                touchY = down.position.y,
                                screenWidth = size.width.toFloat(),
                                screenHeight = size.height.toFloat(),
                                touchOffsetY = settings.touchOffsetY
                            )
                            down.consume()

                            while (true) {
                                val event = awaitPointerEvent()
                                val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!pointer.pressed) {
                                    engine.onDirectTouchUp()
                                    break
                                }
                                pointer.consume()
                                engine.onDirectTouchMove(
                                    touchX = pointer.position.x,
                                    touchY = pointer.position.y,
                                    screenWidth = size.width.toFloat(),
                                    screenHeight = size.height.toFloat(),
                                    sensitivity = settings.touchSensitivity
                                )
                            }
                        }
                    }
                }
        ) {
            val _tick = frameTick
            GameRenderer.render(this, engine, size.width, size.height)
        }

        // 2. Futuristic Cockpit Holographic HUD
        CombatHudOverlay(
            engine = engine,
            hudTick = hudTick,
            onPauseClick = { 
                engine.isPaused = true
                engine.pauseRadioMusic()
            },
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

        // 3b. Interactive Holo-Comms Modal (Concept Art: Holographic Taunt & Player Reply System)
        if (engine.isHoloCommsActive && engine.holoBossProfile != null) {
            HoloCommsModal(
                engine = engine,
                onDismiss = { engine.closeHoloComms() }
            )
        }

        // 4. Pause Menu Modal
        if (engine.isPaused) {
            PauseModal(
                engine = engine,
                onResume = { 
                    engine.isPaused = false
                    engine.resumeRadioMusic()
                },
                onRestart = {
                    engine.isPaused = false
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
                onQuit = {
                    viewModel.saveMissionFinish()
                    onExitMission()
                }
            )
        }

        // 5. Game Over / Debriefing Screen
        if (engine.isGameOver) {
            GameOverModal(
                stats = stats,
            onBankOverclocks = viewModel::bankMissionOverclocks,
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
    hudTick: Long,
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

            // Stage Distance Progress Line Bar (Top Center)
            val distRatio = (engine.stageDistanceCurrent / engine.stageDistanceTotal).coerceIn(0f, 1f)
            val distRemaining = (engine.stageDistanceTotal - engine.stageDistanceCurrent).toInt().coerceAtLeast(0)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                // Distance Text Banner
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Boss Line",
                        tint = if (distRatio >= 0.90f) AeroAmber else AeroCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (engine.isInBonusTunnel) "WARP TUNNEL: +15% BONUS COINS"
                               else if (distRemaining == 0) "BOSS BATTLE ENGAGED"
                               else "BOSS IN ${distRemaining}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (engine.isInBonusTunnel) AeroAmber else if (distRatio >= 0.90f) AeroAmber else AeroCyan,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Distance Progress Line Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DarkSurfaceBorder)
                        .border(0.5.dp, DarkSurfaceBorder, RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(distRatio)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(AeroCyan, ShieldBlue, AeroAmber)
                                )
                            )
                    )
                }

                // Perfect Run Badge Indicator
                if (engine.damageTakenThisStage == 0f && distRatio >= 0.3f) {
                    Text(
                        text = "⭐ PERFECT (0 DMG)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = AeroEmerald,
                        fontWeight = FontWeight.Bold
                    )
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

        // Bottom Action Controls: Boost, Barrel Roll, Radio, Secondary Missile, Special Ability
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

            // Center: Radio Track Selector & Tactical Audio Controller
            val currentTrack = engine.allSoundtracks.getOrNull(engine.radioTrackIndex)
            val currentTitle = currentTrack?.title ?: "OFFLINE"
            Card(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { engine.nextRadioTrack() },
                            onLongPress = { engine.toggleMuteAllSounds() }
                        )
                    }
                    .padding(bottom = 6.dp)
                    .testTag("radio_hud_widget"),
                colors = CardDefaults.cardColors(
                    containerColor = if (engine.isSoundMuted) DangerRed.copy(alpha = 0.85f) else DarkSurfaceElevated.copy(alpha = 0.88f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (engine.isSoundMuted) DangerRed else AeroCyan.copy(alpha = 0.7f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (engine.isSoundMuted) Icons.Default.VolumeOff else Icons.Default.Radio,
                        contentDescription = "Radio Player (Tap: Next, Hold: Mute)",
                        tint = if (engine.isSoundMuted) Color.White else AeroCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (engine.isSoundMuted) "MUTED (HOLD UNMUTE)" else "RADIO [${engine.radioTrackIndex + 1}/${engine.allSoundtracks.size}]",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = if (engine.isSoundMuted) Color.White else AeroCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (engine.isSoundMuted) "SOUND OFF" else currentTitle.take(18),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
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
            val prof = boss.profile
            val pCol = prof?.primaryColor ?: DangerRed
            val aCol = prof?.accentColor ?: AeroAmber

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = boss.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = pCol,
                        fontWeight = FontWeight.Bold
                    )
                    prof?.epithet?.let { ep ->
                        Text(
                            text = ep,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = aCol,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Text(
                    text = boss.phase.name.replace("_", " "),
                    style = MaterialTheme.typography.labelSmall,
                    color = aCol
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
                        .background(Brush.horizontalGradient(listOf(pCol, aCol)))
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
    engine: GameEngine,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onQuit: () -> Unit
) {
    var pauseTab by remember { mutableStateOf("SYSTEMS") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .border(1.5.dp, AeroCyan, RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "MISSION SUSPENDED // PAUSE MATRIX",
                style = MaterialTheme.typography.titleMedium,
                color = AeroCyan,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Pause Tab Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("SYSTEMS", "TACTICAL", "AUDIO").forEach { tab ->
                    val active = pauseTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (active) AeroCyan else Color.Transparent)
                            .clickable { pauseTab = tab }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (active) DarkVoid else TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Content
            when (pauseTab) {
                "SYSTEMS" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("CONTROL SCHEME", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            Text("DIRECT TOUCH", color = AeroCyan, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("GRAPHICS QUALITY", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            Text("ULTRA (60 FPS)", color = AeroEmerald, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("WARP TUNNEL STATUS", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            Text(if (engine.isInBonusTunnel) "ACTIVE" else "READY (90% STAGE)", color = AeroAmber, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                "TACTICAL" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("HULL INTEGRITY", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            Text("${engine.playerState.health.toInt()} / ${engine.playerState.maxHealth.toInt()}", color = HullGreen, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SHIELD STRENGTH", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            Text("${engine.playerState.shield.toInt()} / ${engine.playerState.maxShield.toInt()}", color = ShieldBlue, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("STAGE DISTANCE", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            Text("${engine.stageDistanceCurrent.toInt()}m / ${engine.stageDistanceTotal.toInt()}m", color = AeroCyan, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("HOSTILE TARGETS KILLED", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            Text("${engine.combatStats.kills} KILLS", color = AeroAmber, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                "AUDIO" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("3000 STUDIOS TRACKS", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            Text("ENABLED (100%)", color = AeroCyan, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("HAPTIC VIBRATIONS", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            Text("TACTICAL ON", color = AeroEmerald, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons Matrix
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("pause_resume_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AeroCyan, contentColor = DarkVoid),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RESUME COMBAT", fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRestart,
                        modifier = Modifier.weight(1f).height(44.dp).testTag("pause_restart_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AeroAmber),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("RESTART", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onQuit,
                        modifier = Modifier.weight(1f).height(44.dp).testTag("pause_quit_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ABORT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun GameOverModal(
    stats: com.example.game.engine.GameCombatStats,
    onBankOverclocks: () -> Unit,
    onRetry: () -> Unit,
    onExit: () -> Unit
) {
    var overclocksBanked by remember { mutableStateOf(false) }
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
                if (stats.overclocksEarned > 0 && !overclocksBanked) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("System Overclocks:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        Text("+${stats.overclocksEarned}", color = AeroCyan, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (stats.overclocksEarned > 0 && !overclocksBanked) {
                Button(
                    onClick = {
                        onBankOverclocks()
                        overclocksBanked = true
                    },
                    modifier = Modifier.fillMaxWidth().testTag("bank_overclocks_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AeroAmber, contentColor = DarkVoid)
                ) {
                    Text("BANK SYSTEM OVERCLOCKS", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

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

@Composable
fun HoloCommsModal(
    engine: GameEngine,
    onDismiss: () -> Unit
) {
    val prof = engine.holoBossProfile ?: return
    val pCol = prof.primaryColor
    val aCol = prof.accentColor

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface.copy(alpha = 0.95f))
                .border(2.dp, pCol, RoundedCornerShape(16.dp))
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Sector and Enemy Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(pCol)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE HOLO-COMMS // ${prof.matchedBiomeName.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = pCol,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "SECTOR ${prof.index}",
                    style = MaterialTheme.typography.labelSmall,
                    color = aCol,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Holographic Projection Wireframe & Profile
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF030712))
                    .border(1.dp, pCol.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Background scanline effect
                Canvas(modifier = Modifier.fillMaxSize()) {
                    for (i in 0..12) {
                        val y = i * 10f
                        drawLine(
                            color = pCol.copy(alpha = 0.15f),
                            start = androidx.compose.ui.geometry.Offset(0f, y),
                            end = androidx.compose.ui.geometry.Offset(size.width, y),
                            strokeWidth = 1.5f
                        )
                    }
                    // Central Holographic Beacon Icon
                    drawCircle(color = pCol.copy(alpha = 0.2f), radius = 45f)
                    drawCircle(color = pCol, radius = 28f, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f))
                    drawCircle(color = aCol, radius = 12f)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = prof.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = pCol,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = prof.epithet,
                        style = MaterialTheme.typography.labelMedium,
                        color = aCol,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Incoming Boss Taunt
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated.copy(alpha = 0.9f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, pCol.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "TRANSMISSION RECEIVED:",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"${engine.holoCurrentTaunt}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Enemy Counter-Response (If replied)
            if (engine.holoStep >= 2 && engine.holoEnemyResponse.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1028)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, aCol.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "ENEMY COUNTER-RESPONSE:",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = aCol,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"${engine.holoEnemyResponse}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Player Reply Options (Interactive Quick Replies)
            if (engine.holoStep == 1) {
                Text(
                    text = "SELECT TACTICAL TRANSMISSION REPLY:",
                    style = MaterialTheme.typography.labelSmall,
                    color = AeroCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    prof.comms.quickReplies.forEach { reply ->
                        Button(
                            onClick = { engine.onPlayerReplyToHolo(reply) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = AeroCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AeroCyan.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = reply, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Engage Combat / Dissolve Hologram Button
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("holo_engage_button"),
                colors = ButtonDefaults.buttonColors(containerColor = pCol, contentColor = DarkVoid),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (engine.holoStep == 1) "SKIP COMMS & ENGAGE" else "RESUME DOGFIGHT // BREAK COMMS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
