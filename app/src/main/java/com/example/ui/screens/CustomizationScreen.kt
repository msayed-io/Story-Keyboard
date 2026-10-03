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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun CustomizationScreen(
    currentTheme: String,
    currentOpacity: Float,
    currentBlur: Int,
    hapticIntensity: Int,
    hasCustomBg: Boolean,
    bgBase64: String,
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
                            Spacer(modifier = Modifier.height(22.dp))

                            // ===== LIVE PREVIEW =====
                            // معاينة حيّة مصغّرة: نفس الطبقة الزجاجية ونفس ألوان
                            // الكيبورد، فتظهر النتيجة كما ستكون عليه بالضبط.
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "معاينة حيّة",
                                        fontSize = 14.sp,
                                        fontFamily = ThmanyahSansFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = ApplePorcelain
                                    )
                                    Text(
                                        text = "كما ستظهر على كيبوردك",
                                        fontSize = 11.sp,
                                        fontFamily = ThmanyahSansFontFamily,
                                        color = AppleAsh
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    KeyboardLivePreview(
                                        bgBase64 = bgBase64,
                                        themeName = currentTheme,
                                        opacity = currentOpacity,
                                        blur = currentBlur,
                                        modifier = Modifier.testTag("live_preview")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

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
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "كلما قلّلتِ النسبة، ظهرت صورتك أكثر خلف الأزرار",
                                    fontSize = 11.sp,
                                    fontFamily = ThmanyahSansFontFamily,
                                    color = AppleAsh
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

// =============================================================================
//  المعاينة الحيّة المصغّرة
//
//  إطار بحجم شاشة الجهاز نفسه (نفس النسب) بداخله نفس الطبقة الزجاجية المستخدمة
//  على الكيبورد، وفوقها صفوف مفاتيح مصغّرة بألوان الثيم الحقيقي — فما تراه
//  الكاتبة هنا هو ما ستراه على الكيبورد بالضبط.
// =============================================================================
@Composable
private fun KeyboardLivePreview(
    bgBase64: String,
    themeName: String,
    opacity: Float,
    blur: Int,
    modifier: Modifier = Modifier
) {
    val palette = rememberThemePalette(themeName)
    val source = remember(bgBase64) { decodeBase64Bitmap(bgBase64) }
    val configuration = LocalConfiguration.current
    val aspect = if (configuration.screenHeightDp > 0) {
        configuration.screenWidthDp.toFloat() / configuration.screenHeightDp.toFloat()
    } else {
        0.5f
    }
    val shape = RoundedCornerShape(12.dp)
    val keyShape = RoundedCornerShape(2.dp)

    // نفس محرّك الكيبورد بالحرف، لكن بحجم المعاينة: نفس النسب والتمويه والحدّ الرفيع.
    val density = androidx.compose.ui.platform.LocalDensity.current
    val previewWidthPx = remember(density) { with(density) { 104.dp.roundToPx() } }
    val previewHeightPx = remember(previewWidthPx, aspect) {
        (previewWidthPx / aspect.coerceAtLeast(0.05f)).roundToInt().coerceAtLeast(1)
    }
    val screenSizePx = rememberScreenSizePx()
    val blurForPreview = remember(blur, previewWidthPx, screenSizePx.first) {
        if (screenSizePx.first <= 0) 0 else {
            (blur.toFloat() * previewWidthPx / screenSizePx.first).roundToInt().coerceIn(0, blur)
        }
    }
    val glassImage = rememberGlassImage(
        sourceBitmap = source,
        sourceKey = "preview-${bgBase64.length}-${bgBase64.hashCode()}",
        targetWidthPx = previewWidthPx,
        targetHeightPx = previewHeightPx,
        blurRadiusPx = blurForPreview
    )
    var previewOrigin by remember { mutableStateOf(Offset.Zero) }
    val sampler = remember(glassImage, opacity, previewOrigin) {
        glassImage?.let {
            GlassSampler(
                image = it,
                pxPerNodePx = 1f,
                originInWindow = previewOrigin,
                opacity = opacity
            )
        }
    }
    val previewTint = GlassMath.surfaceTintAlpha(opacity)

    CompositionLocalProvider(LocalGlassSampler provides sampler) {
    Box(
        modifier = modifier
            .width(104.dp)
            .aspectRatio(aspect)
            .onGloballyPositioned { previewOrigin = it.positionInWindow() }
            .clip(shape)
            .border(1.dp, Color.White.copy(alpha = 0.16f), shape)
    ) {
        // الصورة واضحة، والتمويه داخل المفاتيح فقط — كما على الكيبورد تماماً.
        GlassWallpaper(bitmap = source, modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // الشريط العلوي
            Box(
                modifier = Modifier
                    .weight(0.12f)
                    .fillMaxWidth()
                    .clip(keyShape)
                    .glassSurface(
                        sampler = LocalGlassSampler.current,
                        fillColor = palette.surfaceBackground,
                        tintAlpha = previewTint,
                        radius = 4.dp,
                        borderColor = palette.keycapBorder,
                        borderWidth = 0.5.dp,
                        rimWidth = 0.5.dp
                    )
            )

            // صفوف المفاتيح الثلاثة
            MiniKeyRow(keys = 10, palette = palette, shape = keyShape, weight = 0.24f)
            MiniKeyRow(keys = 9, palette = palette, shape = keyShape, weight = 0.24f)
            MiniKeyRow(keys = 7, palette = palette, shape = keyShape, weight = 0.24f)

            // صف المسافة والإدخال
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.16f),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MiniKey(palette = palette, shape = keyShape, modifier = Modifier.weight(1f))
                MiniKey(palette = palette, shape = keyShape, modifier = Modifier.weight(4f))
                MiniKey(palette = palette, shape = keyShape, modifier = Modifier.weight(1f), accent = true)
            }
        }
    }
    }
}

@Composable
private fun ColumnScope.MiniKeyRow(
    keys: Int,
    palette: ThemePalette,
    shape: RoundedCornerShape,
    weight: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .weight(weight),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(keys) {
            MiniKey(palette = palette, shape = shape, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MiniKey(
    palette: ThemePalette,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
    accent: Boolean = false
) {
    val sampler = LocalGlassSampler.current
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(shape)
            .glassSurface(
                sampler = sampler,
                fillColor = if (accent) palette.accentColor else palette.letterKeycapBg,
                tintAlpha = if (accent) {
                    sampler?.accentTintAlpha() ?: 1f
                } else {
                    GlassMath.surfaceTintAlpha(sampler?.opacity ?: 1f)
                },
                radius = 2.dp,
                borderColor = palette.keycapBorder.copy(alpha = 0.30f),
                borderWidth = 0.5.dp,
                rimWidth = 0.5.dp
            )
    )
}
