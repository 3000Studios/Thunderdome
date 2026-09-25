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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

@Composable
fun SettingsScreen(viewModel: GameViewModel) {
    val settings by viewModel.settings.collectAsState()
    val profile by viewModel.playerProfile.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "SYSTEM CONFIGURATION // HARDWARE",
            style = MaterialTheme.typography.titleMedium,
            color = AeroCyan
        )
        Text(
            text = "Graphics pipeline, device profiles, haptics & telemetry",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Graphics Scalability Profiles
        Text("GRAPHICS PROFILE (VULKAN RENDERING)", style = MaterialTheme.typography.labelLarge, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ULTRA", "HIGH", "MEDIUM", "LOW").forEach { preset ->
                val active = settings.graphicsPreset == preset
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) AeroCyan else DarkSurfaceElevated)
                        .clickable { viewModel.updateSettings(preset = preset) }
                        .padding(vertical = 10.dp)
                        .testTag("graphics_preset_$preset"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = preset,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (active) DarkVoid else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Target Framerate
        Text("TARGET REFRESH RATE", style = MaterialTheme.typography.labelLarge, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(60, 120).forEach { fps ->
                val active = settings.targetFps == fps
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) AeroCyan else DarkSurfaceElevated)
                        .clickable { viewModel.updateSettings(fps = fps) }
                        .padding(vertical = 10.dp)
                        .testTag("fps_setting_$fps"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$fps FPS",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (active) DarkVoid else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Control Scheme
        Text("FLIGHT CONTROL SCHEME", style = MaterialTheme.typography.labelLarge, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "JOYSTICK" to "Virtual Stick",
                "TOUCH_FOLLOW" to "Touch-Follow"
            ).forEach { (scheme, title) ->
                val active = settings.controlScheme == scheme
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) AeroCyan else DarkSurfaceElevated)
                        .clickable { viewModel.updateSettings(scheme = scheme) }
                        .padding(vertical = 10.dp)
                        .testTag("control_scheme_$scheme"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (active) DarkVoid else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Audio & Haptics Toggles
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Android Haptic Vibration", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        Text("Weapon recoil, Boost shake, Explosions", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    }
                    Switch(
                        checked = settings.hapticsEnabled,
                        onCheckedChange = { viewModel.updateSettings(haptics = it) },
                        modifier = Modifier.testTag("haptics_switch")
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sound Effects Volume", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        Text("${(settings.sfxVolume * 100).toInt()}%", color = AeroCyan, style = MaterialTheme.typography.labelSmall)
                    }
                    Slider(
                        value = settings.sfxVolume,
                        onValueChange = { viewModel.updateSettings(sfx = it) },
                        modifier = Modifier.fillMaxWidth().testTag("sfx_volume_slider")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Pilot Career Statistics
        Text("PILOT FLIGHT RECORD & TELEMETRY", style = MaterialTheme.typography.labelLarge, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pilot Callsign:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text(profile.callsign, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Career High Score:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("${profile.highScore}", color = Color(0xFFFFD700), style = MaterialTheme.typography.titleMedium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Hostiles Destroyed:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("${profile.totalKills}", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Capital Bosses Defeated:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("${profile.bossesDefeated}", color = DangerRed, style = MaterialTheme.typography.bodyMedium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Sorties Flown:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("${profile.missionsCompleted}", color = AeroCyan, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
