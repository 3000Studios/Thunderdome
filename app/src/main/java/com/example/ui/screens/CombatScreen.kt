package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.geometry.Offset
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
import com.example.game.multiplayer.MultiplayerMode
import com.example.game.multiplayer.RemotePlayerState
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
    val profile by viewModel.playerProfile.collectAsState()

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
                        // Direct Finger Tracking / Relative Touch: craft tracks naturally above thumb with zero lag
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            engine.onDirectTouchDown(
                                touchX = down.position.x,
                                touchY = down.position.y,
                                screenWidth = size.width.toFloat(),
                                screenHeight = size.height.toFloat(),
                                touchOffsetY = settings.touchOffsetY,
                                controlScheme = settings.controlScheme
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
                                    sensitivity = settings.touchSensitivity,
                                    controlScheme = settings.controlScheme,
                                    touchOffsetY = settings.touchOffsetY
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
            onToggleCamera = {
                viewModel.toggleCameraViewMode()
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
                engine.toggleBoost()
            },
            onSetBoostActive = { active ->
                engine.setBoostActive(active)
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

        // 3a. 4-Player Squad Telemetry Widget (Co-op Campaign)
        if (engine.isMultiplayerMatchActive && engine.multiplayerManager.currentMode.collectAsState().value == MultiplayerMode.SQUAD_COOP_4P) {
            val squad by engine.multiplayerManager.squadMembers.collectAsState()
            if (squad.isNotEmpty()) {
                SquadTelemetryWidget(
                    squad = squad,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 110.dp, start = 12.dp)
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

        // 3c. Roguelite System Overclock / Perk Selection Modal
        engine.pendingPerkSelection?.let { perks ->
            RoguelitePerkModal(
                perks = perks,
                onSelectPerk = { selectedPerk ->
                    engine.selectPerk(selectedPerk)
                }
            )
        }

        // 4. Pause Menu Modal
        if (engine.isPaused) {
            PauseModal(
                engine = engine,
                settings = settings,
                onUpdateTouchOffsetY = { viewModel.updateSettings(touchOffsetY = it) },
                onUpdateSensitivity = { viewModel.updateSettings(sensitivity = it) },
                onUpdateScheme = { viewModel.updateSettings(scheme = it) },
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

        val context = androidx.compose.ui.platform.LocalContext.current
        val activity = context as? android.app.Activity

        // 5. Game Over / Debriefing Screen
        if (engine.isGameOver) {
            GameOverModal(
                stats = stats,
                onBankOverclocks = viewModel::bankMissionOverclocks,
                onWatchAdToRevive = {
                    if (activity != null) {
                        viewModel.showRewardedAdForContinue(activity) {
                            engine.revivePlayer()
                        }
                    } else {
                        engine.revivePlayer()
                    }
                },
                onDoubleRewards = {
                    if (activity != null) {
                        viewModel.showRewardedAdForDoubleReward(activity) {
                            stats.creditsEarned *= 2
                            stats.plasmaCoresEarned *= 2
                        }
                    } else {
                        stats.creditsEarned *= 2
                        stats.plasmaCoresEarned *= 2
                    }
                },
                onShareSortie = {
                    com.example.ui.components.ShareHelper.shareSortieResult(
                        context = context,
                        callsign = profile.callsign,
                        score = stats.score,
                        kills = stats.kills,
                        stageName = engine.currentBiome.name,
                        aircraftName = engine.currentAircraftSpec.name
                    )
                },
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

        // 6. Mission Complete / Victory Screen
        if (engine.isVictory) {
            VictoryModal(
                stats = stats,
                biomeName = engine.currentBiome.name,
                onBankOverclocks = viewModel::bankMissionOverclocks,
                onDoubleRewards = {
                    if (activity != null) {
                        viewModel.showRewardedAdForDoubleReward(activity) {
                            stats.creditsEarned *= 2
                            stats.plasmaCoresEarned *= 2
                        }
                    } else {
                        stats.creditsEarned *= 2
                        stats.plasmaCoresEarned *= 2
                    }
                },
                onShareVictory = {
                    com.example.ui.components.ShareHelper.shareSortieResult(
                        context = context,
                        callsign = profile.callsign,
                        score = stats.score,
                        kills = stats.kills,
                        stageName = engine.currentBiome.name,
                        aircraftName = engine.currentAircraftSpec.name
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
    onToggleCamera: () -> Unit,
    onBarrelRoll: () -> Unit,
    onSecondaryFire: () -> Unit,
    onSpecialFire: () -> Unit,
    onToggleBoost: () -> Unit,
    onSetBoostActive: (Boolean) -> Unit = {}
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

            // Score & Radar Compass & Controls
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onToggleCamera,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("combat_camera_toggle_button")
                        ) {
                            Icon(
                                imageVector = when (engine.cameraViewMode) {
                                    "COCKPIT_1ST" -> Icons.Default.Visibility
                                    "TOP_DOWN_CHASE" -> Icons.Default.TravelExplore
                                    else -> Icons.Default.Videocam
                                },
                                contentDescription = "Camera View",
                                tint = when (engine.cameraViewMode) {
                                    "COCKPIT_1ST" -> AeroEmerald
                                    "TOP_DOWN_CHASE" -> AeroViolet
                                    else -> AeroCyan
                                }
                            )
                        }

                        IconButton(
                            onClick = onPauseClick,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("combat_pause_button")
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause", tint = TextSecondary)
                        }
                    }
                }

                // Tactical Radar Compass (media_1790893533430.jpg)
                TacticalRadarCompass(engine = engine)
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
                            .background(if (player.isBoosting) AeroOrange else AeroCyan)
                    )
                }

                // Afterburner Boost Button (Tap: Toggle / Press & Hold: Instant Thrust)
                var isBoostHeld by remember { mutableStateOf(false) }
                val isBoostActive = player.isBoosting || isBoostHeld

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (isBoostActive) AeroOrange else DarkSurfaceElevated)
                        .border(
                            1.5.dp,
                            if (isBoostActive) Color.White else AeroOrange.copy(alpha = 0.6f),
                            CircleShape
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isBoostHeld = true
                                    onSetBoostActive(true)
                                    tryAwaitRelease()
                                    isBoostHeld = false
                                    onSetBoostActive(false)
                                },
                                onTap = {
                                    onToggleBoost()
                                }
                            )
                        }
                        .testTag("boost_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Afterburner Boost",
                        tint = if (isBoostActive) DarkVoid else AeroOrange,
                        modifier = Modifier.size(28.dp)
                    )
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
    var isCollapsed by remember { mutableStateOf(false) }
    var showIntelDialog by remember { mutableStateOf(false) }

    val prof = boss.profile
    val pCol = prof?.primaryColor ?: DangerRed
    val aCol = prof?.accentColor ?: AeroAmber
    val hullRatio = (boss.health / boss.maxHealth).coerceIn(0f, 1f)

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
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            // Header Row: Boss Name, Phase, Collapse Toggle, Intel Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = boss.name,
                                style = MaterialTheme.typography.labelLarge,
                                color = pCol,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${ (hullRatio * 100).toInt() }%)",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = aCol
                            )
                        }
                        if (!isCollapsed) {
                            prof?.epithet?.let { ep ->
                                Text(
                                    text = ep,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = aCol,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Phase Tag
                    Surface(
                        color = aCol.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, aCol.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = boss.phase.name.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = aCol,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    // Intel Briefing Button
                    IconButton(
                        onClick = { showIntelDialog = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Boss Intel",
                            tint = AeroCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Collapse / Expand Toggle
                    IconButton(
                        onClick = { isCollapsed = !isCollapsed },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                            contentDescription = if (isCollapsed) "Expand HUD" else "Collapse HUD",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Boss Hull Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isCollapsed) 6.dp else 9.dp)
                    .clip(RoundedCornerShape(if (isCollapsed) 3.dp else 4.dp))
                    .background(DarkSurfaceBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(hullRatio)
                        .fillMaxHeight()
                        .background(Brush.horizontalGradient(listOf(pCol, aCol)))
                )
            }

            // Destructible Sub-Components Indicator (Visible in expanded mode)
            if (!isCollapsed && boss.components.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    boss.components.forEach { comp ->
                        val ratio = (comp.health / comp.maxHealth).coerceIn(0f, 1f)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (comp.isDestroyed) "${comp.name} [OFF]" else comp.name,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color = if (comp.isDestroyed) TextMuted else TextSecondary
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
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

    // Boss Tactical Intel Dialog
    if (showIntelDialog && prof != null) {
        AlertDialog(
            onDismissRequest = { showIntelDialog = false },
            containerColor = DarkSurfaceElevated,
            titleContentColor = pCol,
            textContentColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = pCol, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "TACTICAL INTEL // ${prof.name}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = prof.epithet, color = aCol, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(text = prof.profileDescription, style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                    Divider(color = DarkSurfaceBorder, thickness = 1.dp)

                    Text(text = "KNOWN WEAPON SYSTEMS:", color = AeroCyan, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    prof.weaponMoves.forEach { move ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "• ", color = AeroAmber, style = MaterialTheme.typography.bodySmall)
                            Text(text = move, color = Color.White, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    if (prof.renderNotes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "FIELD THREAT PROFILE:", color = AeroEmerald, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        prof.renderNotes.forEach { note ->
                            Text(text = "- $note", color = TextSecondary, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIntelDialog = false }) {
                    Text("CLOSE INTEL", color = AeroCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun TacticalRadarCompass(
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    val player = engine.playerState
    val enemies = engine.enemySystem.enemies
    val boss = engine.enemySystem.currentBoss

    Box(
        modifier = modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(DarkSurfaceElevated.copy(alpha = 0.85f))
            .border(1.dp, AeroCyan.copy(alpha = 0.6f), CircleShape)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width * 0.5f
            val cy = size.height * 0.5f
            val r = size.width * 0.44f

            // Concentric range circles
            drawCircle(
                color = AeroCyan.copy(alpha = 0.15f),
                radius = r * 0.5f,
                center = Offset(cx, cy),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.8f)
            )
            drawCircle(
                color = AeroCyan.copy(alpha = 0.25f),
                radius = r,
                center = Offset(cx, cy),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )

            // Crosshair lines
            drawLine(
                color = AeroCyan.copy(alpha = 0.2f),
                start = Offset(cx, cy - r),
                end = Offset(cx, cy + r),
                strokeWidth = 0.8f
            )
            drawLine(
                color = AeroCyan.copy(alpha = 0.2f),
                start = Offset(cx - r, cy),
                end = Offset(cx + r, cy),
                strokeWidth = 0.8f
            )

            // Rotating sweep beam
            val rad = Math.toRadians(sweepAngle.toDouble())
            val sweepX = cx + (r * kotlin.math.cos(rad)).toFloat()
            val sweepY = cy + (r * kotlin.math.sin(rad)).toFloat()
            drawLine(
                brush = Brush.radialGradient(
                    listOf(AeroCyan.copy(alpha = 0.9f), AeroCyan.copy(alpha = 0.1f), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = r
                ),
                start = Offset(cx, cy),
                end = Offset(sweepX, sweepY),
                strokeWidth = 1.5f
            )

            // Player dot at center
            drawCircle(color = AeroCyan, radius = 2.5f, center = Offset(cx, cy))
            drawCircle(color = Color.White, radius = 1.2f, center = Offset(cx, cy))

            // Enemy blips
            for (enemy in enemies) {
                if (enemy.health <= 0f) continue
                val relX = ((enemy.x - player.x) / 400f).coerceIn(-1f, 1f)
                val relY = ((enemy.y - player.y) / 700f).coerceIn(-1f, 1f)
                val blipX = cx + relX * (r * 0.8f)
                val blipY = cy + relY * (r * 0.8f)
                drawCircle(color = DangerRed, radius = 2f, center = Offset(blipX, blipY))
            }

            // Boss blip (larger pulsing gold/crimson dot)
            if (boss != null && boss.phase != BossPhase.DESTROYED) {
                val relX = ((boss.x - player.x) / 500f).coerceIn(-1f, 1f)
                val relY = ((boss.y - player.y) / 800f).coerceIn(-1f, 1f)
                val blipX = cx + relX * (r * 0.85f)
                val blipY = cy + relY * (r * 0.85f)
                drawCircle(color = AeroCrimson, radius = 4f, center = Offset(blipX, blipY))
                drawCircle(color = FounderGold, radius = 2f, center = Offset(blipX, blipY))
            }
        }
    }
}

@Composable
fun SquadTelemetryWidget(
    squad: List<RemotePlayerState>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.width(135.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xDD090D16),
        border = androidx.compose.foundation.BorderStroke(1.dp, AeroCyan.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "⚡ SQUAD WINGMEN (4P)",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Black),
                color = AeroCyan
            )
            squad.forEach { member ->
                val hpRatio = (member.health / member.maxHealth).coerceIn(0f, 1f)
                val shdRatio = (member.shield / member.maxShield).coerceIn(0f, 1f)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = member.callsign.take(10),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp),
                            color = member.primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(hpRatio * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    // Health bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(DarkSurfaceBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(hpRatio)
                                .fillMaxHeight()
                                .background(AeroEmerald)
                        )
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
    settings: com.example.data.SettingsEntity,
    onUpdateTouchOffsetY: (Float) -> Unit,
    onUpdateSensitivity: (Float) -> Unit,
    onUpdateScheme: (String) -> Unit,
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
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .border(1.5.dp, AeroCyan, RoundedCornerShape(16.dp))
                .padding(18.dp),
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

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content
            when (pauseTab) {
                "SYSTEMS" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Flight Steering Scheme Toggle
                        Text("STEERING MODE", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "TOUCH_FOLLOW" to "DIRECT OFFSET",
                                "RELATIVE_DRAG" to "RELATIVE SWIPE",
                                "JOYSTICK" to "JOYSTICK"
                            ).forEach { (sch, label) ->
                                val active = settings.controlScheme == sch
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) AeroCyan else DarkSurfaceElevated)
                                        .clickable { onUpdateScheme(sch) }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = if (active) DarkVoid else TextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Finger Position Offset Slider & Presets
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("PLANE FINGER LEAD DISTANCE", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "${settings.touchOffsetY.toInt()} dp (${if (settings.touchOffsetY > 0) "${settings.touchOffsetY.toInt()}dp in front" else if (settings.touchOffsetY == 0f) "Direct Center" else "${-settings.touchOffsetY.toInt()}dp behind"})",
                                    color = AeroCyan,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(
                                    0f to "DIRECT (0)",
                                    40f to "CLOSE (40)",
                                    75f to "STANDARD (75)",
                                    120f to "EXTENDED (120)",
                                    180f to "BALLOON (180)"
                                ).forEach { (offsetVal, label) ->
                                    val isCurrent = kotlin.math.abs(settings.touchOffsetY - offsetVal) < 8f
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isCurrent) AeroCyan else DarkSurfaceElevated)
                                            .clickable { onUpdateTouchOffsetY(offsetVal) }
                                            .padding(vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp),
                                            color = if (isCurrent) DarkVoid else TextSecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Slider(
                                value = settings.touchOffsetY,
                                onValueChange = { onUpdateTouchOffsetY(it) },
                                valueRange = -20f..220f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Flight Sensitivity Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TOUCH SENSITIVITY", color = Color.White, style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${(settings.touchSensitivity * 100).toInt()}%",
                                color = AeroCyan,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = settings.touchSensitivity,
                            onValueChange = { onUpdateSensitivity(it) },
                            valueRange = 0.5f..2.5f,
                            modifier = Modifier.fillMaxWidth()
                        )
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
    onWatchAdToRevive: () -> Unit,
    onDoubleRewards: () -> Unit,
    onShareSortie: () -> Unit,
    onRetry: () -> Unit,
    onExit: () -> Unit
) {
    var overclocksBanked by remember { mutableStateOf(false) }
    var rewardsDoubled by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .border(1.5.dp, DangerRed, RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "MISSION DEBRIEF",
                style = MaterialTheme.typography.titleLarge,
                color = DangerRed,
                fontWeight = FontWeight.Bold
            )
            Text(
                "AIRCRAFT CRITICAL DAMAGE DESTROYED",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Score:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("${stats.score}", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                    Text("+${stats.creditsEarned} CR", color = AeroEmerald, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Plasma Cores:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("+${stats.plasmaCoresEarned}", color = AeroViolet, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                if (stats.overclocksEarned > 0 && !overclocksBanked) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("System Overclocks:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        Text("+${stats.overclocksEarned}", color = AeroCyan, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Rewarded Revive Button
            Button(
                onClick = onWatchAdToRevive,
                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("revive_ad_button"),
                colors = ButtonDefaults.buttonColors(containerColor = AeroViolet, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("REVIVE WARBIRD (AD / VIP +50% HP)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rewarded 2x Reward Multiplier
            if (!rewardsDoubled) {
                Button(
                    onClick = {
                        onDoubleRewards()
                        rewardsDoubled = true
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("double_rewards_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AeroAmber, contentColor = DarkVoid),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CLAIM 2X REWARDS (AD / VIP)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Viral TikTok / Social Share Button
            OutlinedButton(
                onClick = onShareSortie,
                modifier = Modifier.fillMaxWidth().height(40.dp).testTag("share_debrief_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AeroCyan),
                border = androidx.compose.foundation.BorderStroke(1.dp, AeroCyan.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("SHARE SORTIE SCORE 🚀", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                Spacer(modifier = Modifier.height(8.dp))
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
fun VictoryModal(
    stats: com.example.game.engine.GameCombatStats,
    biomeName: String,
    onBankOverclocks: () -> Unit,
    onDoubleRewards: () -> Unit,
    onShareVictory: () -> Unit,
    onExit: () -> Unit
) {
    var overclocksBanked by remember { mutableStateOf(false) }
    var rewardsDoubled by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .border(2.dp, AeroCyan, RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.MilitaryTech,
                contentDescription = null,
                tint = AeroCyan,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "MISSION ACCOMPLISHED!",
                style = MaterialTheme.typography.titleLarge,
                color = AeroCyan,
                fontWeight = FontWeight.Bold
            )
            Text(
                "THEATER SECURED: ${biomeName.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Final Sortie Score:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("${stats.score}", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Hostiles Destroyed:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("${stats.kills}", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Bosses Annihilated:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("${stats.bossKills}", color = AeroAmber, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Credits Secured:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("+${stats.creditsEarned} CR", color = AeroEmerald, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Plasma Cores:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("+${stats.plasmaCoresEarned}", color = AeroViolet, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                if (stats.overclocksEarned > 0 && !overclocksBanked) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("System Overclocks:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        Text("+${stats.overclocksEarned}", color = AeroCyan, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Rewarded 2x Reward Multiplier
            if (!rewardsDoubled) {
                Button(
                    onClick = {
                        onDoubleRewards()
                        rewardsDoubled = true
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("victory_double_rewards_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AeroAmber, contentColor = DarkVoid),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CLAIM 2X REWARDS (AD / VIP)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Viral TikTok / Social Share Button
            OutlinedButton(
                onClick = onShareVictory,
                modifier = Modifier.fillMaxWidth().height(40.dp).testTag("victory_share_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AeroCyan),
                border = androidx.compose.foundation.BorderStroke(1.dp, AeroCyan.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("SHARE VICTORY CARD 🚀", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (stats.overclocksEarned > 0 && !overclocksBanked) {
                Button(
                    onClick = {
                        onBankOverclocks()
                        overclocksBanked = true
                    },
                    modifier = Modifier.fillMaxWidth().testTag("victory_bank_overclocks_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AeroAmber, contentColor = DarkVoid)
                ) {
                    Text("BANK SYSTEM OVERCLOCKS", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = onExit,
                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("victory_exit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = AeroCyan, contentColor = DarkVoid),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.FlightTakeoff, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("RETURN TO HANGAR", fontWeight = FontWeight.Bold)
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
