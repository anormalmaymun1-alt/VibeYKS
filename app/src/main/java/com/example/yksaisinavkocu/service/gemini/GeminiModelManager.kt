package com.example.yksaisinavkocu.service.gemini

import com.example.yksaisinavkocu.data.preferences.AppPreferences
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig

class GeminiModelManager(private val preferences: AppPreferences) {

    private val modelPriority = listOf(
        "gemini-3.8-flash",
        "gemini-3.5-flash",
        "gemini-3.5-flash-lite"
    )

    fun getModelPriority(): List<String> = modelPriority

    private var currentFallbackIndex = 0

    // ── Model Instance Cache ──
    // Avoids creating a new GenerativeModel object on every call.
    // Cached instances are invalidated when the API key or model selection changes.
    private data class CacheKey(val modelName: String, val apiKey: String, val maxTokens: Int, val temperature: Float)
    private val modelCache = mutableMapOf<CacheKey, GenerativeModel>()

    fun getActiveModel(): GenerativeModel? {
        val apiKey = preferences.getApiKey() ?: return null
        val selectedModel = preferences.getSelectedModel()

        return getOrCreateModel(selectedModel, apiKey)
    }

    fun getFallbackModel(failedModel: String? = null, forOcr: Boolean = false): GenerativeModel? {
        val apiKey = preferences.getApiKey() ?: return null
        val candidates = if (failedModel != null) {
            modelPriority.filter { it != failedModel }
        } else {
            modelPriority
        }
        if (candidates.isEmpty()) return null
        currentFallbackIndex = (currentFallbackIndex + 1) % candidates.size
        val temperature = if (forOcr) 0.15f else 0.7f
        return getOrCreateModel(candidates[currentFallbackIndex], apiKey, maxTokens = 8192, temperature = temperature)
    }

    fun getModelForOcr(): GenerativeModel? {
        val apiKey = preferences.getApiKey() ?: return null
        // OCR için deterministik ve yüksek token kapasiteli model
        return getOrCreateModel(preferences.getSelectedModel(), apiKey, maxTokens = 8192, temperature = 0.15f)
    }

    fun getModelForChat(): GenerativeModel? {
        val apiKey = preferences.getApiKey() ?: return null
        return getOrCreateModel(preferences.getSelectedModel(), apiKey, maxTokens = 8192, temperature = 0.7f)
    }

    private fun getOrCreateModel(
        modelName: String,
        apiKey: String,
        maxTokens: Int = 8192,
        temperature: Float = 0.7f
    ): GenerativeModel {
        val key = CacheKey(modelName, apiKey, maxTokens, temperature)
        return modelCache.getOrPut(key) {
            createModel(modelName, apiKey, maxTokens, temperature)
        }
    }

    private fun createModel(
        modelName: String,
        apiKey: String,
        maxTokens: Int = 8192,
        temperature: Float = 0.7f
    ): GenerativeModel {
        return GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            generationConfig = generationConfig {
                this.temperature = temperature
                topP = 0.95f
                maxOutputTokens = maxTokens
            }
        )
    }

    fun resetFallback() {
        currentFallbackIndex = 0
    }

    /** Clears all cached model instances (e.g. when API key changes). */
    fun clearCache() {
        modelCache.clear()
    }

    fun isApiKeyConfigured(): Boolean = preferences.getApiKey() != null
}
