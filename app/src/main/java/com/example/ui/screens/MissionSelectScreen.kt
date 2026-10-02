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
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.game.model.BiomeCatalog
import com.example.game.model.BiomeSpec
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

@Composable
fun MissionSelectScreen(
    viewModel: GameViewModel,
    onLaunchMission: () -> Unit
) {
    var selectedBiome by remember { mutableStateOf<BiomeSpec>(BiomeCatalog.THEATER_01) }
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
            val stageSpec = com.example.game.model.StageMasterCatalog.getForBiome(biome.id)
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
                        .padding(14.dp)
                ) {
                    Column {
                        // High-Definition Cinematic Stage Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) AeroCyan else DarkSurfaceBorder.copy(alpha = 0.6f),
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            Image(
                                painter = painterResource(id = getStagePreviewResource(stageSpec.stage)),
                                contentDescription = stageSpec.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Ambient Cinematic Vignette Overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                DarkVoid.copy(alpha = 0.8f)
                                            )
                                        )
                                    )
                            )
                            // Weather & Atmosphere Badge
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                                    .background(DarkVoid.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, AeroCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = AeroCyan,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    stageSpec.weather,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${stageSpec.stage.toString().padStart(2, '0')}. ${stageSpec.name}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Black
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

                        Spacer(modifier = Modifier.height(6.dp))

                        // Palette Swatches & Hero Skin Tag
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("PALETTE:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextSecondary)
                            stageSpec.palette.forEach { hex ->
                                val color = try {
                                    Color(android.graphics.Color.parseColor(hex))
                                } catch (_: Exception) {
                                    AeroCyan
                                }
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(color)
                                        .border(0.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Obstacles & Speed Lanes
                        Text(
                            text = "OBSTACLES: ${stageSpec.obstacles.joinToString(", ")}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextSecondary
                        )
                        Text(
                            text = "SPEED LANES: ${stageSpec.boostWarSpeed.joinToString(" • ")}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = AeroCyan
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        val matchedBoss = com.example.game.model.BossProfileCatalog.getForBiome(biome.id)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Cloud, contentDescription = null, tint = AeroCyan, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stageSpec.weather, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = matchedBoss.primaryColor, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stageSpec.boss, color = matchedBoss.primaryColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Wormhole Rule Badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AeroCyan.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, AeroCyan.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "🌌 90% WORMHOLE",
                                    color = AeroCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
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
                val profile = viewModel.playerProfile.value
                val spec = com.example.game.model.AircraftCatalog.getById(profile.selectedAircraftId)
                val allSaves = viewModel.allAircraft.value
                val craftSave = allSaves.find { it.aircraftId == spec.id }
                    ?: com.example.data.AircraftSaveEntity(
                        aircraftId = spec.id,
                        specialAbilityId = spec.defaultSpecialAbilityId
                    )
                val primary = com.example.game.model.WeaponCatalog.getById(craftSave.primaryWeaponId)
                val secondary = com.example.game.model.WeaponCatalog.getById(craftSave.secondaryWeaponId)
                val special = com.example.game.model.WeaponCatalog.getById(craftSave.specialAbilityId)
                val paint = com.example.game.model.PaintCatalog.getById(craftSave.paintSchemeId)
                val exhaust = com.example.game.model.ExhaustCatalog.getById(craftSave.exhaustColorId)
                val upgrades = mapOf(
                    "engine" to craftSave.engineUpgradeLevel,
                    "weapon" to craftSave.weaponUpgradeLevel,
                    "armor" to craftSave.armorUpgradeLevel,
                    "shield" to craftSave.shieldUpgradeLevel,
                    "avionics" to craftSave.avionicsUpgradeLevel
                )
                viewModel.gameEngine.startMission(
                    aircraft = spec,
                    primary = primary,
                    secondary = secondary,
                    special = special,
                    biome = selectedBiome,
                    paint = paint,
                    exhaust = exhaust,
                    screenWidth = 1080f,
                    screenHeight = 2160f,
                    savedUpgrades = upgrades
                )
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

fun getStagePreviewResource(stage: Int): Int = when (stage) {
    1 -> R.drawable.td_stage_01_preview
    2 -> R.drawable.td_stage_02_preview
    3 -> R.drawable.td_stage_03_preview
    4 -> R.drawable.td_stage_04_preview
    5 -> R.drawable.td_stage_05_preview
    6 -> R.drawable.td_stage_06_preview
    7 -> R.drawable.td_stage_07_preview
    8 -> R.drawable.td_stage_08_preview
    9 -> R.drawable.td_stage_09_preview
    10 -> R.drawable.td_stage_10_preview
    11 -> R.drawable.td_stage_11_preview
    12 -> R.drawable.td_stage_12_preview
    13 -> R.drawable.td_stage_13_preview
    14 -> R.drawable.td_stage_14_preview
    15 -> R.drawable.td_stage_15_preview
    16 -> R.drawable.td_stage_16_preview
    17 -> R.drawable.td_stage_17_preview
    18 -> R.drawable.td_stage_18_preview
    19 -> R.drawable.td_stage_19_preview
    20 -> R.drawable.td_stage_20_preview
    21 -> R.drawable.td_stage_21_preview
    22 -> R.drawable.td_stage_22_preview
    23 -> R.drawable.td_stage_23_preview
    24 -> R.drawable.td_stage_24_preview
    else -> R.drawable.td_stage_01_preview
}
