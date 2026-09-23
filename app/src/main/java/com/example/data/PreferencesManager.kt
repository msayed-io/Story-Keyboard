package com.example.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("hikayat_remote_key_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TABLET_IP = "tablet_ip"
        private const val KEY_TABLET_PIN = "tablet_pin"
        private const val KEY_HAPTIC_INTENSITY = "haptic_intensity"
        private const val KEY_KEYBOARD_THEME = "keyboard_theme"
        private const val KEY_KEYBOARD_OPACITY = "keyboard_opacity"
        private const val KEY_KEYBOARD_BLUR = "keyboard_blur"
        private const val KEY_KEYBOARD_BG_BASE64 = "keyboard_bg_base64"
    }

    var tabletIp: String
        get() = prefs.getString(KEY_TABLET_IP, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TABLET_IP, value).apply()

    var tabletPin: String
        get() = prefs.getString(KEY_TABLET_PIN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TABLET_PIN, value).apply()

    var hapticIntensity: Int
        get() = prefs.getInt(KEY_HAPTIC_INTENSITY, 10)
        set(value) = prefs.edit().putInt(KEY_HAPTIC_INTENSITY, value).apply()

    var keyboardTheme: String
        get() = prefs.getString(KEY_KEYBOARD_THEME, "royal_classic") ?: "royal_classic"
        set(value) = prefs.edit().putString(KEY_KEYBOARD_THEME, value).apply()

    var keyboardOpacity: Float
        get() = prefs.getFloat(KEY_KEYBOARD_OPACITY, 0.96f)
        set(value) = prefs.edit().putFloat(KEY_KEYBOARD_OPACITY, value).apply()

    var keyboardBlur: Int
        get() = prefs.getInt(KEY_KEYBOARD_BLUR, 0)
        set(value) = prefs.edit().putInt(KEY_KEYBOARD_BLUR, value).apply()

    var keyboardBgBase64: String
        get() = prefs.getString(KEY_KEYBOARD_BG_BASE64, "") ?: ""
        set(value) = prefs.edit().putString(KEY_KEYBOARD_BG_BASE64, value).apply()

    fun clearConnection() {
        prefs.edit().remove(KEY_TABLET_IP).remove(KEY_TABLET_PIN).apply()
    }
}
