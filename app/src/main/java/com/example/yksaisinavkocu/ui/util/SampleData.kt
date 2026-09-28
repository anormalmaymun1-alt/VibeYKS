package com.example.yksaisinavkocu.ui.util

import com.example.yksaisinavkocu.data.local.entity.ExamEntity
import com.example.yksaisinavkocu.data.local.entity.SubjectResult
import com.example.yksaisinavkocu.data.local.entity.TopicDetail
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar

object SampleData {

    private val json = Json { ignoreUnknownKeys = true }

    fun generateSampleTytExams(): List<ExamEntity> {
        val calendar = Calendar.getInstance()
        val exams = mutableListOf<ExamEntity>()

        val tytData = listOf(
            Triple("Pilot TYT Deneme 1", listOf(28, 5, 7, 14, 3, 3, 22, 6, 12, 12, 4, 4), 60.5f),
            Triple("TYT Deneme 2", listOf(30, 4, 6, 15, 2, 3, 24, 5, 11, 13, 3, 4), 65.75f),
            Triple("TYT Deneme 3", listOf(31, 5, 4, 16, 2, 2, 26, 4, 10, 14, 2, 4), 70.0f),
            Triple("TYT Deneme 4", listOf(29, 6, 5, 15, 3, 2, 25, 5, 10, 15, 3, 2), 68.25f),
            Triple("TYT Deneme 5", listOf(33, 3, 4, 17, 1, 2, 27, 3, 10, 16, 2, 2), 76.0f),
            Triple("TYT Deneme 6", listOf(32, 4, 4, 16, 2, 2, 28, 4, 8, 15, 3, 2), 73.75f),
            Triple("TYT Deneme 7", listOf(34, 2, 4, 17, 1, 2, 29, 3, 8, 17, 1, 2), 79.5f),
            Triple("TYT Deneme 8", listOf(35, 2, 3, 18, 1, 1, 30, 2, 8, 18, 1, 1), 83.75f),
        )

        tytData.forEachIndexed { index, (name, data, totalNet) ->
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -(tytData.size - index) * 7)

            val subjects = listOf(
                SubjectResult("Türkçe", data[0], data[1], data[2], data[0] - data[1] / 4f),
                SubjectResult("Sosyal Bilimler", data[3], data[4], data[5], data[3] - data[4] / 4f),
                SubjectResult("Temel Matematik", data[6], data[7], data[8], data[6] - data[7] / 4f),
                SubjectResult("Fen Bilimleri", data[9], data[10], data[11], data[9] - data[10] / 4f),
            )

            val topics = listOf(
                TopicDetail("Türkçe", "Paragraf", 8, data[0] / 5, 2, 1),
                TopicDetail("Türkçe", "Dil Bilgisi", 5, data[0] / 8, 1, 1),
                TopicDetail("Temel Matematik", "Sayı Problemleri", 4, data[6] / 8, 2, 0),
                TopicDetail("Temel Matematik", "Geometri", 5, data[6] / 7, 1, 1),
                TopicDetail("Fen Bilimleri", "Fizik - Kuvvet", 3, data[9] / 6, 1, 0),
                TopicDetail("Fen Bilimleri", "Kimya - Madde", 3, data[9] / 7, 1, 0),
                TopicDetail("Sosyal Bilimler", "Tarih", 5, data[3] / 4, 1, 0),
            )

            exams.add(
                ExamEntity(
                    examName = name,
                    examDate = calendar.timeInMillis,
                    examType = "TYT",
                    totalNet = totalNet,
                    subjectResults = json.encodeToString(subjects),
                    topicDetails = json.encodeToString(topics),
                    aiAnalysisNote = null
                )
            )
        }
        return exams
    }

    fun generateSampleAytExams(): List<ExamEntity> {
        val calendar = Calendar.getInstance()
        val exams = mutableListOf<ExamEntity>()

        val aytData = listOf(
            Triple("AYT Deneme 1", listOf(18, 8, 14, 8, 4, 2, 7, 3, 3, 6, 4, 3), 35.0f),
            Triple("AYT Deneme 2", listOf(20, 7, 13, 9, 3, 2, 8, 3, 2, 7, 3, 3), 39.5f),
            Triple("AYT Deneme 3", listOf(22, 6, 12, 10, 2, 2, 9, 2, 2, 8, 2, 3), 44.0f),
            Triple("AYT Deneme 4", listOf(21, 7, 12, 9, 3, 2, 8, 3, 2, 7, 3, 3), 40.25f),
            Triple("AYT Deneme 5", listOf(24, 5, 11, 11, 2, 1, 10, 2, 1, 9, 2, 2), 49.5f),
            Triple("AYT Deneme 6", listOf(25, 4, 11, 10, 3, 1, 10, 1, 2, 9, 2, 2), 49.75f),
            Triple("AYT Deneme 7", listOf(27, 3, 10, 12, 1, 1, 11, 1, 1, 10, 1, 2), 55.75f),
            Triple("AYT Deneme 8", listOf(28, 3, 9, 12, 2, 0, 11, 1, 1, 11, 1, 1), 57.25f),
        )

        aytData.forEachIndexed { index, (name, data, totalNet) ->
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -(aytData.size - index) * 7)

            val subjects = listOf(
                SubjectResult("Matematik", data[0], data[1], data[2], data[0] - data[1] / 4f),
                SubjectResult("Fizik", data[3], data[4], data[5], data[3] - data[4] / 4f),
                SubjectResult("Kimya", data[6], data[7], data[8], data[6] - data[7] / 4f),
                SubjectResult("Biyoloji", data[9], data[10], data[11], data[9] - data[10] / 4f),
            )

            val topics = listOf(
                TopicDetail("Matematik", "Türev", 4, data[0] / 7, 2, 1),
                TopicDetail("Matematik", "İntegral", 3, data[0] / 9, 2, 0),
                TopicDetail("Fizik", "Optik", 3, data[3] / 5, 1, 1),
                TopicDetail("Fizik", "Elektrik", 3, data[3] / 4, 2, 0),
                TopicDetail("Kimya", "Asit-Baz", 3, data[6] / 4, 1, 1),
                TopicDetail("Kimya", "Organik Kimya", 3, data[6] / 5, 2, 0),
                TopicDetail("Biyoloji", "Genetik", 3, data[9] / 3, 1, 1),
            )

            exams.add(
                ExamEntity(
                    examName = name,
                    examDate = calendar.timeInMillis,
                    examType = "AYT",
                    totalNet = totalNet,
                    subjectResults = json.encodeToString(subjects),
                    topicDetails = json.encodeToString(topics),
                    aiAnalysisNote = null
                )
            )
        }
        return exams
    }
}
