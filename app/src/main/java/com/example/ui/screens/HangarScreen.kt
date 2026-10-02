package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AircraftSaveEntity
import com.example.game.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Animated starfield particles
private data class StarParticle(
    val x: Float, val y: Float,
    val speed: Float, val size: Float,
    val alpha: Float, val hue: Float
)

private fun generateStars(count: Int = 100): List<StarParticle> {
    val rng = Random(42)
    return List(count) {
        StarParticle(
            x = rng.nextFloat(), y = rng.nextFloat(),
            speed = 0.15f + rng.nextFloat() * 0.85f,
            size = 0.8f + rng.nextFloat() * 2.5f,
            alpha = 0.15f + rng.nextFloat() * 0.65f,
            hue = rng.nextFloat()
        )
    }
}

private fun starColor(hue: Float): Color = when {
    hue < 0.35f -> AeroCyan.copy(alpha = 0.7f)
    hue < 0.6f  -> ShieldBlue.copy(alpha = 0.5f)
    hue < 0.85f -> AeroViolet.copy(alpha = 0.4f)
    else        -> AeroAmber.copy(alpha = 0.35f)
}

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
        ?: AircraftSaveEntity(
            aircraftId = selectedAircraftSpec.id,
            specialAbilityId = selectedAircraftSpec.defaultSpecialAbilityId
        )

    val currentPaint = PaintCatalog.getById(currentSave.paintSchemeId)
    val currentExhaust = ExhaustCatalog.getById(currentSave.exhaustColorId)
    val currentPrimary = WeaponCatalog.getById(currentSave.primaryWeaponId)
    val currentSecondary = WeaponCatalog.getById(currentSave.secondaryWeaponId)
    val currentSpecial = WeaponCatalog.getById(currentSave.specialAbilityId)

    var currentTab by remember { mutableStateOf("OVERVIEW") }
    val isCraftUnlocked = currentSave.isUnlocked || (selectedAircraftSpec.unlockCostCredits == 0L)

    // Animated starfield
    val stars = remember { generateStars() }
    val infiniteTransition = rememberInfiniteTransition(label = "hangar_bg")
    val starDrift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "star_drift"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow_pulse"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // ANIMATED LIVE WALLPAPER BACKGROUND
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF030610), Color(0xFF0A0F25), Color(0xFF050B1A), Color(0xFF020408))
            ))
            val nebulaAlpha = 0.04f + glowPulse * 0.03f
            drawCircle(
                brush = Brush.radialGradient(listOf(AeroCyan.copy(alpha = nebulaAlpha), Color.Transparent), radius = size.width * 0.6f),
                center = Offset(size.width * 0.2f, size.height * 0.15f), radius = size.width * 0.5f
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(AeroViolet.copy(alpha = nebulaAlpha * 0.7f), Color.Transparent), radius = size.width * 0.5f),
                center = Offset(size.width * 0.85f, size.height * 0.7f), radius = size.width * 0.45f
            )
            stars.forEach { star ->
                val dy = ((star.y + starDrift * star.speed) % 1.05f)
                val twinkle = (0.5f + 0.5f * sin((starDrift * 6.283f * star.speed + star.x * 10f).toDouble())).toFloat()
                drawCircle(
                    color = starColor(star.hue).copy(alpha = star.alpha * twinkle),
                    radius = star.size, center = Offset(star.x * size.width, dy * size.height)
                )
            }
            val gridAlpha = 0.015f + glowPulse * 0.01f
            for (i in 0 until (size.height / 60f).toInt()) {
                drawLine(AeroCyan.copy(alpha = gridAlpha), Offset(0f, i * 60f), Offset(size.width, i * 60f), 0.5f)
            }
        }

        // SCROLLABLE CONTENT
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // TOP RESOURCE BAR
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface.copy(alpha = 0.7f)).padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(profile.callsign, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("LEVEL ${profile.level} PILOT", style = MaterialTheme.typography.labelSmall, color = AeroCyan, letterSpacing = 1.5.sp)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(DarkSurfaceElevated.copy(alpha = 0.6f)).padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.MonetizationOn, "Credits", tint = AeroEmerald, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${profile.credits}", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(DarkSurfaceElevated.copy(alpha = 0.6f)).padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Diamond, "Plasma Cores", tint = AeroViolet, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${profile.plasmaCores}", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.openStoreModal() },
                        colors = ButtonDefaults.buttonColors(containerColor = AeroAmber, contentColor = DarkVoid),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp).testTag("hangar_store_button")
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Store", modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("STORE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 24 Warbirds Aircraft Tactical Roster Banner
            var showHangarRoster by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceElevated.copy(alpha = 0.8f))
                    .border(1.dp, AeroCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .clickable { showHangarRoster = !showHangarRoster }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = AeroCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "TACTICAL AIRCRAFT ROSTER // 24 WARBIRDS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        if (showHangarRoster) "HIDE" else "VIEW ROSTER",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = AeroCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(visible = showHangarRoster) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, AeroCyan.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.thunder_dome_24_planes_roster),
                        contentDescription = "24 Aircraft Roster Concept Art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color.Transparent, DarkVoid.copy(alpha = 0.7f))))
                    )
                    Text(
                        text = "3000 STUDIOS // OFFICIAL 24 FIGHTER SPEC SHEET",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = AeroCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // AIRCRAFT SELECTOR with visible plane previews
            Text("SELECT WARBIRD", style = MaterialTheme.typography.labelSmall, color = TextSecondary, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 6.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(end = 8.dp)
            ) {
                items(AircraftCatalog.ALL_AIRCRAFT) { spec ->
                    val isSelected = spec.id == selectedAircraftSpec.id
                    val craftSave = allSaves.find { it.aircraftId == spec.id }
                    val isUnlocked = craftSave?.isUnlocked ?: (spec.unlockCostCredits == 0L)

                    Card(
                        modifier = Modifier.width(140.dp).height(140.dp)
                            .clickable { selectedAircraftSpec = spec; if (isUnlocked) viewModel.selectAircraft(spec.id) }
                            .testTag("aircraft_select_${spec.id}"),
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) DarkSurfaceElevated.copy(alpha = 0.9f) else DarkSurface.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) AeroCyan else if (!isUnlocked) DangerRed.copy(alpha = 0.3f) else DarkSurfaceBorder
                        )
                    ) {
                        Column(Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Canvas(Modifier.fillMaxWidth().height(72.dp)) {
                                drawMiniAircraft(spec, isUnlocked, isSelected)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(spec.name, style = MaterialTheme.typography.labelMedium, color = if (isSelected) AeroCyan else Color.White,
                                maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                            Text(if (isUnlocked) spec.role else "\uD83D\uDD12 LOCKED",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = if (isUnlocked) AeroEmerald else DangerRed, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // TURNTABLE PREVIEW
            TurntablePreview(spec = selectedAircraftSpec, paint = currentPaint, exhaust = currentExhaust,
                isUnlocked = isCraftUnlocked, onUnlock = { viewModel.unlockAircraft(selectedAircraftSpec) })

            Spacer(Modifier.height(14.dp))

            // CUSTOMIZATION TABS
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(DarkSurface.copy(alpha = 0.5f)).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("OVERVIEW", "UPGRADES", "WEAPONS", "PAINT", "EXHAUST").forEach { tab ->
                    val active = currentTab == tab
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                            .background(if (active) AeroCyan else Color.Transparent)
                            .clickable { currentTab = tab }.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (tab == "OVERVIEW") "INFO" else tab,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = if (active) DarkVoid else TextSecondary,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // TAB CONTENT
            when (currentTab) {
                "OVERVIEW" -> {
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.75f)),
                        shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)) {
                        Column(Modifier.padding(14.dp)) {
                            Text(selectedAircraftSpec.name, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(selectedAircraftSpec.role, style = MaterialTheme.typography.labelSmall, color = AeroCyan, letterSpacing = 1.sp)
                            if (selectedAircraftSpec.abilityName.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = AeroAmber, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(selectedAircraftSpec.abilityName, style = MaterialTheme.typography.labelMedium, color = AeroAmber, fontWeight = FontWeight.Bold)
                                }
                                Text(selectedAircraftSpec.abilityDescription, style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(selectedAircraftSpec.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Spacer(Modifier.height(14.dp))
                            StatRow("HULL INTEGRITY", "${selectedAircraftSpec.baseHealth.toInt()}", selectedAircraftSpec.baseHealth / 1600f, HullGreen)
                            StatRow("SHIELD STRENGTH", "${selectedAircraftSpec.baseShield.toInt()}", selectedAircraftSpec.baseShield / 1200f, ShieldBlue)
                            StatRow("TOP SPEED", "${selectedAircraftSpec.baseSpeed.toInt()} km/h", selectedAircraftSpec.baseSpeed / 700f, AeroAmber)
                            StatRow("MANEUVERABILITY", "${(selectedAircraftSpec.baseHandling * 100).toInt()}%", selectedAircraftSpec.baseHandling, AeroCyan)
                            StatRow("CRITICAL RATE", "${(selectedAircraftSpec.baseCritChance * 100).toInt()}%", selectedAircraftSpec.baseCritChance / 0.35f, AeroViolet)
                        }
                    }
                }
                "UPGRADES" -> {
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.75f)),
                        shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.75f)),
                        shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("LOADOUT MOUNTINGS", style = MaterialTheme.typography.labelLarge, color = AeroCyan)
                            Text("PRIMARY WEAPON", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(WeaponCatalog.ALL_PRIMARY) { wp ->
                                    val eq = wp.id == currentPrimary.id
                                    Button(onClick = { viewModel.equipCustomization(selectedAircraftSpec.id, primaryId = wp.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (eq) AeroCyan else DarkSurfaceElevated, contentColor = if (eq) DarkVoid else Color.White),
                                        shape = RoundedCornerShape(8.dp)) { Text(wp.name, fontSize = 12.sp) }
                                }
                            }
                            Text("SECONDARY WEAPON", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(WeaponCatalog.ALL_SECONDARY) { wp ->
                                    val eq = wp.id == currentSecondary.id
                                    Button(onClick = { viewModel.equipCustomization(selectedAircraftSpec.id, secondaryId = wp.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (eq) AeroCrimson else DarkSurfaceElevated, contentColor = Color.White),
                                        shape = RoundedCornerShape(8.dp)) { Text(wp.name, fontSize = 12.sp) }
                                }
                            }
                            Text("TACTICAL SPECIAL ABILITY", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(WeaponCatalog.ALL_SPECIAL) { wp ->
                                    val eq = wp.id == currentSpecial.id
                                    Button(onClick = { viewModel.equipCustomization(selectedAircraftSpec.id, specialId = wp.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (eq) AeroViolet else DarkSurfaceElevated, contentColor = Color.White),
                                        shape = RoundedCornerShape(8.dp)) { Text(wp.name, fontSize = 12.sp) }
                                }
                            }
                        }
                    }
                }
                "PAINT" -> {
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.75f)),
                        shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("AIRCRAFT LIVERY & FINISH", style = MaterialTheme.typography.labelLarge, color = AeroCyan)
                            PaintCatalog.ALL.forEach { p ->
                                val eq = p.id == currentPaint.id
                                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                    .background(if (eq) DarkSurfaceElevated else Color.Transparent)
                                    .border(1.dp, if (eq) AeroCyan else DarkSurfaceBorder, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.equipCustomization(selectedAircraftSpec.id, paintId = p.id) }.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(24.dp).clip(CircleShape).background(p.bodyColor).border(2.dp, p.trimColor, CircleShape))
                                        Spacer(Modifier.width(10.dp))
                                        Text(p.name, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    if (eq) Text("EQUIPPED", color = AeroCyan, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
                "EXHAUST" -> {
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.75f)),
                        shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("AFTERBURNER PLUME COLOR", style = MaterialTheme.typography.labelLarge, color = AeroCyan)
                            ExhaustCatalog.ALL.forEach { ex ->
                                val eq = ex.id == currentExhaust.id
                                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                    .background(if (eq) DarkSurfaceElevated else Color.Transparent)
                                    .border(1.dp, if (eq) AeroOrange else DarkSurfaceBorder, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.equipCustomization(selectedAircraftSpec.id, exhaustId = ex.id) }.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(24.dp).clip(CircleShape).background(ex.coreColor).border(2.dp, ex.outerColor, CircleShape))
                                        Spacer(Modifier.width(10.dp))
                                        Text(ex.name, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    if (eq) Text("ACTIVE", color = AeroOrange, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // LAUNCH BUTTON
            Button(
                onClick = {
                    val upgrades = mapOf("engine" to currentSave.engineUpgradeLevel, "weapon" to currentSave.weaponUpgradeLevel,
                        "armor" to currentSave.armorUpgradeLevel, "shield" to currentSave.shieldUpgradeLevel, "avionics" to currentSave.avionicsUpgradeLevel)
                    viewModel.gameEngine.startMission(aircraft = selectedAircraftSpec, primary = currentPrimary, secondary = currentSecondary,
                        special = currentSpecial, biome = BiomeCatalog.NEO_TOKYO, paint = currentPaint, exhaust = currentExhaust,
                        screenWidth = 1080f, screenHeight = 2160f, savedUpgrades = upgrades)
                    onLaunchMission()
                },
                enabled = isCraftUnlocked,
                modifier = Modifier.fillMaxWidth().height(56.dp).testTag("launch_sortie_button"),
                colors = ButtonDefaults.buttonColors(containerColor = AeroCyan, contentColor = DarkVoid,
                    disabledContainerColor = DarkSurfaceBorder, disabledContentColor = TextMuted),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.FlightTakeoff, null, Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("LAUNCH COMBAT SORTIE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// Draw unique mini aircraft silhouette per spec
private fun DrawScope.drawMiniAircraft(spec: AircraftSpec, isUnlocked: Boolean, isSelected: Boolean) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.5f
    val hash = spec.id.hashCode()
    val wingSpan = 0.55f + (abs(hash % 30) / 100f)
    val noseLen = 0.30f + (abs(hash % 20) / 100f)
    val tailWidth = 0.15f + (abs(hash % 15) / 100f)
    val swept = (hash and 0x4) != 0

    val bodyColor = if (isUnlocked) spec.primaryColor else Color.Gray
    val trimColor = if (isUnlocked) spec.accentColor else DarkSurfaceBorder

    val w = size.width * wingSpan
    val n = size.height * noseLen
    val tw = size.width * tailWidth

    val path = Path().apply {
        moveTo(cx, cy - n)
        if (swept) {
            lineTo(cx + 8f, cy - n * 0.3f); lineTo(cx + w * 0.5f, cy + 2f)
            lineTo(cx + w * 0.3f, cy + n * 0.4f)
        } else {
            lineTo(cx + 6f, cy - n * 0.4f); lineTo(cx + w * 0.5f, cy - n * 0.05f)
            lineTo(cx + w * 0.45f, cy + n * 0.1f); lineTo(cx + 8f, cy + n * 0.15f)
        }
        lineTo(cx + tw, cy + n * 0.7f); lineTo(cx + tw * 1.3f, cy + n * 0.85f)
        lineTo(cx + 4f, cy + n * 0.6f); lineTo(cx, cy + n * 0.65f)
        lineTo(cx - 4f, cy + n * 0.6f); lineTo(cx - tw * 1.3f, cy + n * 0.85f)
        lineTo(cx - tw, cy + n * 0.7f)
        if (swept) {
            lineTo(cx - w * 0.3f, cy + n * 0.4f); lineTo(cx - w * 0.5f, cy + 2f)
            lineTo(cx - 8f, cy - n * 0.3f)
        } else {
            lineTo(cx - 8f, cy + n * 0.15f); lineTo(cx - w * 0.45f, cy + n * 0.1f)
            lineTo(cx - w * 0.5f, cy - n * 0.05f); lineTo(cx - 6f, cy - n * 0.4f)
        }
        close()
    }

    if (isSelected) {
        drawCircle(brush = Brush.radialGradient(listOf(trimColor.copy(alpha = 0.2f), Color.Transparent), radius = size.width * 0.5f),
            center = Offset(cx, cy), radius = size.width * 0.45f)
    }
    drawPath(path, color = bodyColor)
    drawPath(path, color = trimColor, style = Stroke(width = 1.5f))
    drawCircle(color = if (isUnlocked) AeroAmber.copy(alpha = 0.7f) else Color.Gray.copy(alpha = 0.3f), radius = 4f, center = Offset(cx, cy + n * 0.55f))
    if (!isUnlocked) drawCircle(Color.Black.copy(alpha = 0.4f), size.width * 0.35f, Offset(cx, cy))
}

@Composable
fun StatRow(label: String, valueStr: String, ratio: Float, barColor: Color) {
    Column(Modifier.padding(vertical = 3.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            Text(valueStr, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(3.dp))
        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(DarkSurfaceBorder)) {
            Box(Modifier.fillMaxWidth(ratio.coerceIn(0f, 1f)).fillMaxHeight()
                .background(Brush.horizontalGradient(listOf(barColor.copy(alpha = 0.6f), barColor))))
        }
    }
}

@Composable
fun UpgradeItem(title: String, level: Int, attrKey: String, playerCredits: Long, viewModel: GameViewModel, aircraftId: String) {
    val cost = (level + 1) * 600L
    val canAfford = playerCredits >= cost && level < 10
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(DarkSurfaceElevated.copy(alpha = 0.7f)).padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Text("LEVEL $level / 10", color = AeroCyan, style = MaterialTheme.typography.labelSmall)
        }
        if (level < 10) {
            Button(onClick = { viewModel.upgradeAircraftAttribute(aircraftId, attrKey) }, enabled = canAfford,
                colors = ButtonDefaults.buttonColors(containerColor = AeroCyan, contentColor = DarkVoid, disabledContainerColor = DarkSurfaceBorder),
                shape = RoundedCornerShape(6.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                Text("$cost CR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Text("MAXED", color = AeroEmerald, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TurntablePreview(spec: AircraftSpec, paint: PaintScheme, exhaust: ExhaustFlame, isUnlocked: Boolean, onUnlock: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "turntable_preview")
    val turntableRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart), label = "turntable_rotation"
    )
    val enginePulse by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "engine_pulse"
    )

    Box(
        modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp))
            .background(Brush.radialGradient(listOf(DarkSurfaceElevated.copy(alpha = 0.8f), DarkSurface.copy(alpha = 0.6f), Color(0xFF030610)), radius = 500f))
            .border(1.dp, DarkSurfaceBorder.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width * 0.5f; val cy = size.height * 0.55f

            // Holographic 3D Turntable Platform
            drawOval(DarkSurfaceBorder.copy(alpha = 0.8f), Offset(cx - 150f, cy - 38f), androidx.compose.ui.geometry.Size(300f, 76f), style = Stroke(2f))
            drawOval(AeroCyan.copy(alpha = 0.35f), Offset(cx - 105f, cy - 27f), androidx.compose.ui.geometry.Size(210f, 54f), style = Stroke(1.2f))
            // Crosshair Azimuth Grid Lines
            drawLine(AeroCyan.copy(alpha = 0.25f), Offset(cx - 150f, cy), Offset(cx + 150f, cy), 1f)
            drawLine(AeroCyan.copy(alpha = 0.25f), Offset(cx, cy - 38f), Offset(cx, cy + 38f), 1f)
            // Sweeping Holographic Laser Scanner
            val scanY = cy - 38f + ((turntableRotation * 2.5f) % 76f)
            drawLine(
                brush = Brush.horizontalGradient(listOf(Color.Transparent, AeroCyan.copy(alpha = 0.6f), Color.White, AeroCyan.copy(alpha = 0.6f), Color.Transparent)),
                start = Offset(cx - 120f, scanY),
                end = Offset(cx + 120f, scanY),
                strokeWidth = 2f
            )

            val angleRad = (turntableRotation * PI / 180f).toFloat()
            val scaleX = cos(angleRad).coerceIn(-1f, 1f)
            val hash = spec.id.hashCode()
            val wingSpread = 80f + (abs(hash % 30)); val noseLen = 60f + (abs(hash % 20))
            val swept = (hash and 0x4) != 0

            val jetPath = Path().apply {
                moveTo(cx, cy - noseLen)
                if (swept) {
                    lineTo(cx + 12f * scaleX, cy - noseLen * 0.3f); lineTo(cx + wingSpread * scaleX, cy + 5f)
                    lineTo(cx + wingSpread * 0.4f * scaleX, cy + 20f); lineTo(cx + 30f * scaleX, cy + 35f)
                    lineTo(cx + 40f * scaleX, cy + 45f); lineTo(cx + 6f * scaleX, cy + 35f)
                    lineTo(cx, cy + 30f); lineTo(cx - 6f * scaleX, cy + 35f)
                    lineTo(cx - 40f * scaleX, cy + 45f); lineTo(cx - 30f * scaleX, cy + 35f)
                    lineTo(cx - wingSpread * 0.4f * scaleX, cy + 20f); lineTo(cx - wingSpread * scaleX, cy + 5f)
                    lineTo(cx - 12f * scaleX, cy - noseLen * 0.3f)
                } else {
                    lineTo(cx + 10f * scaleX, cy - noseLen * 0.4f); lineTo(cx + wingSpread * scaleX, cy - 5f)
                    lineTo(cx + wingSpread * 0.9f * scaleX, cy + 8f); lineTo(cx + 12f * scaleX, cy + 12f)
                    lineTo(cx + 25f * scaleX, cy + 38f); lineTo(cx + 35f * scaleX, cy + 45f)
                    lineTo(cx + 6f * scaleX, cy + 32f); lineTo(cx, cy + 30f)
                    lineTo(cx - 6f * scaleX, cy + 32f); lineTo(cx - 35f * scaleX, cy + 45f)
                    lineTo(cx - 25f * scaleX, cy + 38f); lineTo(cx - 12f * scaleX, cy + 12f)
                    lineTo(cx - wingSpread * 0.9f * scaleX, cy + 8f); lineTo(cx - wingSpread * scaleX, cy - 5f)
                    lineTo(cx - 10f * scaleX, cy - noseLen * 0.4f)
                }
                close()
            }
            // 3D Metallic Specular Body Shading
            val metallicBrush = Brush.linearGradient(
                colors = listOf(
                    paint.bodyColor,
                    Color.White.copy(alpha = 0.35f),
                    paint.bodyColor,
                    paint.bodyColor.copy(alpha = 0.8f)
                ),
                start = Offset(cx - wingSpread * scaleX, cy - noseLen),
                end = Offset(cx + wingSpread * scaleX, cy + 30f)
            )
            drawPath(jetPath, brush = metallicBrush)
            // Emissive Edge Glow & Outline
            drawPath(jetPath, color = paint.trimColor.copy(alpha = 0.35f), style = Stroke(4f))
            drawPath(jetPath, color = paint.trimColor, style = Stroke(1.8f))
            // 3D Glass Canopy with Specular Glint
            drawOval(
                brush = Brush.verticalGradient(listOf(Color.White, AeroCyan, Color(0xFF0284C7))),
                topLeft = Offset(cx - 6f * abs(scaleX), cy - noseLen * 0.5f),
                size = androidx.compose.ui.geometry.Size(12f * abs(scaleX), 16f)
            )
            // Multi-Stage Pulsing Afterburners
            val er = 22f * enginePulse
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        Color.White,
                        exhaust.coreColor.copy(alpha = 0.95f * enginePulse),
                        exhaust.outerColor.copy(alpha = 0.5f * enginePulse),
                        Color.Transparent
                    ),
                    radius = er * 2.2f
                ),
                radius = er * 2.2f,
                center = Offset(cx, cy + 38f)
            )
            drawCircle(Color.White, 7f * enginePulse, Offset(cx, cy + 38f))
        }

        // Name plate
        Box(Modifier.align(Alignment.TopStart).padding(12.dp)) {
            Column {
                Text(spec.name.uppercase(), style = MaterialTheme.typography.labelMedium, color = AeroCyan, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text(spec.role, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextSecondary, letterSpacing = 1.sp)
            }
        }

        // Lock overlay
        if (!isUnlocked && spec.unlockCostCredits > 0L) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.65f)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Lock, "Locked", tint = AeroAmber, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(6.dp))
                    Button(onClick = onUnlock, colors = ButtonDefaults.buttonColors(containerColor = AeroCyan, contentColor = DarkVoid),
                        modifier = Modifier.testTag("unlock_aircraft_button")) {
                        Text("UNLOCK FOR ${spec.unlockCostCredits} CR + ${spec.unlockCostCores} CORES", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
