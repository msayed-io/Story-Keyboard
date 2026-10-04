package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MediaItem
import com.example.ui.components.LoopingVideoBackground
import com.example.ui.components.MediaPickerModal
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
    bgType: String = "image",
    bgPath: String = "",
    selectedMediaId: String = "",
    curatedItems: List<MediaItem> = emptyList(),
    downloadProgress: Map<String, Int> = emptyMap(),
    onThemeChanged: (String) -> Unit,
    onOpacityChanged: (Float) -> Unit,
    onBlurChanged: (Int) -> Unit,
    onHapticChanged: (Int) -> Unit,
    onSelectCuratedItem: (MediaItem, () -> Unit) -> Unit = { _, _ -> },
    onMediaSelected: (Uri) -> Unit,
    onClearBg: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var isMediaModalOpen by remember { mutableStateOf(false) }

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

        // Scrollable Settings Cards Container مع مسافة علوية كافية تمنع الالتصاق بالكبسولات
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 130.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = minScreenHeight - 162.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                
                // HERO CARD: تظهر المعاينة الحيّة التفاعلية للكيبورد فقط عند رفع وسائط مخصصة (صورة أو فيديو)
                if (hasCustomBg) {
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = AppleCarbon),
                        border = BorderStroke(1.dp, AppleSteel),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_card_preview")
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = "معاينة الكيبورد",
                                        tint = ApplePorcelain,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "المعاينة الحيّة التفاعلية",
                                        fontSize = 15.sp,
                                        fontFamily = ThmanyahSansFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = ApplePorcelain
                                    )
                                }
                                Text(
                                    text = if (bgType == "video") "بث فيديو حي ❦" else "كما يظهر على كيبوردكِ",
                                    fontSize = 11.sp,
                                    fontFamily = ThmanyahSansFontFamily,
                                    color = AppleAsh
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                KeyboardLivePreview(
                                    bgBase64 = bgBase64,
                                    bgType = bgType,
                                    bgPath = bgPath,
                                    themeName = currentTheme,
                                    opacity = currentOpacity,
                                    blur = currentBlur,
                                    modifier = Modifier.testTag("live_preview")
                                )
                            }
                        }
                    }
                }

                // SECTION 1: بطاقة الخلفية المخصصة والشفافية مع نافذة الاختيار الفاخرة
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
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "وسائط الخلفية",
                                    tint = ApplePorcelain,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "خلفية الكيبورد المخصصة",
                                    fontSize = 15.sp,
                                    fontFamily = ThmanyahSansFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = ApplePorcelain
                                )
                            }
                            
                            // أزرار التحكم: فتح نافذة الوسائط الأدبية والمحلية الفاخرة + زر المسح
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val uploadInteraction = remember { MutableInteractionSource() }
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AppleSteel.copy(alpha = 0.5f))
                                        .border(1.dp, AppleSteel, RoundedCornerShape(12.dp))
                                        .appleElasticPinch(uploadInteraction)
                                        .clickable(
                                            interactionSource = uploadInteraction,
                                            indication = null,
                                            onClick = {
                                                isMediaModalOpen = true
                                            }
                                        )
                                        .testTag("btn_select_bg"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = "اختيار وسائط مخصصة",
                                        tint = ApplePorcelain,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                if (hasCustomBg) {
                                    val deleteInteraction = remember { MutableInteractionSource() }
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                            .appleElasticPinch(deleteInteraction)
                                            .clickable(
                                                interactionSource = deleteInteraction,
                                                indication = null,
                                                onClick = onClearBg
                                            )
                                            .testTag("btn_clear_bg"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "مسح الخلفية",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
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
                                        fontSize = 13.sp,
                                        fontFamily = ThmanyahSansFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = ApplePorcelain
                                    )
                                    Text(
                                        text = "${(currentOpacity * 100).toInt()}%",
                                        fontSize = 13.sp,
                                        fontFamily = ThmanyahSansFontFamily,
                                        color = AppleWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                PremiumSlider(
                                    value = currentOpacity,
                                    onValueChange = onOpacityChanged,
                                    valueRange = 0.05f..0.99f,
                                    modifier = Modifier.testTag("slider_opacity")
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "كلما قلّلتِ النسبة، ظهرت الخلفية وحركتها أكثر خلف الأزرار",
                                    fontSize = 11.sp,
                                    fontFamily = ThmanyahSansFontFamily,
                                    color = AppleAsh
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // BACKGROUND BLUR SLIDER
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "مدى ضبابية الخلفية وغبشها (Blur)",
                                        fontSize = 13.sp,
                                        fontFamily = ThmanyahSansFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = ApplePorcelain
                                    )
                                    Text(
                                        text = "${currentBlur}px",
                                        fontSize = 13.sp,
                                        fontFamily = ThmanyahSansFontFamily,
                                        color = AppleWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                PremiumSlider(
                                    value = currentBlur.toFloat(),
                                    onValueChange = { onBlurChanged(it.toInt()) },
                                    valueRange = 0f..25f,
                                    modifier = Modifier.testTag("slider_blur")
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "لإضافة صورة أو فيديو مخصص خلف أزرار الكيبورد، اضغطي على زر الرفع في الأعلى ❦",
                                fontSize = 12.sp,
                                fontFamily = ThmanyahSansFontFamily,
                                color = AppleAsh,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // SECTION 2: اهتزاز مفاتيح الكيبورد مع سلايدر فخم
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
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "اهتزاز مفاتيح الكيبورد",
                                fontSize = 15.sp,
                                fontFamily = ThmanyahSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = ApplePorcelain
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "شدة الاهتزاز عند النقر والكتابة",
                                fontSize = 13.sp,
                                fontFamily = ThmanyahSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = ApplePorcelain
                            )
                            Text(
                                text = if (hapticIntensity == 0) "إيقاف الاهتزاز" else "${hapticIntensity}ms",
                                fontSize = 13.sp,
                                fontFamily = ThmanyahSansFontFamily,
                                color = AppleWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        PremiumSlider(
                            value = hapticIntensity.toFloat(),
                            onValueChange = { onHapticChanged(it.toInt()) },
                            valueRange = 0f..100f,
                            modifier = Modifier.testTag("slider_haptic")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

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

        // FLOATING TOP CAPSULE HEADER SYSTEM
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title/Brand Floating Capsule - 100% Full Pill Curve
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(CircleShape)
                        .background(AppleGraphite.copy(alpha = 0.94f))
                        .border(
                            width = 1.dp,
                            color = AppleSteel,
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
                            fontSize = 14.sp
                        ),
                        color = ApplePorcelain
                    )
                }

                // Standalone circular floating capsule back button
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

        // نافذة اختيار الوسائط المنبثقة العائمة (المكتبة الأدبية ومحلي)
        MediaPickerModal(
            isOpen = isMediaModalOpen,
            selectedMediaId = selectedMediaId,
            curatedItems = curatedItems,
            downloadProgress = downloadProgress,
            onSelectCuratedItem = { item ->
                onSelectCuratedItem(item) {
                    isMediaModalOpen = false
                }
            },
            onSelectLocalMedia = { uri ->
                onMediaSelected(uri)
                isMediaModalOpen = false
            },
            onDismiss = { isMediaModalOpen = false }
        )
    }
}

// =============================================================================
//  سلايدر فاخر مخصص بالكامل (Premium Custom Slider Component)
// =============================================================================
@Composable
fun PremiumSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
) {
    val density = LocalDensity.current
    var widthPx by remember { mutableStateOf(0) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AppleSteel.copy(alpha = 0.35f))
                .border(1.dp, AppleSteel.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                .onGloballyPositioned { widthPx = it.size.width }
                .pointerInput(valueRange) {
                    detectTapGestures { offset ->
                        if (widthPx > 0) {
                            val fraction = (offset.x / widthPx).coerceIn(0f, 1f)
                            val newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                            onValueChange(newValue)
                        }
                    }
                }
                .pointerInput(valueRange) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (widthPx > 0) {
                                val fraction = (offset.x / widthPx).coerceIn(0f, 1f)
                                val newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                                onValueChange(newValue)
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            if (widthPx > 0) {
                                val fraction = (change.position.x / widthPx).coerceIn(0f, 1f)
                                val newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                                onValueChange(newValue)
                            }
                        }
                    )
                }
        ) {
            val fraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
            
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(if (fraction > 0f) fraction else 0.0001f)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                AppleWhite.copy(alpha = 0.12f),
                                AppleWhite.copy(alpha = 0.40f)
                            )
                        )
                    )
            )
            
            val thumbWidth = 14.dp
            val maxOffsetDp = with(density) { (widthPx).toDp() } - thumbWidth
            val offsetDp = (fraction * maxOffsetDp.value).dp
            
            Box(
                modifier = Modifier
                    .offset(x = offsetDp)
                    .width(thumbWidth)
                    .fillMaxHeight()
                    .padding(2.dp)
                    .background(AppleWhite, RoundedCornerShape(12.dp))
            )
        }
    }
}

// =============================================================================
//  المعاينة الحيّة التفاعلية للكيبورد الحقيقي (تشمل الصور وفيديوهات الخلفية بدقة)
// =============================================================================
@Composable
private fun KeyboardLivePreview(
    bgBase64: String,
    bgType: String,
    bgPath: String,
    themeName: String,
    opacity: Float,
    blur: Int,
    modifier: Modifier = Modifier
) {
    val palette = rememberThemePalette(themeName)
    val bgBitmap = remember(bgBase64) { decodeBase64Bitmap(bgBase64) }

    val refWidthDp = 840.dp
    val refHeightDp = 380.dp
    val density = LocalDensity.current

    val targetWPx = with(density) { refWidthDp.roundToPx() }
    val targetHPx = with(density) { refHeightDp.roundToPx() }

    val previewTargetWidthPx = 640
    val previewTargetHeightPx = 290

    val glassImage = rememberGlassImage(
        sourceBitmap = bgBitmap,
        sourceKey = "preview-real-${bgBase64.length}-${bgBase64.hashCode()}",
        targetWidthPx = previewTargetWidthPx,
        targetHeightPx = previewTargetHeightPx,
        blurRadiusPx = blur
    )

    var containerOriginInWindow by remember { mutableStateOf(Offset.Zero) }

    val sampler = remember(glassImage, opacity, targetWPx, targetHPx, containerOriginInWindow) {
        glassImage?.let {
            GlassSampler(
                image = it,
                containerWidthPx = targetWPx,
                containerHeightPx = targetHPx,
                originInWindow = containerOriginInWindow,
                opacity = opacity
            )
        }
    }

    var currentPreviewMode by remember { mutableStateOf(KeyboardMode.ARABIC) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(840f / 380f)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
                .background(palette.canvasBackground)
        ) {
            // خلفية المعاينة: فيديو حيّ أو صورة ثابتة
            if (bgType == "video" && bgPath.isNotEmpty()) {
                LoopingVideoBackground(
                    videoPathOrUri = bgPath,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (bgBitmap != null) {
                GlassWallpaper(bitmap = bgBitmap, modifier = Modifier.fillMaxSize())
            }

            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val widthPx = constraints.maxWidth
                val heightPx = constraints.maxHeight
                val scale = if (targetWPx > 0) widthPx.toFloat() / targetWPx.toFloat() else 0.4f

                Box(
                    modifier = Modifier.layout { measurable, _ ->
                        val placeable = measurable.measure(
                            Constraints.fixed(targetWPx, targetHPx)
                        )
                        layout(widthPx, heightPx) {
                            placeable.placeWithLayer(0, 0) {
                                scaleX = scale
                                scaleY = scale
                                transformOrigin = TransformOrigin(0f, 0f)
                            }
                        }
                    }
                ) {
                    KeyboardFullDesign(
                        palette = palette,
                        opacity = opacity,
                        bgBitmap = null, // الفيديو أو الصورة يُرسمان كخلفية موحدة في الأعلى
                        bgType = bgType,
                        bgPath = bgPath,
                        glassSampler = sampler,
                        currentMode = currentPreviewMode,
                        onModeChange = { currentPreviewMode = it },
                        isToolbarVisible = false,
                        isConnected = true,
                        tabletIp = "192.168.43.68:8080",
                        onContainerPositioned = { _, origin ->
                            containerOriginInWindow = origin
                        }
                    )
                }
            }
        }
    }
}
