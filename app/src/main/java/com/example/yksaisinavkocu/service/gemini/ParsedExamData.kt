package com.example.yksaisinavkocu.service.gemini

import com.example.yksaisinavkocu.data.local.entity.SubjectResult
import com.example.yksaisinavkocu.data.local.entity.TopicDetail
import kotlinx.serialization.Serializable

@Serializable
data class ParsedExamData(
    val examName: String = "Deneme Sınavı",
    val examDate: String = "",
    val examType: String = "TYT",
    val subjects: List<SubjectResult> = emptyList(),
    val topics: List<TopicDetail> = emptyList(),
    val rawNotes: String? = null
)
