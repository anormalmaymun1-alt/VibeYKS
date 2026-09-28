package com.example.yksaisinavkocu.ui.screen.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yksaisinavkocu.YksApp
import com.example.yksaisinavkocu.data.local.entity.ExamEntity
import com.example.yksaisinavkocu.data.local.entity.SubjectResult
import com.example.yksaisinavkocu.data.local.entity.TopicDetail
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WeaknessItem(
    val rank: Int,
    val topicName: String,
    val subjectName: String,
    val wrongCount: Int,
    val errorRate: Float
)

data class AnalyticsUiState(
    val selectedExamType: String = "TYT",
    val exams: List<ExamEntity> = emptyList(),
    val selectedSubjectTab: String = "Genel",
    val highestNet: Float = 0f,
    val averageNet: Float = 0f,
    val lastNet: Float = 0f,
    val netChange: Float = 0f,
    val weaknesses: List<WeaknessItem> = emptyList(),
    val isLoading: Boolean = true
)

class AnalyticsViewModel : ViewModel() {
    private val repo = YksApp.instance.examRepository

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private var cachedAllTopics: List<TopicDetail> = emptyList()

    init {
        loadExams("TYT")
    }

    fun selectExamType(type: String) {
        _uiState.update { it.copy(selectedExamType = type, selectedSubjectTab = "Genel", isLoading = true) }
        loadExams(type)
    }

    fun selectSubjectTab(tab: String) {
        val weaknesses = computeWeaknesses(cachedAllTopics, tab)
        _uiState.update { it.copy(selectedSubjectTab = tab, weaknesses = weaknesses) }
    }

    private fun loadExams(type: String) {
        viewModelScope.launch {
            repo.getExamsByType(type).collect { exams ->
                val sortedByDate = exams.sortedBy { it.examDate }

                val highest = sortedByDate.maxOfOrNull { it.totalNet } ?: 0f
                val average = if (sortedByDate.isNotEmpty())
                    sortedByDate.map { it.totalNet }.average().toFloat() else 0f
                val last = sortedByDate.lastOrNull()?.totalNet ?: 0f
                val secondLast = sortedByDate.dropLast(1).lastOrNull()?.totalNet ?: last
                val change = last - secondLast

                // Konu zafiyet analizi
                val allTopics = mutableListOf<TopicDetail>()
                exams.forEach { exam ->
                    allTopics.addAll(repo.parseTopicDetails(exam.topicDetails))
                }
                cachedAllTopics = allTopics

                val currentTab = _uiState.value.selectedSubjectTab
                val weaknesses = computeWeaknesses(allTopics, currentTab)

                _uiState.update {
                    it.copy(
                        exams = sortedByDate,
                        highestNet = highest,
                        averageNet = average,
                        lastNet = last,
                        netChange = change,
                        weaknesses = weaknesses,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun matchesSubject(tab: String, topicSubject: String): Boolean {
        if (tab == "Genel") return true
        val normTab = tab.trim().lowercase(java.util.Locale("tr"))
        val normTopic = topicSubject.trim().lowercase(java.util.Locale("tr"))

        if (normTopic == normTab || normTopic.contains(normTab) || normTab.contains(normTopic)) {
            return true
        }

        return when {
            normTab.contains("sosyal") ->
                normTopic.contains("tarih") || normTopic.contains("coğrafya") ||
                        normTopic.contains("felsefe") || normTopic.contains("din") || normTopic.contains("sosyal")
            normTab.contains("fen") ->
                normTopic.contains("fizik") || normTopic.contains("kimya") ||
                        normTopic.contains("biyoloji") || normTopic.contains("fen")
            normTab.contains("matematik") ->
                normTopic.contains("matematik") || normTopic.contains("geometri")
            normTab.contains("türkçe") ->
                normTopic.contains("türkçe") || normTopic.contains("edebiyat") || normTopic.contains("dil")
            else -> false
        }
    }

    private fun computeWeaknesses(allTopics: List<TopicDetail>, tab: String): List<WeaknessItem> {
        val filteredTopics = if (tab == "Genel") {
            allTopics
        } else {
            allTopics.filter { matchesSubject(tab, it.subjectName) }
        }

        return filteredTopics
            .groupBy { "${it.subjectName}|${it.topicName}" }
            .map { (key, details) ->
                val parts = key.split("|")
                val totalWrong = details.sumOf { it.wrongCount }
                val totalQ = details.sumOf { it.totalQuestions }
                val rate = if (totalQ > 0) (totalWrong.toFloat() / totalQ * 100) else 0f
                WeaknessItem(
                    rank = 0,
                    topicName = parts.getOrElse(1) { "Bilinmeyen Konu" },
                    subjectName = parts.getOrElse(0) { "Ders" },
                    wrongCount = totalWrong,
                    errorRate = rate
                )
            }
            .filter { it.wrongCount > 0 }
            .sortedWith(compareByDescending<WeaknessItem> { it.errorRate }.thenByDescending { it.wrongCount })
            .take(10)
            .mapIndexed { index, item -> item.copy(rank = index + 1) }
    }

    fun getSubjectNets(subjectName: String): List<Pair<String, Float>> {
        val exams = _uiState.value.exams
        return exams.mapNotNull { exam ->
            val subjects = repo.parseSubjectResults(exam.subjectResults)
            val subject = subjects.find { it.subjectName == subjectName }
            if (subject != null) {
                com.example.yksaisinavkocu.ui.util.DateFormatter.formatDayMonth(exam.examDate) to subject.net
            } else null
        }
    }

    fun getSubjectTabs(): List<String> {
        return if (_uiState.value.selectedExamType == "TYT") {
            listOf("Genel", "Türkçe", "Sosyal Bilimler", "Temel Matematik", "Fen Bilimleri")
        } else {
            listOf("Genel", "Matematik", "Fizik", "Kimya", "Biyoloji")
        }
    }

    fun getMaxNetFor(examType: String, subjectName: String? = null): Float {
        if (subjectName == null || subjectName == "Genel") {
            return when (examType.trim().uppercase()) {
                "TYT" -> 120f
                "AYT" -> 80f
                "YDT" -> 80f
                else -> 120f
            }
        }

        val norm = subjectName.trim().lowercase(java.util.Locale("tr"))
        return when {
            // TYT Dersleri
            norm == "türkçe" || norm == "turkce" -> 40f
            norm == "temel matematik" -> 40f
            norm.contains("sosyal") -> 20f
            norm.contains("fen") && !norm.contains("fizik") && !norm.contains("kimya") && !norm.contains("biyoloji") -> 20f

            // AYT Sayısal Dersleri
            norm == "matematik" -> 40f
            norm.contains("fizik") -> 14f
            norm.contains("kimya") -> 13f
            norm.contains("biyoloji") -> 13f

            // AYT Eşit Ağırlık / Sözel Dersleri
            norm.contains("edebiyat") -> 24f
            norm.contains("tarih") -> 10f
            norm.contains("coğrafya") || norm.contains("cografya") -> 6f
            norm.contains("felsefe") -> 12f
            norm.contains("din") -> 6f

            // YDT
            norm.contains("ingilizce") || norm.contains("almanca") || norm.contains("fransızca") || norm.contains("dil") -> 80f

            else -> {
                // Sınav verilerinden bu ders için kayıtlı toplam soru sayısını kontrol et
                val exam = _uiState.value.exams.firstOrNull()
                val parsedSubj = exam?.let { repo.parseSubjectResults(it.subjectResults).find { s -> s.subjectName.equals(subjectName, ignoreCase = true) } }
                if (parsedSubj != null) {
                    val totalQ = parsedSubj.correct + parsedSubj.wrong + parsedSubj.empty
                    if (totalQ > 0) totalQ.toFloat() else 40f
                } else {
                    40f
                }
            }
        }
    }
}

