package com.example.ui.screens

import android.media.MediaPlayer
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.theme.*

@Composable
fun VideoSplashScreen(
    onSplashFinished: () -> Unit
) {
    val context = LocalContext.current
    var isVideoReady by remember { mutableStateOf(false) }

    // Track both completions — only transition when BOTH are done
    var videoCompleted by remember { mutableStateOf(false) }
    var audioCompleted by remember { mutableStateOf(false) }
    var hasNavigated by remember { mutableStateOf(false) }

    // When both finish, transition
    LaunchedEffect(videoCompleted, audioCompleted) {
        if (videoCompleted && audioCompleted && !hasNavigated) {
            hasNavigated = true
            onSplashFinished()
        }
    }

    // Announcer audio — plays full clip independently of video
    DisposableEffect(Unit) {
        val announcerPlayer = MediaPlayer.create(context, R.raw.welcome_to_thunder_dome)?.apply {
            setVolume(1.0f, 1.0f)
            setOnCompletionListener { audioCompleted = true }
            start()
        }

        onDispose {
            try {
                announcerPlayer?.let {
                    if (it.isPlaying) it.stop()
                    it.release()
                }
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable {
                if (!hasNavigated) {
                    hasNavigated = true
                    onSplashFinished()
                }
            }
            .testTag("video_splash_screen")
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                VideoView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    val videoUri = Uri.parse("android.resource://${ctx.packageName}/${R.raw.thunder_dome_intro}")
                    setVideoURI(videoUri)
                    setOnPreparedListener { mp ->
                        mp.isLooping = false
                        // Mute video's own audio so announcer is clear
                        mp.setVolume(0f, 0f)
                        isVideoReady = true
                        start()
                    }
                    setOnCompletionListener {
                        videoCompleted = true
                    }
                    setOnErrorListener { _, _, _ ->
                        videoCompleted = true
                        audioCompleted = true
                        true
                    }
                }
            }
        )

        // Cinematic vignette overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
        )

        // Top skip & branding banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "3000 STUDIOS",
                    style = MaterialTheme.typography.labelSmall,
                    color = AeroCyan,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "THUNDER DOME",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }

            Button(
                onClick = {
                    if (!hasNavigated) {
                        hasNavigated = true
                        onSplashFinished()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkSurfaceElevated.copy(alpha = 0.85f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.testTag("skip_splash_button")
            ) {
                Icon(
                    Icons.Default.FastForward,
                    contentDescription = "Skip",
                    modifier = Modifier.size(16.dp),
                    tint = AeroCyan
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("SKIP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }

        // Bottom Tap to Enter Prompt
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TAP SCREEN TO ENGAGE",
                style = MaterialTheme.typography.labelMedium,
                color = AeroAmber,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "24 WARBIRDS // 24 THEATERS // 1V1 DOGFIGHTS",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
        }
    }
}
