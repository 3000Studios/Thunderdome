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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.multiplayer.MultiplayerStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel
import kotlin.math.sin

@Composable
fun TitleScreen(
    viewModel: GameViewModel,
    onStartCampaign: () -> Unit,
    onStartMultiplayer: () -> Unit,
    onOpenHangar: () -> Unit,
    onOpenBattlePass: () -> Unit
) {
    val profile by viewModel.playerProfile.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "title_bg")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )
    val radarRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_rotation"
    )
    val warbirdYaw by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "warbird_yaw"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // High-Fidelity Sci-Fi Holographic Background & 3D Warbird Wireframe
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width * 0.5f
            val cy = size.height * 0.38f

            // 1. Dark Void Space Gradient
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF020409), Color(0xFF070E20), Color(0xFF010206))
                )
            )

            // 2. Tactical Perspective Grid Floor
            val gridY = size.height * 0.55f
            for (i in 0..12) {
                val lineX = size.width * (i / 12f)
                drawLine(
                    color = AeroCyan.copy(alpha = 0.08f),
                    start = Offset(cx, cy - 30f),
                    end = Offset(lineX, size.height),
                    strokeWidth = 1f
                )
            }
            for (j in 0..6) {
                val y = gridY + (j * j * 12f)
                drawLine(
                    color = AeroCyan.copy(alpha = 0.12f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }

            // 3. Neon Radial Glow Core
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(AeroCyan.copy(alpha = 0.20f * glowPulse), AeroViolet.copy(alpha = 0.08f), Color.Transparent),
                    radius = size.width * 0.65f
                ),
                center = Offset(cx, cy),
                radius = size.width * 0.55f
            )

            // 4. Tactical Radar Elevation Rings
            for (r in listOf(70f, 130f, 190f)) {
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

            // 5. Sweeping Radar Beam
            val radAngle = radarRotation * (Math.PI / 180f).toFloat()
            drawLine(
                color = AeroCyan.copy(alpha = 0.7f),
                start = Offset(cx, cy),
                end = Offset(cx + kotlin.math.cos(radAngle) * 190f, cy + kotlin.math.sin(radAngle) * 190f),
                strokeWidth = 2f
            )

            // 6. Holographic 3D Warbird Wireframe Model
            androidx.compose.ui.graphics.drawscope.DrawScope.let {
                val wireColor = AeroCyan.copy(alpha = 0.85f * glowPulse)
                val wireGlow = AeroEmerald.copy(alpha = 0.6f)

                val yawOffset = warbirdYaw * 1.8f
                val nose = Offset(cx + yawOffset * 0.5f, cy - 70f)
                val leftWing = Offset(cx - 90f + yawOffset, cy + 20f)
                val rightWing = Offset(cx + 90f + yawOffset, cy + 20f)
                val leftTail = Offset(cx - 35f + yawOffset * 0.8f, cy + 55f)
                val rightTail = Offset(cx + 35f + yawOffset * 0.8f, cy + 55f)
                val centerFuselage = Offset(cx + yawOffset * 0.6f, cy + 30f)

                // Outer Wing Lines
                drawLine(wireColor, nose, leftWing, strokeWidth = 2.2f)
                drawLine(wireColor, nose, rightWing, strokeWidth = 2.2f)
                drawLine(wireColor, leftWing, leftTail, strokeWidth = 1.8f)
                drawLine(wireColor, rightWing, rightTail, strokeWidth = 1.8f)
                drawLine(wireColor, leftTail, rightTail, strokeWidth = 1.8f)

                // Fuselage Ribs & Canopy Diamond
                drawLine(wireGlow, nose, centerFuselage, strokeWidth = 1.5f)
                drawLine(wireGlow, leftWing, centerFuselage, strokeWidth = 1.2f)
                drawLine(wireGlow, rightWing, centerFuselage, strokeWidth = 1.2f)
                drawLine(wireGlow, leftTail, centerFuselage, strokeWidth = 1.2f)
                drawLine(wireGlow, rightTail, centerFuselage, strokeWidth = 1.2f)

                // Canopy Hologram
                val canopyCenter = Offset(cx + yawOffset * 0.5f, cy - 25f)
                drawCircle(AeroCyan, 6f, canopyCenter)
                drawCircle(Color.White, 3f, canopyCenter)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── TOP LOGO BRANDING ──
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Text(
                    text = "3000 STUDIOS // CYBER DEFENSE",
                    style = MaterialTheme.typography.labelSmall,
                    color = AeroCyan,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "THUNDER DOME",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 34.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "NEXT-GEN AIR COMBAT TACTICS // 120 FPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 1.5.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── PILOT CARD & QUICK HUD TOGGLE BADGE ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.85f)),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, AeroCyan.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = profile.callsign,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "LEVEL ${profile.level} COMMANDER",
                                style = MaterialTheme.typography.labelSmall,
                                color = AeroCyan
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("${profile.credits} CR", color = AeroEmerald, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${profile.plasmaCores} CORES", color = AeroViolet, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // View Mode & Screen Scale Status Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CAMERA:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (settings.cameraViewMode) {
                                    "COCKPIT_1ST" -> AeroEmerald.copy(alpha = 0.2f)
                                    "TOP_DOWN_CHASE" -> AeroViolet.copy(alpha = 0.2f)
                                    else -> AeroCyan.copy(alpha = 0.2f)
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    when (settings.cameraViewMode) {
                                        "COCKPIT_1ST" -> AeroEmerald
                                        "TOP_DOWN_CHASE" -> AeroViolet
                                        else -> AeroCyan
                                    }
                                ),
                                modifier = Modifier.clickable { viewModel.toggleCameraViewMode() }
                            ) {
                                Text(
                                    text = when (settings.cameraViewMode) {
                                        "COCKPIT_1ST" -> "👁️ 1ST COCKPIT"
                                        "TOP_DOWN_CHASE" -> "🛰️ TOP-DOWN"
                                        else -> "🚀 3RD FOLLOW"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SCALE:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AeroAmber.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AeroAmber),
                                modifier = Modifier.clickable {
                                    val nextScale = when (settings.screenSizeScale) {
                                        "MAX_IMMERSIVE" -> "STANDARD"
                                        "STANDARD" -> "COMPACT"
                                        else -> "MAX_IMMERSIVE"
                                    }
                                    viewModel.setScreenSizeScale(nextScale)
                                }
                            ) {
                                Text(
                                    text = when (settings.screenSizeScale) {
                                        "MAX_IMMERSIVE" -> "115% MAX"
                                        "COMPACT" -> "90% COMPACT"
                                        else -> "100% STD"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AeroAmber,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── MAIN COMBAT MODES SELECTOR ──
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Singleplayer Campaign Mode
                Button(
                    onClick = onStartCampaign,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_campaign_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AeroCyan, contentColor = DarkVoid),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FlightTakeoff, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("CAMPAIGN SORTIE (24 THEATERS)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                // 2. Real-Time 1v1 PvP Multiplayer Dogfight Mode
                Button(
                    onClick = {
                        viewModel.gameEngine.multiplayerManager.connectToMatchmaking(
                            playerCallsign = profile.callsign,
                            aircraftId = profile.selectedAircraftId
                        )
                        onStartMultiplayer()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_multiplayer_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AeroCrimson, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Wifi, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("1V1 MULTIPLAYER DOGFIGHT (PVP)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 3. Hangar Button
                    OutlinedButton(
                        onClick = onOpenHangar,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("HANGAR", fontWeight = FontWeight.Bold)
                    }

                    // 4. Battle Pass / Rewards Button
                    OutlinedButton(
                        onClick = onOpenBattlePass,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AeroAmber),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.MilitaryTech, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PASS & REWARDS", fontWeight = FontWeight.Bold)
                    }
                }

                // 5. Warbird Depot & In-App Purchase Store
                Button(
                    onClick = { viewModel.openStoreModal() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("open_store_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AeroAmber, contentColor = DarkVoid),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("DEPOT & FOUNDER STORE 🛒", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }

            // ── FOOTER STATUS ──
            Text(
                text = "SYSTEM READY // VERSION 3.2 ULTRA // 3000 STUDIOS",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                letterSpacing = 1.sp
            )
        }
    }
}
