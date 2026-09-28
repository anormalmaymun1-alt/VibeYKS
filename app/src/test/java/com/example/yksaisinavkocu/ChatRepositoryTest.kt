package com.example.yksaisinavkocu

import com.example.yksaisinavkocu.data.local.ChatDao
import com.example.yksaisinavkocu.data.local.entity.ChatMessageEntity
import com.example.yksaisinavkocu.data.repository.ChatRepository
import com.example.yksaisinavkocu.service.parser.AppAction
import com.example.yksaisinavkocu.ui.screen.coach.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatRepositoryTest {

    // Dummy ChatDao for pure unit tests
    private val fakeDao = object : ChatDao {
        val messages = mutableListOf<ChatMessageEntity>()

        override fun getAllMessages(): Flow<List<ChatMessageEntity>> = flowOf(messages)
        override suspend fun getAllMessagesList(): List<ChatMessageEntity> = messages.toList()
        override suspend fun getRecentMessages(limit: Int): List<ChatMessageEntity> = messages.takeLast(limit)
        override suspend fun insertMessage(message: ChatMessageEntity) { messages.add(message) }
        override suspend fun insertMessages(messages: List<ChatMessageEntity>) { this.messages.addAll(messages) }
        override suspend fun deleteAllMessages() { messages.clear() }
        override suspend fun getMessageCount(): Int = messages.size
        override suspend fun trimOldMessages(keepCount: Int) {
            while (messages.size > keepCount) {
                messages.removeAt(0)
            }
        }
    }

    private val repository = ChatRepository(fakeDao)

    @Test
    fun testActionSerializationAndDeserialization() {
        val actions = listOf(
            AppAction.NavigateTab(tab = "analytics", subject = "Matematik"),
            AppAction.OpenScanner,
            AppAction.NavigateExams,
            AppAction.SwitchTheme,
            AppAction.StudyPlan(plan = listOf("Konu 1 çalış", "100 soru çöz")),
            AppAction.NavigateSettings
        )

        val jsonStr = repository.serializeActions(actions)
        assertTrue(jsonStr.contains("NAVIGATE_TAB"))
        assertTrue(jsonStr.contains("OPEN_SCANNER"))
        assertTrue(jsonStr.contains("STUDY_PLAN"))

        val parsedActions = repository.deserializeActions(jsonStr)
        assertEquals(actions.size, parsedActions.size)

        val navTab = parsedActions[0] as AppAction.NavigateTab
        assertEquals("analytics", navTab.tab)
        assertEquals("Matematik", navTab.subject)

        assertTrue(parsedActions[1] is AppAction.OpenScanner)
        assertTrue(parsedActions[2] is AppAction.NavigateExams)
        assertTrue(parsedActions[3] is AppAction.SwitchTheme)

        val plan = parsedActions[4] as AppAction.StudyPlan
        assertEquals(2, plan.plan.size)
        assertEquals("Konu 1 çalış", plan.plan[0])

        assertTrue(parsedActions[5] is AppAction.NavigateSettings)
    }

    @Test
    fun testEmptyActionsHandling() {
        assertEquals("[]", repository.serializeActions(emptyList()))
        assertTrue(repository.deserializeActions("[]").isEmpty())
        assertTrue(repository.deserializeActions("").isEmpty())
        assertTrue(repository.deserializeActions("invalid json").isEmpty())
    }
}
