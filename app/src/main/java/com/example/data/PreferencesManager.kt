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
        private const val KEY_KEYBOARD_BG_TYPE = "keyboard_bg_type"
        private const val KEY_KEYBOARD_BG_PATH = "keyboard_bg_path"
        private const val KEY_SELECTED_MEDIA_ID = "selected_media_id"
        private const val KEY_BG_GLASS_MIGRATED = "keyboard_bg_glass_migrated"
        private const val KEY_WELCOME_AUDIO_PLAYED = "welcome_audio_played_v1"
    }

    var hasPlayedWelcomeAudio: Boolean
        get() = prefs.getBoolean(KEY_WELCOME_AUDIO_PLAYED, false)
        set(value) = prefs.edit().putBoolean(KEY_WELCOME_AUDIO_PLAYED, value).apply()

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

    var keyboardBgType: String
        get() = prefs.getString(KEY_KEYBOARD_BG_TYPE, if (keyboardBgBase64.isNotEmpty() || keyboardBgPath.isNotEmpty()) "image" else "none") ?: "none"
        set(value) = prefs.edit().putString(KEY_KEYBOARD_BG_TYPE, value).apply()

    var keyboardBgPath: String
        get() = prefs.getString(KEY_KEYBOARD_BG_PATH, "") ?: ""
        set(value) = prefs.edit().putString(KEY_KEYBOARD_BG_PATH, value).apply()

    var selectedMediaId: String
        get() = prefs.getString(KEY_SELECTED_MEDIA_ID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SELECTED_MEDIA_ID, value).apply()

    fun clearBackground() {
        prefs.edit()
            .remove(KEY_KEYBOARD_BG_BASE64)
            .remove(KEY_KEYBOARD_BG_PATH)
            .remove(KEY_KEYBOARD_BG_TYPE)
            .remove(KEY_SELECTED_MEDIA_ID)
            .apply()
    }

    /**
     * ترحيل لمرة واحدة فقط: مَن اختارت صورة قبل إصلاح الزجاج بقي slider الشفافية
     * عند 96% فلا تظهر صورتها أبداً. نُنزله مرّة واحدة إلى قيمة تُظهر الصورة،
     * وبعدها تتحكّم الكاتبة بنفسها بالكامل.
     */
    fun migrateBackgroundGlassOnce(): Boolean {
        if (prefs.getBoolean(KEY_BG_GLASS_MIGRATED, false)) return false
        prefs.edit().putBoolean(KEY_BG_GLASS_MIGRATED, true).apply()
        val hasImage = keyboardBgBase64.isNotEmpty()
        if (hasImage && keyboardOpacity > 0.75f) {
            keyboardOpacity = 0.45f
            return true
        }
        return false
    }

    fun clearConnection() {
        prefs.edit().remove(KEY_TABLET_IP).remove(KEY_TABLET_PIN).apply()
    }
}
