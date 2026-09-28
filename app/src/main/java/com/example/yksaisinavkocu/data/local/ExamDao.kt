package com.example.yksaisinavkocu.data.local

import androidx.room.*
import com.example.yksaisinavkocu.data.local.entity.ExamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY examDate DESC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE examType = :type ORDER BY examDate DESC")
    fun getExamsByType(type: String): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE examType = :type ORDER BY examDate DESC LIMIT :limit")
    suspend fun getRecentExams(type: String, limit: Int): List<ExamEntity>

    @Query("SELECT * FROM exams ORDER BY createdAt DESC, id DESC LIMIT :limit")
    suspend fun getLatestCreatedExams(limit: Int): List<ExamEntity>

    @Query("SELECT * FROM exams ORDER BY examDate DESC")
    suspend fun getAllExamsList(): List<ExamEntity>

    @Query("SELECT * FROM exams WHERE id = :id")
    suspend fun getExamById(id: Long): ExamEntity?

    @Query("SELECT * FROM exams WHERE examName LIKE '%' || :query || '%' ORDER BY examDate DESC")
    fun searchExams(query: String): Flow<List<ExamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity): Long

    @Update
    suspend fun updateExam(exam: ExamEntity)

    @Delete
    suspend fun deleteExam(exam: ExamEntity)

    @Query("DELETE FROM exams")
    suspend fun deleteAllExams()

    @Query("SELECT COUNT(*) FROM exams")
    suspend fun getExamCount(): Int
}
