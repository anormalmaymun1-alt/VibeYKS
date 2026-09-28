package com.example.yksaisinavkocu.service.gemini

import com.example.yksaisinavkocu.data.local.entity.SubjectResult
import com.example.yksaisinavkocu.data.local.entity.TopicDetail
import com.example.yksaisinavkocu.data.repository.ExamRepository
import com.example.yksaisinavkocu.ui.util.DateFormatter
import kotlinx.serialization.json.Json

class CoachContextBuilder(private val examRepository: ExamRepository) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun buildContext(): String {
        val latestCreated = examRepository.getLatestCreatedExams(5)
        val recentTyt = examRepository.getRecentExams("TYT", 10)
        val recentAyt = examRepository.getRecentExams("AYT", 10)

        if (latestCreated.isEmpty()) {
            return "Henüz sisteme kaydedilmiş bir deneme sınavı bulunmuyor. Öğrenciye bir karne taratmasını veya manuel sınav sonucu eklemesini öner."
        }

        val allTopics = mutableListOf<TopicDetail>()
        (recentTyt + recentAyt).forEach { exam ->
            allTopics.addAll(examRepository.parseTopicDetails(exam.topicDetails))
        }

        val topWeaknesses = allTopics
            .groupBy { "${it.subjectName} - ${it.topicName}" }
            .map { (key, details) ->
                val totalWrong = details.sumOf { it.wrongCount }
                val totalQuestions = details.sumOf { it.totalQuestions }
                val errorRate = if (totalQuestions > 0)
                    (totalWrong.toFloat() / totalQuestions * 100) else 0f
                Triple(key, totalWrong, errorRate)
            }
            .sortedByDescending { it.third }
            .take(15)

        return buildString {
            appendLine("### En Son Sisteme Eklenen/Taranan Sınavlar (Kronolojik Sıra):")
            latestCreated.forEach { exam ->
                val subjects = examRepository.parseSubjectResults(exam.subjectResults)
                val dateStr = DateFormatter.formatShort(exam.examDate)
                appendLine("- ${exam.examName} (${exam.examType}) [Tarih: $dateStr, Toplam: %.2f net]:".format(exam.totalNet))
                subjects.forEach { sr ->
                    appendLine("  • ${sr.subjectName}: ${sr.correct}D ${sr.wrong}Y ${sr.empty}B → %.2f net".format(sr.net))
                }
            }
            appendLine()

            appendLine("### Son TYT Denemeleri (${recentTyt.size} adet):")
            recentTyt.forEach { exam ->
                val subjects = examRepository.parseSubjectResults(exam.subjectResults)
                val dateStr = DateFormatter.formatShort(exam.examDate)
                appendLine("- ${exam.examName} [Tarih: $dateStr]: Toplam %.2f net".format(exam.totalNet))
                subjects.forEach { sr ->
                    appendLine("  • ${sr.subjectName}: ${sr.correct}D ${sr.wrong}Y → %.2f net".format(sr.net))
                }
            }
            appendLine()

            appendLine("### Son AYT Denemeleri (${recentAyt.size} adet):")
            recentAyt.forEach { exam ->
                val subjects = examRepository.parseSubjectResults(exam.subjectResults)
                val dateStr = DateFormatter.formatShort(exam.examDate)
                appendLine("- ${exam.examName} [Tarih: $dateStr]: Toplam %.2f net".format(exam.totalNet))
                subjects.forEach { sr ->
                    appendLine("  • ${sr.subjectName}: ${sr.correct}D ${sr.wrong}Y → %.2f net".format(sr.net))
                }
            }
            appendLine()

            if (topWeaknesses.isNotEmpty()) {
                appendLine("### En Çok Hata Yapılan Konular:")
                topWeaknesses.forEachIndexed { index, (topic, wrongCount, errorRate) ->
                    appendLine("${index + 1}. $topic → $wrongCount yanlış (Hata oranı: %.1f%%)".format(errorRate))
                }
            }
        }
    }
}
