package com.example.yksaisinavkocu.ui.screen.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yksaisinavkocu.YksApp
import com.example.yksaisinavkocu.data.local.entity.ExamEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ExamsListUiState(
    val exams: List<ExamEntity> = emptyList(),
    val searchQuery: String = "",
    val filterType: String? = null,
    val isLoading: Boolean = true
)

class ExamsListViewModel : ViewModel() {
    private val repo = YksApp.instance.examRepository
    private val _uiState = MutableStateFlow(ExamsListUiState())
    val uiState: StateFlow<ExamsListUiState> = _uiState.asStateFlow()

    init { loadExams() }

    private fun loadExams() {
        viewModelScope.launch {
            repo.getAllExams().collect { exams ->
                _uiState.update { state ->
                    val filtered = exams.filter { exam ->
                        val matchesType = state.filterType?.let { exam.examType == it } ?: true
                        val matchesQuery = state.searchQuery.isBlank() ||
                                exam.examName.contains(state.searchQuery, ignoreCase = true)
                        matchesType && matchesQuery
                    }
                    state.copy(exams = filtered, isLoading = false)
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadExams()
    }

    fun setFilterType(type: String?) {
        _uiState.update { it.copy(filterType = type) }
        loadExams()
    }

    fun deleteExam(exam: ExamEntity) {
        viewModelScope.launch { repo.deleteExam(exam) }
    }
}
