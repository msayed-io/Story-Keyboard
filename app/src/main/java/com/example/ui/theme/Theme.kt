package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppleDarkColorScheme = darkColorScheme(
    primary = AppleWhite, // High-contrast Pure White Pill Buttons
    onPrimary = AppleObsidian, // Black text/icons on white primary pills
    secondary = ApplePorcelain, // #F5F5F7
    onSecondary = AppleObsidian,
    tertiary = AppleAsh, // #86868B
    background = AppleObsidian, // #000000 Pure Obsidian Canvas Stage
    onBackground = ApplePorcelain, // #F5F5F7 Display Text
    surface = AppleCarbon, // #1D1D1F Carbon Panel
    onSurface = ApplePorcelain, // #F5F5F7
    surfaceVariant = AppleGraphite, // #0E0E0E Graphite Chrome
    onSurfaceVariant = AppleAsh, // #86868B Ash Secondary Text
    outline = AppleSteel // #333336 Steel Hairline Keyline
)

@Composable
fun MyApplicationTheme(
    themeId: String = "apple_dark",
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AppleDarkColorScheme,
        typography = Typography,
        content = content
    )
}
