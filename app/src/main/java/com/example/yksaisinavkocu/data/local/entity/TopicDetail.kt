package com.example.yksaisinavkocu.data.local.entity

import kotlinx.serialization.Serializable

@Serializable
data class TopicDetail(
    val subjectName: String,
    val topicName: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val emptyCount: Int
)
