package com.example.yksaisinavkocu.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val actionsJson: String = "[]"
)
