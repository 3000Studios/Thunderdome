package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

// ── CUSTOM VECTOR EMBLEMS ──

@Composable
fun CreditEmblem(modifier: Modifier = Modifier.size(20.dp), glowColor: Color = AeroEmerald) {
    Canvas(modifier = modifier) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val r = size.minDimension * 0.46f

        // Outer Neon Glow Ring
        drawCircle(
            brush = Brush.radialGradient(
                listOf(glowColor.copy(alpha = 0.5f), Color.Transparent),
                center = Offset(cx, cy),
                radius = size.width * 0.5f
            )
        )
        // Metallic Disc Base
        drawCircle(
            brush = Brush.linearGradient(
                listOf(Color(0xFF065F46), Color(0xFF047857), Color(0xFF10B981)),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
            ),
            radius = r,
            center = Offset(cx, cy)
        )
        // Beveled Inner Rim
        drawCircle(
            color = Color(0xFF6EE7B7),
            radius = r * 0.85f,
            center = Offset(cx, cy),
            style = Stroke(width = 1.5f)
        )
        // Credit Glyph: Stylized 'C' Hex
        val path = Path().apply {
            moveTo(cx + r * 0.45f, cy - r * 0.45f)
            lineTo(cx - r * 0.25f, cy - r * 0.45f)
            lineTo(cx - r * 0.5f, cy)
            lineTo(cx - r * 0.25f, cy + r * 0.45f)
            lineTo(cx + r * 0.45f, cy + r * 0.45f)
        }
        drawPath(path, color = Color.White, style = Stroke(width = 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        // Center Core Dot
        drawCircle(Color.White, radius = 2f, center = Offset(cx, cy))
    }
}

@Composable
fun CoreEmblem(modifier: Modifier = Modifier.size(20.dp), glowColor: Color = AeroViolet) {
    Canvas(modifier = modifier) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val w = size.width * 0.42f
        val h = size.height * 0.48f

        // Plasma Core Radiant Glow
        drawCircle(
            brush = Brush.radialGradient(
                listOf(glowColor.copy(alpha = 0.65f), Color.Transparent),
                center = Offset(cx, cy),
                radius = size.width * 0.55f
            )
        )
        // Octahedron Faceted Crystal Diamond
        val outerPath = Path().apply {
            moveTo(cx, cy - h)
            lineTo(cx + w, cy)
            lineTo(cx, cy + h)
            lineTo(cx - w, cy)
            close()
        }
        drawPath(
            outerPath,
            brush = Brush.verticalGradient(
                listOf(Color(0xFFE9D5FF), Color(0xFFC084FC), Color(0xFF7E22CE))
            )
        )
        // Crystal Facet Lines
        drawLine(Color.White.copy(alpha = 0.9f), Offset(cx, cy - h), Offset(cx, cy + h), strokeWidth = 1.2f)
        drawLine(Color.White.copy(alpha = 0.9f), Offset(cx - w, cy), Offset(cx + w, cy), strokeWidth = 1.2f)
        drawPath(outerPath, color = Color.White, style = Stroke(width = 1.2f))
    }
}

// ── CARBON FIBER TEXTURE HELPER ──
fun DrawScope.drawCarbonFiberBackground(baseColor: Color = CarbonBlack, weaveColor: Color = Color(0x33475569)) {
    drawRect(color = baseColor)
    val step = 6f
    var x = -size.height
    while (x < size.width + size.height) {
        drawLine(
            color = weaveColor,
            start = Offset(x, 0f),
            end = Offset(x + size.height, size.height),
            strokeWidth = 1.5f
        )
        x += step * 2
    }
    // High-Gloss Reflection Top Gradient
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.02f), Color.Transparent),
            startY = 0f,
            endY = size.height * 0.6f
        )
    )
}

// ── TACTICAL GLOSS BUTTON ──
@Composable
fun TacticalGlossButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = AeroCyan,
    contentColor: Color = DarkVoid,
    borderColor: Color = MetallicBorder,
    height: Dp = 54.dp,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.965f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btn_press_scale"
    )

    // Animated Light Shimmer Idle Sweep
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer_btn")
    val shimmerPos by infiniteTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_pos"
    )

    Surface(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = modifier
            .scale(scale)
            .height(height)
            .shadow(if (isPressed) 2.dp else 6.dp, RoundedCornerShape(12.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.verticalGradient(
                            listOf(
                                borderColor.copy(alpha = 0.9f),
                                borderColor.copy(alpha = 0.4f),
                                Color.White.copy(alpha = 0.2f)
                            )
                        )
                    ),
                    RoundedCornerShape(12.dp)
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Carbon Fiber Weave Background
                drawCarbonFiberBackground(baseColor = CarbonBlack)

                // Colored Accent Fill
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(containerColor.copy(alpha = if (isPressed) 0.85f else 0.75f), containerColor.copy(alpha = 0.4f))
                    )
                )

                // Shimmer Beam Sweep
                val beamX = size.width * shimmerPos
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, Color.White.copy(alpha = 0.25f), Color.Transparent),
                        startX = beamX - 60f,
                        endX = beamX + 60f
                    )
                )

                // 3D Bevel Highlight Top Line
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(6f, 1f),
                    end = Offset(size.width - 6f, 1f),
                    strokeWidth = 1.5f
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProvideTextStyle(
                    value = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = contentColor,
                        letterSpacing = 0.5.sp
                    )
                ) {
                    content()
                }
            }
        }
    }
}

// ── EXTRA-PREMIUM DEPOT & FOUNDER STORE BUTTON ──
@Composable
fun DepotFounderButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 54.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "depot_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "depot_anim")
    val sweepPos by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "depot_sweep"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "depot_glow"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        modifier = modifier
            .scale(scale)
            .height(height)
            .shadow(8.dp, RoundedCornerShape(14.dp), ambientColor = FounderGold, spotColor = FounderGold)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))
                .border(
                    BorderStroke(
                        2.dp,
                        Brush.linearGradient(
                            listOf(FounderGold, AeroCyan, FounderGoldDark, FounderGold)
                        )
                    ),
                    RoundedCornerShape(14.dp)
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Carbon Base
                drawCarbonFiberBackground(baseColor = CarbonBlack)

                // Gold & Amber Core Wash
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(
                            FounderGoldDark.copy(alpha = 0.55f * glowPulse),
                            AeroOrange.copy(alpha = 0.40f * glowPulse),
                            FounderGoldDark.copy(alpha = 0.55f * glowPulse)
                        )
                    )
                )

                // High-Speed Holographic Light Sweep
                val sx = size.width * sweepPos
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, Color.White.copy(alpha = 0.45f), AeroCyan.copy(alpha = 0.6f), Color.Transparent),
                        startX = sx - 80f,
                        endX = sx + 80f
                    )
                )

                // Top & Bottom Metallic Lip Highlight
                drawLine(Color.White.copy(alpha = 0.7f), Offset(10f, 1.5f), Offset(size.width - 10f, 1.5f), strokeWidth = 1.8f)
                drawLine(FounderGold.copy(alpha = 0.5f), Offset(10f, size.height - 1.5f), Offset(size.width - 10f, size.height - 1.5f), strokeWidth = 1.2f)
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoreEmblem(modifier = Modifier.size(24.dp), glowColor = FounderGold)
                Spacer(modifier = Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "WARBIRD DEPOT & FOUNDER STORE",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.5.sp,
                            color = Color.White,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Text(
                        text = "EXCLUSIVE APEX ZERO // IAP PACKS // NO ADS",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = FounderGold,
                            letterSpacing = 1.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                CreditEmblem(modifier = Modifier.size(22.dp), glowColor = AeroAmber)
            }
        }
    }
}

// ── FUTURISTIC HUD RESOURCE COCKPIT MODULE ──
@Composable
fun HudResourceModule(
    callsign: String,
    level: Int,
    credits: Long,
    plasmaCores: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.88f)),
        border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(AeroCyan.copy(alpha = 0.6f), AeroViolet.copy(alpha = 0.4f))))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Callsign & Rank Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(AeroCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = callsign.uppercase(),
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AeroCyan.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AeroCyan.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "LVL $level COMMANDER",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = AeroCyan,
                            letterSpacing = 1.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Resource Displays: Credits & Plasma Cores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Credits HUD Cell
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = CarbonBlack.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, AeroEmerald.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CreditEmblem(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "CREDITS",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = "$credits",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Plasma Cores HUD Cell
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = CarbonBlack.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, AeroViolet.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CoreEmblem(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "PLASMA CORES",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = "$plasmaCores",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── DYNAMIC ANIMATED & ROTATING GAME MODE BADGES ──

@Composable
fun RotatingCampaignBadge(modifier: Modifier = Modifier.size(36.dp)) {
    val transition = rememberInfiniteTransition(label = "campaign_badge")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "radar_sweep"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "radar_pulse"
    )

    Canvas(modifier = modifier) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val r = size.minDimension * 0.45f

        // Outer Tactical Holo Ring
        drawCircle(
            color = AeroCyan.copy(alpha = 0.35f * pulse),
            radius = r,
            center = Offset(cx, cy),
            style = Stroke(width = 1.8f)
        )
        drawCircle(
            color = AeroCyan.copy(alpha = 0.15f),
            radius = r * 0.65f,
            center = Offset(cx, cy),
            style = Stroke(width = 1.0f)
        )

        // Crosshairs
        drawLine(AeroCyan.copy(alpha = 0.4f), Offset(cx - r, cy), Offset(cx + r, cy), strokeWidth = 1f)
        drawLine(AeroCyan.copy(alpha = 0.4f), Offset(cx, cy - r), Offset(cx, cy + r), strokeWidth = 1f)

        // Rotating Radar Sweep Vector
        val rad = (rotation * Math.PI / 180f).toFloat()
        val sweepEnd = Offset(cx + kotlin.math.cos(rad) * r, cy + kotlin.math.sin(rad) * r)
        drawLine(
            brush = Brush.linearGradient(
                listOf(AeroCyan, Color.White, Color.Transparent),
                start = Offset(cx, cy),
                end = sweepEnd
            ),
            start = Offset(cx, cy),
            end = sweepEnd,
            strokeWidth = 2.2f
        )

        // Center Target Beacon Blip
        drawCircle(AeroEmerald, radius = 3.5f * pulse, center = Offset(cx, cy))
        drawCircle(Color.White, radius = 1.5f, center = Offset(cx, cy))
    }
}

@Composable
fun RotatingMultiplayerBadge(modifier: Modifier = Modifier.size(36.dp)) {
    val transition = rememberInfiniteTransition(label = "mp_badge")
    val rotAngle by transition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "formation_yaw"
    )
    val beaconPulse by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
        label = "beacon"
    )

    Canvas(modifier = modifier) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val scale = size.minDimension / 40f

        // Tactical 4-Wingmen Diamond Formation
        val leadNose = Offset(cx, cy - 14f * scale)
        val leadLeft = Offset(cx - 8f * scale, cy - 2f * scale)
        val leadRight = Offset(cx + 8f * scale, cy - 2f * scale)

        val wing1Nose = Offset(cx - 12f * scale, cy + 3f * scale)
        val wing1Left = Offset(cx - 18f * scale, cy + 12f * scale)
        val wing1Right = Offset(cx - 6f * scale, cy + 12f * scale)

        val wing2Nose = Offset(cx + 12f * scale, cy + 3f * scale)
        val wing2Left = Offset(cx + 6f * scale, cy + 12f * scale)
        val wing2Right = Offset(cx + 18f * scale, cy + 12f * scale)

        // Lead Fighter (Cyan)
        val leadPath = Path().apply {
            moveTo(leadNose.x, leadNose.y)
            lineTo(leadLeft.x, leadLeft.y)
            lineTo(leadRight.x, leadRight.y)
            close()
        }
        drawPath(leadPath, color = Color.White)

        // Wingmen Fighters (Crimson & Amber)
        val wing1Path = Path().apply {
            moveTo(wing1Nose.x, wing1Nose.y)
            lineTo(wing1Left.x, wing1Left.y)
            lineTo(wing1Right.x, wing1Right.y)
            close()
        }
        drawPath(wing1Path, color = AeroCrimson)

        val wing2Path = Path().apply {
            moveTo(wing2Nose.x, wing2Nose.y)
            lineTo(wing2Left.x, wing2Left.y)
            lineTo(wing2Right.x, wing2Right.y)
            close()
        }
        drawPath(wing2Path, color = AeroAmber)

        // Pulsing Telemetry Signal Rings
        drawCircle(AeroCrimson.copy(alpha = 0.5f * beaconPulse), radius = 17f * scale, center = Offset(cx, cy), style = Stroke(1.2f))
    }
}

@Composable
fun HangarBayBadge(modifier: Modifier = Modifier.size(36.dp)) {
    val transition = rememberInfiniteTransition(label = "hangar_badge")
    val gantrySlide by transition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "gantry"
    )
    val lightBlink by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse),
        label = "neon"
    )

    Canvas(modifier = modifier) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val w = size.width
        val h = size.height

        // Hangar Bay Blast Doors & Floor Grid
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
            )
        )

        // Maintenance Bay Floor Chevron Stripes
        for (i in 0..3) {
            val yOffset = h * 0.25f + i * (h * 0.2f)
            drawLine(
                color = AeroViolet.copy(alpha = 0.35f),
                start = Offset(w * 0.15f, yOffset),
                end = Offset(w * 0.85f, yOffset),
                strokeWidth = 1.5f
            )
        }

        // Yellow Warning Hazard Edge Lines
        drawLine(Color(0xFFFBBF24).copy(alpha = lightBlink), Offset(2f, 0f), Offset(2f, h), strokeWidth = 2.5f)
        drawLine(Color(0xFFFBBF24).copy(alpha = lightBlink), Offset(w - 2f, 0f), Offset(w - 2f, h), strokeWidth = 2.5f)

        // Docked Warbird Silhouette in Center Bay
        val nose = Offset(cx, cy - h * 0.28f)
        val leftTip = Offset(cx - w * 0.32f + gantrySlide, cy + h * 0.15f)
        val rightTip = Offset(cx + w * 0.32f + gantrySlide, cy + h * 0.15f)
        val tail = Offset(cx, cy + h * 0.30f)

        val craftPath = Path().apply {
            moveTo(nose.x, nose.y)
            lineTo(leftTip.x, leftTip.y)
            lineTo(cx, cy + h * 0.05f)
            lineTo(rightTip.x, rightTip.y)
            close()
        }
        drawPath(craftPath, color = Color.White)
        drawCircle(AeroCyan, radius = 2.5f, center = nose)
    }
}

@Composable
fun DepotStoreBadge(modifier: Modifier = Modifier.size(36.dp)) {
    val transition = rememberInfiniteTransition(label = "depot_badge")
    val crateGlow by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "crate_glow"
    )
    val floatY by transition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float"
    )

    Canvas(modifier = modifier) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f + floatY
        val sizeBox = size.minDimension * 0.68f

        // Golden Supply Crate Container Body
        val left = cx - sizeBox * 0.5f
        val top = cy - sizeBox * 0.5f
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFF59E0B), Color(0xFFB45309), Color(0xFF78350F))
            ),
            topLeft = Offset(left, top),
            size = Size(sizeBox, sizeBox),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )

        // Metallic Reinforced Crate Straps & Latches
        drawRoundRect(
            color = Color(0xFFFEF3C7),
            topLeft = Offset(left, top),
            size = Size(sizeBox, sizeBox),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
            style = Stroke(width = 1.8f)
        )
        drawLine(Color(0xFFFEF3C7), Offset(left, cy), Offset(left + sizeBox, cy), strokeWidth = 1.5f)
        drawLine(Color(0xFFFEF3C7), Offset(cx, top), Offset(cx, top + sizeBox), strokeWidth = 1.5f)

        // Glowing Center Plasma Crystal Inset
        val crystalPath = Path().apply {
            moveTo(cx, cy - 6f)
            lineTo(cx + 6f, cy)
            lineTo(cx, cy + 6f)
            lineTo(cx - 6f, cy)
            close()
        }
        drawPath(crystalPath, brush = Brush.radialGradient(listOf(Color(0xFFE9D5FF), Color(0xFFA855F7))))
        drawCircle(Color.White.copy(alpha = crateGlow), radius = 2f, center = Offset(cx, cy))
    }
}

@Composable
fun RotatingStagesBadge(modifier: Modifier = Modifier.size(24.dp)) {
    val transition = rememberInfiniteTransition(label = "stages_badge")
    val rot by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
        label = "globe_rot"
    )

    Canvas(modifier = modifier) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val r = size.minDimension * 0.45f

        drawCircle(color = AeroCyan.copy(alpha = 0.35f), radius = r, center = Offset(cx, cy), style = Stroke(1.2f))
        drawOval(
            color = AeroCyan.copy(alpha = 0.5f),
            topLeft = Offset(cx - r * 0.6f, cy - r),
            size = Size(r * 1.2f, r * 2f),
            style = Stroke(1f)
        )
        drawLine(AeroCyan.copy(alpha = 0.6f), Offset(cx - r, cy), Offset(cx + r, cy), strokeWidth = 1f)
        drawCircle(Color.White, radius = 2f, center = Offset(cx, cy))
    }
}

@Composable
fun RotatingBossesBadge(modifier: Modifier = Modifier.size(24.dp)) {
    val transition = rememberInfiniteTransition(label = "bosses_badge")
    val pulse by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "boss_pulse"
    )

    Canvas(modifier = modifier) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val r = size.minDimension * 0.45f

        drawCircle(color = AeroCrimson.copy(alpha = 0.4f * pulse), radius = r, center = Offset(cx, cy))
        drawCircle(color = DangerRed, radius = r * 0.8f, center = Offset(cx, cy), style = Stroke(1.5f))

        // Red Dreadnought Eye
        drawCircle(Color.White, radius = 3f * pulse, center = Offset(cx, cy))
        drawCircle(DangerRed, radius = 1.5f, center = Offset(cx, cy))
    }
}

@Composable
fun RotatingPassBadge(modifier: Modifier = Modifier.size(24.dp)) {
    val transition = rememberInfiniteTransition(label = "pass_badge")
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4500, easing = LinearEasing), RepeatMode.Restart),
        label = "star_spin"
    )

    Canvas(modifier = modifier) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val r = size.minDimension * 0.44f

        drawCircle(color = AeroAmber.copy(alpha = 0.35f), radius = r, center = Offset(cx, cy), style = Stroke(1.2f))

        // 4-Point Star
        val path = Path().apply {
            moveTo(cx, cy - r * 0.8f)
            lineTo(cx + r * 0.25f, cy - r * 0.25f)
            lineTo(cx + r * 0.8f, cy)
            lineTo(cx + r * 0.25f, cy + r * 0.25f)
            lineTo(cx, cy + r * 0.8f)
            lineTo(cx - r * 0.25f, cy + r * 0.25f)
            lineTo(cx - r * 0.8f, cy)
            lineTo(cx - r * 0.25f, cy - r * 0.25f)
            close()
        }
        drawPath(path, color = Color(0xFFFBBF24))
        drawCircle(Color.White, radius = 2f, center = Offset(cx, cy))
    }
}

@Composable
fun RotatingSystemsBadge(modifier: Modifier = Modifier.size(24.dp)) {
    val transition = rememberInfiniteTransition(label = "sys_badge")
    val rot by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Restart),
        label = "gear_rot"
    )

    Canvas(modifier = modifier) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val r = size.minDimension * 0.42f

        drawCircle(color = AeroCyan.copy(alpha = 0.5f), radius = r, center = Offset(cx, cy), style = Stroke(1.5f))
        for (i in 0 until 6) {
            val a = (i * 60f + rot) * Math.PI / 180f
            val x1 = cx + kotlin.math.cos(a).toFloat() * r * 0.7f
            val y1 = cy + kotlin.math.sin(a).toFloat() * r * 0.7f
            val x2 = cx + kotlin.math.cos(a).toFloat() * r * 1.15f
            val y2 = cy + kotlin.math.sin(a).toFloat() * r * 1.15f
            drawLine(AeroCyan, Offset(x1, y1), Offset(x2, y2), strokeWidth = 1.8f)
        }
        drawCircle(Color.White, radius = 2.5f, center = Offset(cx, cy))
    }
}

