package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.BonusStageSpec
import com.example.game.multiplayer.MultiplayerMode
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

/**
 * Polished home title screen — hierarchy tightened for one-handed mobile:
 * brand → HUD → hero radar → primary Campaign → secondary Co-op|PvP → Hangar|Pass|Settings → Depot → footer.
 * Optional BONUS TUNNEL ARMED chip when warp bonus is available.
 */
@Composable
fun TitleScreen(
    viewModel: GameViewModel,
    onStartCampaign: () -> Unit,
    onStartMultiplayer: () -> Unit,
    onOpenHangar: () -> Unit,
    onOpenBattlePass: () -> Unit,
    onOpenSettings: (() -> Unit)? = null
) {
    val profile by viewModel.playerProfile.collectAsState()
    val settings by viewModel.settings.collectAsState()

    // Soft reduced-motion: slow animations when user prefers less motion (Android a11y flag not always wired; keep gentle defaults)
    val reduceMotion = false

    val infiniteTransition = rememberInfiniteTransition(label = "title_bg")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = if (reduceMotion) 0.7f else 0.4f,
        targetValue = if (reduceMotion) 0.85f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (reduceMotion) 6000 else 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )
    val radarRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (reduceMotion) 16000 else 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_rotation"
    )
    val warbirdYaw by infiniteTransition.animateFloat(
        initialValue = if (reduceMotion) 0f else -12f,
        targetValue = if (reduceMotion) 0f else 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "warbird_yaw"
    )

    // Bonus tunnel armed when player level is high enough or flag exists on profile
    val bonusArmed = profile.level >= 3

    Box(modifier = Modifier.fillMaxSize().semantics { contentDescription = "Thunder Dome title screen" }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width * 0.5f
            val cy = size.height * 0.30f

            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF020409), Color(0xFF070E20), Color(0xFF010206))
                )
            )

            val gridY = size.height * 0.48f
            for (i in 0..12) {
                val lineX = size.width * (i / 12f)
                drawLine(
                    color = AeroCyan.copy(alpha = 0.08f),
                    start = Offset(cx, cy - 20f),
                    end = Offset(lineX, size.height),
                    strokeWidth = 1f
                )
            }
            for (j in 0..5) {
                val y = gridY + (j * j * 12f)
                drawLine(
                    color = AeroCyan.copy(alpha = 0.12f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }

            drawCircle(
                brush = Brush.radialGradient(
                    listOf(AeroCyan.copy(alpha = 0.20f * glowPulse), AeroViolet.copy(alpha = 0.08f), Color.Transparent),
                    radius = size.width * 0.55f
                ),
                center = Offset(cx, cy),
                radius = size.width * 0.48f
            )

            for (r in listOf(55f, 100f, 150f)) {
                drawCircle(
                    color = AeroCyan.copy(alpha = 0.22f),
                    radius = r,
                    center = Offset(cx, cy),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 1.2f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )
                )
            }

            val radAngle = radarRotation * (Math.PI / 180f).toFloat()
            drawLine(
                color = AeroCyan.copy(alpha = 0.7f),
                start = Offset(cx, cy),
                end = Offset(cx + kotlin.math.cos(radAngle) * 150f, cy + kotlin.math.sin(radAngle) * 150f),
                strokeWidth = 2f
            )

            val wireColor = AeroCyan.copy(alpha = 0.85f * glowPulse)
            val wireGlow = AeroEmerald.copy(alpha = 0.6f)
            val yawOffset = warbirdYaw * 1.8f
            val nose = Offset(cx + yawOffset * 0.5f, cy - 55f)
            val leftWing = Offset(cx - 72f + yawOffset, cy + 16f)
            val rightWing = Offset(cx + 72f + yawOffset, cy + 16f)
            val leftTail = Offset(cx - 28f + yawOffset * 0.8f, cy + 44f)
            val rightTail = Offset(cx + 28f + yawOffset * 0.8f, cy + 44f)
            val centerFuselage = Offset(cx + yawOffset * 0.6f, cy + 24f)

            drawLine(wireColor, nose, leftWing, strokeWidth = 2.2f)
            drawLine(wireColor, nose, rightWing, strokeWidth = 2.2f)
            drawLine(wireColor, leftWing, leftTail, strokeWidth = 1.8f)
            drawLine(wireColor, rightWing, rightTail, strokeWidth = 1.8f)
            drawLine(wireColor, leftTail, rightTail, strokeWidth = 1.8f)
            drawLine(wireGlow, nose, centerFuselage, strokeWidth = 1.5f)
            drawLine(wireGlow, leftWing, centerFuselage, strokeWidth = 1.2f)
            drawLine(wireGlow, rightWing, centerFuselage, strokeWidth = 1.2f)
            drawCircle(AeroCyan, 5f, Offset(cx + yawOffset * 0.5f, cy - 18f))
            drawCircle(Color.White, 2.5f, Offset(cx + yawOffset * 0.5f, cy - 18f))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "3000 STUDIOS // CYBER DEFENSE",
                    style = MaterialTheme.typography.labelSmall,
                    color = AeroCyan,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "THUNDER DOME",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 32.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "NEXT-GEN AIR COMBAT // TOP-DOWN 120 FPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 1.2.sp
                )
                if (bonusArmed) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = FounderGold.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FounderGold),
                        modifier = Modifier.semantics {
                            contentDescription = "Bonus warp tunnel armed. Lights up on entry."
                        }
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bolt, null, tint = FounderGold, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "${BonusStageSpec.NAME.uppercase()} // LIGHTS UP",
                                color = FounderGold,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            com.example.ui.components.HudResourceModule(
                callsign = profile.callsign,
                level = profile.level,
                credits = profile.credits,
                plasmaCores = profile.plasmaCores,
                modifier = Modifier.fillMaxWidth()
            )

            // Spacer for radar hero zone (drawn in Canvas)
            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // PRIMARY
                com.example.ui.components.TacticalGlossButton(
                    onClick = onStartCampaign,
                    containerColor = AeroCyan,
                    contentColor = DarkVoid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_campaign_button")
                        .semantics { contentDescription = "Start campaign sortie solo" }
                ) {
                    Icon(Icons.Default.FlightTakeoff, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("CAMPAIGN SORTIE", fontWeight = FontWeight.Black, fontSize = 15.sp)
                }

                // SECONDARY: Co-op | PvP
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.example.ui.components.TacticalGlossButton(
                        onClick = {
                            viewModel.gameEngine.isMultiplayerMatchActive = true
                            viewModel.gameEngine.multiplayerManager.connectToMatchmaking(
                                playerCallsign = profile.callsign,
                                aircraftId = profile.selectedAircraftId,
                                mode = MultiplayerMode.SQUAD_COOP_4P
                            )
                            onStartCampaign()
                        },
                        containerColor = AeroEmerald,
                        contentColor = DarkVoid,
                        height = 48.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("start_coop_campaign_button")
                    ) {
                        Icon(Icons.Default.Groups, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("4P CO-OP", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    com.example.ui.components.TacticalGlossButton(
                        onClick = {
                            viewModel.gameEngine.isMultiplayerMatchActive = true
                            viewModel.gameEngine.multiplayerManager.connectToMatchmaking(
                                playerCallsign = profile.callsign,
                                aircraftId = profile.selectedAircraftId,
                                mode = MultiplayerMode.PVP_DOGFIGHT_1V1
                            )
                            onStartMultiplayer()
                        },
                        containerColor = AeroCrimson,
                        contentColor = Color.White,
                        height = 48.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("start_multiplayer_button")
                    ) {
                        Icon(Icons.Default.Wifi, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("1V1 PVP", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // TERTIARY: Hangar | Pass | Settings
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.example.ui.components.TacticalGlossButton(
                        onClick = onOpenHangar,
                        containerColor = CarbonElevated,
                        contentColor = Color.White,
                        height = 46.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Build, null, Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("HANGAR", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    com.example.ui.components.TacticalGlossButton(
                        onClick = onOpenBattlePass,
                        containerColor = CarbonElevated,
                        contentColor = AeroAmber,
                        height = 46.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.MilitaryTech, null, Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("PASS", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    com.example.ui.components.TacticalGlossButton(
                        onClick = {
                            onOpenSettings?.invoke()
                            // Fallback: cycle camera if settings route not wired yet
                            if (onOpenSettings == null) viewModel.toggleCameraViewMode()
                        },
                        containerColor = CarbonElevated,
                        contentColor = AeroViolet,
                        height = 46.dp,
                        modifier = Modifier.weight(1f).testTag("title_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, null, Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("SETTINGS", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                // Compact cam/scale strip (moved off primary path)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurface.copy(alpha = 0.65f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (settings.cameraViewMode) {
                            "COCKPIT_1ST" -> "CAM: 1ST COCKPIT"
                            "TOP_DOWN_CHASE" -> "CAM: TOP-DOWN"
                            else -> "CAM: 3RD FOLLOW"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = AeroCyan,
                        modifier = Modifier
                            .clickable { viewModel.toggleCameraViewMode() }
                            .semantics { contentDescription = "Toggle camera view mode" }
                    )
                    Text(
                        text = when (settings.screenSizeScale) {
                            "MAX_IMMERSIVE" -> "SCALE 115%"
                            "COMPACT" -> "SCALE 90%"
                            else -> "SCALE 100%"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = AeroAmber,
                        modifier = Modifier.clickable {
                            val next = when (settings.screenSizeScale) {
                                "MAX_IMMERSIVE" -> "STANDARD"
                                "STANDARD" -> "COMPACT"
                                else -> "MAX_IMMERSIVE"
                            }
                            viewModel.setScreenSizeScale(next)
                        }
                    )
                }

                com.example.ui.components.DepotFounderButton(
                    onClick = { viewModel.openStoreModal() },
                    modifier = Modifier.fillMaxWidth().testTag("open_store_button")
                )
            }

            Text(
                text = "SYSTEM READY // v3.2 ULTRA // 24 STAGES + BONUS TUNNEL // 3000 STUDIOS",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )
        }
    }
}
