package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RoyalClassicColorScheme = lightColorScheme(
    primary = Color(0xFFA7AA63), // Core Accent Gold
    onPrimary = Color(0xFF121A1B),
    secondary = Color(0xFF8F3C35), // Crimson
    onSecondary = Color.White,
    tertiary = Color(0xFFB88A4F), // Supporting Accent Gold
    background = Color(0xFFEAE6D2), // Canvas BG
    onBackground = Color(0xFF121A1B),
    surface = Color(0xFFF4F1EA), // Glass/Alternative BG #F4F1EA
    onSurface = Color(0xFF121A1B),
    surfaceVariant = Color(0xFFF4F1EA),
    onSurfaceVariant = Color(0xFF4A5556)
)

private val NightWhisperColorScheme = darkColorScheme(
    primary = Color(0xFF9FA365), // Accent Night
    onPrimary = Color(0xFF111718),
    secondary = Color(0xFF8F3C35),
    onSecondary = Color.White,
    tertiary = Color(0xFFB88A4F),
    background = Color(0xFF111718), // Canvas BG
    onBackground = Color(0xFFE2DFD2),
    surface = Color(0xFF171F21), // Glass color #171F21
    onSurface = Color(0xFFE2DFD2),
    surfaceVariant = Color(0xFF171F21),
    onSurfaceVariant = Color(0xFF7F8C8E)
)

private val AppleDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF5F5F5), // Glowing Silver
    onPrimary = Color(0xFF000000), // Absolute Black
    secondary = Color(0xFFFF453A), // Apple Red / Accent
    onSecondary = Color.White,
    tertiary = Color.White,
    background = Color(0xFF000000), // Absolute Black Canvas BG
    onBackground = Color(0xFFF5F5F5),
    surface = Color(0xFF1C1C1E), // Apple Dark System Gray
    onSurface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFF8E8E93)
)

private val LightInkColorScheme = lightColorScheme(
    primary = Color(0xFF121A1B), // Ink
    onPrimary = Color.White,
    secondary = Color(0xFF8F3C35),
    onSecondary = Color.White,
    tertiary = Color(0xFF4A5556),
    background = Color(0xFFFFFFFF), // White Canvas BG
    onBackground = Color(0xFF000000),
    surface = Color(0xFFF0F0F0),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF4A5556)
)

@Composable
fun MyApplicationTheme(
    themeId: String = "royal_classic",
    content: @Composable () -> Unit,
) {
    val colorScheme = when (themeId) {
        "royal_classic" -> RoyalClassicColorScheme
        "night_whisper" -> NightWhisperColorScheme
        "apple_dark" -> AppleDarkColorScheme
        "light_ink" -> LightInkColorScheme
        else -> RoyalClassicColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
