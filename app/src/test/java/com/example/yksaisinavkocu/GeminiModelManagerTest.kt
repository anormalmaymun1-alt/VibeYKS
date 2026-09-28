package com.example.yksaisinavkocu

import com.example.yksaisinavkocu.data.preferences.AppPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiModelManagerTest {

    @Test
    fun testDefaultModelIsGemini38Flash() {
        assertEquals("gemini-3.8-flash", AppPreferences.DEFAULT_MODEL)
    }

    @Test
    fun testAvailableModelsContainsGemini38And35() {
        val models = AppPreferences.AVAILABLE_MODELS.map { it.first }

        // En az 3 model bulunmalı (3.8 Flash, 3.5 Flash, 3.5 Flash Lite)
        assertTrue(models.size >= 3)

        // İlk ve önerilen model gemini-3.8-flash olmalı
        assertEquals("gemini-3.8-flash", models.first())

        // 3.8 ve 3.5 modelleri listede bulunmalı
        assertTrue("gemini-3.8-flash bulunamadı", models.contains("gemini-3.8-flash"))
        assertTrue("gemini-3.5-flash bulunamadı", models.contains("gemini-3.5-flash"))
        assertTrue("gemini-3.5-flash-lite bulunamadı", models.contains("gemini-3.5-flash-lite"))

        // Kapatılan 2.0 modeli listede OLMAMALI
        assertTrue("gemini-2.0-flash listeden kaldırılmamış", !models.contains("gemini-2.0-flash"))
    }

    @Test
    fun testModelLabelsAreDescriptive() {
        val modelMap = AppPreferences.AVAILABLE_MODELS.toMap()

        assertTrue(modelMap["gemini-3.8-flash"]?.contains("3.8") == true)
        assertTrue(modelMap["gemini-3.8-flash"]?.contains("Önerilen") == true)
        assertTrue(modelMap["gemini-3.5-flash"]?.contains("3.5") == true)
    }
}
