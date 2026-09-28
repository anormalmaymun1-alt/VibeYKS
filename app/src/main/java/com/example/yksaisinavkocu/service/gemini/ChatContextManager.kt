package com.example.yksaisinavkocu.service.gemini

import com.example.yksaisinavkocu.ui.screen.coach.ChatMessage
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content

object ChatContextManager {

    const val MAX_CONTEXT_MESSAGES = 10 // Maximum messages retained in API context sliding window (5 user + 5 model turns)
    const val MAX_SAVED_MESSAGES = 60   // Maximum messages kept in local database to prevent DB bloat
    const val WELCOME_MESSAGE_TEXT =
        "Merhaba! Ben YKS AI Sınav Koçun. Sınav verilerini inceledim ve sana özel stratejiler sunabilirim. " +
                "Aşağıdaki hızlı butonları kullanabilir veya doğrudan soru sorabilirsin!"

    /**
     * Builds the history list for Gemini startChat().
     * - Preserves the System Prompt turn (Turn 0: user system prompt, model ack)
     * - Filters and trims recent messages to MAX_CONTEXT_MESSAGES (sliding window)
     * - Guarantees strict user -> model -> user -> model alternation
     * - Ensures the history ends with 'model' so the next user message alternates properly
     */
    fun buildSlidingWindowHistory(
        systemPrompt: String,
        recentMessages: List<ChatMessage>,
        maxContextCount: Int = MAX_CONTEXT_MESSAGES
    ): List<Content> {
        val systemTurn = listOf(
            content(role = "user") { text(systemPrompt) },
            content(role = "model") { text("Anladım, öğrencinin sınav verilerini ve özel talimatlarını inceledim. Yardıma hazırım.") }
        )

        // Filter out empty messages, command outputs, and the welcome greeting
        val validConversation = recentMessages.filter {
            it.text.isNotBlank() && !it.text.startsWith("//") && it.text != WELCOME_MESSAGE_TEXT
        }

        if (validConversation.isEmpty()) {
            return systemTurn
        }

        // Take the most recent messages up to maxContextCount
        val rawSlice = validConversation.takeLast(maxContextCount)

        // Normalize roles: must start with user, alternate user/model, and end with model
        val alternating = sanitizeRoleAlternation(rawSlice)

        val historyContents = alternating.map { msg ->
            content(role = if (msg.isFromUser) "user" else "model") {
                text(msg.text)
            }
        }

        return systemTurn + historyContents
    }

    /**
     * Ensures messages strictly alternate: user, model, user, model...
     * Must start with a user message and end with a model message.
     */
    fun sanitizeRoleAlternation(messages: List<ChatMessage>): List<ChatMessage> {
        if (messages.isEmpty()) return emptyList()

        // 1. Drop leading model messages until we find the first user message
        val startIdx = messages.indexOfFirst { it.isFromUser }
        if (startIdx == -1) return emptyList()
        val fromFirstUser = messages.subList(startIdx, messages.size)

        // 2. Ensure strictly alternating roles by collapsing consecutive same-role messages
        val result = mutableListOf<ChatMessage>()
        for (msg in fromFirstUser) {
            if (result.isEmpty()) {
                result.add(msg)
            } else {
                val last = result.last()
                if (last.isFromUser == msg.isFromUser) {
                    // Combine consecutive same-role messages to preserve context
                    result[result.size - 1] = last.copy(
                        text = "${last.text}\n\n${msg.text}"
                    )
                } else {
                    result.add(msg)
                }
            }
        }

        // 3. In Gemini multi-turn chat, the history passed to startChat must end with a 'model' turn,
        // because the user's next action will be sending a 'user' message via chat.sendMessage().
        while (result.isNotEmpty() && result.last().isFromUser) {
            result.removeAt(result.size - 1)
        }

        return result
    }
}
