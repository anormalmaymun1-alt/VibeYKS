package com.example.yksaisinavkocu

import com.example.yksaisinavkocu.service.gemini.ChatContextManager
import com.example.yksaisinavkocu.ui.screen.coach.ChatMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatContextLimiterTest {

    @Test
    fun testSanitizeRoleAlternation_dropsLeadingModelMessage() {
        val messages = listOf(
            ChatMessage(text = "AI welcome greeting", isFromUser = false),
            ChatMessage(text = "User message 1", isFromUser = true),
            ChatMessage(text = "AI reply 1", isFromUser = false)
        )

        val sanitized = ChatContextManager.sanitizeRoleAlternation(messages)

        assertEquals(2, sanitized.size)
        assertTrue(sanitized[0].isFromUser)
        assertEquals("User message 1", sanitized[0].text)
        assertTrue(!sanitized[1].isFromUser)
        assertEquals("AI reply 1", sanitized[1].text)
    }

    @Test
    fun testSanitizeRoleAlternation_dropsTrailingUserMessage() {
        // If the session ended with an unreplied user message, it must be dropped
        // so history ends with 'model' before the next user prompt
        val messages = listOf(
            ChatMessage(text = "User 1", isFromUser = true),
            ChatMessage(text = "Model 1", isFromUser = false),
            ChatMessage(text = "User 2 (unanswered)", isFromUser = true)
        )

        val sanitized = ChatContextManager.sanitizeRoleAlternation(messages)

        assertEquals(2, sanitized.size)
        assertEquals("User 1", sanitized[0].text)
        assertEquals("Model 1", sanitized[1].text)
        assertTrue(!sanitized.last().isFromUser)
    }

    @Test
    fun testSanitizeRoleAlternation_collapsesConsecutiveUserMessages() {
        val messages = listOf(
            ChatMessage(text = "First user note", isFromUser = true),
            ChatMessage(text = "Second user note", isFromUser = true),
            ChatMessage(text = "Model reply", isFromUser = false)
        )

        val sanitized = ChatContextManager.sanitizeRoleAlternation(messages)

        assertEquals(2, sanitized.size)
        assertTrue(sanitized[0].isFromUser)
        assertTrue(sanitized[0].text.contains("First user note"))
        assertTrue(sanitized[0].text.contains("Second user note"))
        assertTrue(!sanitized[1].isFromUser)
    }

    @Test
    fun testBuildSlidingWindowHistory_alwaysIncludesSystemTurn() {
        val history = ChatContextManager.buildSlidingWindowHistory(
            systemPrompt = "You are a test coach",
            recentMessages = emptyList()
        )

        assertEquals(2, history.size)
        assertEquals("user", history[0].role)
        assertEquals("model", history[1].role)
    }

    @Test
    fun testBuildSlidingWindowHistory_limitsToMaxContextCount() {
        // Create 20 alternating messages (10 user + 10 model)
        val messages = (1..10).flatMap { i ->
            listOf(
                ChatMessage(text = "User message $i", isFromUser = true),
                ChatMessage(text = "Model message $i", isFromUser = false)
            )
        }

        // Limit to 6 messages (3 turns)
        val history = ChatContextManager.buildSlidingWindowHistory(
            systemPrompt = "System prompt",
            recentMessages = messages,
            maxContextCount = 6
        )

        // System prompt (2 items) + max 6 conversation items = 8 items
        assertEquals(8, history.size)
        assertEquals("user", history[0].role)
        assertEquals("model", history[1].role)

        // The remaining 6 must be the latest messages: User 8, Model 8, User 9, Model 9, User 10, Model 10
        val conversationHistory = history.drop(2)
        assertEquals(6, conversationHistory.size)
        assertEquals("user", conversationHistory[0].role)
        assertEquals("model", conversationHistory.last().role)
    }

    @Test
    fun testBuildSlidingWindowHistory_filtersCommandsAndWelcome() {
        val messages = listOf(
            ChatMessage(text = ChatContextManager.WELCOME_MESSAGE_TEXT, isFromUser = false),
            ChatMessage(text = "//help", isFromUser = true),
            ChatMessage(text = "//stats", isFromUser = true),
            ChatMessage(text = "Actual question", isFromUser = true),
            ChatMessage(text = "Actual answer", isFromUser = false)
        )

        val history = ChatContextManager.buildSlidingWindowHistory(
            systemPrompt = "System prompt",
            recentMessages = messages
        )

        // System prompt (2) + Actual question + Actual answer = 4
        assertEquals(4, history.size)
    }
}
