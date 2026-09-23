package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CustomizationScreen(
    currentTheme: String,
    currentOpacity: Float,
    currentBlur: Int,
    hapticIntensity: Int,
    hasCustomBg: Boolean,
    onThemeChanged: (String) -> Unit,
    onOpacityChanged: (Float) -> Unit,
    onBlurChanged: (Int) -> Unit,
    onHapticChanged: (Int) -> Unit,
    onImageSelected: (Uri) -> Unit,
    onClearBg: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Zero-permission Photo Picker for maximum security and policy compliance
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onImageSelected(uri)
        }
    }

    val isDark = MaterialTheme.colorScheme.background == Color(0xFF111718)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .linenBackground(isDark)
    ) {
        // TOP BACKDROP GRADIENT & MAGNETIC SCROLL DISSOLVE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.45f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.75f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Scrollable Settings Cards Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 100.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // SECTION 1: Built-in Themes Selection (Segmented Switcher Card)
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_card_theme")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    // Upper row: Title and currently active theme name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp, start = 4.dp, end = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "أجواء الدار",
                            fontSize = 16.sp,
                            fontFamily = ThmanyahSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary // Accent Gold
                        )
                        
                        val activeThemeName = when (currentTheme) {
                            "royal_classic" -> "كلاسيكي ملكي"
                            "night_whisper" -> "همس الليالي"
                            "apple_dark" -> "داكن آبل"
                            else -> "كلاسيكي ملكي"
                        }
                        Text(
                            text = activeThemeName,
                            fontSize = 14.sp,
                            fontFamily = ThmanyahSansFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    // Track Container
                    val trackBgColor = if (currentTheme == "royal_classic") Color(0x0D121A1B) else Color(0x66000000)
                    val trackBorderColor = if (currentTheme == "royal_classic") Color(0x1F121A1B) else Color(0x14FFFFFF)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(trackBgColor, shape = CircleShape)
                            .border(BorderStroke(0.5.dp, trackBorderColor), shape = CircleShape)
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Button 3: كلاسيكى • (Placed on the right in RTL Row)
                        val button3Interaction = remember { MutableInteractionSource() }
                        val isSelected3 = currentTheme == "royal_classic"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(if (isSelected3) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .appleElasticPinch(button3Interaction)
                                .clickable(
                                    interactionSource = button3Interaction,
                                    indication = null
                               ) { onThemeChanged("royal_classic") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "كلاسيكى •",
                                color = if (isSelected3) Color.White else {
                                    if (currentTheme == "royal_classic") Color(0xA6121A1B) else Color(0xA6F5F5F5)
                                },
                                fontSize = 14.sp,
                                fontFamily = ThmanyahSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Button 2: • ليلى • (Placed in the middle in RTL Row)
                        val button2Interaction = remember { MutableInteractionSource() }
                        val isSelected2 = currentTheme == "night_whisper"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(if (isSelected2) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .appleElasticPinch(button2Interaction)
                                .clickable(
                                    interactionSource = button2Interaction,
                                    indication = null
                                ) { onThemeChanged("night_whisper") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "• ليلى •",
                                color = if (isSelected2) Color.White else {
                                    if (currentTheme == "royal_classic") Color(0xA6121A1B) else Color(0xA6F5F5F5)
                                },
                                fontSize = 14.sp,
                                fontFamily = ThmanyahSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Button 1: داكن آبل (Placed on the left in RTL Row)
                        val button1Interaction = remember { MutableInteractionSource() }
                        val isSelected1 = currentTheme == "apple_dark"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(if (isSelected1) Color(0xFF2C2C2E) else Color.Transparent)
                                .appleElasticPinch(button1Interaction)
                                .clickable(
                                    interactionSource = button1Interaction,
                                    indication = null
                                ) { onThemeChanged("apple_dark") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "داكن آبل",
                                color = if (isSelected1) Color.White else {
                                    if (currentTheme == "royal_classic") Color(0xA6121A1B) else Color(0xA6F5F5F5)
                                },
                                fontSize = 14.sp,
                                fontFamily = ThmanyahSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // SECTION 2: Custom Background Wallpaper Picker
            val wallpaperCardInteraction = remember { MutableInteractionSource() }
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "خلفية مخصصة",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "خلفية لوحة المفاتيح المخصصة",
                            fontSize = 16.sp,
                            fontFamily = ThmanyahSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "يمكنكِ اختيار أي صورة من ألبوم صوركِ لتكون خلفية لوحة مفاتيح الرواية اللاسلكية.",
                        fontSize = 13.sp,
                        fontFamily = ThmanyahSansFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Select Background Button
                        val selectBgInteraction = remember { MutableInteractionSource() }
                        Button(
                            onClick = {
                                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .appleElasticPinch(selectBgInteraction)
                                .testTag("btn_select_bg"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            interactionSource = selectBgInteraction
                        ) {
                            Text(
                                text = "اختيار صورة مخصصة ❦",
                                fontSize = 14.sp,
                                fontFamily = ThmanyahSansFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Clear Background Button
                        if (hasCustomBg) {
                            val clearBgInteraction = remember { MutableInteractionSource() }
                            OutlinedButton(
                                onClick = onClearBg,
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .width(60.dp)
                                    .appleElasticPinch(clearBgInteraction)
                                    .testTag("btn_clear_bg"),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                contentPadding = PaddingValues(0.dp),
                                interactionSource = clearBgInteraction
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "مسح الخلفية",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    if (hasCustomBg) {
                        Spacer(modifier = Modifier.height(24.dp))

                        // BACKGROUND OPACITY SLIDER
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "مدى شفافية الأزرار الملعونة",
                                    fontSize = 14.sp,
                                    fontFamily = ThmanyahSansFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${(currentOpacity * 100).toInt()}%",
                                    fontSize = 14.sp,
                                    fontFamily = ThmanyahSansFontFamily,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Slider(
                                value = currentOpacity,
                                onValueChange = onOpacityChanged,
                                valueRange = 0.05f..0.99f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                                ),
                                modifier = Modifier.testTag("slider_opacity")
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // BACKGROUND BLUR SLIDER
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "مدى ضبابية الخلفية وغبشها (Blur)",
                                    fontSize = 14.sp,
                                    fontFamily = ThmanyahSansFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${currentBlur}px",
                                    fontSize = 14.sp,
                                    fontFamily = ThmanyahSansFontFamily,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Slider(
                                value = currentBlur.toFloat(),
                                onValueChange = { onBlurChanged(it.toInt()) },
                                valueRange = 0f..25f,
                                steps = 25,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                                ),
                                modifier = Modifier.testTag("slider_blur")
                            )
                        }
                    }
                }
            }

            // SECTION 3: Haptic Feedback Controls
            val hapticCardInteraction = remember { MutableInteractionSource() }
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "الاهتزاز",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "اهتزاز مفاتيح الكيبورد",
                            fontSize = 16.sp,
                            fontFamily = ThmanyahSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "شدة الاهتزاز عند النقر والكتابة",
                            fontSize = 14.sp,
                            fontFamily = ThmanyahSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (hapticIntensity == 0) "إيقاف الاهتزاز" else "${hapticIntensity}ms",
                            fontSize = 14.sp,
                            fontFamily = ThmanyahSansFontFamily,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = hapticIntensity.toFloat(),
                        onValueChange = { onHapticChanged(it.toInt()) },
                        valueRange = 0f..100f,
                        steps = 20,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                        ),
                        modifier = Modifier.testTag("slider_haptic")
                    )
                }
            }
        }

        // FLOATING TOP CAPSULE HEADER SYSTEM (نظام الكبسولات العلوية الطافية)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Standalone circular floating capsule back button
            val backInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
                    .appleElasticPinch(backInteraction)
                    .clickable(
                        interactionSource = backInteraction,
                        indication = null,
                        onClick = onBack
                    )
                    .testTag("btn_customization_back"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Title/Brand Floating Capsule
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "مركز التخصيص والثيمات",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = ThmanyahSerifDisplayFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Layout spacer for beautiful symmetry
            Spacer(modifier = Modifier.size(44.dp))
        }
    }
}
