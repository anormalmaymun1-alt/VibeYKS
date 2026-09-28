package com.example.yksaisinavkocu.data.repository

import com.example.yksaisinavkocu.data.local.ExamDao
import com.example.yksaisinavkocu.data.local.entity.ExamEntity
import com.example.yksaisinavkocu.data.local.entity.SubjectResult
import com.example.yksaisinavkocu.data.local.entity.TopicDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

class ExamRepository(private val examDao: ExamDao) {
    private val json = Json { ignoreUnknownKeys = true }

    fun getAllExams(): Flow<List<ExamEntity>> = examDao.getAllExams()

    fun getExamsByType(type: String): Flow<List<ExamEntity>> = examDao.getExamsByType(type)

    suspend fun getRecentExams(type: String, limit: Int): List<ExamEntity> =
        examDao.getRecentExams(type, limit)

    suspend fun getLatestCreatedExams(limit: Int = 10): List<ExamEntity> =
        examDao.getLatestCreatedExams(limit)

    suspend fun getAllExamsList(): List<ExamEntity> =
        examDao.getAllExamsList()

    suspend fun getExamById(id: Long): ExamEntity? = examDao.getExamById(id)

    fun searchExams(query: String): Flow<List<ExamEntity>> = examDao.searchExams(query)

    suspend fun insertExam(exam: ExamEntity): Long = examDao.insertExam(exam)

    suspend fun updateExam(exam: ExamEntity) = examDao.updateExam(exam)

    suspend fun deleteExam(exam: ExamEntity) = examDao.deleteExam(exam)

    suspend fun deleteAllExams() = examDao.deleteAllExams()

    suspend fun getExamCount(): Int = examDao.getExamCount()

    fun parseSubjectResults(jsonStr: String): List<SubjectResult> =
        try { json.decodeFromString(jsonStr) } catch (_: Exception) { emptyList() }

    fun parseTopicDetails(jsonStr: String): List<TopicDetail> =
        try { json.decodeFromString(jsonStr) } catch (_: Exception) { emptyList() }

    fun encodeSubjectResults(results: List<SubjectResult>): String =
        json.encodeToString(kotlinx.serialization.builtins.ListSerializer(SubjectResult.serializer()), results)

    fun encodeTopicDetails(details: List<TopicDetail>): String =
        json.encodeToString(kotlinx.serialization.builtins.ListSerializer(TopicDetail.serializer()), details)
}
