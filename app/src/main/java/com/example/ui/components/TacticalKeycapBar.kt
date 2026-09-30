package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainNavTab
import com.example.ui.theme.*

@Composable
fun TacticalKeycapBar(
    currentTab: MainNavTab,
    onTabSelected: (MainNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkVoid.copy(alpha = 0.96f))
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("tactical_bottom_nav")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainNavTab.values().forEach { tab ->
                val selected = currentTab == tab
                TacticalKeycapItem(
                    tab = tab,
                    isSelected = selected,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TacticalKeycapItem(
    tab: MainNavTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else if (isSelected) 1.04f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "key_scale"
    )

    val keyHeight = if (isSelected) 56.dp else 50.dp

    Box(
        modifier = modifier
            .scale(scale)
            .height(keyHeight)
            .shadow(
                elevation = if (isSelected) 8.dp else 2.dp,
                shape = RoundedCornerShape(10.dp),
                ambientColor = if (isSelected) AeroCyan else Color.Black,
                spotColor = if (isSelected) AeroCyan else Color.Black
            )
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (isSelected) 1.8.dp else 1.0.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        if (isSelected) AeroCyan else MetallicBorder,
                        if (isSelected) AeroCyan.copy(alpha = 0.4f) else CarbonDark
                    )
                ),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("nav_key_${tab.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        // 3D Mechanical Keycap Geometry Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Keycap body fill
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        if (isSelected) CarbonElevated else CarbonBlack,
                        if (isSelected) CarbonDark else Color(0xFF04070D)
                    )
                )
            )

            // Top Chamfer Highlight
            drawLine(
                color = if (isSelected) AeroCyan.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.15f),
                start = Offset(4f, 1.5f),
                end = Offset(size.width - 4f, 1.5f),
                strokeWidth = 1.5f
            )

            // Keycap active base illumination
            if (isSelected) {
                drawRect(
                    brush = Brush.radialGradient(
                        listOf(AeroCyan.copy(alpha = 0.25f), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.5f),
                        radius = size.width * 0.6f
                    )
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(2.dp)
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.title,
                tint = if (isSelected) AeroCyan else TextSecondary,
                modifier = Modifier.size(if (isSelected) 20.dp else 18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tab.title.uppercase(),
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                    fontSize = 8.5.sp,
                    color = if (isSelected) Color.White else TextMuted,
                    letterSpacing = 0.5.sp
                ),
                maxLines = 1
            )
        }
    }
}
