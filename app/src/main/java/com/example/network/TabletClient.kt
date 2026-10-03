package com.example.network

import android.net.Uri
import android.util.Log
import com.example.data.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class TabletClient(private val prefs: PreferencesManager) {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(3000, TimeUnit.MILLISECONDS)
        .readTimeout(2000, TimeUnit.MILLISECONDS)
        .writeTimeout(2000, TimeUnit.MILLISECONDS)
        .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES)) // Maintain persistent keep-alive connections
        .build()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private var pingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun startPingLoop() {
        pingJob?.cancel()
        pingJob = scope.launch {
            while (true) {
                val ip = prefs.tabletIp
                val pin = prefs.tabletPin
                if (ip.isNotEmpty()) {
                    val url = buildEndpointUrl(ip, "ping", pin)
                    val isAlive = pingTablet(url)
                    _isConnected.value = isAlive
                } else {
                    _isConnected.value = false
                }
                delay(3000) // Ping every 3 seconds for responsive state
            }
        }
    }

    fun stopPingLoop() {
        pingJob?.cancel()
        pingJob = null
    }

    suspend fun verifyConnection(ip: String, pin: String): Boolean = withContext(Dispatchers.IO) {
        val url = buildEndpointUrl(ip, "ping", pin)
        val isAlive = pingTablet(url)
        _isConnected.value = isAlive
        isAlive
    }

    fun pingTablet(url: String): Boolean {
        return try {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.d("TabletClient", "Ping failed for $url: ${e.message}")
            false
        }
    }

    fun sendCommand(action: String, extraParams: String = ""): Boolean {
        val ip = prefs.tabletIp
        if (ip.isEmpty()) return false

        val pin = prefs.tabletPin
        val url = buildEndpointUrl(ip, action, pin, extraParams)

        val requestBuilder = Request.Builder()
            .url(url)
            .get()

        if (pin.isNotEmpty()) {
            requestBuilder.header("X-Tablet-PIN", pin)
            requestBuilder.header("pin", pin)
        }

        val request = requestBuilder.build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    _isConnected.value = true
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("TabletClient", "Failed to send command $action: ${e.message}")
            false
        }
    }

    /**
     * Sends a large text payload (a full novel excerpt, 30k+ characters) with a
     * POST/JSON body instead of a GET query string: no URL length limits, exact
     * UTF-8 bytes, and the tablet writes it at the caret in one piece.
     */
    fun sendPasteText(text: String): Boolean {
        val ip = prefs.tabletIp
        if (ip.isEmpty() || text.isEmpty()) return false

        val pin = prefs.tabletPin
        val host = ip.trim().removeSuffix("/").let { if (it.startsWith("http")) it else "http://$it" }
        val url = host.removeSuffix("/") + "/api/command"

        // JSONObject escapes quotes/newlines in Arabic text correctly.
        val bodyJson = org.json.JSONObject().apply {
            put("action", "paste")
            put("text", text)
            if (pin.isNotEmpty()) put("pin", pin)
        }.toString()

        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .apply {
                if (pin.isNotEmpty()) {
                    header("X-Tablet-PIN", pin)
                    header("pin", pin)
                }
            }
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    _isConnected.value = true
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("TabletClient", "Failed to paste text: ${e.message}")
            false
        }
    }

    companion object {
        /**
         * Cleanly builds an HTTP endpoint URL without double-port bugs.
         * Handles:
         * - "192.168.1.15" -> "http://192.168.1.15:8080/api/command..."
         * - "192.168.1.15:8080" -> "http://192.168.1.15:8080/api/command..."
         * - "http://192.168.1.15:8080" -> "http://192.168.1.15:8080/api/command..."
         * - "http://192.168.1.15:8080/" -> "http://192.168.1.15:8080/api/command..."
         */
        fun buildEndpointUrl(
            rawIp: String,
            action: String,
            pin: String = "",
            extraParams: String = ""
        ): String {
            var hostPart = rawIp.trim().removeSuffix("/")
            if (!hostPart.startsWith("http://") && !hostPart.startsWith("https://")) {
                hostPart = "http://$hostPart"
            }

            val uri = Uri.parse(hostPart)
            val host = uri.host ?: hostPart.removePrefix("http://").removePrefix("https://").substringBefore(":")
            val port = if (uri.port != -1) uri.port else 8080
            val scheme = uri.scheme ?: "http"

            val baseUrl = "$scheme://$host:$port/api/command?action=${UriUtils.encode(action)}"
            val sb = java.lang.StringBuilder(baseUrl)
            if (extraParams.isNotEmpty()) {
                sb.append("&").append(extraParams)
            }
            if (pin.isNotEmpty()) {
                sb.append("&pin=").append(UriUtils.encode(pin))
            }
            return sb.toString()
        }
    }
}

object UriUtils {
    fun encode(s: String): String {
        return java.net.URLEncoder.encode(s, "UTF-8")
    }
}
