package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.audio.AudioHapticSystem
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

@Composable
fun BattlePassScreen(viewModel: GameViewModel) {
    val profile by viewModel.playerProfile.collectAsState()
    val tiers by viewModel.battlePassTiers.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
            .padding(16.dp)
    ) {
        // Season Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AeroViolet)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "VANGUARD PASS // SEASON 1",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        "TIER ${profile.vanguardPassTier} / 15",
                        style = MaterialTheme.typography.labelLarge,
                        color = AeroViolet
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Earn Pass XP by logging sorties, shooting down enemy fighters, and destroying boss flagships.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "SEASONAL REWARDS TRACK",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tiers) { tier ->
                val isUnlocked = tier.tier <= profile.vanguardPassTier
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("battle_pass_tier_${tier.tier}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) DarkSurface else DarkSurface.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isUnlocked) AeroViolet.copy(alpha = 0.6f) else DarkSurfaceBorder
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
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isUnlocked) AeroViolet else DarkSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${tier.tier}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = tier.rewardTitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${tier.requiredXp} XP Required",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        if (tier.isClaimed) {
                            Text(
                                "CLAIMED",
                                color = AeroEmerald,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (isUnlocked) {
                            Button(
                                onClick = {
                                    viewModel.audioHaptics.playSound(AudioHapticSystem.SoundType.POWERUP)
                                    // Update tier claimed in state
                                    val updated = viewModel.battlePassTiers.value.map {
                                        if (it.tier == tier.tier) it.copy(isClaimed = true) else it
                                    }
                                    viewModel.battlePassTiers.value = updated
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AeroViolet,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("CLAIM", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Icon(Icons.Default.Lock, contentDescription = "Locked", tint = TextMuted)
                        }
                    }
                }
            }
        }
    }
}
