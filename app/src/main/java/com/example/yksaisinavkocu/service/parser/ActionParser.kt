package com.example.yksaisinavkocu.service.parser

import kotlinx.serialization.json.*

sealed class AppAction {
    data class NavigateTab(val tab: String, val subject: String? = null) : AppAction()
    data object OpenScanner : AppAction()
    data object NavigateExams : AppAction()
    data object SwitchTheme : AppAction()
    data class StudyPlan(val plan: List<String>) : AppAction()
    data object NavigateSettings : AppAction()
}

object ActionParser {
    private val actionPattern = Regex("<!-- ACTIONS: (\\[.*?]) -->", RegexOption.DOT_MATCHES_ALL)
    private val json = Json { ignoreUnknownKeys = true }

    fun parseActions(responseText: String): List<AppAction> {
        val match = actionPattern.find(responseText) ?: return emptyList()
        return try {
            val jsonArray = json.parseToJsonElement(match.groupValues[1]).jsonArray
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
                        plan = obj["plan"]?.jsonArray?.map {
                            it.jsonPrimitive.content
                        } ?: emptyList()
                    )
                    "NAVIGATE_SETTINGS" -> AppAction.NavigateSettings
                    else -> null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun cleanResponseText(responseText: String): String {
        return responseText.replace(actionPattern, "").trim()
    }
}
