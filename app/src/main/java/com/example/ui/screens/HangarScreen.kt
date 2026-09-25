package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AircraftSaveEntity
import com.example.game.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HangarScreen(
    viewModel: GameViewModel,
    onLaunchMission: () -> Unit
) {
    val profile by viewModel.playerProfile.collectAsState()
    val allSaves by viewModel.allAircraft.collectAsState()

    var selectedAircraftSpec by remember(profile.selectedAircraftId) {
        mutableStateOf(AircraftCatalog.getById(profile.selectedAircraftId))
    }

    val currentSave = allSaves.find { it.aircraftId == selectedAircraftSpec.id }
        ?: AircraftSaveEntity(aircraftId = selectedAircraftSpec.id)

    val currentPaint = PaintCatalog.getById(currentSave.paintSchemeId)
    val currentExhaust = ExhaustCatalog.getById(currentSave.exhaustColorId)
    val currentPrimary = WeaponCatalog.getById(currentSave.primaryWeaponId)
    val currentSecondary = WeaponCatalog.getById(currentSave.secondaryWeaponId)
    val currentSpecial = WeaponCatalog.getById(currentSave.specialAbilityId)

    var currentTab by remember { mutableStateOf("OVERVIEW") }
    var turntableRotation by remember { mutableFloatStateOf(0f) }

    // Turntable animation
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos {
                turntableRotation = (turntableRotation + 0.35f) % 360f
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Player Resource Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = profile.callsign,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = "LEVEL ${profile.level} PILOT",
                    style = MaterialTheme.typography.labelSmall,
                    color = AeroCyan
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Credits
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.MonetizationOn,
                        contentDescription = "Credits",
                        tint = AeroEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${profile.credits}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }

                // Plasma Cores
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Diamond,
                        contentDescription = "Plasma Cores",
                        tint = AeroViolet,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${profile.plasmaCores}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }
        }

        // Aircraft Selector Carousel
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(AircraftCatalog.ALL_AIRCRAFT) { spec ->
                val isSelected = spec.id == selectedAircraftSpec.id
                val craftSave = allSaves.find { it.aircraftId == spec.id }
                val isUnlocked = craftSave?.isUnlocked ?: (spec.unlockCostCredits == 0L)

                Card(
                    modifier = Modifier
                        .width(130.dp)
                        .clickable {
                            selectedAircraftSpec = spec
                            if (isUnlocked) viewModel.selectAircraft(spec.id)
                        }
                        .testTag("aircraft_select_${spec.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) DarkSurfaceElevated else DarkSurface
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) AeroCyan else DarkSurfaceBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = spec.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) AeroCyan else Color.White,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isUnlocked) "READY" else "LOCKED",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isUnlocked) AeroEmerald else DangerRed
                        )
                    }
                }
            }
        }

        // 3D Hangar Turntable Interactive Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(DarkSurfaceElevated, DarkSurface, DarkVoid),
                        radius = 450f
                    )
                )
                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width * 0.5f
                val cy = size.height * 0.55f

                // Turntable Rings
                drawOval(
                    color = DarkSurfaceBorder,
                    topLeft = Offset(cx - 140f, cy - 35f),
                    size = androidx.compose.ui.geometry.Size(280f, 70f),
                    style = Stroke(width = 2f)
                )
                drawOval(
                    color = AeroCyan.copy(alpha = 0.25f),
                    topLeft = Offset(cx - 100f, cy - 25f),
                    size = androidx.compose.ui.geometry.Size(200f, 50f),
                    style = Stroke(width = 1.5f)
                )

                // Projected Aircraft on Turntable
                val angleRad = (turntableRotation * PI / 180f).toFloat()
                val scaleX = cos(angleRad).coerceIn(-1f, 1f)

                // Jet body in 3D angled perspective
                val bodyColor = currentPaint.bodyColor
                val trimColor = currentPaint.trimColor

                val jetPath = Path().apply {
                    moveTo(cx, cy - 60f)
                    lineTo(cx + 45f * scaleX, cy + 10f)
                    lineTo(cx + 80f * scaleX, cy + 25f)
                    lineTo(cx + 25f * scaleX, cy + 40f)
                    lineTo(cx, cy + 30f)
                    lineTo(cx - 25f * scaleX, cy + 40f)
                    lineTo(cx - 80f * scaleX, cy + 25f)
                    lineTo(cx - 45f * scaleX, cy + 10f)
                    close()
                }

                drawPath(jetPath, color = bodyColor)
                drawPath(jetPath, color = trimColor, style = Stroke(width = 2f))

                // Engine exhaust glow on turntable
                drawCircle(
                    color = currentExhaust.outerColor.copy(alpha = 0.6f),
                    radius = 18f,
                    center = Offset(cx, cy + 35f)
                )
                drawCircle(
                    color = currentExhaust.coreColor,
                    radius = 8f,
                    center = Offset(cx, cy + 35f)
                )
            }

            // Lock Overlay if not purchased
            if (!currentSave.isUnlocked && selectedAircraftSpec.unlockCostCredits > 0L) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.65f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = AeroAmber,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { viewModel.unlockAircraft(selectedAircraftSpec) },
                            colors = ButtonDefaults.buttonColors(containerColor = AeroCyan, contentColor = DarkVoid),
                            modifier = Modifier.testTag("unlock_aircraft_button")
                        ) {
                            Text(
                                "UNLOCK FOR ${selectedAircraftSpec.unlockCostCredits} CR + ${selectedAircraftSpec.unlockCostCores} CORES",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hangar Customization Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("OVERVIEW", "UPGRADES", "WEAPONS", "PAINT", "EXHAUST").forEach { tab ->
                val active = currentTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) AeroCyan else DarkSurfaceElevated)
                        .clickable { currentTab = tab }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (active) DarkVoid else TextSecondary,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Content
        when (currentTab) {
            "OVERVIEW" -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = selectedAircraftSpec.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = selectedAircraftSpec.role,
                            style = MaterialTheme.typography.labelSmall,
                            color = AeroCyan
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = selectedAircraftSpec.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Aircraft Stats Comparison
                        StatRow("HULL INTEGRITY", "${selectedAircraftSpec.baseHealth.toInt()}", selectedAircraftSpec.baseHealth / 1600f, HullGreen)
                        StatRow("SHIELD STRENGTH", "${selectedAircraftSpec.baseShield.toInt()}", selectedAircraftSpec.baseShield / 1200f, ShieldBlue)
                        StatRow("TOP SPEED", "${selectedAircraftSpec.baseSpeed.toInt()} km/h", selectedAircraftSpec.baseSpeed / 700f, AeroAmber)
                        StatRow("MANEUVERABILITY", "${(selectedAircraftSpec.baseHandling * 100).toInt()}%", selectedAircraftSpec.baseHandling, AeroCyan)
                        StatRow("CRITICAL RATE", "${(selectedAircraftSpec.baseCritChance * 100).toInt()}%", selectedAircraftSpec.baseCritChance / 0.35f, AeroViolet)
                    }
                }
            }
            "UPGRADES" -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("SYSTEM UPGRADE MATRIX", style = MaterialTheme.typography.labelLarge, color = AeroCyan)

                        UpgradeItem("Engine & Afterburners", currentSave.engineUpgradeLevel, "engine", profile.credits, viewModel, selectedAircraftSpec.id)
                        UpgradeItem("Kinetic Shield Generator", currentSave.shieldUpgradeLevel, "shield", profile.credits, viewModel, selectedAircraftSpec.id)
                        UpgradeItem("Reinforced Armor Plating", currentSave.armorUpgradeLevel, "armor", profile.credits, viewModel, selectedAircraftSpec.id)
                        UpgradeItem("Weapon Cooling & Overdrive", currentSave.weaponUpgradeLevel, "weapon", profile.credits, viewModel, selectedAircraftSpec.id)
                        UpgradeItem("Avionics & Radar Array", currentSave.avionicsUpgradeLevel, "avionics", profile.credits, viewModel, selectedAircraftSpec.id)
                    }
                }
            }
            "WEAPONS" -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("LOADOUT MOUNTINGS", style = MaterialTheme.typography.labelLarge, color = AeroCyan)

                        // Primary Selector
                        Text("PRIMARY WEAPON", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(WeaponCatalog.ALL_PRIMARY) { wp ->
                                val equipped = wp.id == currentPrimary.id
                                Button(
                                    onClick = { viewModel.equipCustomization(selectedAircraftSpec.id, primaryId = wp.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (equipped) AeroCyan else DarkSurfaceElevated,
                                        contentColor = if (equipped) DarkVoid else Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(wp.name, fontSize = 12.sp)
                                }
                            }
                        }

                        // Secondary Selector
                        Text("SECONDARY WEAPON", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(WeaponCatalog.ALL_SECONDARY) { wp ->
                                val equipped = wp.id == currentSecondary.id
                                Button(
                                    onClick = { viewModel.equipCustomization(selectedAircraftSpec.id, secondaryId = wp.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (equipped) AeroCrimson else DarkSurfaceElevated,
                                        contentColor = if (equipped) Color.White else Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(wp.name, fontSize = 12.sp)
                                }
                            }
                        }

                        // Special Ability Selector
                        Text("TACTICAL SPECIAL ABILITY", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(WeaponCatalog.ALL_SPECIAL) { wp ->
                                val equipped = wp.id == currentSpecial.id
                                Button(
                                    onClick = { viewModel.equipCustomization(selectedAircraftSpec.id, specialId = wp.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (equipped) AeroViolet else DarkSurfaceElevated,
                                        contentColor = if (equipped) Color.White else Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(wp.name, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
            "PAINT" -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("AIRCRAFT LIVERY & FINISH", style = MaterialTheme.typography.labelLarge, color = AeroCyan)
                        PaintCatalog.ALL.forEach { p ->
                            val equipped = p.id == currentPaint.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (equipped) DarkSurfaceElevated else Color.Transparent)
                                    .border(1.dp, if (equipped) AeroCyan else DarkSurfaceBorder, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.equipCustomization(selectedAircraftSpec.id, paintId = p.id) }
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(p.bodyColor)
                                            .border(2.dp, p.trimColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(p.name, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                }
                                if (equipped) {
                                    Text("EQUIPPED", color = AeroCyan, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
            "EXHAUST" -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("AFTERBURNER PLUME COLOR", style = MaterialTheme.typography.labelLarge, color = AeroCyan)
                        ExhaustCatalog.ALL.forEach { ex ->
                            val equipped = ex.id == currentExhaust.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (equipped) DarkSurfaceElevated else Color.Transparent)
                                    .border(1.dp, if (equipped) AeroOrange else DarkSurfaceBorder, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.equipCustomization(selectedAircraftSpec.id, exhaustId = ex.id) }
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(ex.coreColor)
                                            .border(2.dp, ex.outerColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(ex.name, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                }
                                if (equipped) {
                                    Text("ACTIVE", color = AeroOrange, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Launch Sortie Button
        Button(
            onClick = {
                // Initialize engine for mission
                val upgrades = mapOf(
                    "engine" to currentSave.engineUpgradeLevel,
                    "weapon" to currentSave.weaponUpgradeLevel,
                    "armor" to currentSave.armorUpgradeLevel,
                    "shield" to currentSave.shieldUpgradeLevel,
                    "avionics" to currentSave.avionicsUpgradeLevel
                )
                viewModel.gameEngine.startMission(
                    aircraft = selectedAircraftSpec,
                    primary = currentPrimary,
                    secondary = currentSecondary,
                    special = currentSpecial,
                    biome = BiomeCatalog.NEO_TOKYO,
                    paint = currentPaint,
                    exhaust = currentExhaust,
                    screenWidth = 1080f,
                    screenHeight = 2160f,
                    savedUpgrades = upgrades
                )
                onLaunchMission()
            },
            enabled = currentSave.isUnlocked,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("launch_sortie_button"),
            colors = ButtonDefaults.buttonColors(containerColor = AeroCyan, contentColor = DarkVoid),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.FlightTakeoff, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "LAUNCH COMBAT SORTIE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun StatRow(label: String, valueStr: String, ratio: Float, barColor: Color) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            Text(valueStr, color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(DarkSurfaceBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(barColor)
            )
        }
    }
}

@Composable
fun UpgradeItem(
    title: String,
    level: Int,
    attrKey: String,
    playerCredits: Long,
    viewModel: GameViewModel,
    aircraftId: String
) {
    val cost = (level + 1) * 600L
    val canAfford = playerCredits >= cost && level < 10

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceElevated)
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Text(
                "LEVEL $level / 10",
                color = AeroCyan,
                style = MaterialTheme.typography.labelSmall
            )
        }

        if (level < 10) {
            Button(
                onClick = { viewModel.upgradeAircraftAttribute(aircraftId, attrKey) },
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AeroCyan,
                    contentColor = DarkVoid,
                    disabledContainerColor = DarkSurfaceBorder
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("$cost CR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Text("MAXED", color = AeroEmerald, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}
