package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PreferencesManager
import androidx.exifinterface.media.ExifInterface
import com.example.network.TabletClient
import com.example.ui.theme.GlassMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

        // إصلاح خلفية واحدة: صورة محفوظة بشفافية 96% كانت تختفي تماماً
        if (prefs.migrateBackgroundGlassOnce()) {
            _keyboardOpacity.value = prefs.keyboardOpacity
        }
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

    /**
     * إعادة تشغيل حلقة الفحص فوراً (عند العودة إلى التطبيق): حالة الاتصال
     * يجب أن تكون حقيقية لحظتها، لا مجرّد بيانات محفوظة.
     */
    fun refreshConnection() {
        client.startPingLoop()
    }

    fun setKeyboardBlur(blur: Int) {
        prefs.keyboardBlur = blur
        _keyboardBlur.value = blur
    }

    fun handleImageSelection(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val resolver = context.contentResolver

                // 1) قياسات الصورة أولاً دون تحميلها كاملة (حماية من انفجار الذاكرة)
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@launch

                // 2) تنزيل ذكي بحسب دقّة الشاشة: تفاصيل كافية للطبقة الزجاجية بلا زيادة
                val metrics = context.resources.displayMetrics
                // 1440 حدّ أعلى متعمّد: أوضح من أي طبقة زجاجية نحتاجها، وبلا تضخيم
                // للذاكرة أو لحجم ما يُحفظ.
                val targetLongest = maxOf(metrics.widthPixels, metrics.heightPixels)
                    .coerceIn(1080, 1440)
                val options = BitmapFactory.Options().apply {
                    inSampleSize = GlassMath.sampleSizeFor(bounds.outWidth, bounds.outHeight, targetLongest)
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val decoded = resolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, options)
                } ?: return@launch

                // 3) احترام اتجاه الكاميرا حتى لا تظهر الصورة مقلوبة
                val oriented = applyExifOrientation(context, uri, decoded)

                // 4) حفظ بجودة أعلى: الطبقة الزجاجية تُبنى من هذه الصورة
                val outputStream = ByteArrayOutputStream()
                oriented.compress(Bitmap.CompressFormat.JPEG, 84, outputStream)
                val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                oriented.recycle()

                prefs.keyboardBgBase64 = base64String
                _keyboardBgBase64.value = base64String

                // 5) اختيار صورة يعني رغبة في رؤيتها: الشفافية العالية كانت تُخفيها تماماً
                if (_keyboardOpacity.value > 0.75f) {
                    setKeyboardOpacity(0.45f)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** يطبّق اتجاه الكاميرا المسجَّل في الصورة (EXIF). */
    private fun applyExifOrientation(
        context: Application,
        uri: Uri,
        bitmap: Bitmap
    ): Bitmap {
        val orientation = try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }

        val transform = GlassMath.exifTransform(orientation)
        if (transform.isIdentity) return bitmap

        val matrix = Matrix()
        if (transform.rotationDegrees != 0) matrix.postRotate(transform.rotationDegrees.toFloat())
        if (transform.flipHorizontal) matrix.postScale(-1f, 1f)

        return try {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                .also { if (it !== bitmap) bitmap.recycle() }
        } catch (e: Exception) {
            bitmap
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

    /** Large paste from the phone clipboard: POST/JSON, never a URL. */
    fun sendPasteText(text: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val ok = client.sendPasteText(text)
            withContext(Dispatchers.Main) { onResult(ok) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.stopPingLoop()
    }
}
