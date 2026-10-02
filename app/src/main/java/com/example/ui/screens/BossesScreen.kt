package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.BossProfileCatalog
import com.example.game.model.BossProfileSpec
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

@Composable
fun BossesScreen(
    viewModel: GameViewModel,
    onLaunchBossSortie: ((String) -> Unit)? = null
) {
    val allBosses = remember { BossProfileCatalog.PROFILES.values.sortedBy { it.index } }
    var selectedBoss by remember { mutableStateOf(allBosses.last()) } // Default Nexus Obliterator

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
            .padding(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Dangerous,
                        contentDescription = null,
                        tint = AeroCrimson,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BOSS ROSTER // 24 ENCOUNTERS",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
                Text(
                    text = "ANTIGRAVITY WARFARE • LEGENDARY ENEMY FLEETS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, AeroCrimson.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "${allBosses.size} BOSSES ACTIVE",
                    color = AeroCrimson,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 24 Enemy Planes Fleet Reconnaissance Banner
        var showReconSheet by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurfaceElevated.copy(alpha = 0.8f))
                .border(1.dp, AeroCrimson.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .clickable { showReconSheet = !showReconSheet }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RemoveRedEye, contentDescription = null, tint = AeroCrimson, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "FLEET RECON INTEL // 24 ENEMY WARBIRDS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    if (showReconSheet) "HIDE" else "VIEW INTEL",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = AeroCrimson,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        AnimatedVisibility(visible = showReconSheet) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, AeroCrimson.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.thunder_dome_24_enemy_planes),
                    contentDescription = "Enemy Planes Intel Roster",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, DarkVoid.copy(alpha = 0.7f))))
                )
                Text(
                    text = "CLASSIFIED 3000 STUDIOS RECON SHEET",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = AeroAmber,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Top Horizontal Boss Carousel (01 to 24)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allBosses) { boss ->
                val isSelected = boss.id == selectedBoss.id
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(84.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) DarkSurfaceElevated else CarbonDark)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) boss.primaryColor else DarkSurfaceBorder,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedBoss = boss }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${boss.index.toString().padStart(2, '0')}",
                            color = if (isSelected) boss.primaryColor else TextSecondary,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(
                            imageVector = when {
                                boss.index == 24 -> Icons.Default.Stars
                                boss.index % 4 == 0 -> Icons.Default.Warning
                                boss.index % 3 == 0 -> Icons.Default.FlashOn
                                else -> Icons.Default.Shield
                            },
                            contentDescription = null,
                            tint = boss.primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = boss.name.take(7),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Boss Showcase Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    // Boss Title & Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedBoss.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = selectedBoss.primaryColor,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "${selectedBoss.matchedBiomeName.uppercase()} // ${selectedBoss.epithet}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AeroAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Class Tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(selectedBoss.primaryColor.copy(alpha = 0.2f))
                                .border(1.dp, selectedBoss.primaryColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (selectedBoss.index == 24) "FINAL CLASS" else "ELITE CLASS",
                                color = selectedBoss.primaryColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tactical Wireframe Arena Hologram
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkVoid)
                            .border(1.dp, selectedBoss.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width * 0.5f
                            val cy = size.height * 0.5f
                            // Glowing aura rings
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(selectedBoss.primaryColor.copy(alpha = 0.35f), Color.Transparent),
                                    center = Offset(cx, cy),
                                    radius = size.width * 0.4f
                                ),
                                radius = size.width * 0.35f,
                                center = Offset(cx, cy)
                            )
                            drawCircle(
                                color = selectedBoss.primaryColor.copy(alpha = 0.4f),
                                radius = 55f,
                                center = Offset(cx, cy),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                            )
                            drawCircle(
                                color = selectedBoss.accentColor.copy(alpha = 0.6f),
                                radius = 75f,
                                center = Offset(cx, cy),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                            )
                            // Crosshairs
                            drawLine(
                                color = selectedBoss.primaryColor.copy(alpha = 0.3f),
                                start = Offset(cx - 90f, cy),
                                end = Offset(cx + 90f, cy),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = selectedBoss.primaryColor.copy(alpha = 0.3f),
                                start = Offset(cx, cy - 65f),
                                end = Offset(cx, cy + 65f),
                                strokeWidth = 1f
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (selectedBoss.index == 24) Icons.Default.AllInclusive else Icons.Default.DeviceHub,
                                contentDescription = null,
                                tint = selectedBoss.primaryColor,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "TACTICAL THREAT RATING: ${selectedBoss.index * 4200} PTS",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Description Lore
                    Text(
                        text = "INTEL BRIEFING",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedBoss.profileDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Boss Signature Attacks
                    Text(
                        text = "COMBAT SIGNATURE ATTACKS",
                        style = MaterialTheme.typography.labelSmall,
                        color = AeroAmber,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        selectedBoss.weaponMoves.forEach { move ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CarbonDark)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = selectedBoss.primaryColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = move,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Holo Comms / Taunts Preview
                    Text(
                        text = "ENCOUNTER COMMS TRANSCRIPT",
                        style = MaterialTheme.typography.labelSmall,
                        color = AeroCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkVoid)
                            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "\"${selectedBoss.taunts.trigger1}\"",
                                color = AeroCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "REPLY: \"${selectedBoss.comms.quickReplies.firstOrNull() ?: "Clear the skies."}\"",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Sortie Launch Action Button
                    Button(
                        onClick = {
                            viewModel.selectStage(selectedBoss.index)
                            onLaunchBossSortie?.invoke(selectedBoss.name)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("launch_boss_sortie_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = selectedBoss.primaryColor,
                            contentColor = DarkVoid
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FlightTakeoff, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LAUNCH ${selectedBoss.name} SORTIE",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
