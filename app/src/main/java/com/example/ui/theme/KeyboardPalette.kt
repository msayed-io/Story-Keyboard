package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

// =============================================================================
//  لوحة ألوان الكيبورد لكل ثيم — ملف مشترك بين شاشة الكيبورد وشاشة الإعدادات،
//  فتكون المعاينة الحيّة بنفس ألوان الكيبورد الحقيقية تماماً.
// =============================================================================

internal data class ThemePalette(
    val canvasBackground: Color,
    val surfaceBackground: Color,
    val letterKeycapBg: Color,
    val modifierKeycapBg: Color,
    val keycapBorder: Color,
    val keyGlyphColor: Color,
    val accentColor: Color,
    val accentGlyphColor: Color
)

@Composable
internal fun rememberThemePalette(themeName: String): ThemePalette {
    return remember(themeName) {
        when (themeName) {
            "cyberpunk" -> ThemePalette(
                canvasBackground = Color(0xFF090014),
                surfaceBackground = Color(0xFF16032B),
                letterKeycapBg = Color(0xFF280C4C),
                modifierKeycapBg = Color(0xFF3B126D),
                keycapBorder = Color(0xFFFF007F).copy(alpha = 0.6f),
                keyGlyphColor = Color(0xFF00F5D4),
                accentColor = Color(0xFFFF007F),
                accentGlyphColor = Color.White
            )
            "apple_dark" -> ThemePalette(
                canvasBackground = AppleObsidian,
                surfaceBackground = AppleGraphite,
                letterKeycapBg = AppleCarbon,
                modifierKeycapBg = AppleGraphite,
                keycapBorder = AppleSteel,
                keyGlyphColor = ApplePorcelain,
                accentColor = AppleWhite,
                accentGlyphColor = AppleObsidian
            )
            "night_whisper" -> ThemePalette(
                canvasBackground = AppleObsidian,
                surfaceBackground = AppleGraphite,
                letterKeycapBg = AppleCarbon,
                modifierKeycapBg = AppleGraphite,
                keycapBorder = AppleSteel,
                keyGlyphColor = ApplePorcelain,
                accentColor = AppleWhite,
                accentGlyphColor = AppleObsidian
            )
            else -> // royal_classic & default
                ThemePalette(
                    canvasBackground = AppleObsidian,
                    surfaceBackground = AppleGraphite,
                    letterKeycapBg = AppleCarbon,
                    modifierKeycapBg = AppleGraphite,
                    keycapBorder = AppleSteel,
                    keyGlyphColor = ApplePorcelain,
                    accentColor = AppleWhite,
                    accentGlyphColor = AppleObsidian
                )
        }
    }
}
