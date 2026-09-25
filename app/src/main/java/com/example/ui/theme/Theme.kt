package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AeroCyan,
    onPrimary = DarkVoid,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = AeroCyan,
    secondary = AeroAmber,
    onSecondary = DarkVoid,
    secondaryContainer = DarkSurfaceBorder,
    onSecondaryContainer = AeroAmber,
    tertiary = AeroViolet,
    background = DarkVoid,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkSurfaceBorder,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun AeroStrikeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
