package com.example.yksaisinavkocu.data.local

import androidx.room.TypeConverter
import com.example.yksaisinavkocu.data.local.entity.SubjectResult
import com.example.yksaisinavkocu.data.local.entity.TopicDetail
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromSubjectResultList(value: List<SubjectResult>): String =
        json.encodeToString(value)

    @TypeConverter
    fun toSubjectResultList(value: String): List<SubjectResult> =
        try { json.decodeFromString(value) } catch (_: Exception) { emptyList() }

    @TypeConverter
    fun fromTopicDetailList(value: List<TopicDetail>): String =
        json.encodeToString(value)

    @TypeConverter
    fun toTopicDetailList(value: String): List<TopicDetail> =
        try { json.decodeFromString(value) } catch (_: Exception) { emptyList() }
}
