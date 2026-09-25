package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.game.model.BiomeCatalog
import com.example.game.model.BiomeSpec
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

@Composable
fun MissionSelectScreen(
    viewModel: GameViewModel,
    onLaunchMission: () -> Unit
) {
    var selectedBiome by remember { mutableStateOf(BiomeCatalog.NEO_TOKYO) }
    var selectedMode by remember { mutableStateOf("CAMPAIGN") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "TACTICAL OPERATIONS // SECTOR SELECT",
            style = MaterialTheme.typography.titleMedium,
            color = AeroCyan
        )
        Text(
            text = "Select combat theater and mission profile:",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Game Mode Toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "CAMPAIGN" to "Campaign Sortie",
                "SURVIVAL" to "Endless Wave",
                "BOSS_RUSH" to "Boss Rush",
                "ROGUE_RUN" to "Rogue Protocol"
            ).forEach { (modeKey, title) ->
                val active = selectedMode == modeKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) AeroCyan else DarkSurfaceElevated)
                        .clickable { selectedMode = modeKey }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (active) DarkVoid else Color.White,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Biome Cards
        Text(
            text = "OPERATIONAL THEATERS",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))

        BiomeCatalog.ALL_BIOMES.forEach { biome ->
            val isSelected = biome.id == selectedBiome.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { selectedBiome = biome }
                    .testTag("biome_select_${biome.id}"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) AeroCyan else DarkSurfaceBorder
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(biome.skyColorTop, biome.skyColorBottom.copy(alpha = 0.6f))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = biome.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "DIFFICULTY x${biome.difficultyMultiplier}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AeroAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = biome.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Cloud, contentDescription = null, tint = AeroCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(biome.weatherType.replace("_", " "), color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = AeroViolet, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Goliath Dreadnought", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Deploy Sortie Button
        Button(
            onClick = {
                viewModel.gameEngine.currentBiome = selectedBiome
                onLaunchMission()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("deploy_mission_button"),
            colors = ButtonDefaults.buttonColors(containerColor = AeroCyan, contentColor = DarkVoid),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.RocketLaunch, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "DEPLOY TO ${selectedBiome.name.uppercase()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
