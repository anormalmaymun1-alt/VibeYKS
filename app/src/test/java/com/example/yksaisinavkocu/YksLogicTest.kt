package com.example.yksaisinavkocu

import com.example.yksaisinavkocu.data.local.entity.SubjectResult
import com.example.yksaisinavkocu.service.parser.ActionParser
import com.example.yksaisinavkocu.service.parser.AppAction
import com.example.yksaisinavkocu.ui.util.DateFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class YksLogicTest {

    @Test
    fun testNetCalculationFormula() {
        val subject = SubjectResult(
            subjectName = "Türkçe",
            correct = 35,
            wrong = 4,
            empty = 1,
            net = 35f - (4f / 4f)
        )
        assertEquals(34.0f, subject.net, 0.001f)

        val subject2 = SubjectResult(
            subjectName = "Matematik",
            correct = 27,
            wrong = 5,
            empty = 8,
            net = 27f - (5f / 4f)
        )
        assertEquals(25.75f, subject2.net, 0.001f)
    }

    @Test
    fun testActionParser_parsesMultipleActions() {
        val rawResponse = """
            Tebrikler! Matematik netlerin harika görünüyor.
            Bir sonraki sınav için tarama yapabilirsin.
            <!-- ACTIONS: [{"type": "OPEN_SCANNER"}, {"type": "NAVIGATE_TAB", "tab": "analytics", "subject": "Matematik"}, {"type": "SWITCH_THEME"}] -->
        """.trimIndent()

        val actions = ActionParser.parseActions(rawResponse)
        assertEquals(3, actions.size)
        assertTrue(actions[0] is AppAction.OpenScanner)
        assertTrue(actions[1] is AppAction.NavigateTab)
        assertEquals("analytics", (actions[1] as AppAction.NavigateTab).tab)
        assertEquals("Matematik", (actions[1] as AppAction.NavigateTab).subject)
        assertTrue(actions[2] is AppAction.SwitchTheme)

        val clean = ActionParser.cleanResponseText(rawResponse)
        assertTrue(!clean.contains("<!-- ACTIONS:"))
        assertTrue(clean.contains("Tebrikler!"))
    }

    @Test
    fun testDateFormatter_parseFormats() {
        val millis1 = DateFormatter.parseDateToMillis("27.09.2026")
        assertNotNull(millis1)

        val millis2 = DateFormatter.parseDateToMillis("27/09/2026")
        assertNotNull(millis2)

        val millis3 = DateFormatter.parseDateToMillis("2026-09-27")
        assertNotNull(millis3)
    }

    @Test
    fun testCustomSystemPromptInjection() {
        val examContext = "Öğrenci son denemede 75 net yaptı."
        val customInstructions = "Bana her zaman bir komutan gibi hitap et ve sert ol."

        // Özel komut verildiğinde
        val promptWithCustom = com.example.yksaisinavkocu.service.gemini.PromptTemplates.buildCoachSystemPrompt(
            examContext = examContext,
            customInstructions = customInstructions
        )
        assertTrue(promptWithCustom.contains("Öğrenci Talimatları:"))
        assertTrue(promptWithCustom.contains(customInstructions))
        assertTrue(promptWithCustom.contains(examContext))

        // Özel komut boş olduğunda
        val promptWithoutCustom = com.example.yksaisinavkocu.service.gemini.PromptTemplates.buildCoachSystemPrompt(
            examContext = examContext,
            customInstructions = ""
        )
        assertTrue(!promptWithoutCustom.contains("Öğrenci Talimatları"))
        assertTrue(promptWithoutCustom.contains(examContext))
    }
}
