package com.example.yksaisinavkocu.ui.screen.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yksaisinavkocu.YksApp
import com.example.yksaisinavkocu.data.preferences.AppPreferences
import com.example.yksaisinavkocu.data.repository.ChatRepository
import com.example.yksaisinavkocu.data.repository.ExamRepository
import com.example.yksaisinavkocu.service.gemini.CoachContextBuilder
import com.example.yksaisinavkocu.service.gemini.GeminiModelManager
import com.example.yksaisinavkocu.service.gemini.PromptTemplates
import com.example.yksaisinavkocu.service.gemini.ChatContextManager
import com.example.yksaisinavkocu.service.parser.ActionParser
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.type.Content
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CoachUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false,
    val inputText: String = "",
    val isApiConfigured: Boolean = false,
    val errorMessage: String? = null
)

class AiCoachViewModel(
    private val modelManager: GeminiModelManager = YksApp.instance.geminiModelManager,
    private val contextBuilder: CoachContextBuilder = YksApp.instance.coachContextBuilder,
    private val examRepository: ExamRepository = YksApp.instance.examRepository,
    private val chatRepository: ChatRepository = YksApp.instance.chatRepository,
    private val preferences: AppPreferences = YksApp.instance.preferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoachUiState())
    val uiState: StateFlow<CoachUiState> = _uiState.asStateFlow()

    private var examContext: String = ""

    // ── Cached Chat session ──
    // Reusing the same Chat object avoids re-sending the system prompt + exam
    // context as input tokens with every single message.
    private var cachedChat: Chat? = null

    // ── Dirty-flag for exam context ──
    // Marked dirty when exam database updates, then rebuilt on next user interaction.
    private var isContextDirty = true

    companion object {
        const val MAX_CONTEXT_MESSAGES = ChatContextManager.MAX_CONTEXT_MESSAGES
        const val MAX_SAVED_MESSAGES = ChatContextManager.MAX_SAVED_MESSAGES
        const val WELCOME_MESSAGE_TEXT = ChatContextManager.WELCOME_MESSAGE_TEXT
    }

    init {
        _uiState.update { it.copy(isApiConfigured = modelManager.isApiKeyConfigured()) }

        // Mark context dirty whenever exam data changes — but do NOT rebuild yet
        viewModelScope.launch {
            examRepository.getAllExams()
                .drop(1) // skip the initial emission, we'll build on first use
                .collect {
                    isContextDirty = true
                }
        }

        viewModelScope.launch {
            examContext = contextBuilder.buildContext()
            isContextDirty = false

            // ── Multi-session persistence: load saved messages from Room DB ──
            val savedMessages = chatRepository.getRecentMessages(MAX_SAVED_MESSAGES)
            if (savedMessages.isEmpty()) {
                val welcomeMsg = ChatMessage(
                    text = WELCOME_MESSAGE_TEXT,
                    isFromUser = false
                )
                chatRepository.saveMessage(welcomeMsg)
                _uiState.update { it.copy(messages = listOf(welcomeMsg)) }
            } else {
                _uiState.update { it.copy(messages = savedMessages) }

                // Pre-warm cachedChat session from saved context
                val model = modelManager.getModelForChat()
                if (model != null) {
                    val customPrompt = preferences.getCustomSystemPrompt()
                    val systemPrompt = PromptTemplates.buildCoachSystemPrompt(examContext, customPrompt)
                    val history = buildSlidingWindowHistory(systemPrompt, savedMessages)
                    cachedChat = model.startChat(history = history)
                }
            }
        }
    }

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage(text: String? = null) {
        val message = text ?: _uiState.value.inputText.trim()
        if (message.isBlank()) return

        // ── Hidden system commands — bypass API entirely ──
        if (message.startsWith("//")) {
            handleSystemCommand(message)
            return
        }

        val userMsg = ChatMessage(text = message, isFromUser = true)

        _uiState.update {
            it.copy(
                messages = it.messages + userMsg,
                inputText = "",
                isTyping = true,
                errorMessage = null
            )
        }

        // Persist user message to local database
        viewModelScope.launch {
            chatRepository.saveMessage(userMsg)
        }

        viewModelScope.launch {
            try {
                // Rebuild context ONLY if exam data changed since last build
                if (isContextDirty) {
                    examContext = contextBuilder.buildContext()
                    isContextDirty = false
                    // Re-anchor session with updated exam context while retaining recent conversational memory
                    val model = modelManager.getModelForChat()
                    if (model != null) {
                        val customPrompt = preferences.getCustomSystemPrompt()
                        val systemPrompt = PromptTemplates.buildCoachSystemPrompt(examContext, customPrompt)
                        val history = buildSlidingWindowHistory(systemPrompt, _uiState.value.messages.dropLast(1))
                        cachedChat = model.startChat(history = history)
                    }
                }

                val model = modelManager.getModelForChat()
                if (model == null) {
                    _uiState.update {
                        it.copy(
                            isTyping = false,
                            errorMessage = "API anahtarı tanımlanmamış. Lütfen Ayarlar'dan ekleyin."
                        )
                    }
                    return@launch
                }

                // ── Prevent Context Bloat: Trim in-flight history if it exceeds sliding window limit ──
                trimContextIfExceeded(model)

                // Reuse existing chat session or create one with sliding window history
                val chat = cachedChat ?: run {
                    val customPrompt = preferences.getCustomSystemPrompt()
                    val systemPrompt = PromptTemplates.buildCoachSystemPrompt(examContext, customPrompt)
                    val history = buildSlidingWindowHistory(systemPrompt, _uiState.value.messages.dropLast(1))
                    model.startChat(history = history).also { cachedChat = it }
                }

                val response = chat.sendMessage(message)
                val responseText = response.text ?: "Yanıt alınamadı."

                // Record token usage
                val tokensUsed = response.usageMetadata?.totalTokenCount
                    ?: ((message.length + responseText.length) / 4).coerceAtLeast(80)
                YksApp.instance.tokenUsageManager.recordUsage(tokensUsed)

                val actions = ActionParser.parseActions(responseText)
                val cleanText = ActionParser.cleanResponseText(responseText)

                val aiMsg = ChatMessage(
                    text = cleanText,
                    isFromUser = false,
                    actions = actions
                )

                _uiState.update {
                    it.copy(
                        messages = it.messages + aiMsg,
                        isTyping = false
                    )
                }

                // Persist AI response and trim database records to MAX_SAVED_MESSAGES
                viewModelScope.launch {
                    chatRepository.saveMessage(aiMsg)
                    chatRepository.trimOldMessages(MAX_SAVED_MESSAGES)
                }
            } catch (e: Exception) {
                // Invalidate cached chat on error so next attempt can rebuild cleanly
                cachedChat = null
                // Failover attempt with fallback model
                try {
                    val fallbackModel = modelManager.getFallbackModel()
                    if (fallbackModel != null) {
                        val customPrompt = preferences.getCustomSystemPrompt()
                        val systemPrompt = PromptTemplates.buildCoachSystemPrompt(examContext, customPrompt)
                        val fallbackHistory = buildSlidingWindowHistory(systemPrompt, _uiState.value.messages.dropLast(1))
                        val fallbackChat = fallbackModel.startChat(history = fallbackHistory)
                        val response = fallbackChat.sendMessage(message)
                        val responseText = response.text ?: "Yanıt alınamadı."

                        val fallbackTokens = response.usageMetadata?.totalTokenCount
                            ?: ((message.length + responseText.length) / 4).coerceAtLeast(80)
                        YksApp.instance.tokenUsageManager.recordUsage(fallbackTokens)

                        val actions = ActionParser.parseActions(responseText)
                        val cleanText = ActionParser.cleanResponseText(responseText)

                        val aiMsg = ChatMessage(
                            text = cleanText,
                            isFromUser = false,
                            actions = actions
                        )

                        _uiState.update {
                            it.copy(
                                messages = it.messages + aiMsg,
                                isTyping = false
                            )
                        }

                        // Maintain active session on fallback model
                        cachedChat = fallbackChat

                        viewModelScope.launch {
                            chatRepository.saveMessage(aiMsg)
                            chatRepository.trimOldMessages(MAX_SAVED_MESSAGES)
                        }
                    }
                } catch (fallbackE: Exception) {
                    val fullErr = "${e.message} ${fallbackE.message}"
                    if (fullErr.contains("RESOURCE_EXHAUSTED") || fullErr.contains("quota") || fullErr.contains("429")) {
                        YksApp.instance.tokenUsageManager.markQuotaExhausted()
                    }
                    _uiState.update {
                        it.copy(
                            isTyping = false,
                            errorMessage = formatUserFriendlyError(e)
                        )
                    }
                }
            }
        }
    }

    /**
     * Prevents context bloat by checking the active chat history size.
     * If the session exceeds system turn (2) + MAX_CONTEXT_MESSAGES, it re-anchors
     * the session to the most recent window of conversation.
     */
    private fun trimContextIfExceeded(model: com.google.ai.client.generativeai.GenerativeModel) {
        val currentChat = cachedChat ?: return
        // 2 items for system prompt turn (user prompt + model ack)
        if (currentChat.history.size > (2 + MAX_CONTEXT_MESSAGES)) {
            val customPrompt = preferences.getCustomSystemPrompt()
            val systemPrompt = PromptTemplates.buildCoachSystemPrompt(examContext, customPrompt)
            val trimmedHistory = buildSlidingWindowHistory(systemPrompt, _uiState.value.messages.dropLast(1))
            cachedChat = model.startChat(history = trimmedHistory)
        }
    }

    fun buildSlidingWindowHistory(
        systemPrompt: String,
        recentMessages: List<ChatMessage>,
        maxContextCount: Int = MAX_CONTEXT_MESSAGES
    ): List<Content> = ChatContextManager.buildSlidingWindowHistory(systemPrompt, recentMessages, maxContextCount)

    fun sanitizeRoleAlternation(messages: List<ChatMessage>): List<ChatMessage> =
        ChatContextManager.sanitizeRoleAlternation(messages)

    // ── Hidden System Commands ──
    // These execute locally without any API call, saving tokens.
    private fun handleSystemCommand(command: String) {
        val cmd = command.lowercase().trim()
        val userMsg = ChatMessage(text = command, isFromUser = true)

        when {
            cmd == "//help" -> {
                _uiState.update {
                    it.copy(
                        messages = it.messages + userMsg + ChatMessage(
                            text = "📋 **Kullanılabilir Komutlar:**\n\n" +
                                    "• `//refresh` — Sınav verilerini yeniden yükle\n" +
                                    "• `//clear` — Sohbet geçmişini ve önbelleği temizle\n" +
                                    "• `//cache` / `//model` — Aktif model ve bağlam önbellek durumu\n" +
                                    "• `//stats` — Hızlı sınav istatistikleri\n" +
                                    "• `//help` — Bu yardım mesajı",
                            isFromUser = false
                        ),
                        inputText = ""
                    )
                }
            }
            cmd == "//refresh" -> {
                viewModelScope.launch {
                    examContext = contextBuilder.buildContext()
                    isContextDirty = false
                    val model = modelManager.getModelForChat()
                    if (model != null) {
                        val customPrompt = preferences.getCustomSystemPrompt()
                        val systemPrompt = PromptTemplates.buildCoachSystemPrompt(examContext, customPrompt)
                        val history = buildSlidingWindowHistory(systemPrompt, _uiState.value.messages)
                        cachedChat = model.startChat(history = history)
                    }
                    _uiState.update {
                        it.copy(
                            messages = it.messages + userMsg + ChatMessage(
                                text = "✅ Sınav verileri başarıyla yenilendi. Önbellekteki sohbet bağlamı güncel verilerle güncellendi.",
                                isFromUser = false
                            ),
                            inputText = ""
                        )
                    }
                }
            }
            cmd == "//clear" -> {
                cachedChat = null
                viewModelScope.launch {
                    chatRepository.clearHistory()
                    val welcomeMsg = ChatMessage(
                        text = "🧹 Sohbet geçmişi ve oturum önbelleği temizlendi. Yeni bir sohbet başlatabilirsin!",
                        isFromUser = false
                    )
                    chatRepository.saveMessage(welcomeMsg)
                    _uiState.update {
                        it.copy(
                            messages = listOf(welcomeMsg),
                            inputText = "",
                            errorMessage = null
                        )
                    }
                }
            }
            cmd == "//stats" -> {
                viewModelScope.launch {
                    val stats = buildLocalStats()
                    _uiState.update {
                        it.copy(
                            messages = it.messages + userMsg + ChatMessage(
                                text = stats,
                                isFromUser = false
                            ),
                            inputText = ""
                        )
                    }
                }
            }
            cmd == "//model" || cmd == "//cache" -> {
                val selectedModel = preferences.getSelectedModel()
                val modelDisplay = AppPreferences.AVAILABLE_MODELS
                    .firstOrNull { it.first == selectedModel }?.second ?: selectedModel
                val hasKey = modelManager.isApiKeyConfigured()
                val cachedSessionActive = cachedChat != null
                val historySize = cachedChat?.history?.size ?: 0
                val activeContextTurns = if (historySize > 2) (historySize - 2) else 0
                val totalSaved = _uiState.value.messages.size

                val cacheInfo = buildString {
                    appendLine("🤖 **Model ve Önbellek Durumu**")
                    appendLine()
                    appendLine("• **Aktif Model:** $modelDisplay")
                    appendLine("• **API Anahtarı:** ${if (hasKey) "Yapılandırılmış ✅" else "Eksik ❌"}")
                    appendLine("• **Oturum Durumu:** ${if (cachedSessionActive) "Aktif (Oturumlar arası korunuyor) ✅" else "Yeni oturum oluşturulacak"}")
                    appendLine("• **Aktif Bağlam Penceresi:** $activeContextTurns / $MAX_CONTEXT_MESSAGES mesaj (Bağlam şişmesi koruması devrede)")
                    appendLine("• **Kayıtlı Mesaj Sayısı:** $totalSaved mesaj (Oturumlar arası yerel Room DB)")
                    appendLine()
                    appendLine("💡 *Not: Bağlam şişmesini ve gereksiz token tüketimini önlemek için API'ye en fazla son $MAX_CONTEXT_MESSAGES mesaj iletilir.*")
                }

                _uiState.update {
                    it.copy(
                        messages = it.messages + userMsg + ChatMessage(
                            text = cacheInfo,
                            isFromUser = false
                        ),
                        inputText = ""
                    )
                }
            }
            else -> {
                _uiState.update {
                    it.copy(
                        messages = it.messages + userMsg + ChatMessage(
                            text = "⚠️ Bilinmeyen komut: `$command`\n`//help` yazarak komut listesini görebilirsin.",
                            isFromUser = false
                        ),
                        inputText = ""
                    )
                }
            }
        }
    }

    private suspend fun buildLocalStats(): String {
        val tytExams = examRepository.getRecentExams("TYT", 5)
        val aytExams = examRepository.getRecentExams("AYT", 5)
        val totalCount = examRepository.getExamCount()

        return buildString {
            appendLine("📊 **Hızlı Sınav İstatistikleri** (API kullanılmadı)")
            appendLine()
            appendLine("📝 Toplam kayıtlı sınav: **$totalCount**")

            if (tytExams.isNotEmpty()) {
                val avgTyt = tytExams.map { it.totalNet }.average()
                val bestTyt = tytExams.maxOf { it.totalNet }
                appendLine()
                appendLine("**Son ${tytExams.size} TYT:**")
                appendLine("• Ortalama net: **%.2f**".format(avgTyt))
                appendLine("• En yüksek: **%.2f**".format(bestTyt))
            }

            if (aytExams.isNotEmpty()) {
                val avgAyt = aytExams.map { it.totalNet }.average()
                val bestAyt = aytExams.maxOf { it.totalNet }
                appendLine()
                appendLine("**Son ${aytExams.size} AYT:**")
                appendLine("• Ortalama net: **%.2f**".format(avgAyt))
                appendLine("• En yüksek: **%.2f**".format(bestAyt))
            }

            if (tytExams.isEmpty() && aytExams.isEmpty()) {
                appendLine("\nHenüz kaydedilmiş sınav bulunmuyor.")
            }
        }
    }

    private fun formatUserFriendlyError(e: Throwable): String {
        val msg = e.message ?: ""
        return when {
            msg.contains("503") || msg.contains("high demand") || msg.contains("UNAVAILABLE") ->
                "Google AI sunucularında geçici yoğunluk var (503). Lütfen birkaç saniye sonra tekrar deneyin veya Ayarlar'dan başka bir model seçin."
            msg.contains("API_KEY_INVALID") || msg.contains("API key not valid") ->
                "Geçersiz API anahtarı. Lütfen Ayarlar sekmesinden anahtarınızı kontrol edin."
            msg.contains("RESOURCE_EXHAUSTED") || msg.contains("quota") ->
                "API kotası aşıldı. Lütfen biraz bekleyin veya Ayarlar'dan Flash Lite modelini seçin."
            else -> "Bağlantı hatası: ${e.localizedMessage?.take(100) ?: "Sunucuya ulaşılamadı."}"
        }
    }

    fun refreshContext() {
        if (!isContextDirty) return // no-op if context is already fresh
        viewModelScope.launch {
            examContext = contextBuilder.buildContext()
            isContextDirty = false
            val model = modelManager.getModelForChat()
            if (model != null) {
                val customPrompt = preferences.getCustomSystemPrompt()
                val systemPrompt = PromptTemplates.buildCoachSystemPrompt(examContext, customPrompt)
                val history = buildSlidingWindowHistory(systemPrompt, _uiState.value.messages)
                cachedChat = model.startChat(history = history)
            }
        }
    }
}
