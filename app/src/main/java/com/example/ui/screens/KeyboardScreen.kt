package com.example.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognizerIntent
import android.util.Log
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ui.theme.*
import java.net.URLEncoder

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
    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    
    val currentBgBase64 by rememberUpdatedState(bgBase64)
    val currentTheme by rememberUpdatedState(theme)
    val currentOpacity by rememberUpdatedState(opacity)
    val currentBlur by rememberUpdatedState(blur)
    
    // Immersive landscape locks
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        
        // Force Landscape Immersive Mode
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
            
            // Restore standard status and system bars on exit
            val windowObj = activity?.window
            val viewObj = windowObj?.decorView
            if (windowObj != null && viewObj != null) {
                val insetsController = WindowCompat.getInsetsController(windowObj, viewObj)
                insetsController.show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            }
        }
    }

    // Native Speech Recognizer integration
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.firstOrNull() ?: ""
            if (spokenText.isNotEmpty()) {
                val encodedText = URLEncoder.encode("$spokenText ", "UTF-8")
                webViewRef.value?.post {
                    webViewRef.value?.evaluateJavascript(
                        "if (window.sendCmd) { window.sendCmd('paste', 'text=$encodedText'); }",
                        null
                    )
                }
            }
        }
    }

    // Sync live connection status to WebView UI in real time
    LaunchedEffect(isConnected, tabletIp) {
        webViewRef.value?.evaluateJavascript(
            "if (window.setConnectionStatus) { window.setConnectionStatus($isConnected, '$tabletIp'); }",
            null
        )
    }

    // Apply settings if user updates them (and on page loaded)
    LaunchedEffect(theme, opacity, blur, bgBase64) {
        webViewRef.value?.let { webView ->
            injectSettings(webView, theme, opacity, blur, bgBase64)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        databaseEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        cacheMode = WebSettings.LOAD_NO_CACHE
                    }
                    clearCache(true)

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            // Initial preference injection
                            injectSettings(this@apply, theme, opacity, blur, bgBase64)
                            // Apply current live connection status
                            view?.evaluateJavascript(
                                "if (window.setConnectionStatus) { window.setConnectionStatus($isConnected, '$tabletIp'); }",
                                null
                            )
                        }
                    }

                    // Register native bridge for ultra fast communication and system capabilities
                    addJavascriptInterface(
                        WebAppInterface(
                            context = context,
                            onCommand = onSendCommand,
                            onVibrate = { ms ->
                                val finalMs = if (configuredVibration > 0) configuredVibration else ms
                                if (finalMs > 0) {
                                    vibratePhone(context, finalMs.toLong())
                                }
                            },
                            onSpeech = {
                                try {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
                                    }
                                    speechRecognizerLauncher.launch(intent)
                                } catch (e: Exception) {
                                    Log.e("KeyboardScreen", "Speech recognition launch failed", e)
                                }
                            },
                            onToggleFullscreen = {
                                // Handled natively or ignored in immersive landscape
                            },
                            onExit = onBack,
                            getCustomBgProvider = { currentBgBase64 },
                            getThemeProvider = { currentTheme },
                            getOpacityProvider = { currentOpacity },
                            getBlurProvider = { currentBlur }
                        ),
                        "AndroidInterface"
                    )

                    loadUrl("file:///android_asset/keyboard.html")
                    webViewRef.value = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun injectSettings(webView: WebView, theme: String, opacity: Float, blur: Int, bgBase64: String) {
    webView.post {
        // Set HTML theme attribute
        webView.evaluateJavascript("document.documentElement.setAttribute('data-theme', '$theme');", null)
        
        // Invoke applyCustomBackground using native AndroidInterface bridge to stream base64 safely
        val jsCode = """
            if (window.applyCustomBackground) {
                var bg = (window.AndroidInterface && window.AndroidInterface.getCustomBg) ? window.AndroidInterface.getCustomBg() : "";
                window.applyCustomBackground(bg, '$theme', $opacity, $blur);
            }
        """.trimIndent()
        webView.evaluateJavascript(jsCode, null)
    }
}

@Suppress("DEPRECATION")
private fun vibratePhone(context: Context, ms: Long) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    } catch (e: Exception) {
        // Safe check
    }
}

class WebAppInterface(
    private val context: Context,
    private val onCommand: (String, String) -> Unit,
    private val onVibrate: (Int) -> Unit,
    private val onSpeech: () -> Unit,
    private val onToggleFullscreen: () -> Unit,
    private val onExit: () -> Unit,
    private val getCustomBgProvider: () -> String = { "" },
    private val getThemeProvider: () -> String = { "royal_classic" },
    private val getOpacityProvider: () -> Float = { 0.85f },
    private val getBlurProvider: () -> Int = { 0 }
) {
    @JavascriptInterface
    fun getCustomBg(): String = getCustomBgProvider()

    @JavascriptInterface
    fun getTheme(): String = getThemeProvider()

    @JavascriptInterface
    fun getOpacity(): Float = getOpacityProvider()

    @JavascriptInterface
    fun getBlur(): Int = getBlurProvider()

    @JavascriptInterface
    fun sendCommand(action: String, extraParams: String) {
        onCommand(action, extraParams)
    }

    @JavascriptInterface
    fun vibrate(ms: Int) {
        onVibrate(ms)
    }

    @JavascriptInterface
    fun startSpeechRecognition() {
        onSpeech()
    }

    @JavascriptInterface
    fun toggleFullscreen() {
        onToggleFullscreen()
    }

    @JavascriptInterface
    fun exitKeyboard() {
        onExit()
    }
}
