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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .linenBackground(isDark)
    ) {
        val minScreenHeight = maxHeight

        // TOP BACKDROP GRADIENT & MAGNETIC SCROLL DISSOLVE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.98f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.90f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.70f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.40f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Scrollable Settings Cards Container مع توزيع رأسي انسيابي ومريح
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 80.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = minScreenHeight - 112.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // SECTION 1: Custom Wallpaper Background Integration Card
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = AppleCarbon),
                    border = BorderStroke(1.dp, AppleSteel),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_card_custom_bg")
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "صورة الخلفية",
                                tint = ApplePorcelain,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "خلفية الكيبورد المخصصة",
                                fontSize = 16.sp,
                                fontFamily = ThmanyahSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = ApplePorcelain
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val selectBgInteraction = remember { MutableInteractionSource() }
                            Button(
                                onClick = {
                                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                shape = RoundedCornerShape(170.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .appleElasticPinch(selectBgInteraction)
                                    .testTag("btn_select_bg"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppleWhite,
                                    contentColor = AppleObsidian
                                ),
                                interactionSource = selectBgInteraction
                            ) {
                                Text(
                                    text = "اختيار صورة مخصصة ❦",
                                    fontSize = 14.sp,
                                    fontFamily = ThmanyahSansFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AppleObsidian
                                )
                            }

                            // Clear Background Button
                            if (hasCustomBg) {
                                val clearBgInteraction = remember { MutableInteractionSource() }
                                OutlinedButton(
                                    onClick = onClearBg,
                                    shape = RoundedCornerShape(170.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .width(60.dp)
                                        .appleElasticPinch(clearBgInteraction)
                                        .testTag("btn_clear_bg"),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFFEF4444)
                                    ),
                                    contentPadding = PaddingValues(0.dp),
                                    interactionSource = clearBgInteraction
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "مسح الخلفية",
                                        tint = Color(0xFFEF4444),
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
                                        text = "مدى شفافية وتعتيم أزرار اللوحة",
                                        fontSize = 14.sp,
                                        fontFamily = ThmanyahSansFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = ApplePorcelain
                                    )
                                    Text(
                                        text = "${(currentOpacity * 100).toInt()}%",
                                        fontSize = 14.sp,
                                        fontFamily = ThmanyahSansFontFamily,
                                        color = AppleWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Slider(
                                    value = currentOpacity,
                                    onValueChange = onOpacityChanged,
                                    valueRange = 0.05f..0.99f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = AppleWhite,
                                        activeTrackColor = AppleWhite,
                                        inactiveTrackColor = AppleSteel
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
                                        color = ApplePorcelain
                                    )
                                    Text(
                                        text = "${currentBlur}px",
                                        fontSize = 14.sp,
                                        fontFamily = ThmanyahSansFontFamily,
                                        color = AppleWhite,
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
                                        thumbColor = AppleWhite,
                                        activeTrackColor = AppleWhite,
                                        inactiveTrackColor = AppleSteel
                                    ),
                                    modifier = Modifier.testTag("slider_blur")
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // SECTION 2: Haptic Feedback Controls
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = AppleCarbon),
                    border = BorderStroke(1.dp, AppleSteel),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = "الاهتزاز",
                                tint = ApplePorcelain,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "اهتزاز مفاتيح الكيبورد",
                                fontSize = 16.sp,
                                fontFamily = ThmanyahSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = ApplePorcelain
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

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
                                color = ApplePorcelain
                            )
                            Text(
                                text = if (hapticIntensity == 0) "إيقاف الاهتزاز" else "${hapticIntensity}ms",
                                fontSize = 14.sp,
                                fontFamily = ThmanyahSansFontFamily,
                                color = AppleWhite,
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
                                thumbColor = AppleWhite,
                                activeTrackColor = AppleWhite,
                                inactiveTrackColor = AppleSteel
                            ),
                            modifier = Modifier.testTag("slider_haptic")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Literary Signature Footer
                Text(
                    text = "دار الحكايات ❦ تخصيص لوحة مفاتيح الرواية",
                    fontSize = 12.sp,
                    fontFamily = ThmanyahSansFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // FLOATING TOP CAPSULE HEADER SYSTEM (نظام الكبسولات العلوية الفاخرة)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title/Brand Floating Capsule (First -> Right side)
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppleGraphite.copy(alpha = 0.94f))
                        .border(
                            width = 1.dp,
                            color = AppleSteel,
                            shape = RoundedCornerShape(20.dp)
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
                        color = ApplePorcelain
                    )
                }

                // Standalone circular floating capsule back button (Second -> Left side)
                val backInteraction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AppleGraphite.copy(alpha = 0.94f))
                        .border(
                            width = 1.dp,
                            color = AppleSteel,
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
                        tint = ApplePorcelain,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
