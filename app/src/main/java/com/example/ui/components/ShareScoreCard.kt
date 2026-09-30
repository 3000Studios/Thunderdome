package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.analytics.AnalyticsManager
import com.example.ui.theme.*

object ShareHelper {
    fun shareSortieResult(
        context: Context,
        callsign: String,
        score: Long,
        kills: Int,
        stageName: String,
        aircraftName: String
    ) {
        AnalyticsManager.logShareClicked(score, stageName)
        val shareText = """
            🔥 THUNDER DOME SORTIE RECORD 🔥
            Pilot: $callsign
            Stage: $stageName
            Aircraft: $aircraftName
            Score: $score | Kills: $kills
            
            Can you survive this boss? 🚀
            Play Thunder Dome on Google Play:
            https://play.google.com/store/apps/details?id=com.aistudio.aerostrike.xrkfpz
            
            #ThunderDome #3000Studios #AirCombat #GamingTok #MobileGaming
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Thunder Dome High Score // $callsign")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        val chooser = Intent.createChooser(intent, "Share Sortie Result to TikTok / Socials")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}

@Composable
fun ShareScoreCard(
    callsign: String,
    score: Long,
    kills: Int,
    bossKills: Int,
    stageName: String,
    aircraftName: String,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(AeroCyan, AeroViolet, AeroAmber)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF070E20), Color(0xFF020409))
                    )
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Branding
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "3000 STUDIOS // OFFICIAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = AeroCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "THUNDER DOME",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = null,
                    tint = AeroAmber,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Score Highlight Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceElevated)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SORTIE SCORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        ),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text("THEATER", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(stageName, style = MaterialTheme.typography.bodySmall, color = AeroCyan, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("WARBIRD", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(aircraftName, style = MaterialTheme.typography.bodySmall, color = AeroEmerald, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("KILLS", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text("$kills ${if (bossKills > 0) "($bossKills 👑)" else ""}", style = MaterialTheme.typography.bodySmall, color = AeroAmber, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Share Button
            Button(
                onClick = onShareClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AeroCyan, contentColor = DarkVoid),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SHARE TO TIKTOK / SOCIALS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
