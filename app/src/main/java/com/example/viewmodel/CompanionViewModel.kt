package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PreferencesManager
import com.example.network.TabletClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream

class CompanionViewModel(application: Application) : AndroidViewModel(application) {

    val prefs = PreferencesManager(application)
    val client = TabletClient(prefs)

    private val _tabletIp = MutableStateFlow(prefs.tabletIp)
    val tabletIp: StateFlow<String> = _tabletIp.asStateFlow()

    private val _tabletPin = MutableStateFlow(prefs.tabletPin)
    val tabletPin: StateFlow<String> = _tabletPin.asStateFlow()

    private val _hapticIntensity = MutableStateFlow(prefs.hapticIntensity)
    val hapticIntensity: StateFlow<Int> = _hapticIntensity.asStateFlow()

    private val _keyboardTheme = MutableStateFlow(prefs.keyboardTheme)
    val keyboardTheme: StateFlow<String> = _keyboardTheme.asStateFlow()

    private val _keyboardOpacity = MutableStateFlow(prefs.keyboardOpacity)
    val keyboardOpacity: StateFlow<Float> = _keyboardOpacity.asStateFlow()

    private val _keyboardBlur = MutableStateFlow(prefs.keyboardBlur)
    val keyboardBlur: StateFlow<Int> = _keyboardBlur.asStateFlow()

    private val _keyboardBgBase64 = MutableStateFlow(prefs.keyboardBgBase64)
    val keyboardBgBase64: StateFlow<String> = _keyboardBgBase64.asStateFlow()

    val isConnected: StateFlow<Boolean> = client.isConnected

    init {
        client.startPingLoop()
    }

    fun saveConnection(ip: String, pin: String) {
        // Clean IP to support hostnames or standard raw IPs
        var cleanIp = ip.trim()
        if (cleanIp.startsWith("http://")) {
            cleanIp = cleanIp.substring(7)
        } else if (cleanIp.startsWith("https://")) {
            cleanIp = cleanIp.substring(8)
        }
        
        // If IP contains a path or queries, extract just the host:port
        if (cleanIp.contains("/")) {
            cleanIp = cleanIp.substringBefore("/")
        }

        prefs.tabletIp = cleanIp
        prefs.tabletPin = pin.trim()
        
        _tabletIp.value = cleanIp
        _tabletPin.value = pin.trim()
        
        // Restart ping immediately for faster feedback
        client.startPingLoop()
    }

    suspend fun verifyAndSaveConnection(ip: String, pin: String): Boolean {
        saveConnection(ip, pin)
        return client.verifyConnection(ip, pin)
    }

    fun disconnect() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                client.sendCommand("disconnect")
            } catch (_: Exception) {}
            prefs.clearConnection()
            _tabletIp.value = ""
            _tabletPin.value = ""
            client.stopPingLoop()
            client.startPingLoop()
        }
    }

    fun setHapticIntensity(intensity: Int) {
        prefs.hapticIntensity = intensity
        _hapticIntensity.value = intensity
    }

    fun setKeyboardTheme(theme: String) {
        prefs.keyboardTheme = theme
        _keyboardTheme.value = theme
    }

    fun setKeyboardOpacity(opacity: Float) {
        prefs.keyboardOpacity = opacity
        _keyboardOpacity.value = opacity
    }

    fun setKeyboardBlur(blur: Int) {
        prefs.keyboardBlur = blur
        _keyboardBlur.value = blur
    }

    fun handleImageSelection(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                    if (originalBitmap != null) {
                        // Resize image to keep base64 lightweight and prevent OutOfMemory/TransactionTooLargeException
                        val maxDimension = 800
                        val width = originalBitmap.width
                        val height = originalBitmap.height
                        val scaledBitmap = if (width > maxDimension || height > maxDimension) {
                            val aspectRatio = width.toFloat() / height.toFloat()
                            val newWidth: Int
                            val newHeight: Int
                            if (width > height) {
                                newWidth = maxDimension
                                newHeight = (maxDimension / aspectRatio).toInt()
                            } else {
                                newHeight = maxDimension
                                newWidth = (maxDimension * aspectRatio).toInt()
                            }
                            Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
                        } else {
                            originalBitmap
                        }

                        val outputStream = ByteArrayOutputStream()
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
                        val bytes = outputStream.toByteArray()
                        val base64String = Base64.encodeToString(bytes, Base64.DEFAULT).replace("\n", "").replace("\r", "")
                        
                        prefs.keyboardBgBase64 = base64String
                        _keyboardBgBase64.value = base64String
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearKeyboardBg() {
        prefs.keyboardBgBase64 = ""
        _keyboardBgBase64.value = ""
    }

    fun sendCommand(action: String, extraParams: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            client.sendCommand(action, extraParams)
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.stopPingLoop()
    }
}
