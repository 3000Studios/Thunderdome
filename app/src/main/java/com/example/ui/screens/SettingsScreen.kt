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
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Button(
            onClick = { viewModel.optimizeAllSettingsToBest() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("optimize_all_settings_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = AeroCyan,
                contentColor = DarkVoid
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "APPLY BEST POSSIBLE FIDELITY & FPS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }


        Text("AUDIO & FLIGHT INPUT", style = MaterialTheme.typography.labelLarge, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Sound", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = settings.soundEnabled,
                onCheckedChange = { viewModel.updateSettings(soundEnabled = it) },
                modifier = Modifier.testTag("sound_enabled_switch")
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Swipe Flight Control", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Text("Disable to prevent accidental movement", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            }
            Switch(
                checked = settings.touchInputEnabled,
                onCheckedChange = { viewModel.updateSettings(touchInputEnabled = it) },
                modifier = Modifier.testTag("touch_input_switch")
            )
        }
        // Steering Scheme Selector
        Text("FLIGHT CONTROL SCHEME", style = MaterialTheme.typography.labelMedium, color = Color.White)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "TOUCH_FOLLOW" to "DIRECT OFFSET",
                "RELATIVE_DRAG" to "RELATIVE SWIPE",
                "JOYSTICK" to "JOYSTICK"
            ).forEach { (sch, label) ->
                val active = settings.controlScheme == sch
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) AeroCyan else DarkSurfaceElevated)
                        .clickable { viewModel.updateSettings(scheme = sch) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (active) DarkVoid else TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PLANE FINGER LEAD DISTANCE", color = Color.White, style = MaterialTheme.typography.labelMedium)
                Text(
                    text = "${settings.touchOffsetY.toInt()} dp (${if (settings.touchOffsetY > 0) "${settings.touchOffsetY.toInt()}dp in front" else if (settings.touchOffsetY == 0f) "Direct Center" else "${-settings.touchOffsetY.toInt()}dp behind"})",
                    color = AeroCyan,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Controls how far in front/above your finger the plane flies so your finger never blocks the aircraft or combat view (matches Balloons Pilot control standard).",
                color = TextSecondary,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
            )

            // Preset Quick Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    0f to "DIRECT (0)",
                    40f to "CLOSE (40)",
                    75f to "STANDARD (75)",
                    120f to "EXTENDED (120)",
                    180f to "BALLOON (180)"
                ).forEach { (offsetVal, label) ->
                    val isCurrent = kotlin.math.abs(settings.touchOffsetY - offsetVal) < 8f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCurrent) AeroCyan else DarkSurfaceElevated)
                            .clickable { viewModel.updateSettings(touchOffsetY = offsetVal) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = if (isCurrent) DarkVoid else TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Slider(
                value = settings.touchOffsetY,
                onValueChange = { viewModel.updateSettings(touchOffsetY = it) },
                valueRange = -20f..220f,
                modifier = Modifier.fillMaxWidth().testTag("plane_position_slider")
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Touch Flight Sensitivity", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            Text("${(settings.touchSensitivity * 100).toInt()}%", color = AeroCyan, style = MaterialTheme.typography.labelSmall)
        }
        Slider(
            value = settings.touchSensitivity,
            onValueChange = { viewModel.updateSettings(sensitivity = it) },
            valueRange = 0.5f..2.5f,
            modifier = Modifier.fillMaxWidth().testTag("touch_sensitivity_slider")
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Graphics Scalability Profiles
        Text("GRAPHICS PROFILE (HARDWARE 120HZ CANVAS)", style = MaterialTheme.typography.labelLarge, color = Color.White)
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
                "TOUCH_FOLLOW" to "Finger Tracking (Direct)",
                "JOYSTICK" to "Virtual Stick"
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

        // Combat Camera View Perspective
        Text("COMBAT CAMERA VIEW PERSPECTIVE", style = MaterialTheme.typography.labelLarge, color = Color.White)
        Text("Switch between 1st-person cockpit, 3rd-person follow, or tactical top-down", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "FOLLOW_3RD" to "🚀 3rd Follow",
                "COCKPIT_1ST" to "👁️ 1st Cockpit",
                "TOP_DOWN_CHASE" to "🛰️ Top-Down"
            ).forEach { (mode, title) ->
                val active = settings.cameraViewMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) AeroCyan else DarkSurfaceElevated)
                        .clickable { viewModel.setCameraViewMode(mode) }
                        .padding(vertical = 10.dp)
                        .testTag("camera_mode_$mode"),
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

        // Combat Screen Viewport Scaling
        Text("COMBAT SCREEN VIEWPORT SCALING", style = MaterialTheme.typography.labelLarge, color = Color.White)
        Text("Scale combat arena size and field of view", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "MAX_IMMERSIVE" to "115% Max (Ultra-Wide)",
                "STANDARD" to "100% Standard",
                "COMPACT" to "90% Compact"
            ).forEach { (scale, title) ->
                val active = settings.screenSizeScale == scale
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) AeroAmber else DarkSurfaceElevated)
                        .clickable { viewModel.setScreenSizeScale(scale) }
                        .padding(vertical = 10.dp)
                        .testTag("screen_scale_$scale"),
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

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Soundtrack Music Volume", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        Text("${(settings.musicVolume * 100).toInt()}%", color = AeroCyan, style = MaterialTheme.typography.labelSmall)
                    }
                    Slider(
                        value = settings.musicVolume,
                        onValueChange = { viewModel.updateSettings(music = it) },
                        modifier = Modifier.fillMaxWidth().testTag("music_volume_slider")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Pilot Career Statistics (7 Rapid Taps on High Score activates Dev Mode)
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { viewModel.onCareerScoreTapped() }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Career High Score (Tap 7x):", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("${profile.highScore} ⚡", color = Color(0xFFFFD700), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
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

        Spacer(modifier = Modifier.height(18.dp))

        // Google Account & Cloud Save Synchronization
        val authState by viewModel.authManager.authState.collectAsState()
        val syncStatus by viewModel.authManager.syncStatus.collectAsState()
        val context = androidx.compose.ui.platform.LocalContext.current
        val activity = context as? android.app.Activity

        Text("CLOUD SAVE & GOOGLE IDENTITY", style = MaterialTheme.typography.labelLarge, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (authState is com.example.game.auth.AuthState.Authenticated && !(authState as com.example.game.auth.AuthState.Authenticated).isAnonymous) AeroCyan else DarkSurfaceBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Cloud Status:", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                        Text(syncStatus, color = AeroCyan, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    when (val st = authState) {
                        is com.example.game.auth.AuthState.Authenticated -> {
                            if (st.isAnonymous) {
                                Text("GUEST PILOT", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            } else {
                                Text("LINKED (GOOGLE)", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        is com.example.game.auth.AuthState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = AeroCyan)
                        }
                        else -> {
                            Text("DISCONNECTED", color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                when (val st = authState) {
                    is com.example.game.auth.AuthState.Authenticated -> {
                        if (st.isAnonymous) {
                            Text(
                                "Sign in with your Google Account to automatically back up your fighters, upgrades, credits, and store purchases across devices.",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Button(
                                onClick = {
                                    if (activity != null) {
                                        viewModel.signInWithGoogle(activity)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SIGN IN WITH GOOGLE", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        } else {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Signed In As:", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                                    Text(st.displayName ?: st.email ?: "Google Pilot", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                    st.email?.let { Text(it, color = TextSecondary, style = MaterialTheme.typography.labelSmall) }
                                }
                                Button(
                                    onClick = { viewModel.signOut() },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = DangerRed),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("SIGN OUT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    else -> {}
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.syncProfileToCloud() },
                        colors = ButtonDefaults.buttonColors(containerColor = AeroCyan, contentColor = DarkVoid),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("BACKUP NOW", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.restoreProfileFromCloud() },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = AeroCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RESTORE SAVE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Play Store Compliance & Legal
        Text("LEGAL & GOOGLE PLAY COMPLIANCE", style = MaterialTheme.typography.labelLarge, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://3000studios.vip/privacy"))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Privacy Policy", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = AeroCyan, modifier = Modifier.size(16.dp))
                }
                Divider(color = DarkSurfaceBorder, thickness = 0.5.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://3000studios.vip/terms"))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Terms of Service", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = AeroCyan, modifier = Modifier.size(16.dp))
                }
                Divider(color = DarkSurfaceBorder, thickness = 0.5.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.restorePurchases()
                        }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Restore Google Play Purchases", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = AeroCyan, modifier = Modifier.size(16.dp))
                }
                Divider(color = DarkSurfaceBorder, thickness = 0.5.dp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Studio & Publisher:", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Text("3000 Studios LLC", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Support & Inquiries:", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Text("support@3000studios.vip", color = AeroCyan, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Developer Mode Active Status & Control Banner
        if (settings.isDeveloperMode) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFA855F7))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🛠️ DEVELOPER MODE OVERRIDE ACTIVE", color = Color(0xFFE9D5FF), fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    Text(
                        "• All 24 Sortie Theaters Unlocked\n• All 26 Warbirds Unlocked (Including Jerica & Jadon)\n• 100,000 Credits & 1,000 Plasma Cores Granted\n• All Paint Schemes & Exhausts Available",
                        color = Color(0xFFC084FC),
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp
                    )
                    Button(
                        onClick = { viewModel.resetDeveloperMode() },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("RESET TO STANDARD CADET PROGRESS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
