package com.example

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.CustomizationScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.KeyboardScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CompanionViewModel

enum class AppScreen {
    HOME,
    SCANNER,
    CUSTOMIZATION,
    KEYBOARD
}

class MainActivity : ComponentActivity() {

    private val viewModel: CompanionViewModel by viewModels()

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            // Collect state with lifecycle-aware flow collector
            val keyboardTheme by viewModel.keyboardTheme.collectAsStateWithLifecycle()
            val isConnected by viewModel.isConnected.collectAsStateWithLifecycle()
            val tabletIp by viewModel.tabletIp.collectAsStateWithLifecycle()
            val tabletPin by viewModel.tabletPin.collectAsStateWithLifecycle()
            val hapticIntensity by viewModel.hapticIntensity.collectAsStateWithLifecycle()
            val keyboardOpacity by viewModel.keyboardOpacity.collectAsStateWithLifecycle()
            val keyboardBlur by viewModel.keyboardBlur.collectAsStateWithLifecycle()
            val keyboardBgBase64 by viewModel.keyboardBgBase64.collectAsStateWithLifecycle()

            MyApplicationTheme(themeId = keyboardTheme) {
                var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Animated screen transitions
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            if (targetState.ordinal > initialState.ordinal) {
                                slideInHorizontally { width -> width } with slideOutHorizontally { width -> -width }
                            } else {
                                slideInHorizontally { width -> -width } with slideOutHorizontally { width -> width }
                            }
                        },
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            AppScreen.HOME -> {
                                HomeScreen(
                                    isConnected = isConnected,
                                    savedIp = tabletIp,
                                    onNavigateToKeyboard = { currentScreen = AppScreen.KEYBOARD },
                                    onNavigateToScanner = { currentScreen = AppScreen.SCANNER },
                                    onNavigateToCustomization = { currentScreen = AppScreen.CUSTOMIZATION }
                                )
                            }
                                    AppScreen.SCANNER -> {
                                        ScannerScreen(
                                            onQrScanned = { qrText ->
                                                // Parse QR string
                                                val (ip, pin) = parseQrCodeContent(qrText)
                                                if (ip.isNotEmpty()) {
                                                    viewModel.saveConnection(ip, pin)
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        "تم الاقتران بالتابلت بنجاح وحفظ الجلسة! 🟢",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    currentScreen = AppScreen.KEYBOARD
                                                } else {
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        "رمز الاقتران غير صالح. الرجاء المحاولة مجدداً.",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    currentScreen = AppScreen.HOME
                                                }
                                            },
                                            onBack = { currentScreen = AppScreen.HOME }
                                        )
                                    }
                            AppScreen.CUSTOMIZATION -> {
                                CustomizationScreen(
                                    currentTheme = keyboardTheme,
                                    currentOpacity = keyboardOpacity,
                                    currentBlur = keyboardBlur,
                                    hapticIntensity = hapticIntensity,
                                    hasCustomBg = keyboardBgBase64.isNotEmpty(),
                                    onThemeChanged = { viewModel.setKeyboardTheme(it) },
                                    onOpacityChanged = { viewModel.setKeyboardOpacity(it) },
                                    onBlurChanged = { viewModel.setKeyboardBlur(it) },
                                    onHapticChanged = { viewModel.setHapticIntensity(it) },
                                    onImageSelected = { uri -> viewModel.handleImageSelection(uri) },
                                    onClearBg = { viewModel.clearKeyboardBg() },
                                    onBack = { currentScreen = AppScreen.HOME }
                                )
                            }
                            AppScreen.KEYBOARD -> {
                                KeyboardScreen(
                                    isConnected = isConnected,
                                    tabletIp = tabletIp,
                                    theme = keyboardTheme,
                                    opacity = keyboardOpacity,
                                    blur = keyboardBlur,
                                    bgBase64 = keyboardBgBase64,
                                    configuredVibration = hapticIntensity,
                                    onSendCommand = { action, extra ->
                                        viewModel.sendCommand(action, extra)
                                    },
                                    onBack = { currentScreen = AppScreen.HOME }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Parses the QR code text.
     * Expects either:
     * - An HTTP link: e.g. http://192.168.1.15:8080/?pin=123456
     * - A raw IP or IP:PORT: e.g. 192.168.1.15:8080
     * Returns Pair of (IP_address, PIN)
     */
    private fun parseQrCodeContent(qrText: String): Pair<String, String> {
        val trimmed = qrText.trim()
        try {
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                val uri = Uri.parse(trimmed)
                val host = uri.host
                val port = uri.port
                val pin = uri.getQueryParameter("pin") ?: ""
                
                val ipWithPort = if (host != null) {
                    if (port != -1) "$host:$port" else host
                } else {
                    ""
                }
                
                if (ipWithPort.isNotEmpty()) {
                    return Pair(ipWithPort, pin)
                }
            }
            
            // Fallback for raw IP or formatted string
            if (trimmed.contains("pin=")) {
                val ipPart = trimmed.substringBefore("?").substringBefore("pin=").removeSuffix("&").removeSuffix("?")
                val pinPart = trimmed.substringAfter("pin=").substringBefore("&")
                return Pair(ipPart, pinPart)
            }
            
            // Plain raw string ip:port
            return Pair(trimmed, "")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Pair(trimmed, "")
    }
}
