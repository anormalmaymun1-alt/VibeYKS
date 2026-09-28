package com.example.yksaisinavkocu

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class SubjectWeaknessTest {

    private fun matchesSubject(tab: String, topicSubject: String): Boolean {
        if (tab == "Genel") return true
        val normTab = tab.trim().lowercase(Locale("tr"))
        val normTopic = topicSubject.trim().lowercase(Locale("tr"))

        if (normTopic == normTab || normTopic.contains(normTab) || normTab.contains(normTopic)) {
            return true
        }

        return when {
            normTab.contains("sosyal") ->
                normTopic.contains("tarih") || normTopic.contains("coğrafya") ||
                        normTopic.contains("felsefe") || normTopic.contains("din") || normTopic.contains("sosyal")
            normTab.contains("fen") ->
                normTopic.contains("fizik") || normTopic.contains("kimya") ||
                        normTopic.contains("biyoloji") || normTopic.contains("fen")
            normTab.contains("matematik") ->
                normTopic.contains("matematik") || normTopic.contains("geometri")
            normTab.contains("türkçe") ->
                normTopic.contains("türkçe") || normTopic.contains("edebiyat") || normTopic.contains("dil")
            else -> false
        }
    }

    @Test
    fun testGenelMatchesAll() {
        assertTrue(matchesSubject("Genel", "Türkçe"))
        assertTrue(matchesSubject("Genel", "Fizik"))
        assertTrue(matchesSubject("Genel", "Coğrafya-1"))
    }

    @Test
    fun testSosyalBilimlerMatchesSubDisciplines() {
        assertTrue(matchesSubject("Sosyal Bilimler", "Coğrafya-1"))
        assertTrue(matchesSubject("Sosyal Bilimler", "Tarih-1"))
        assertTrue(matchesSubject("Sosyal Bilimler", "Felsefe"))
        assertTrue(matchesSubject("Sosyal Bilimler", "Din Kültürü"))
        assertFalse(matchesSubject("Sosyal Bilimler", "Fizik"))
        assertFalse(matchesSubject("Sosyal Bilimler", "Türkçe"))
    }

    @Test
    fun testFenBilimleriMatchesSubDisciplines() {
        assertTrue(matchesSubject("Fen Bilimleri", "Fizik"))
        assertTrue(matchesSubject("Fen Bilimleri", "Kimya"))
        assertTrue(matchesSubject("Fen Bilimleri", "Biyoloji"))
        assertFalse(matchesSubject("Fen Bilimleri", "Matematik"))
        assertFalse(matchesSubject("Fen Bilimleri", "Tarih"))
    }

    @Test
    fun testTemelMatematikMatchesMatematikAndGeometri() {
        assertTrue(matchesSubject("Temel Matematik", "Temel Matematik"))
        assertTrue(matchesSubject("Temel Matematik", "Matematik"))
        assertTrue(matchesSubject("Temel Matematik", "Geometri"))
        assertFalse(matchesSubject("Temel Matematik", "Fizik"))
    }

    @Test
    fun testTurkceMatchesTurkceAndDil() {
        assertTrue(matchesSubject("Türkçe", "Türkçe"))
        assertTrue(matchesSubject("Türkçe", "Dil ve Anlatım"))
        assertFalse(matchesSubject("Türkçe", "Biyoloji"))
    }
}
