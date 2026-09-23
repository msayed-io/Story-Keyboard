@file:OptIn(com.google.accompanist.permissions.ExperimentalPermissionsApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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

@Composable
fun CameraScannerView(
    onQrScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    
    var hasFiredScan by remember { mutableStateOf(false) }
    var activeCamera by remember { mutableStateOf<Camera?>(null) }
    var isTorchEnabled by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
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
                    
                    // Tap-to-focus and automatic lighting optimization
                    setOnTouchListener { _, event ->
                        if (event.action == android.view.MotionEvent.ACTION_UP) {
                            val cameraControl = activeCamera?.cameraControl
                            if (cameraControl != null) {
                                val factory = meteringPointFactory
                                val point = factory.createPoint(event.x, event.y)
                                val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
                                    .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS)
                                    .build()
                                cameraControl.startFocusAndMetering(action)
                            }
                        }
                        true
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    
                    // Force high definition resolution for incredible sharpness and lightning fast QR recognition
                    val preview = Preview.Builder()
                        .setTargetResolution(android.util.Size(1280, 720))
                        .build()
                        .also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(android.util.Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(cameraExecutor, QrCodeAnalyzer { qrText ->
                                if (!hasFiredScan) {
                                    hasFiredScan = true
                                    ContextCompat.getMainExecutor(context).execute {
                                        onQrScanned(qrText)
                                    }
                                }
                            })
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
                    } catch (e: Exception) {
                        Log.e("CameraScannerView", "Use case binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )

        // Scanning Overlay with Custom Design
        ScanningOverlay(modifier = Modifier.fillMaxSize())

        // Premium Floating Torch/Flashlight controller (لتحسين الإضاءة وتسهيل الالتقاط السريع)
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
    }
}

@Composable
fun ScanningOverlay(modifier: Modifier = Modifier) {
    val strokeColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        // Scan target square in center
        val boxSize = size.width * 0.65f
        val left = (width - boxSize) / 2
        val top = (height - boxSize) / 2
        val right = left + boxSize
        val bottom = top + boxSize

        // 1. Draw dim background screen
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            size = size
        )

        // 2. Clear out the scanning target square
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(boxSize, boxSize),
            cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
            blendMode = BlendMode.Clear
        )

        // 3. Glowing corners (Arabic literary Golden Brackets)
        val lineLength = 28.dp.toPx()
        val thickness = 5.dp.toPx()
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
    }

    // Centered helper text at the bottom of scanner screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = Color.Black.copy(alpha = 0.82f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Text(
                text = "وجهي الكاميرا نحو رمز QR على شاشة التابلت ❦",
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = ThmanyahSansFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalGetImage::class)
class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (rawValue != null) {
                            onQrCodeScanned(rawValue)
                            break
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
}
