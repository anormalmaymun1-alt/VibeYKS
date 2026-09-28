package com.example.yksaisinavkocu.data.local.entity

import kotlinx.serialization.Serializable

@Serializable
data class SubjectResult(
    val subjectName: String,
    val correct: Int,
    val wrong: Int,
    val empty: Int,
    val net: Float // doğru - (yanlış / 4)
)
