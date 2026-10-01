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
    onOpenMissions: (() -> Unit)? = null,
    onOpenBosses: (() -> Unit)? = null,
    onOpenBattlePass: () -> Unit,
    onOpenSettings: (() -> Unit)? = null
) {
    val profile by viewModel.playerProfile.collectAsState()
    val settings by viewModel.settings.collectAsState()

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
    val eventPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "event_pulse"
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

    val bonusArmed = profile.level >= 3

    Box(modifier = Modifier.fillMaxSize().semantics { contentDescription = "Thunder Dome title screen" }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width * 0.5f
            val cy = size.height * 0.26f

            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF020409), Color(0xFF070E20), Color(0xFF010206))
                )
            )

            val gridY = size.height * 0.44f
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
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Info & Brand
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "3000 STUDIOS // AIR COMBAT SYSTEMS",
                    style = MaterialTheme.typography.labelSmall,
                    color = AeroCyan,
                    letterSpacing = 2.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "THUNDER DOME",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "NEXT-GEN TOP-DOWN ARCADE // 120 FPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )

                // SPECIAL EVENT BANNER (Nexus Obliterator Live / Bonus Tunnel)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    AeroCrimson.copy(alpha = 0.25f * eventPulse),
                                    AeroAmber.copy(alpha = 0.20f * eventPulse),
                                    DarkSurfaceElevated
                                )
                            )
                        )
                        .border(1.dp, AeroCrimson.copy(alpha = 0.7f * eventPulse), RoundedCornerShape(8.dp))
                        .clickable {
                            if (onOpenBosses != null) {
                                onOpenBosses()
                            } else {
                                onStartCampaign()
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Dangerous,
                            contentDescription = null,
                            tint = AeroCrimson,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "SPECIAL EVENT // NEXUS OBLITERATOR LIVE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "DEFEAT STAGE 24 BOSS FOR +500 PLASMA CORES",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = AeroAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = AeroCrimson, modifier = Modifier.size(16.dp))
                }
            }

            com.example.ui.components.HudResourceModule(
                callsign = profile.callsign,
                level = profile.level,
                credits = profile.credits,
                plasmaCores = profile.plasmaCores,
                modifier = Modifier.fillMaxWidth()
            )

            // Middle 4 CORE BEVELED ACTION BUTTONS (Concept Art media_1790893533484.jpg)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1: CAMPAIGN (Cyan) & MULTIPLAYER (Crimson)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.example.ui.components.TacticalGlossButton(
                        onClick = onStartCampaign,
                        containerColor = AeroCyan,
                        contentColor = DarkVoid,
                        height = 54.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("start_campaign_button")
                    ) {
                        Icon(Icons.Default.FlightTakeoff, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text("CAMPAIGN", fontWeight = FontWeight.Black, fontSize = 13.sp)
                            Text("SOLO SORTIE", fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        }
                    }

                    com.example.ui.components.TacticalGlossButton(
                        onClick = {
                            viewModel.gameEngine.isMultiplayerMatchActive = true
                            viewModel.gameEngine.multiplayerManager.connectToMatchmaking(
                                playerCallsign = profile.callsign,
                                aircraftId = profile.selectedAircraftId,
                                mode = MultiplayerMode.SQUAD_COOP_4P
                            )
                            onStartMultiplayer()
                        },
                        containerColor = AeroCrimson,
                        contentColor = Color.White,
                        height = 54.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("start_multiplayer_button")
                    ) {
                        Icon(Icons.Default.Groups, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text("MULTIPLAYER", fontWeight = FontWeight.Black, fontSize = 13.sp)
                            Text("4P CO-OP // 1V1 PVP", fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        }
                    }
                }

                // Row 2: HANGAR (Violet) & STORE / DEPOT (Gold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.example.ui.components.TacticalGlossButton(
                        onClick = onOpenHangar,
                        containerColor = AeroViolet,
                        contentColor = Color.White,
                        height = 50.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Flight, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text("HANGAR", fontWeight = FontWeight.Black, fontSize = 12.sp)
                            Text("CUSTOMIZE CRAFT", fontWeight = FontWeight.Bold, fontSize = 8.5.sp)
                        }
                    }

                    com.example.ui.components.TacticalGlossButton(
                        onClick = { viewModel.openStoreModal() },
                        containerColor = FounderGold,
                        contentColor = DarkVoid,
                        height = 50.dp,
                        modifier = Modifier.weight(1f).testTag("open_store_button")
                    ) {
                        Icon(Icons.Default.ShoppingBag, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text("DEPOT STORE", fontWeight = FontWeight.Black, fontSize = 12.sp)
                            Text("FOUNDER PACKS", fontWeight = FontWeight.Bold, fontSize = 8.5.sp)
                        }
                    }
                }

                // Row 3: Quick Systems Strip (MISSIONS | BOSSES | PASS | SETTINGS)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    com.example.ui.components.TacticalGlossButton(
                        onClick = { onOpenMissions?.invoke() ?: onStartCampaign() },
                        containerColor = CarbonElevated,
                        contentColor = AeroCyan,
                        height = 42.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Public, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("STAGES", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }

                    com.example.ui.components.TacticalGlossButton(
                        onClick = { onOpenBosses?.invoke() ?: onStartCampaign() },
                        containerColor = CarbonElevated,
                        contentColor = AeroCrimson,
                        height = 42.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Dangerous, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("BOSSES", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }

                    com.example.ui.components.TacticalGlossButton(
                        onClick = onOpenBattlePass,
                        containerColor = CarbonElevated,
                        contentColor = AeroAmber,
                        height = 42.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.MilitaryTech, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("PASS", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }

                    com.example.ui.components.TacticalGlossButton(
                        onClick = {
                            onOpenSettings?.invoke() ?: viewModel.toggleCameraViewMode()
                        },
                        containerColor = CarbonElevated,
                        contentColor = TextSecondary,
                        height = 42.dp,
                        modifier = Modifier.weight(1f).testTag("title_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("SYSTEMS", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }

                // 24-Stage Quick Carousel Progress Track
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurface.copy(alpha = 0.85f))
                        .border(1.dp, MetallicBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "24-STAGE CAMPAIGN TRACK",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                            color = AeroCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ACTIVE: STAGE ${viewModel.selectedStageIndex.collectAsState().value}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = AeroAmber
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(24) { i ->
                            val stageNum = i + 1
                            val isSelected = viewModel.selectedStageIndex.collectAsState().value == stageNum
                            Box(
                                modifier = Modifier
                                    .size(width = 36.dp, height = 28.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) AeroCyan else DarkSurfaceElevated)
                                    .border(
                                        1.dp,
                                        if (isSelected) Color.White else MetallicBorder.copy(alpha = 0.6f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable {
                                        viewModel.selectStage(stageNum)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "%02d".format(stageNum),
                                    color = if (isSelected) DarkVoid else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "SYSTEM READY // v3.2 ULTRA // 24 STAGES + BONUS TUNNEL // 3000 STUDIOS",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = TextMuted,
                letterSpacing = 0.8.sp
            )
        }
    }
}
