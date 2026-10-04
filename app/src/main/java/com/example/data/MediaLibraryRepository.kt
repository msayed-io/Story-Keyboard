package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

object LibraryConfig {
    const val HIKAYAT_LIBRARY_VERSION = "v1.3.1"
    const val PRIMARY_MANIFEST_URL = "https://raw.githubusercontent.com/msayed-io/hikayat-keyboard-library/v1.3.1/metadata/manifest.json"
    const val CDN_MANIFEST_URL = "https://cdn.jsdelivr.net/gh/msayed-io/hikayat-keyboard-library@v1.3.1/metadata/manifest.json"
    const val RAW_BASE_URL = "https://raw.githubusercontent.com/msayed-io/hikayat-keyboard-library/v1.3.1/"
    const val CDN_BASE_URL = "https://cdn.jsdelivr.net/gh/msayed-io/hikayat-keyboard-library@v1.3.1/"

    const val EXPECTED_ORIGINAL_ASSETS_COUNT = 27
    const val EXPECTED_CURATED_ASSETS_COUNT = 50
    const val EXPECTED_TOTAL_IMAGES_COUNT = 77
}

enum class MediaType {
    IMAGE,
    VIDEO
}

enum class MediaCategory {
    ALL,
    ORIGINAL_BOARD,
    CURATED_LANDSCAPES
}

enum class AssetDownloadStatus {
    NOT_DOWNLOADED,
    QUEUED,
    DOWNLOADING,
    DOWNLOADED,
    FAILED,
    CANCELLED
}

data class MediaItem(
    val id: String,
    val file: String,
    val type: MediaType,
    val previewUrl: String,
    val downloadUrl: String,
    val sha256: String? = null,
    val bytes: Long = 0,
    val width: Int = 0,
    val height: Int = 0,
    val category: MediaCategory = MediaCategory.CURATED_LANDSCAPES,
    val durationSeconds: Int? = null,
    val isDownloaded: Boolean = false,
    val status: AssetDownloadStatus = AssetDownloadStatus.NOT_DOWNLOADED,
    val localPath: String? = null
)

class MediaLibraryRepository(private val context: Context) {

    companion object {
        private const val TAG = "MediaLibraryRepository"

        /**
         * بناء رابط مباشر وموثوق لكل أصل من أصول المستودع
         */
        fun buildAssetUrl(file: String, useCdn: Boolean = false): String {
            require(file.startsWith("assets/")) { "Asset path must start with assets/: $file" }
            require(!file.contains("..")) { "Path traversal is not allowed: $file" }

            val cleanFile = file.trimStart('/')
            val baseUrl = if (useCdn) LibraryConfig.CDN_BASE_URL else LibraryConfig.RAW_BASE_URL
            return baseUrl + cleanFile
        }
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val _downloadProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Int>> = _downloadProgress.asStateFlow()

    private val mediaCacheDir: File
        get() = File(context.filesDir, "hikayat_media_cache").apply { if (!exists()) mkdirs() }

    private val manifestCacheFile: File
        get() = File(context.filesDir, "hikayat_manifest_cache.json")

    @Volatile
    private var inMemoryItemsCache: List<MediaItem> = emptyList()

    /**
     * استرجاع فوري من الذاكرة أو القرص لتسريع فتح التطبيق بدون أي انتظار
     */
    fun getCachedItemsImmediately(): List<MediaItem> {
        if (inMemoryItemsCache.isNotEmpty()) return inMemoryItemsCache
        val cached = readCachedManifest()
        if (cached.isNotEmpty()) {
            inMemoryItemsCache = cached
        }
        return inMemoryItemsCache
    }

    /**
     * جلب قائمة الوسائط الكاملة من مستودع Hikayat Keyboard الأصلي
     * يدمج المصفوفات: assets (27) + curated_assets (50) = 77 صورة
     */
    suspend fun fetchRemoteItems(customUrl: String? = null): List<MediaItem> = withContext(Dispatchers.IO) {
        // 1. القراءة الفورية من الكاش المحلي إن وجد لتسريع العرض
        val cachedItems = getCachedItemsImmediately()

        // 2. تحديث الـ manifest في الخلفية من شبكة الـ CDN السريعة v1.3.1
        val freshJson = downloadFreshManifest(customUrl)
        if (!freshJson.isNullOrEmpty()) {
            val freshItems = parseJsonManifest(freshJson)
            if (freshItems.isNotEmpty()) {
                inMemoryItemsCache = freshItems
                try {
                    manifestCacheFile.writeText(freshJson)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to write manifest cache: ${e.message}")
                }
                return@withContext freshItems
            }
        }

        // 3. في حالة انقطاع الشبكة، استخدام الكاش المحلي الحقيقي إن وُجد
        if (cachedItems.isNotEmpty()) {
            return@withContext cachedItems
        }

        emptyList()
    }

    private fun readCachedManifest(): List<MediaItem> {
        return try {
            if (manifestCacheFile.exists() && manifestCacheFile.length() > 0) {
                parseJsonManifest(manifestCacheFile.readText())
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading cached manifest: ${e.message}")
            emptyList()
        }
    }

    private fun downloadFreshManifest(customUrl: String?): String? {
        val urlsToTry = if (!customUrl.isNullOrEmpty()) {
            listOf(customUrl)
        } else {
            listOf(
                LibraryConfig.CDN_MANIFEST_URL,      // CDN أولاً لسرعة الاستجابة اللحظية
                LibraryConfig.PRIMARY_MANIFEST_URL
            )
        }

        for (url in urlsToTry) {
            try {
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string()
                    if (!bodyStr.isNullOrEmpty()) {
                        return bodyStr
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to fetch manifest from $url: ${e.message}")
            }
        }
        return null
    }

    /**
     * تحليل ملف الـ Manifest الرسمي ودمج كل مصادر الصور:
     * - assets (27 صورة أصلية من لوحة الدار)
     * - curated_assets (50 صورة منسقة للمناظر الطبيعية)
     * والتحقق من العدد الإجمالي 77 عنصرًا فريدًا
     */
    fun parseJsonManifest(jsonStr: String): List<MediaItem> {
        val items = mutableListOf<MediaItem>()
        val seenKeys = mutableSetOf<String>()

        var originalCount = 0
        var curatedCount = 0

        try {
            val rootObj = JSONObject(jsonStr)

            // 1. قراءة مصفوفة الصور الأصلية: assets (27 صورة)
            val originalArray = rootObj.optJSONArray("assets")
            if (originalArray != null) {
                for (i in 0 until originalArray.length()) {
                    val obj = originalArray.getJSONObject(i)
                    val parsed = parseSingleAsset(obj, MediaCategory.ORIGINAL_BOARD, seenKeys)
                    if (parsed != null) {
                        items.add(parsed)
                        originalCount++
                    }
                }
            }

            // 2. قراءة مصفوفة الصور المنسقة: curated_assets (50 صورة)
            val curatedArray = rootObj.optJSONArray("curated_assets")
            if (curatedArray != null) {
                for (i in 0 until curatedArray.length()) {
                    val obj = curatedArray.getJSONObject(i)
                    val parsed = parseSingleAsset(obj, MediaCategory.CURATED_LANDSCAPES, seenKeys)
                    if (parsed != null) {
                        items.add(parsed)
                        curatedCount++
                    }
                }
            }

            val totalImages = items.count { it.type == MediaType.IMAGE }

            // التحقق الصارم من سلامة واكتمال المكتبة
            if (totalImages < LibraryConfig.EXPECTED_TOTAL_IMAGES_COUNT) {
                Log.e(
                    TAG,
                    "Hikayat library integrity error: expected ${LibraryConfig.EXPECTED_TOTAL_IMAGES_COUNT} images, loaded $totalImages (assets: $originalCount, curated_assets: $curatedCount)"
                )
            } else {
                Log.i(
                    TAG,
                    "Hikayat library integrity verified: $originalCount original + $curatedCount curated = $totalImages images loaded successfully."
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing manifest JSON: ${e.message}", e)
        }

        return items
    }

    private fun parseSingleAsset(
        obj: JSONObject,
        category: MediaCategory,
        seenKeys: MutableSet<String>
    ): MediaItem? {
        val filePath = obj.optString("file", "")
        val typeStr = obj.optString("type", "").lowercase()
        val sha256 = obj.optString("sha256", "")
        val bytes = obj.optLong("bytes", 0L)
        val width = obj.optInt("width", 0)
        val height = obj.optInt("height", 0)

        // تجاهل الملفات غير الصالحة، ملفات التوثيق، والتنقل بين المسارات
        if (filePath.isEmpty() ||
            !filePath.startsWith("assets/") ||
            filePath.contains("..") ||
            filePath.endsWith(".md", ignoreCase = true) ||
            filePath.endsWith("curated-preview.jpg", ignoreCase = true)
        ) {
            return null
        }

        val mediaType = when (typeStr) {
            "image" -> MediaType.IMAGE
            "video" -> MediaType.VIDEO
            else -> return null
        }

        // إزالة التكرار بدقة: أولاً عبر sha256 إن وُجد، ثم عبر مسار الملف
        val uniqueKey = if (sha256.isNotEmpty()) sha256 else filePath
        if (!seenKeys.add(uniqueKey)) {
            return null
        }

        val remoteUrl = buildAssetUrl(filePath, useCdn = false)
        val cdnUrl = buildAssetUrl(filePath, useCdn = true)
        val id = if (sha256.isNotEmpty()) sha256.take(16) else "asset_${filePath.hashCode()}"

        val cachedFile = getTargetLocalFile(filePath, sha256)
        val isCached = cachedFile.exists() && cachedFile.length() > 0

        return MediaItem(
            id = id,
            file = filePath,
            type = mediaType,
            previewUrl = cdnUrl,
            downloadUrl = remoteUrl,
            sha256 = sha256.ifEmpty { null },
            bytes = bytes,
            width = width,
            height = height,
            category = category,
            isDownloaded = isCached,
            status = if (isCached) AssetDownloadStatus.DOWNLOADED else AssetDownloadStatus.NOT_DOWNLOADED,
            localPath = if (isCached) cachedFile.absolutePath else null
        )
    }

    fun isDownloaded(item: MediaItem): Boolean {
        return isDownloadedByFile(item.file, item.sha256 ?: "")
    }

    private fun isDownloadedByFile(filePath: String, sha256: String): Boolean {
        val file = getTargetLocalFile(filePath, sha256)
        return file.exists() && file.length() > 0
    }

    fun getCachedFile(item: MediaItem): File {
        return getTargetLocalFile(item.file, item.sha256 ?: "")
    }

    private fun getTargetLocalFile(filePath: String, sha256: String): File {
        val ext = when {
            filePath.endsWith(".png", ignoreCase = true) -> "png"
            filePath.endsWith(".mp4", ignoreCase = true) -> "mp4"
            else -> "jpg"
        }
        val fileName = if (sha256.isNotEmpty()) "$sha256.$ext" else "asset_${filePath.hashCode()}.$ext"
        return File(mediaCacheDir, fileName)
    }

    /**
     * تنزيل حقيقي للملف عند طلب المستخدم فقط (On-Demand) مع:
     * 1. التنزيل إلى ملف مؤقت (.download)
     * 2. التحقق الذري من الحجم والسلامة والـ SHA-256
     * 3. النقل الذري إلى المسار النهائي
     */
    suspend fun downloadItem(item: MediaItem, onProgress: (Int) -> Unit = {}): File? = withContext(Dispatchers.IO) {
        val targetFile = getCachedFile(item)
        if (targetFile.exists() && targetFile.length() > 0) {
            onProgress(100)
            return@withContext targetFile
        }

        val tempFile = File(mediaCacheDir, "${targetFile.name}.download")
        if (tempFile.exists()) tempFile.delete()

        try {
            updateProgress(item.id, 5)
            onProgress(5)

            // تجربة الرابط المباشر، وإذا تعثرت فاستخدام الـ CDN
            val urlsToTry = listOf(item.downloadUrl, item.previewUrl)
            var downloadSuccess = false

            for (url in urlsToTry) {
                try {
                    val request = Request.Builder().url(url).build()
                    val response = httpClient.newCall(request).execute()

                    if (response.isSuccessful) {
                        val body = response.body
                        if (body != null) {
                            val contentLength = body.contentLength()
                            body.byteStream().use { input ->
                                FileOutputStream(tempFile).use { output ->
                                    val buffer = ByteArray(8192)
                                    var bytesRead: Int
                                    var totalBytesRead = 0L

                                    while (input.read(buffer).also { bytesRead = it } != -1) {
                                        output.write(buffer, 0, bytesRead)
                                        totalBytesRead += bytesRead
                                        if (contentLength > 0) {
                                            val percent = ((totalBytesRead * 100) / contentLength).toInt().coerceIn(5, 98)
                                            updateProgress(item.id, percent)
                                            onProgress(percent)
                                        }
                                    }
                                    output.flush()
                                }
                            }
                            if (tempFile.exists() && tempFile.length() > 0) {
                                downloadSuccess = true
                                break
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Download attempt failed for $url: ${e.message}")
                }
            }

            if (!downloadSuccess || !tempFile.exists() || tempFile.length() <= 0) {
                if (tempFile.exists()) tempFile.delete()
                updateProgress(item.id, 0)
                return@withContext null
            }

            // 2. التحقق من مطابقة SHA-256 إذا كانت متوفرة في الـ Manifest
            if (!item.sha256.isNullOrEmpty()) {
                val isHashValid = verifySha256(tempFile, item.sha256)
                if (!isHashValid) {
                    Log.e(TAG, "SHA-256 hash mismatch for ${item.file}!")
                    tempFile.delete()
                    updateProgress(item.id, 0)
                    return@withContext null
                }
            }

            // 3. النقل الذري للملف بعد نجاح التحقق الكامل
            if (tempFile.renameTo(targetFile)) {
                updateProgress(item.id, 100)
                onProgress(100)
                saveLocalMetadata(item, targetFile)
                return@withContext targetFile
            } else {
                if (tempFile.exists()) tempFile.delete()
                updateProgress(item.id, 0)
                return@withContext null
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error downloading item ${item.file}: ${e.message}", e)
            if (tempFile.exists()) tempFile.delete()
            updateProgress(item.id, 0)
            return@withContext null
        }
    }

    fun verifySha256(file: File, expectedHash: String): Boolean {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            val calculatedHash = digest.digest().joinToString("") { "%02x".format(it) }
            calculatedHash.equals(expectedHash, ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }

    private fun saveLocalMetadata(item: MediaItem, targetFile: File) {
        try {
            val metaFile = File(mediaCacheDir, "${targetFile.name}.json")
            val metaObj = JSONObject().apply {
                put("file", item.file)
                put("localPath", targetFile.absolutePath)
                put("sha256", item.sha256 ?: "")
                put("bytes", item.bytes)
                put("category", item.category.name)
                put("downloadedAt", System.currentTimeMillis())
                put("status", "downloaded")
            }
            metaFile.writeText(metaObj.toString())
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save local metadata: ${e.message}")
        }
    }

    private fun updateProgress(itemId: String, percent: Int) {
        val current = _downloadProgress.value.toMutableMap()
        if (percent >= 100 || percent <= 0) {
            current.remove(itemId)
        } else {
            current[itemId] = percent
        }
        _downloadProgress.value = current
    }

    /**
     * استخراج إطار لقطة مصغرة من الفيديو لحسابات الزجاج المموّه (Glass Sampler)
     */
    suspend fun extractVideoThumbnail(videoFile: File): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoFile.absolutePath)
            val frame = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.frameAtTime
            retriever.release()
            frame
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting video thumbnail: ${e.message}")
            null
        }
    }
}
