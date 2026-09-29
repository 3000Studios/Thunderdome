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
    val mpStatus by viewModel.gameEngine.multiplayerManager.status.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "title_bg")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Sci-Fi Dynamic Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF030610), Color(0xFF0A0F25), Color(0xFF020408))
                )
            )

            // Neon Radial Glow
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(AeroCyan.copy(alpha = 0.15f * glowPulse), Color.Transparent),
                    radius = size.width * 0.7f
                ),
                center = Offset(size.width * 0.5f, size.height * 0.3f),
                radius = size.width * 0.6f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── TOP LOGO BRANDING ──
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "3000 STUDIOS PRESENTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = AeroCyan,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "THUNDER DOME",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 32.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "AIR COMBAT TACTICS // 24 WARBIRDS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 1.5.sp
                )
            }

            // ── PILOT CARD BADGE ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.8f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
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

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("${profile.credits} CR", color = AeroEmerald, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${profile.plasmaCores} CORES", color = AeroViolet, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
