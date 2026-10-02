package com.example.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognizerIntent
import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ui.theme.*
import com.example.ui.theme.ThmanyahSansFontFamily
import com.example.ui.theme.ThmanyahSerifDisplayFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

enum class KeyboardMode {
    ARABIC,
    ENGLISH,
    SYMBOLS,
    TRACKPAD
}

/**
 * GROUND-UP STUDIO-GRADE REDESIGN
 * Ultra-premium Apple Magic Keyboard aesthetic for Android.
 * Precise 100% mathematical symmetry, hardware keycaps, fluid micro-interactions.
 */
@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun KeyboardScreen(
    isConnected: Boolean,
    tabletIp: String,
    theme: String,
    opacity: Float,
    blur: Int,
    bgBase64: String,
    configuredVibration: Int,
    onSendCommand: (String, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentMode by remember { mutableStateOf(KeyboardMode.ARABIC) }
    var isShifted by remember { mutableStateOf(false) }

    // One official path: the tablet shows its remote pointer ONLY while the 🖱️
    // tab is open, and hides it again the moment we leave the tab or the screen.
    LaunchedEffect(currentMode) {
        onSendCommand("mouse_mode", "enabled=${if (currentMode == KeyboardMode.TRACKPAD) 1 else 0}")
    }
    DisposableEffect(Unit) {
        onDispose { onSendCommand("mouse_mode", "enabled=0") }
    }
    var pressedKeyLabel by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Intercept hardware Back button
    BackHandler {
        onBack()
    }

    // Lock Landscape Mode
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        
        val window = activity?.window
        val view = window?.decorView
        if (window != null && view != null) {
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        
        onDispose {
            activity?.requestedOrientation = originalOrientation
            val windowObj = activity?.window
            val viewObj = windowObj?.decorView
            if (windowObj != null && viewObj != null) {
                val insetsController = WindowCompat.getInsetsController(windowObj, viewObj)
                insetsController.show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            }
        }
    }

    // Voice Speech Recognizer
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.firstOrNull() ?: ""
            if (spokenText.isNotEmpty()) {
                val encodedText = URLEncoder.encode("$spokenText ", "UTF-8")
                onSendCommand("paste", "text=$encodedText")
            }
        }
    }

    // Base64 Custom Background Decoder
    val bgBitmap: ImageBitmap? = remember(bgBase64) {
        if (bgBase64.isNotEmpty()) {
            try {
                val bytes = Base64.decode(bgBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    // Preload Realistic Keyboard Click Sound System
    LaunchedEffect(Unit) {
        KeyboardSoundEffect.init(context.applicationContext)
    }

    // Theme Palette
    val palette = rememberThemePalette(theme)

    // Key Press Handler with Authentic Apple Scissor-Switch Sound & Haptic Response
    val handleKeyTap = { label: String, action: String, extraParams: String ->
        val soundType = when {
            label == "مسافة" || label == "Space" -> "space"
            label == "حذف" || label == "Backspace" -> "delete"
            label == "إدخال" || label == "Enter" -> "return"
            else -> "standard"
        }
        KeyboardSoundEffect.playClick(context, soundType)
        vibratePhone(context, configuredVibration)
        pressedKeyLabel = label
        scope.launch {
            delay(150)
            if (pressedKeyLabel == label) {
                pressedKeyLabel = null
            }
        }
        onSendCommand(action, extraParams)
    }

    // Enforce LTR Layout Direction for the entire keyboard screen to prevent mirroring on Arabic system phones
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(palette.canvasBackground)
                .testTag("native_keyboard_screen")
        ) {
        // Optional Background Image
        bgBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(opacity.coerceIn(0.1f, 1f))
            )
        }

        // Ambient Dark Surface Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            palette.surfaceBackground.copy(alpha = 0.90f),
                            palette.canvasBackground.copy(alpha = 0.98f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // =============================================================
            // TOP MINIMAL CONTROL HEADER (44dp Apple Utility Navigation)
            // =============================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(palette.surfaceBackground)
                    .border(1.dp, palette.keycapBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Exit & Live Status Indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(palette.modifierKeycapBg)
                            .clickable(onClick = onBack)
                            .testTag("btn_keyboard_exit"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "خروج",
                            tint = palette.keyGlyphColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Status Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(palette.modifierKeycapBg.copy(alpha = 0.8f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isConnected) (if (tabletIp.isNotEmpty()) tabletIp else "متصل") else "غير متصل",
                            fontSize = 11.sp,
                            fontFamily = ThmanyahSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.keyGlyphColor.copy(alpha = 0.85f)
                        )
                    }
                }

                // SEGMENTED MODE PILLS (عربي | EN | 123 | 🖱️)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.canvasBackground.copy(alpha = 0.7f))
                        .padding(2.dp)
                ) {
                    SegmentedModePill("عربي", currentMode == KeyboardMode.ARABIC, palette) { currentMode = KeyboardMode.ARABIC }
                    SegmentedModePill("EN", currentMode == KeyboardMode.ENGLISH, palette) { currentMode = KeyboardMode.ENGLISH }
                    SegmentedModePill("123", currentMode == KeyboardMode.SYMBOLS, palette) { currentMode = KeyboardMode.SYMBOLS }
                    SegmentedModePill("فأرة 🖱️", currentMode == KeyboardMode.TRACKPAD, palette) { currentMode = KeyboardMode.TRACKPAD }
                }

                // Voice Speech Recognition Button (Apple Circular Control)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(palette.accentColor)
                        .clickable {
                            try {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
                                }
                                speechRecognizerLauncher.launch(intent)
                            } catch (_: Exception) {}
                        }
                        .testTag("btn_voice_input"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "الإملاء الصوتي",
                        tint = palette.accentGlyphColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // =============================================================
            // PERMANENT APPLE-GRADE WRITER SHORTCUTS TOOLBAR (Always Visible)
            // =============================================================
            WriterToolsRow(palette, handleKeyTap)

            Spacer(modifier = Modifier.height(4.dp))

            // =============================================================
            // KEYBOARD CANVAS
            // =============================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (currentMode) {
                    KeyboardMode.ARABIC -> StudioArabicLayout(
                        palette = palette,
                        onKey = handleKeyTap,
                        onSwitchEn = { currentMode = KeyboardMode.ENGLISH },
                        onSwitchSym = { currentMode = KeyboardMode.SYMBOLS }
                    )

                    KeyboardMode.ENGLISH -> StudioEnglishLayout(
                        palette = palette,
                        isShifted = isShifted,
                        onToggleShift = { isShifted = !isShifted },
                        onKey = handleKeyTap,
                        onSwitchAr = { currentMode = KeyboardMode.ARABIC },
                        onSwitchSym = { currentMode = KeyboardMode.SYMBOLS },
                        configuredVibration = configuredVibration
                    )

                    KeyboardMode.SYMBOLS -> StudioSymbolsLayout(
                        palette = palette,
                        onKey = handleKeyTap,
                        onSwitchAr = { currentMode = KeyboardMode.ARABIC }
                    )

                    KeyboardMode.TRACKPAD -> StudioTrackpadLayout(
                        palette = palette,
                        onSendCommand = onSendCommand,
                        vibrate = { vibratePhone(context, configuredVibration) }
                    )
                }

                // Floating Keypop Preview Bubble
                pressedKeyLabel?.let { label ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-12).dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(palette.accentColor)
                            .padding(horizontal = 18.dp, vertical = 6.dp)
                            .shadow(8.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.accentGlyphColor,
                            fontFamily = ThmanyahSansFontFamily
                        )
                    }
                }
            }
        }
    }
}
}

// =========================================================================
// SEGMENTED PILL TAB
// =========================================================================
@Composable
private fun SegmentedModePill(
    label: String,
    isSelected: Boolean,
    palette: ThemePalette,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(26.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) palette.accentColor else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) palette.accentGlyphColor else palette.keyGlyphColor.copy(alpha = 0.75f),
            fontFamily = ThmanyahSansFontFamily
        )
    }
}

// =========================================================================
// PERMANENT WRITER TOOLS ACCESSORY BAR (Apple iPadOS Style with Vector Icons)
// =========================================================================
private data class WriterToolItem(
    val label: String,
    val icon: ImageVector,
    val action: String,
    val params: String
)

@Composable
private fun WriterToolsRow(
    palette: ThemePalette,
    onKey: (String, String, String) -> Unit
) {
    val items = remember {
        listOf(
            WriterToolItem("تراجع", Icons.AutoMirrored.Filled.Undo, "shortcut", "cmd=undo"),
            WriterToolItem("إعادة", Icons.AutoMirrored.Filled.Redo, "shortcut", "cmd=redo"),
            WriterToolItem("قص", Icons.Default.ContentCut, "shortcut", "cmd=cut"),
            WriterToolItem("نسخ", Icons.Default.ContentCopy, "shortcut", "cmd=copy"),
            WriterToolItem("لصق", Icons.Default.ContentPaste, "shortcut", "cmd=paste"),
            WriterToolItem("تحديد", Icons.Default.SelectAll, "shortcut", "cmd=select_all"),
            WriterToolItem("حفظ", Icons.Default.Save, "shortcut", "cmd=save")
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(29.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            WriterToolButton(
                item = item,
                palette = palette,
                modifier = Modifier.weight(1f),
                onClick = { onKey(item.label, item.action, item.params) }
            )
        }
    }
}

@Composable
private fun WriterToolButton(
    item: WriterToolItem,
    palette: ThemePalette,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(stiffness = 850f, dampingRatio = 0.55f),
        label = "tool_scale"
    )

    val shape = RoundedCornerShape(8.dp)
    val topColor = if (isPressed) palette.modifierKeycapBg.copy(alpha = 0.70f) else palette.modifierKeycapBg.copy(alpha = 0.95f)
    val bottomColor = if (isPressed) palette.modifierKeycapBg.copy(alpha = 0.55f) else palette.modifierKeycapBg.copy(alpha = 0.80f)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 0.dp else 1.dp,
                shape = shape,
                clip = false
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(topColor, bottomColor)
                )
            )
            .border(0.6.dp, palette.keycapBorder.copy(alpha = 0.35f), shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = palette.keyGlyphColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = item.label,
                fontSize = 11.sp,
                fontFamily = ThmanyahSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = palette.keyGlyphColor,
                maxLines = 1
            )
        }
    }
}

// =========================================================================
// STUDIO ARABIC LAYOUT (100% Perfectly Symmetrical 12-Column Grid)
// =========================================================================
@Composable
private fun StudioArabicLayout(
    palette: ThemePalette,
    onKey: (String, String, String) -> Unit,
    onSwitchEn: () -> Unit,
    onSwitchSym: () -> Unit
) {
    val row1 = remember { listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "د") }
    val row2 = remember { listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط") }
    val row3 = remember { listOf("ذ", "ئ", "ء", "ؤ", "ر", "ى", "ة", "و", "ز", "ظ") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Row 1 (12 Keys)
        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            row1.forEach { char ->
                KeycapTile(char, Modifier.weight(1f), palette) {
                    val enc = URLEncoder.encode(char, "UTF-8")
                    onKey(char, "paste", "text=$enc")
                }
            }
        }

        // Row 2 (11 Keys Centered)
        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Spacer(modifier = Modifier.weight(0.5f))
            row2.forEach { char ->
                KeycapTile(char, Modifier.weight(1f), palette) {
                    val enc = URLEncoder.encode(char, "UTF-8")
                    onKey(char, "paste", "text=$enc")
                }
            }
            Spacer(modifier = Modifier.weight(0.5f))
        }

        // Row 3 (10 Keys Centered)
        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Spacer(modifier = Modifier.weight(1f))
            row3.forEach { char ->
                KeycapTile(char, Modifier.weight(1f), palette) {
                    val enc = URLEncoder.encode(char, "UTF-8")
                    onKey(char, "paste", "text=$enc")
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        // Row 4 (Slimmer Compact Bottom Control Row)
        Row(modifier = Modifier.fillMaxWidth().weight(0.7f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            KeycapIconTile(
                icon = Icons.AutoMirrored.Filled.Backspace,
                modifier = Modifier.weight(1.5f),
                palette = palette,
                isModifier = true,
                isCapsule = true,
                onKey = { onKey("حذف", "key", "key=Backspace") }
            )

            KeycapTextTile("مسافة", Modifier.weight(7.5f), palette, isCapsule = true) {
                onKey("مسافة", "key", "key=Space")
            }

            KeycapTextTile(".", Modifier.weight(1.5f), palette, isCapsule = true) {
                val enc = URLEncoder.encode(".", "UTF-8")
                onKey(".", "paste", "text=$enc")
            }

            KeycapIconTile(
                icon = Icons.Default.KeyboardReturn,
                modifier = Modifier.weight(1.5f),
                palette = palette,
                isAccent = true,
                isCapsule = true,
                onKey = { onKey("إدخال", "key", "key=Enter") }
            )
        }
    }
}

// =========================================================================
// STUDIO ENGLISH LAYOUT (Clean QWERTY)
// =========================================================================
@Composable
private fun StudioEnglishLayout(
    palette: ThemePalette,
    isShifted: Boolean,
    onToggleShift: () -> Unit,
    onKey: (String, String, String) -> Unit,
    onSwitchAr: () -> Unit,
    onSwitchSym: () -> Unit,
    configuredVibration: Int = 20
) {
    val context = LocalContext.current
    val row1 = remember { listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p") }
    val row2 = remember { listOf("a", "s", "d", "f", "g", "h", "j", "k", "l") }
    val row3 = remember { listOf("z", "x", "c", "v", "b", "n", "m") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Row 1 (10 Keys)
        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            row1.forEach { char ->
                val finalChar = if (isShifted) char.uppercase() else char
                KeycapTile(finalChar, Modifier.weight(1f), palette) {
                    val enc = URLEncoder.encode(finalChar, "UTF-8")
                    onKey(finalChar, "paste", "text=$enc")
                }
            }
        }

        // Row 2 (9 Keys, Centered Indent)
        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Spacer(modifier = Modifier.weight(0.5f))
            row2.forEach { char ->
                val finalChar = if (isShifted) char.uppercase() else char
                KeycapTile(finalChar, Modifier.weight(1f), palette) {
                    val enc = URLEncoder.encode(finalChar, "UTF-8")
                    onKey(finalChar, "paste", "text=$enc")
                }
            }
            Spacer(modifier = Modifier.weight(0.5f))
        }

        // Row 3 (Shift + 7 Keys + Backspace)
        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            KeycapIconTile(
                icon = Icons.Default.ArrowUpward,
                modifier = Modifier.weight(1.5f),
                palette = palette,
                isAccent = isShifted,
                isModifier = !isShifted,
                isCapsule = true,
                onKey = {
                    KeyboardSoundEffect.playClick(context, "standard")
                    vibratePhone(context, configuredVibration)
                    onToggleShift()
                }
            )

            row3.forEach { char ->
                val finalChar = if (isShifted) char.uppercase() else char
                KeycapTile(finalChar, Modifier.weight(1f), palette) {
                    val enc = URLEncoder.encode(finalChar, "UTF-8")
                    onKey(finalChar, "paste", "text=$enc")
                }
            }

            KeycapIconTile(
                icon = Icons.AutoMirrored.Filled.Backspace,
                modifier = Modifier.weight(1.5f),
                palette = palette,
                isModifier = true,
                isCapsule = true,
                onKey = { onKey("Backspace", "key", "key=Backspace") }
            )
        }

        // Row 4 (Slimmer Compact Bottom Control Row)
        Row(modifier = Modifier.fillMaxWidth().weight(0.7f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            KeycapTextTile("Space", Modifier.weight(7f), palette, isCapsule = true) {
                onKey("Space", "key", "key=Space")
            }

            KeycapTextTile(".", Modifier.weight(1.5f), palette, isCapsule = true) {
                val enc = URLEncoder.encode(".", "UTF-8")
                onKey(".", "paste", "text=$enc")
            }

            KeycapIconTile(
                icon = Icons.Default.KeyboardReturn,
                modifier = Modifier.weight(1.5f),
                palette = palette,
                isAccent = true,
                isCapsule = true,
                onKey = { onKey("Enter", "key", "key=Enter") }
            )
        }
    }
}

// =========================================================================
// STUDIO SYMBOLS LAYOUT
// =========================================================================
@Composable
private fun StudioSymbolsLayout(
    palette: ThemePalette,
    onKey: (String, String, String) -> Unit,
    onSwitchAr: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Left Column: Special Symbols (65% width)
        Column(
            modifier = Modifier.weight(0.65f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val symRow1 = remember { listOf("!", "@", "#", "$", "%", "^", "&", "*") }
            val symRow2 = remember { listOf("(", ")", "-", "_", "=", "+", "[", "]") }
            val symRow3 = remember { listOf("{", "}", ";", ":", "<", ">", ",", ".") }
            val symRow4 = remember { listOf("؟", "،", "/", "\\", "|", "`", "~") }

            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                symRow1.forEach { sym ->
                    KeycapTile(sym, Modifier.weight(1f), palette) {
                        val enc = URLEncoder.encode(sym, "UTF-8")
                        onKey(sym, "paste", "text=$enc")
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                symRow2.forEach { sym ->
                    KeycapTile(sym, Modifier.weight(1f), palette) {
                        val enc = URLEncoder.encode(sym, "UTF-8")
                        onKey(sym, "paste", "text=$enc")
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                symRow3.forEach { sym ->
                    KeycapTile(sym, Modifier.weight(1f), palette) {
                        val enc = URLEncoder.encode(sym, "UTF-8")
                        onKey(sym, "paste", "text=$enc")
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                symRow4.forEach { sym ->
                    KeycapTile(sym, Modifier.weight(1f), palette) {
                        val enc = URLEncoder.encode(sym, "UTF-8")
                        onKey(sym, "paste", "text=$enc")
                    }
                }
            }
        }

        // Right Column: Professional Numpad (35% width) - Starting with 1, 2, 3 going down
        Column(
            modifier = Modifier.weight(0.35f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val numRow1 = remember { listOf("1", "2", "3") }
            val numRow2 = remember { listOf("4", "5", "6") }
            val numRow3 = remember { listOf("7", "8", "9") }

            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                numRow1.forEach { num ->
                    KeycapTile(num, Modifier.weight(1f), palette, fontSize = 20.sp) {
                        onKey(num, "paste", "text=$num")
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                numRow2.forEach { num ->
                    KeycapTile(num, Modifier.weight(1f), palette, fontSize = 20.sp) {
                        onKey(num, "paste", "text=$num")
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                numRow3.forEach { num ->
                    KeycapTile(num, Modifier.weight(1f), palette, fontSize = 20.sp) {
                        onKey(num, "paste", "text=$num")
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                KeycapTile("0", Modifier.weight(1f), palette, fontSize = 20.sp) {
                    onKey("0", "paste", "text=0")
                }
                KeycapTile(".", Modifier.weight(1f), palette, fontSize = 20.sp) {
                    val enc = URLEncoder.encode(".", "UTF-8")
                    onKey(".", "paste", "text=$enc")
                }
                KeycapIconTile(
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    modifier = Modifier.weight(1f),
                    palette = palette,
                    isModifier = true,
                    isCapsule = true,
                    onKey = { onKey("Backspace", "key", "key=Backspace") }
                )
            }
        }
    }
}



// =========================================================================
// STUDIO TRACKPAD LAYOUT
// =========================================================================
@Composable
private fun StudioTrackpadLayout(
    palette: ThemePalette,
    onSendCommand: (String, String) -> Unit,
    vibrate: () -> Unit
) {
    val context = LocalContext.current
    // Ultra-responsive 160Hz polling loop using AtomicIntegerArray
    val movementState = remember { java.util.concurrent.atomic.AtomicIntegerArray(2) }
    // Drag = the tablet keeps the left button held while the finger moves.
    var isDragging by remember { mutableStateOf(false) }

    // Leaving the tab can never leave the tablet with a stuck pressed button.
    DisposableEffect(Unit) {
        onDispose {
            onSendCommand("mouse_up", "button=left")
            onSendCommand("mouse_mode", "enabled=0")
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(8) // 120Hz-160Hz ultra-fluid poll & dispatch rate!
            val dx = movementState.getAndSet(0, 0)
            val dy = movementState.getAndSet(1, 0)
            if (dx != 0 || dy != 0) {
                onSendCommand("mouse", "dx=$dx&dy=$dy")
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            palette.letterKeycapBg.copy(alpha = 0.95f),
                            palette.letterKeycapBg.copy(alpha = 0.85f)
                        )
                    )
                )
                .border(1.dp, palette.accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            vibrate()
                            KeyboardSoundEffect.playClick(context, "standard")
                            // Fires immediately — the tablet turns two quick taps
                            // into a native double click, so taps never lag.
                            onSendCommand("mouse_click", "button=left")
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val dx = dragAmount.x.toInt()
                            val dy = dragAmount.y.toInt()
                            movementState.addAndGet(0, dx)
                            movementState.addAndGet(1, dy)
                        }
                    )
                }
                // Two-finger swipe = mouse wheel (one finger keeps moving the pointer).
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        var lastAverageY = 0f
                        var scrolling = false
                        var lastSentAt = 0L
                        while (true) {
                            val event = awaitPointerEvent()
                            val pressed = event.changes.filter { it.pressed }
                            if (pressed.isEmpty()) break
                            if (pressed.size >= 2) {
                                val averageY = pressed.map { it.position.y }.average().toFloat()
                                if (!scrolling) {
                                    scrolling = true
                                } else {
                                    val dy = averageY - lastAverageY
                                    if (abs(dy) >= 1.5f) {
                                        val now = System.currentTimeMillis()
                                        if (now - lastSentAt >= 16L) {
                                            lastSentAt = now
                                            onSendCommand("mouse_scroll", "deltaY=${(-dy * 3f).toInt()}")
                                        }
                                    }
                                    pressed.forEach { it.consume() }
                                }
                                lastAverageY = averageY
                            } else {
                                scrolling = false
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = palette.accentColor,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "اسحبي بإصبع للتحكم بالمؤشر • بإصبعين للتمرير",
                    fontSize = 12.sp,
                    fontFamily = ThmanyahSansFontFamily,
                    color = palette.keyGlyphColor.copy(alpha = 0.85f)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = {
                    vibrate()
                    KeyboardSoundEffect.playClick(context, "standard")
                    onSendCommand("mouse_click", "button=left")
                },
                modifier = Modifier.weight(1f).fillMaxHeight(),
                shape = RoundedCornerShape(22.dp), // Fully rounded capsule shape
                color = palette.accentColor,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "نقرة يسار (Left Click)",
                        fontWeight = FontWeight.Bold,
                        color = palette.accentGlyphColor,
                        fontFamily = ThmanyahSansFontFamily,
                        fontSize = 12.sp
                    )
                }
            }

            Surface(
                onClick = {
                    vibrate()
                    KeyboardSoundEffect.playClick(context, "standard")
                    onSendCommand("mouse_click", "button=right")
                },
                modifier = Modifier.weight(1f).fillMaxHeight(),
                shape = RoundedCornerShape(22.dp), // Fully rounded capsule shape
                color = palette.modifierKeycapBg,
                border = borderFromColor(palette.accentColor.copy(alpha = 0.6f)),
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "نقرة يمين (Right Click)",
                        fontWeight = FontWeight.Bold,
                        color = palette.keyGlyphColor,
                        fontFamily = ThmanyahSansFontFamily,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Drag (hold) toggle: press once to grab, press again to release.
        Surface(
            onClick = {
                val next = !isDragging
                isDragging = next
                vibrate()
                KeyboardSoundEffect.playClick(context, "standard")
                onSendCommand(if (next) "mouse_down" else "mouse_up", "button=left")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            shape = RoundedCornerShape(20.dp),
            color = if (isDragging) palette.accentColor else palette.modifierKeycapBg,
            border = borderFromColor(palette.accentColor.copy(alpha = if (isDragging) 1f else 0.6f)),
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isDragging) Icons.Default.PanTool else Icons.Default.OpenWith,
                        contentDescription = null,
                        tint = if (isDragging) palette.accentGlyphColor else palette.keyGlyphColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isDragging) "إفلات الآن (Release)" else "سحب وإفلات (Drag)",
                        fontWeight = FontWeight.Bold,
                        color = if (isDragging) palette.accentGlyphColor else palette.keyGlyphColor,
                        fontFamily = ThmanyahSansFontFamily,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// =========================================================================
// HIGH-END TACTILE HARDWARE KEYCAP COMPOSABLES (Apple Design Philosophy)
// =========================================================================
@Composable
private fun KeycapTile(
    glyph: String,
    modifier: Modifier = Modifier,
    palette: ThemePalette,
    fontSize: TextUnit = 17.sp,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(stiffness = 850f, dampingRatio = 0.55f),
        label = "key_scale"
    )

    val shape = RoundedCornerShape(9.dp)
    val topColor = if (isPressed) palette.letterKeycapBg.copy(alpha = 0.70f) else palette.letterKeycapBg
    val bottomColor = if (isPressed) palette.letterKeycapBg.copy(alpha = 0.55f) else palette.letterKeycapBg.copy(alpha = 0.82f)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 0.dp else 1.5.dp,
                shape = shape,
                clip = false
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(topColor, bottomColor)
                )
            )
            .border(0.6.dp, palette.keycapBorder.copy(alpha = 0.35f), shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = glyph,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            color = palette.keyGlyphColor,
            fontFamily = ThmanyahSansFontFamily
        )
    }
}

@Composable
private fun KeycapTextTile(
    label: String,
    modifier: Modifier = Modifier,
    palette: ThemePalette,
    isModifier: Boolean = false,
    isCapsule: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(stiffness = 850f, dampingRatio = 0.55f),
        label = "text_key_scale"
    )

    val baseBg = if (isModifier) palette.modifierKeycapBg else palette.letterKeycapBg
    val shape = if (isCapsule) RoundedCornerShape(percent = 50) else RoundedCornerShape(9.dp)
    val paddingModifier = if (isCapsule) Modifier.padding(vertical = 3.dp, horizontal = 2.dp) else Modifier
    val topColor = if (isPressed) baseBg.copy(alpha = 0.70f) else baseBg
    val bottomColor = if (isPressed) baseBg.copy(alpha = 0.55f) else baseBg.copy(alpha = 0.85f)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .then(paddingModifier)
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 0.dp else 1.5.dp,
                shape = shape,
                clip = false
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(topColor, bottomColor)
                )
            )
            .border(0.6.dp, palette.keycapBorder.copy(alpha = 0.35f), shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = palette.keyGlyphColor,
            fontFamily = ThmanyahSansFontFamily
        )
    }
}

@Composable
private fun KeycapIconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    palette: ThemePalette,
    isModifier: Boolean = false,
    isAccent: Boolean = false,
    isCapsule: Boolean = false,
    onKey: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(stiffness = 850f, dampingRatio = 0.55f),
        label = "icon_key_scale"
    )

    val baseBg = when {
        isAccent -> palette.accentColor
        isModifier -> palette.modifierKeycapBg
        else -> palette.letterKeycapBg
    }
    val iconColor = when {
        isAccent -> palette.accentGlyphColor
        else -> palette.keyGlyphColor
    }
    val shape = if (isCapsule) RoundedCornerShape(percent = 50) else RoundedCornerShape(9.dp)
    val paddingModifier = if (isCapsule) Modifier.padding(vertical = 3.dp, horizontal = 2.dp) else Modifier
    val topColor = if (isPressed) baseBg.copy(alpha = 0.75f) else baseBg
    val bottomColor = if (isPressed) baseBg.copy(alpha = 0.60f) else baseBg.copy(alpha = 0.88f)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .then(paddingModifier)
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 0.dp else 1.5.dp,
                shape = shape,
                clip = false
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(topColor, bottomColor)
                )
            )
            .border(
                0.6.dp,
                if (isAccent) palette.accentColor.copy(alpha = 0.6f) else palette.keycapBorder.copy(alpha = 0.35f),
                shape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onKey()
                        val job = scope.launch {
                            delay(350)
                            while (true) {
                                onKey()
                                delay(80)
                            }
                        }
                        tryAwaitRelease()
                        isPressed = false
                        job.cancel()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(17.dp)
        )
    }
}

// =========================================================================
// STUDIO THEME PALETTE SYSTEM
// =========================================================================
private data class ThemePalette(
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
private fun rememberThemePalette(themeName: String): ThemePalette {
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

private fun borderFromColor(color: Color): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(1.dp, color)
}

@Suppress("DEPRECATION")
private fun vibratePhone(context: Context, configuredVibration: Int) {
    try {
        val ms = if (configuredVibration > 0) configuredVibration.toLong() else 20L
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                return
            }
        }
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
    } catch (_: Exception) {}
}

// =========================================================================
// REALISTIC APPLE SCISSOR-SWITCH KEYBOARD CLICK ACOUSTIC ENGINE
// =========================================================================
object KeyboardSoundEffect {
    private var soundPool: SoundPool? = null
    private var clickSoundId: Int = -1
    private var spaceSoundId: Int = -1
    private var deleteSoundId: Int = -1
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val pool = SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build()
            soundPool = pool

            val cacheDir = context.cacheDir
            val clickFile = File(cacheDir, "apple_key_click.wav")
            val spaceFile = File(cacheDir, "apple_key_space.wav")
            val deleteFile = File(cacheDir, "apple_key_delete.wav")

            if (!clickFile.exists()) generateWav(clickFile, freq1 = 1850.0, freq2 = 2700.0, decayMs = 3.2, durationMs = 15.0, amplitude = 9500)
            if (!spaceFile.exists()) generateWav(spaceFile, freq1 = 1050.0, freq2 = 1500.0, decayMs = 5.0, durationMs = 22.0, amplitude = 8500)
            if (!deleteFile.exists()) generateWav(deleteFile, freq1 = 1600.0, freq2 = 2300.0, decayMs = 3.8, durationMs = 18.0, amplitude = 9000)

            clickSoundId = pool.load(clickFile.absolutePath, 1)
            spaceSoundId = pool.load(spaceFile.absolutePath, 1)
            deleteSoundId = pool.load(deleteFile.absolutePath, 1)
            isInitialized = true
        } catch (_: Exception) {}
    }

    fun playClick(context: Context, type: String = "standard") {
        try {
            if (!isInitialized) {
                init(context.applicationContext)
            }

            // Complementary system sound trigger
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            val sysFx = when (type) {
                "space" -> AudioManager.FX_KEYPRESS_SPACEBAR
                "delete" -> AudioManager.FX_KEYPRESS_DELETE
                "return" -> AudioManager.FX_KEYPRESS_RETURN
                else -> AudioManager.FX_KEYPRESS_STANDARD
            }
            audioManager?.playSoundEffect(sysFx, 0.35f)

            // Play synthesized Apple scissor-switch tactile click
            val pool = soundPool ?: return
            val soundId = when (type) {
                "space" -> spaceSoundId
                "delete" -> deleteSoundId
                else -> clickSoundId
            }
            if (soundId > 0) {
                // Natural organic pitch jitter gives distinct acoustic authenticity to each key press
                val pitch = 0.98f + (Random.nextFloat() * 0.05f)
                val vol = if (type == "space") 0.30f else 0.26f // Gentle, elegant, soft & non-disturbing!
                pool.play(soundId, vol, vol, 1, 0, pitch)
            }
        } catch (_: Exception) {}
    }

    private fun generateWav(
        file: File,
        freq1: Double,
        freq2: Double,
        decayMs: Double,
        durationMs: Double,
        amplitude: Int
    ) {
        val sampleRate = 44100
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)
        val decaySec = decayMs / 1000.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = exp(-t / decaySec)
            val s1 = sin(2.0 * PI * freq1 * t)
            val s2 = 0.4 * sin(2.0 * PI * freq2 * t)
            val noise = 0.25 * (Random.nextDouble() * 2.0 - 1.0)
            val raw = (envelope * (s1 + s2 + noise) * amplitude).toInt()
            samples[i] = raw.coerceIn(-32767, 32767).toShort()
        }

        val totalAudioLen = samples.size * 2
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * 2

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
        header[20] = 1; header[21] = 0 // PCM
        header[22] = 1; header[23] = 0 // Mono
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2; header[33] = 0 // Block align
        header[34] = 16; header[35] = 0 // 16-bit
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        FileOutputStream(file).use { fos ->
            fos.write(header)
            val buffer = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
            for (s in samples) {
                buffer.putShort(s)
            }
            fos.write(buffer.array())
        }
    }
}
