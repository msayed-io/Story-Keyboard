package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MediaItem
import com.example.data.MediaLibraryRepository
import com.example.data.MediaType
import com.example.data.PreferencesManager
import com.example.network.TabletClient
import com.example.ui.theme.GlassMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class CompanionViewModel(application: Application) : AndroidViewModel(application) {

    val prefs = PreferencesManager(application)
    val client = TabletClient(prefs)
    val mediaRepository = MediaLibraryRepository(application)

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

    private val _keyboardBgType = MutableStateFlow(prefs.keyboardBgType)
    val keyboardBgType: StateFlow<String> = _keyboardBgType.asStateFlow()

    private val _keyboardBgPath = MutableStateFlow(prefs.keyboardBgPath)
    val keyboardBgPath: StateFlow<String> = _keyboardBgPath.asStateFlow()

    private val _selectedMediaId = MutableStateFlow(prefs.selectedMediaId)
    val selectedMediaId: StateFlow<String> = _selectedMediaId.asStateFlow()

    private val _videoThumbnailBitmap = MutableStateFlow<Bitmap?>(null)
    val videoThumbnailBitmap: StateFlow<Bitmap?> = _videoThumbnailBitmap.asStateFlow()

    private val _curatedMediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val curatedMediaItems: StateFlow<List<MediaItem>> = _curatedMediaItems.asStateFlow()

    private val _mediaDownloadError = MutableStateFlow<String?>(null)
    val mediaDownloadError: StateFlow<String?> = _mediaDownloadError.asStateFlow()

    val downloadProgress: StateFlow<Map<String, Int>> = mediaRepository.downloadProgress

    val isConnected: StateFlow<Boolean> = client.isConnected

    init {
        // قراءة فورية للكاش المحلي بدون أي تأخير عند فتح التطبيق
        val cached = mediaRepository.getCachedItemsImmediately()
        if (cached.isNotEmpty()) {
            _curatedMediaItems.value = cached
        }

        viewModelScope.launch(Dispatchers.IO) {
            client.startPingLoop()

            // إصلاح خلفية واحدة: صورة محفوظة بشفافية 96% كانت تختفي تماماً
            if (prefs.migrateBackgroundGlassOnce()) {
                _keyboardOpacity.value = prefs.keyboardOpacity
            }

            // إذا كانت الخلفية المحفوظة عبارة عن فيديو، استخراج اللقطة المصغرة للزجاج فوراً
            if (prefs.keyboardBgType == "video" && prefs.keyboardBgPath.isNotEmpty()) {
                loadVideoThumbnail(File(prefs.keyboardBgPath))
            }

            // تحديث قائمة الوسائط في الخلفية
            refreshCuratedItems()
        }
    }

    fun refreshCuratedItems(customManifestUrl: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val items = mediaRepository.fetchRemoteItems(customManifestUrl)
            if (items.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    _curatedMediaItems.value = items
                }
            }
        }
    }

    fun saveConnection(ip: String, pin: String) {
        var cleanIp = ip.trim()
        if (cleanIp.startsWith("http://")) {
            cleanIp = cleanIp.substring(7)
        } else if (cleanIp.startsWith("https://")) {
            cleanIp = cleanIp.substring(8)
        }
        
        if (cleanIp.contains("/")) {
            cleanIp = cleanIp.substringBefore("/")
        }

        prefs.tabletIp = cleanIp
        prefs.tabletPin = pin.trim()
        
        _tabletIp.value = cleanIp
        _tabletPin.value = pin.trim()
        
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

    fun refreshConnection() {
        client.startPingLoop()
    }

    fun setKeyboardBlur(blur: Int) {
        prefs.keyboardBlur = blur
        _keyboardBlur.value = blur
    }

    /**
     * معالج موحد لاختيار وسائط الجهاز (صور أو فيديوهات) بدقة وسرعة فائقة.
     */
    fun handleMediaSelection(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val mimeType = context.contentResolver.getType(uri) ?: ""
                val isVideo = mimeType.startsWith("video") || uri.toString().contains(".mp4", ignoreCase = true)

                if (isVideo) {
                    // نسخ الفيديو إلى التخزين الداخلي للتطبيق لضمان تشغيله السلس دائماً
                    val localVideoFile = File(context.filesDir, "custom_bg_video.mp4")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(localVideoFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    val path = localVideoFile.absolutePath
                    prefs.keyboardBgType = "video"
                    prefs.keyboardBgPath = path
                    prefs.selectedMediaId = ""
                    _keyboardBgType.value = "video"
                    _keyboardBgPath.value = path
                    _selectedMediaId.value = ""

                    // استخراج إطار لطبقة الزجاج وتعيين الشفافية المناسبة
                    loadVideoThumbnail(localVideoFile)

                    if (_keyboardOpacity.value > 0.75f) {
                        setKeyboardOpacity(0.45f)
                    }
                } else {
                    // معالجة الصور
                    processImageSelection(uri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun processImageSelection(uri: Uri) = withContext(Dispatchers.IO) {
        try {
            val context = getApplication<Application>()
            val resolver = context.contentResolver

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext

            val metrics = context.resources.displayMetrics
            val targetLongest = maxOf(metrics.widthPixels, metrics.heightPixels).coerceIn(1080, 1440)
            val options = BitmapFactory.Options().apply {
                inSampleSize = GlassMath.sampleSizeFor(bounds.outWidth, bounds.outHeight, targetLongest)
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return@withContext

            val oriented = applyExifOrientation(context, uri, decoded)

            val outputStream = ByteArrayOutputStream()
            oriented.compress(Bitmap.CompressFormat.JPEG, 84, outputStream)
            val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
            oriented.recycle()

            prefs.keyboardBgType = "image"
            prefs.keyboardBgBase64 = base64String
            prefs.keyboardBgPath = ""
            prefs.selectedMediaId = ""
            
            _keyboardBgType.value = "image"
            _keyboardBgBase64.value = base64String
            _keyboardBgPath.value = ""
            _selectedMediaId.value = ""
            _videoThumbnailBitmap.value = null

            if (_keyboardOpacity.value > 0.75f) {
                setKeyboardOpacity(0.45f)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * اختيار عنصر من المكتبة الأدبية (مع تنزيل حقيقي وعرض النسبة المئوية إذا لم يكن محمّلاً مسبقاً).
     */
    fun selectCuratedItem(item: MediaItem, onFinished: () -> Unit = {}) {
        _mediaDownloadError.value = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = if (mediaRepository.isDownloaded(item)) {
                    mediaRepository.getCachedFile(item)
                } else {
                    mediaRepository.downloadItem(item)
                }

                if (file != null && file.exists() && file.length() > 0) {
                    if (item.type == MediaType.VIDEO) {
                        val path = file.absolutePath
                        prefs.keyboardBgType = "video"
                        prefs.keyboardBgPath = path
                        prefs.selectedMediaId = item.id
                        _keyboardBgType.value = "video"
                        _keyboardBgPath.value = path
                        _selectedMediaId.value = item.id

                        loadVideoThumbnail(file)
                    } else {
                        // تحميل الصورة كـ Base64
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        if (bitmap != null) {
                            val outputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 84, outputStream)
                            val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                            bitmap.recycle()

                            prefs.keyboardBgType = "image"
                            prefs.keyboardBgBase64 = base64String
                            prefs.keyboardBgPath = file.absolutePath
                            prefs.selectedMediaId = item.id

                            _keyboardBgType.value = "image"
                            _keyboardBgBase64.value = base64String
                            _keyboardBgPath.value = file.absolutePath
                            _selectedMediaId.value = item.id
                            _videoThumbnailBitmap.value = null
                        }
                    }

                    // تحديث حالة التحميل في القائمة المعروضة
                    _curatedMediaItems.value = _curatedMediaItems.value.map { current ->
                        if (current.id == item.id) {
                            current.copy(isDownloaded = true, localPath = file.absolutePath)
                        } else {
                            current
                        }
                    }

                    if (_keyboardOpacity.value > 0.75f) {
                        setKeyboardOpacity(0.45f)
                    }

                    withContext(Dispatchers.Main) {
                        onFinished()
                    }
                } else {
                    _mediaDownloadError.value = "تعذر تنزيل الصورة أو التحقق من صحتها. يرجى المحاولة مرة أخرى."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _mediaDownloadError.value = "حدث خطأ أثناء تنزيل الصورة: ${e.localizedMessage}"
            }
        }
    }

    fun clearMediaDownloadError() {
        _mediaDownloadError.value = null
    }

    private fun loadVideoThumbnail(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            val thumb = mediaRepository.extractVideoThumbnail(file)
            if (thumb != null) {
                _videoThumbnailBitmap.value = thumb
                // حفظ نسخة Base64 سريعة للـ GlassEffect
                val outputStream = ByteArrayOutputStream()
                thumb.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                _keyboardBgBase64.value = base64String
            }
        }
    }

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
        prefs.clearBackground()
        _keyboardBgBase64.value = ""
        _keyboardBgType.value = "none"
        _keyboardBgPath.value = ""
        _selectedMediaId.value = ""
        _videoThumbnailBitmap.value = null
    }

    fun sendCommand(action: String, extraParams: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            client.sendCommand(action, extraParams)
        }
    }

    fun sendPasteText(text: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val ok = client.sendPasteText(text)
            withContext(Dispatchers.Main) { onResult(ok) }
        }
    }

    private val _isAudioGuidePlaying = MutableStateFlow(false)
    val isAudioGuidePlaying: StateFlow<Boolean> = _isAudioGuidePlaying.asStateFlow()

    val audioEngine = com.example.data.AudioReactivityEngine(viewModelScope)
    val orbAudioState: StateFlow<com.example.ui.components.OrbAudioState> = audioEngine.audioState

    private var mediaPlayer: android.media.MediaPlayer? = null
    private var fadeJob: Job? = null

    fun checkAndPlayWelcomeAudioOnFirstLaunch() {
        if (!prefs.hasPlayedWelcomeAudio) {
            prefs.hasPlayedWelcomeAudio = true
            playWelcomeAudio()
        }
    }

    fun playWelcomeAudio() {
        fadeJob?.cancel()
        try {
            val context = getApplication<Application>()
            val resId = context.resources.getIdentifier("welcome_narration", "raw", context.packageName)
            if (resId != 0) {
                mediaPlayer?.release()
                val player = android.media.MediaPlayer.create(context, resId)
                if (player != null) {
                    mediaPlayer = player
                    player.setVolume(1.0f, 1.0f)
                    player.setOnCompletionListener {
                        viewModelScope.launch(Dispatchers.Main) {
                            _isAudioGuidePlaying.value = false
                            audioEngine.stop()
                            try {
                                it.release()
                            } catch (e: Exception) {}
                            mediaPlayer = null
                        }
                    }
                    player.start()
                    _isAudioGuidePlaying.value = true
                    audioEngine.attachAudioSession(player.audioSessionId)

                    // مراقبة نهاية المقطع لتطبيق التلاشي التدريجي الناعم (Smooth Fade-Out) قبل النهاية بـ 1200ms
                    fadeJob = viewModelScope.launch(Dispatchers.Default) {
                        try {
                            val duration = player.duration
                            val fadeDurationMs = 1200L
                            val fadeStartTime = (duration - fadeDurationMs).coerceAtLeast(0L)

                            while (isActive && player.isPlaying) {
                                val current = try { player.currentPosition } catch (e: Exception) { break }
                                if (current >= fadeStartTime && duration > 0) {
                                    val remaining = (duration - current).coerceAtLeast(0)
                                    val volumeFraction = (remaining.toFloat() / fadeDurationMs.toFloat()).coerceIn(0f, 1f)
                                    try {
                                        player.setVolume(volumeFraction, volumeFraction)
                                    } catch (e: Exception) {}
                                }
                                delay(40)
                            }
                        } catch (e: Exception) {}
                    }
                } else {
                    _isAudioGuidePlaying.value = true
                    audioEngine.startSyntheticSpeechModulation()
                }
            } else {
                // Visual simulation for UI preview when raw file is being added
                _isAudioGuidePlaying.value = true
                audioEngine.startSyntheticSpeechModulation()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isAudioGuidePlaying.value = false
            audioEngine.stop()
        }
    }

    fun stopWelcomeAudio() {
        fadeJob?.cancel()
        fadeJob = viewModelScope.launch(Dispatchers.Default) {
            try {
                val player = mediaPlayer
                if (player != null && player.isPlaying) {
                    // Gentle fast fade-out over 250ms on manual dismiss
                    for (i in 10 downTo 0) {
                        val vol = (i / 10f)
                        try { player.setVolume(vol, vol) } catch (e: Exception) {}
                        delay(25)
                    }
                    try { player.stop() } catch (e: Exception) {}
                    try { player.release() } catch (e: Exception) {}
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                mediaPlayer = null
                withContext(Dispatchers.Main) {
                    audioEngine.stop()
                    _isAudioGuidePlaying.value = false
                }
            }
        }
    }

    fun setAudioGuidePlaying(playing: Boolean) {
        if (playing) {
            playWelcomeAudio()
        } else {
            stopWelcomeAudio()
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.stopPingLoop()
        fadeJob?.cancel()
        audioEngine.release()
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {}
        mediaPlayer = null
    }
}
