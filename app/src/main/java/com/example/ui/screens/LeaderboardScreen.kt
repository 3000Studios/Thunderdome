package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

@Composable
fun LeaderboardScreen(viewModel: GameViewModel) {
    val leaders by viewModel.leaderboards.collectAsState()
    val challenges by viewModel.weeklyChallenges.collectAsState()
    var selectedSubTab by remember { mutableStateOf("LEADERBOARD") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
            .padding(16.dp)
    ) {
        // Tab Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedSubTab == "LEADERBOARD") AeroCyan else DarkSurfaceElevated)
                    .clickable { selectedSubTab = "LEADERBOARD" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "GLOBAL LEADERBOARD",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selectedSubTab == "LEADERBOARD") DarkVoid else Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedSubTab == "CHALLENGES") AeroCyan else DarkSurfaceElevated)
                    .clickable { selectedSubTab = "CHALLENGES" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "WEEKLY OPS",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selectedSubTab == "CHALLENGES") DarkVoid else Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedSubTab == "LEADERBOARD") {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(leaders) { entry ->
                    val isPlayer = entry.callsign.contains("YOU")
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("leaderboard_row_${entry.rank}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPlayer) DarkSurfaceElevated else DarkSurface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isPlayer) AeroCyan else DarkSurfaceBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "#${entry.rank}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                                    color = when (entry.rank) {
                                        1 -> Color(0xFFFFD700)
                                        2 -> Color(0xFFE2E8F0)
                                        3 -> Color(0xFFCD7F32)
                                        else -> TextSecondary
                                    },
                                    modifier = Modifier.width(36.dp)
                                )
                                Column {
                                    Text(
                                        text = entry.callsign,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isPlayer) AeroCyan else Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${entry.aircraft} • ${entry.tier}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Text(
                                text = "${entry.score}",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        } else {
            // Weekly Challenges
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(challenges) { ch ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ch.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = "+${ch.rewardCredits} CR / +${ch.rewardCores} CORES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AeroEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ch.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Progress Bar
                            val progressRatio = (ch.progress.toFloat() / ch.maxProgress).coerceIn(0f, 1f)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(DarkSurfaceBorder)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(progressRatio)
                                            .fillMaxHeight()
                                            .background(if (ch.isCompleted) AeroEmerald else AeroCyan)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "${ch.progress}/${ch.maxProgress}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
