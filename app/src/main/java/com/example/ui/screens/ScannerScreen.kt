@file:OptIn(com.google.accompanist.permissions.ExperimentalPermissionsApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.ZoomSuggestionOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onQrScanned: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val isDark = MaterialTheme.colorScheme.background == Color(0xFF111718)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .linenBackground(isDark)
    ) {
        // Main camera/rationale view - full screen bleed so there's no sharp cutoff!
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                cameraPermissionState.status.isGranted -> {
                    CameraScannerView(
                        onQrScanned = onQrScanned,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                cameraPermissionState.status.shouldShowRationale -> {
                    PermissionRationaleView(
                        onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                    )
                }
                else -> {
                    PermissionRequestView(
                        onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                    )
                }
            }
        }

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

        // FLOATING TOP CAPSULE HEADER SYSTEM (نظام الكبسولات العلوية الطافية)
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
                        text = "اقتران جهاز التابلت",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = ThmanyahSerifDisplayFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Standalone circular floating capsule back button (Second -> Left side)
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
                        .testTag("btn_scanner_back"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionRequestView(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = "ماسح الـ QR",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "مطلوب صلاحية الكاميرا",
            fontSize = 22.sp,
            fontFamily = ThmanyahSerifDisplayFontFamily,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "يرجى منح صلاحية استخدام الكاميرا لمسح رمز الاقتران بالتابلت لاسلكياً والبدء في الكتابة على الفور.",
            fontSize = 14.sp,
            fontFamily = ThmanyahSansFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        val btnInteraction = remember { MutableInteractionSource() }
        Button(
            onClick = onRequestPermission,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .appleElasticPinch(btnInteraction)
                .testTag("btn_request_permission"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            interactionSource = btnInteraction
        ) {
            Text(
                text = "السماح باستخدام الكاميرا ❦",
                fontSize = 15.sp,
                fontFamily = ThmanyahSansFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PermissionRationaleView(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = "ماسح الـ QR",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "لماذا نحتاج الكاميرا؟",
            fontSize = 22.sp,
            fontFamily = ThmanyahSerifDisplayFontFamily,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "تطبيق كيبورد الحكايات يستخدم الكاميرا حصرياً لمسح الـ QR للاتصال الفوري والسريع بجهاز التابلت المحلي دون كتابة معقدة لعنوان الـ IP.",
            fontSize = 14.sp,
            fontFamily = ThmanyahSansFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(32.dp))

        val btnInteraction = remember { MutableInteractionSource() }
        Button(
            onClick = onRequestPermission,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .appleElasticPinch(btnInteraction)
                .testTag("btn_request_permission_rationale"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            interactionSource = btnInteraction
        ) {
            Text(
                text = "متابعة وطلب الإذن ❦",
                fontSize = 15.sp,
                fontFamily = ThmanyahSansFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
@Composable
fun CameraScannerView(
    onQrScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    
    var hasFiredScan by remember { mutableStateOf(false) }
    var isSuccessOverlayVisible by remember { mutableStateOf(false) }
    var activeCamera by remember { mutableStateOf<Camera?>(null) }
    var isTorchEnabled by remember { mutableStateOf(false) }
    var currentZoomRatio by remember { mutableFloatStateOf(1f) }
    var maxZoomRatio by remember { mutableFloatStateOf(5f) }
    var previewViewInstance by remember { mutableStateOf<PreviewView?>(null) }

    val qrAnalyzer = remember {
        QrCodeAnalyzer(
            getCamera = { activeCamera },
            onQrCodeScanned = { qrText ->
                if (!hasFiredScan) {
                    hasFiredScan = true

                    // 1. Instant subtle haptic feedback
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                            vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                v?.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                @Suppress("DEPRECATION")
                                v?.vibrate(60)
                            }
                        }
                    } catch (_: Exception) {}

                    // 2. Visual feedback and dispatch callback
                    ContextCompat.getMainExecutor(context).execute {
                        isSuccessOverlayVisible = true
                        onQrScanned(qrText)
                    }
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            qrAnalyzer.close()
            cameraExecutor.shutdown()
        }
    }

    // Stable one-time camera lifecycle binding (Does NOT rebind on recompositions!)
    LaunchedEffect(previewViewInstance, lifecycleOwner) {
        val previewView = previewViewInstance ?: return@LaunchedEffect
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // State-of-the-art Resolution Strategy: Full HD (1920x1080) for razor-sharp QR scanning from a distance
            val resolutionSelector = ResolutionSelector.Builder()
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                .setResolutionStrategy(
                    ResolutionStrategy(
                        android.util.Size(1920, 1080),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                    )
                )
                .build()

            val preview = Preview.Builder()
                .setResolutionSelector(resolutionSelector)
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val imageAnalysis = ImageAnalysis.Builder()
                .setResolutionSelector(resolutionSelector)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor, qrAnalyzer)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
                activeCamera = camera

                // Continuous Auto-Focus (AF) and Center Auto-Exposure (AE)
                val factory = SurfaceOrientedMeteringPointFactory(1f, 1f)
                val centerPoint = factory.createPoint(0.5f, 0.5f)
                val action = FocusMeteringAction.Builder(centerPoint, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
                    .setAutoCancelDuration(2, TimeUnit.SECONDS)
                    .build()
                camera.cameraControl.startFocusAndMetering(action)

                // Track live zoom state
                camera.cameraInfo.zoomState.observe(lifecycleOwner) { state ->
                    if (state != null) {
                        currentZoomRatio = state.zoomRatio
                        maxZoomRatio = state.maxZoomRatio
                    }
                }
            } catch (e: Exception) {
                Log.e("CameraScannerView", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.PERFORMANCE

                    // Smooth Pinch-to-zoom gesture
                    val scaleGestureDetector = ScaleGestureDetector(
                        ctx,
                        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                            override fun onScale(detector: ScaleGestureDetector): Boolean {
                                val camera = activeCamera ?: return false
                                val current = camera.cameraInfo.zoomState.value?.zoomRatio ?: 1f
                                val target = (current * detector.scaleFactor).coerceIn(1f, maxZoomRatio)
                                camera.cameraControl.setZoomRatio(target)
                                return true
                            }
                        }
                    )

                    setOnTouchListener { view, event ->
                        scaleGestureDetector.onTouchEvent(event)
                        if (event.action == MotionEvent.ACTION_UP && !scaleGestureDetector.isInProgress) {
                            val cameraControl = activeCamera?.cameraControl
                            if (cameraControl != null) {
                                val point = meteringPointFactory.createPoint(event.x, event.y)
                                val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
                                    .setAutoCancelDuration(3, TimeUnit.SECONDS)
                                    .build()
                                cameraControl.startFocusAndMetering(action)
                            }
                            view.performClick()
                        }
                        true
                    }
                }.also {
                    previewViewInstance = it
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Scanning Overlay with Animated Laser Beam & Quick Controls
        ScanningOverlay(
            currentZoom = currentZoomRatio,
            onZoomChange = { targetZoom ->
                activeCamera?.cameraControl?.setZoomRatio(targetZoom.coerceIn(1f, maxZoomRatio))
            },
            modifier = Modifier.fillMaxSize()
        )

        // Premium Floating Torch/Flashlight controller (Toggles torch without resetting camera)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp, end = 24.dp)
                .statusBarsPadding(),
            contentAlignment = Alignment.BottomEnd
        ) {
            val torchInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        if (isTorchEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.95f)
                        else Color.Black.copy(alpha = 0.65f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isTorchEnabled) Color.White else Color.White.copy(alpha = 0.25f),
                        shape = CircleShape
                    )
                    .appleElasticPinch(torchInteraction)
                    .clickable(
                        interactionSource = torchInteraction,
                        indication = null,
                        onClick = {
                            val nextState = !isTorchEnabled
                            isTorchEnabled = nextState
                            activeCamera?.cameraControl?.enableTorch(nextState)
                        }
                    )
                    .testTag("btn_scanner_torch"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlashlightOn,
                    contentDescription = "كشاف الإضاءة",
                    tint = if (isTorchEnabled) Color.Black else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Instant Success Confirmation Overlay
        AnimatedVisibility(
            visible = isSuccessOverlayVisible,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "تم المسح",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "تم التقاط الرمز بنجاح! ❦",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontFamily = ThmanyahSerifDisplayFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ScanningOverlay(
    currentZoom: Float,
    onZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val strokeColor = MaterialTheme.colorScheme.primary

    // Animated laser beam sweeping vertically
    val infiniteTransition = rememberInfiniteTransition(label = "scannerLaser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserProgress"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Viewfinder box (Generous size for effortless alignment)
            val boxSize = size.width * 0.72f
            val left = (width - boxSize) / 2
            val top = (height - boxSize) / 2.3f
            val right = left + boxSize
            val bottom = top + boxSize

            // 1. Dim background mask
            drawRect(
                color = Color.Black.copy(alpha = 0.52f),
                size = size
            )

            // 2. Clear out the viewfinder window
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(boxSize, boxSize),
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                blendMode = BlendMode.Clear
            )

            // 3. Four Golden Arabic Brackets framing the viewfinder
            val lineLength = 32.dp.toPx()
            val thickness = 4.5.dp.toPx()
            val r = 24.dp.toPx()

            // Top Left Corner
            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(left, top + lineLength)
                    lineTo(left, top + r)
                    quadraticTo(left, top, left + r, top)
                    lineTo(left + lineLength, top)
                },
                color = strokeColor,
                style = Stroke(width = thickness)
            )

            // Top Right Corner
            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(right - lineLength, top)
                    lineTo(right - r, top)
                    quadraticTo(right, top, right, top + r)
                    lineTo(right, top + lineLength)
                },
                color = strokeColor,
                style = Stroke(width = thickness)
            )

            // Bottom Left Corner
            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(left, bottom - lineLength)
                    lineTo(left, bottom - r)
                    quadraticTo(left, bottom, left + r, bottom)
                    lineTo(left + lineLength, bottom)
                },
                color = strokeColor,
                style = Stroke(width = thickness)
            )

            // Bottom Right Corner
            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(right - lineLength, bottom)
                    lineTo(right - r, bottom)
                    quadraticTo(right, bottom, right, bottom - r)
                    lineTo(right, bottom - lineLength)
                },
                color = strokeColor,
                style = Stroke(width = thickness)
            )

            // 4. Sweeping Laser Beam Line with glow gradient
            val laserY = top + (boxSize * laserProgress)
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        strokeColor.copy(alpha = 0.3f),
                        strokeColor.copy(alpha = 0.95f),
                        strokeColor.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                start = Offset(left + 12.dp.toPx(), laserY),
                end = Offset(right - 12.dp.toPx(), laserY),
                strokeWidth = 3.dp.toPx()
            )
        }

        // Bottom Controls Container: Quick Zoom Pills + Guidance Text
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Quick Zoom Selector Pills (1x, 2x, 3x)
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(
                        width = 0.5.dp,
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val zoomPresets = listOf(1f to "1x", 2f to "2x", 3f to "3x")
                zoomPresets.forEach { (zoomValue, label) ->
                    val isSelected = kotlin.math.abs(currentZoom - zoomValue) < 0.4f
                    val zoomInteraction = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) strokeColor
                                else Color.Transparent
                            )
                            .clickable(
                                interactionSource = zoomInteraction,
                                indication = null,
                                onClick = { onZoomChange(zoomValue) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = ThmanyahSansFontFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Guidance badge
            Surface(
                color = Color.Black.copy(alpha = 0.78f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "وجّه الكاميرا نحو رمز QR على شاشة التابلت ❦",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = ThmanyahSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "التقاط فوري فائق السرعة • تكبير تلقائي ذكي",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontFamily = ThmanyahSansFontFamily,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
class QrCodeAnalyzer(
    private val getCamera: () -> Camera?,
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val isScanned = AtomicBoolean(false)

    // Google ML Kit Auto-Zoom Suggestion (يكتشف الكود البعيد ويكبر العدسة آلياً فوراً!)
    private val zoomSuggestionOptions = ZoomSuggestionOptions.Builder { suggestedZoomRatio ->
        val camera = getCamera() ?: return@Builder false
        val maxZoom = camera.cameraInfo.zoomState.value?.maxZoomRatio ?: 1f
        val clampedZoom = suggestedZoomRatio.coerceIn(1f, minOf(maxZoom, 6.0f))
        camera.cameraControl.setZoomRatio(clampedZoom)
        true
    }.setMaxSupportedZoomRatio(6.0f).build()

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAllPotentialBarcodes() // Enables immediate detection of distant, partial, or low-contrast QR codes
            .setZoomSuggestionOptions(zoomSuggestionOptions)
            .build()
    )

    override fun analyze(imageProxy: ImageProxy) {
        if (isScanned.get()) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (isScanned.get()) return@addOnSuccessListener
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (!rawValue.isNullOrBlank()) {
                            if (isScanned.compareAndSet(false, true)) {
                                onQrCodeScanned(rawValue)
                                break
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("QrCodeAnalyzer", "MLKit Barcode scanning failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    fun close() {
        try {
            scanner.close()
        } catch (e: Exception) {
            Log.e("QrCodeAnalyzer", "Failed to close MLKit scanner", e)
        }
    }
}
