package com.example.network

import android.util.Log
import com.example.data.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class TabletClient(private val prefs: PreferencesManager) {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(500, TimeUnit.MILLISECONDS)
        .readTimeout(500, TimeUnit.MILLISECONDS)
        .writeTimeout(500, TimeUnit.MILLISECONDS)
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
                if (ip.isNotEmpty()) {
                    val url = if (ip.startsWith("http://") || ip.startsWith("https://")) {
                        "$ip/"
                    } else {
                        "http://$ip:8080/"
                    }
                    val isAlive = pingTablet(url)
                    _isConnected.value = isAlive
                } else {
                    _isConnected.value = false
                }
                delay(5000) // Ping every 5 seconds
            }
        }
    }

    fun stopPingLoop() {
        pingJob?.cancel()
        pingJob = null
    }

    private fun pingTablet(url: String): Boolean {
        return try {
            val request = Request.Builder()
                .url(url)
                .build()
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            // Check if IP matches but server replies with 404 or something, still counts as connected, but let's be careful.
            // A simple socket timeout means disconnected.
            Log.d("TabletClient", "Ping failed: ${e.message}")
            false
        }
    }

    fun sendCommand(action: String, extraParams: String = ""): Boolean {
        val ip = prefs.tabletIp
        if (ip.isEmpty()) return false

        val hostUrl = if (ip.startsWith("http://") || ip.startsWith("https://")) {
            ip.removeSuffix("/")
        } else {
            "http://$ip:8080"
        }

        // Build elegant API query
        val queryBuilder = StringBuilder("$hostUrl/api/command?action=${UriUtils.encode(action)}")
        if (extraParams.isNotEmpty()) {
            // extraParams is already form-encoded from JS like "char=%D8%A7" or "delta=1"
            queryBuilder.append("&").append(extraParams)
        }
        
        val pin = prefs.tabletPin
        if (pin.isNotEmpty()) {
            queryBuilder.append("&pin=${UriUtils.encode(pin)}")
        }

        val request = Request.Builder()
            .url(queryBuilder.toString())
            .get() // Simple GET as per Javascript interface
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
        } catch (e: IOException) {
            Log.e("TabletClient", "Failed to send command $action: ${e.message}")
            false
        }
    }
}

object UriUtils {
    fun encode(s: String): String {
        return java.net.URLEncoder.encode(s, "UTF-8")
    }
}
