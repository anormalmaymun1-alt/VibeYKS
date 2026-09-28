package com.example.yksaisinavkocu.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AppPreferences(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "yks_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val regularPrefs: SharedPreferences =
        context.getSharedPreferences("yks_prefs", Context.MODE_PRIVATE)

    // API Key (Encrypted)
    fun getApiKey(): String? = securePrefs.getString(KEY_API_KEY, null)

    fun setApiKey(key: String) {
        securePrefs.edit().putString(KEY_API_KEY, key).apply()
    }

    fun clearApiKey() {
        securePrefs.edit().remove(KEY_API_KEY).apply()
    }

    fun getMaskedApiKey(): String? {
        val key = getApiKey() ?: return null
        if (key.length <= 8) return "****"
        return key.take(4) + "****" + key.takeLast(4)
    }

    // Selected Model
    fun getSelectedModel(): String {
        val version = regularPrefs.getInt(KEY_MODEL_CONFIG_VERSION, 1)
        val savedModel = regularPrefs.getString(KEY_SELECTED_MODEL, null)
        val isInvalidModel = savedModel == null || savedModel == "gemini-2.0-flash" || AVAILABLE_MODELS.none { it.first == savedModel }

        if (version < 3 || isInvalidModel) {
            regularPrefs.edit()
                .putString(KEY_SELECTED_MODEL, DEFAULT_MODEL)
                .putInt(KEY_MODEL_CONFIG_VERSION, 3)
                .apply()
            return DEFAULT_MODEL
        }
        return savedModel
    }

    fun setSelectedModel(model: String) {
        val targetModel = if (AVAILABLE_MODELS.any { it.first == model }) model else DEFAULT_MODEL
        regularPrefs.edit()
            .putString(KEY_SELECTED_MODEL, targetModel)
            .putInt(KEY_MODEL_CONFIG_VERSION, 3)
            .apply()
    }

    // Custom System Prompt / Instructions
    fun getCustomSystemPrompt(): String =
        regularPrefs.getString(KEY_CUSTOM_SYSTEM_PROMPT, "") ?: ""

    fun setCustomSystemPrompt(prompt: String) {
        regularPrefs.edit().putString(KEY_CUSTOM_SYSTEM_PROMPT, prompt).apply()
    }

    fun clearCustomSystemPrompt() {
        regularPrefs.edit().remove(KEY_CUSTOM_SYSTEM_PROMPT).apply()
    }

    // Theme
    fun isDarkTheme(): Boolean = regularPrefs.getBoolean(KEY_DARK_THEME, false)

    fun setDarkTheme(isDark: Boolean) {
        regularPrefs.edit().putBoolean(KEY_DARK_THEME, isDark).apply()
    }

    // Onboarding
    fun isOnboardingCompleted(): Boolean = regularPrefs.getBoolean(KEY_ONBOARDING, false)

    // Token Usage Tracking
    fun getTokenUsageCount(): Long = regularPrefs.getLong(KEY_TOKEN_USAGE_COUNT, 0L)

    fun setTokenUsageCount(count: Long) {
        regularPrefs.edit().putLong(KEY_TOKEN_USAGE_COUNT, count).apply()
    }

    fun getTokenResetTimestamp(): Long = regularPrefs.getLong(KEY_TOKEN_RESET_TIMESTAMP, 0L)

    fun setTokenResetTimestamp(timestamp: Long) {
        regularPrefs.edit().putLong(KEY_TOKEN_RESET_TIMESTAMP, timestamp).apply()
    }

    companion object {
        private const val KEY_API_KEY = "gemini_api_key"
        private const val KEY_SELECTED_MODEL = "selected_model"
        private const val KEY_MODEL_CONFIG_VERSION = "model_config_version"
        private const val KEY_CUSTOM_SYSTEM_PROMPT = "custom_system_prompt"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_ONBOARDING = "onboarding_completed"
        private const val KEY_TOKEN_USAGE_COUNT = "token_usage_count"
        private const val KEY_TOKEN_RESET_TIMESTAMP = "token_reset_timestamp"
        const val DEFAULT_MODEL = "gemini-3.8-flash"

        val AVAILABLE_MODELS = listOf(
            "gemini-3.8-flash" to "Gemini 3.8 Flash (Önerilen)",
            "gemini-3.5-flash" to "Gemini 3.5 Flash",
            "gemini-3.5-flash-lite" to "Gemini 3.5 Flash Lite"
        )
    }
}
