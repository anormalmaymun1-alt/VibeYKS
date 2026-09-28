package com.example.yksaisinavkocu.data.repository

import com.example.yksaisinavkocu.data.local.ChatDao
import com.example.yksaisinavkocu.data.local.entity.ChatMessageEntity
import com.example.yksaisinavkocu.service.parser.AppAction
import com.example.yksaisinavkocu.ui.screen.coach.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.*

class ChatRepository(private val chatDao: ChatDao) {

    private val json = Json { ignoreUnknownKeys = true }

    fun getAllMessagesFlow(): Flow<List<ChatMessage>> =
        chatDao.getAllMessages().map { list -> list.map { it.toDomain() } }

    suspend fun getRecentMessages(limit: Int = 60): List<ChatMessage> =
        chatDao.getRecentMessages(limit).map { it.toDomain() }

    suspend fun getAllMessagesList(): List<ChatMessage> =
        chatDao.getAllMessagesList().map { it.toDomain() }

    suspend fun saveMessage(message: ChatMessage) {
        chatDao.insertMessage(message.toEntity())
    }

    suspend fun saveMessages(messages: List<ChatMessage>) {
        chatDao.insertMessages(messages.map { it.toEntity() })
    }

    suspend fun trimOldMessages(keepCount: Int = 60) {
        chatDao.trimOldMessages(keepCount)
    }

    suspend fun clearHistory() {
        chatDao.deleteAllMessages()
    }

    suspend fun getMessageCount(): Int = chatDao.getMessageCount()

    private fun ChatMessageEntity.toDomain(): ChatMessage {
        return ChatMessage(
            id = id,
            text = text,
            isFromUser = isFromUser,
            timestamp = timestamp,
            actions = deserializeActions(actionsJson)
        )
    }

    private fun ChatMessage.toEntity(): ChatMessageEntity {
        return ChatMessageEntity(
            id = id,
            text = text,
            isFromUser = isFromUser,
            timestamp = timestamp,
            actionsJson = serializeActions(actions)
        )
    }

    fun serializeActions(actions: List<AppAction>): String {
        if (actions.isEmpty()) return "[]"
        val array = buildJsonArray {
            actions.forEach { action ->
                addJsonObject {
                    when (action) {
                        is AppAction.NavigateTab -> {
                            put("type", "NAVIGATE_TAB")
                            put("tab", action.tab)
                            action.subject?.let { put("subject", it) }
                        }
                        is AppAction.OpenScanner -> put("type", "OPEN_SCANNER")
                        is AppAction.NavigateExams -> put("type", "NAVIGATE_EXAMS")
                        is AppAction.SwitchTheme -> put("type", "SWITCH_THEME")
                        is AppAction.StudyPlan -> {
                            put("type", "STUDY_PLAN")
                            putJsonArray("plan") {
                                action.plan.forEach { add(it) }
                            }
                        }
                        is AppAction.NavigateSettings -> put("type", "NAVIGATE_SETTINGS")
                    }
                }
            }
        }
        return array.toString()
    }

    fun deserializeActions(jsonStr: String): List<AppAction> {
        if (jsonStr.isBlank() || jsonStr == "[]") return emptyList()
        return try {
            val jsonArray = json.parseToJsonElement(jsonStr).jsonArray
            jsonArray.mapNotNull { element ->
                val obj = element.jsonObject
                when (obj["type"]?.jsonPrimitive?.content) {
                    "NAVIGATE_TAB" -> AppAction.NavigateTab(
                        tab = obj["tab"]?.jsonPrimitive?.content ?: "analytics",
                        subject = obj["subject"]?.jsonPrimitive?.contentOrNull
                    )
                    "OPEN_SCANNER" -> AppAction.OpenScanner
                    "NAVIGATE_EXAMS" -> AppAction.NavigateExams
                    "SWITCH_THEME" -> AppAction.SwitchTheme
                    "STUDY_PLAN" -> AppAction.StudyPlan(
                        plan = obj["plan"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
                    )
                    "NAVIGATE_SETTINGS" -> AppAction.NavigateSettings
                    else -> null
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
