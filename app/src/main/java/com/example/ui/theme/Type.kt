package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// Official Thmanyah Font Families for Dar Al-Hikayat (Hikayat Keyboard)
val ThmanyahSansFontFamily = FontFamily(
    Font(R.font.thmanyah_sans_regular, FontWeight.Normal),
    Font(R.font.thmanyah_sans_bold, FontWeight.Bold)
)

val ThmanyahSerifDisplayFontFamily = FontFamily(
    Font(R.font.thmanyah_serif_display_bold, FontWeight.Bold),
    Font(R.font.thmanyah_serif_display_black, FontWeight.Black)
)

val ThmanyahSerifTextFontFamily = FontFamily(
    Font(R.font.thmanyah_serif_text_regular, FontWeight.Normal),
    Font(R.font.thmanyah_serif_text_medium, FontWeight.Medium)
)

// Set of Material typography styles following Bevel Design System specifications
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = ThmanyahSerifDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 64.sp,
        lineHeight = 64.sp,
        letterSpacing = (-1.92).sp
    ),
    displayMedium = TextStyle(
        fontFamily = ThmanyahSerifDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 40.sp,
        letterSpacing = (-1.2).sp
    ),
    displaySmall = TextStyle(
        fontFamily = ThmanyahSerifDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.9).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = ThmanyahSerifDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.4).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = ThmanyahSerifDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.24).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = ThmanyahSerifDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.2).sp
    ),
    titleLarge = TextStyle(
        fontFamily = ThmanyahSerifTextFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = ThmanyahSerifTextFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 25.2.sp,
        letterSpacing = 0.16.sp
    ),
    titleSmall = TextStyle(
        fontFamily = ThmanyahSerifTextFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = ThmanyahSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 25.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = ThmanyahSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.4.sp
    ),
    bodySmall = TextStyle(
        fontFamily = ThmanyahSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = ThmanyahSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.4.sp
    ),
    labelMedium = TextStyle(
        fontFamily = ThmanyahSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = ThmanyahSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)

