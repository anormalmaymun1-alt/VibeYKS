package com.example.yksaisinavkocu.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val examName: String,
    val examDate: Long,
    val examType: String, // "TYT" veya "AYT"
    val totalNet: Float,
    val subjectResults: String, // JSON: List<SubjectResult>
    val topicDetails: String,   // JSON: List<TopicDetail>
    val aiAnalysisNote: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
