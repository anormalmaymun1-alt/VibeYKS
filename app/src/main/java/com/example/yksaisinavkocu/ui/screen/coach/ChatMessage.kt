package com.example.yksaisinavkocu.ui.screen.coach

import androidx.compose.runtime.Immutable

@Immutable
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val actions: List<com.example.yksaisinavkocu.service.parser.AppAction> = emptyList(),
    val isLoading: Boolean = false
)
