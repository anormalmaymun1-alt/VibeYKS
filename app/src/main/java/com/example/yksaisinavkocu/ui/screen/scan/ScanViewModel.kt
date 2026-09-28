package com.example.yksaisinavkocu.ui.screen.scan

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yksaisinavkocu.YksApp
import com.example.yksaisinavkocu.data.local.entity.ExamEntity
import com.example.yksaisinavkocu.data.preferences.AppPreferences
import com.example.yksaisinavkocu.service.gemini.ParsedExamData
import com.example.yksaisinavkocu.service.scanner.ImageProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ScanStatus {
    data object Idle : ScanStatus
    data class Processing(val message: String) : ScanStatus
    data class Review(val examData: ParsedExamData, val previewBitmap: Bitmap? = null) : ScanStatus
    data class Success(val examName: String, val totalNet: Float) : ScanStatus
    data class Error(val message: String) : ScanStatus
}

data class ScanUiState(
    val status: ScanStatus = ScanStatus.Idle,
    val showManualEntry: Boolean = false,
    val isApiKeyConfigured: Boolean = false
)

class ScanViewModel : ViewModel() {
    private val geminiService = YksApp.instance.geminiService
    private val pdfRenderer = YksApp.instance.pdfBitmapRenderer
    private val repository = YksApp.instance.examRepository
    private val preferences = YksApp.instance.preferences

    private val _uiState = MutableStateFlow(
        ScanUiState(isApiKeyConfigured = preferences.getApiKey() != null)
    )
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun checkApiKey() {
        _uiState.update { it.copy(isApiKeyConfigured = preferences.getApiKey() != null) }
    }

    fun openManualEntry() {
        _uiState.update { it.copy(showManualEntry = true) }
    }

    fun closeManualEntry() {
        _uiState.update { it.copy(showManualEntry = false) }
    }

    fun dismissReview() {
        _uiState.update { it.copy(status = ScanStatus.Idle) }
    }

    fun resetStatus() {
        _uiState.update { it.copy(status = ScanStatus.Idle) }
    }

    fun processCapturedBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(status = ScanStatus.Processing("Gemini AI karne görselini analiz ediyor..."))
            }

            val result = geminiService.parseExamCard(bitmap)
            result.fold(
                onSuccess = { parsed ->
                    _uiState.update {
                        it.copy(status = ScanStatus.Review(parsed, previewBitmap = bitmap))
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            status = ScanStatus.Error(
                                formatUserFriendlyError(error)
                            )
                        )
                    }
                }
            )
        }
    }

    fun processImageUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(status = ScanStatus.Processing("Görsel yükleniyor ve optimize ediliyor..."))
            }

            val bitmap = ImageProcessor.loadAndOptimizeBitmap(context, uri)
            if (bitmap == null) {
                _uiState.update {
                    it.copy(status = ScanStatus.Error("Görsel okunamadı veya biçim desteklenmiyor."))
                }
                return@launch
            }

            _uiState.update {
                it.copy(status = ScanStatus.Processing("Gemini Multimodal OCR ile sınav verileri çıkarılıyor..."))
            }

            val result = geminiService.parseExamCard(bitmap)
            result.fold(
                onSuccess = { parsed ->
                    _uiState.update {
                        it.copy(status = ScanStatus.Review(parsed, previewBitmap = bitmap))
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            status = ScanStatus.Error(
                                formatUserFriendlyError(error)
                            )
                        )
                    }
                }
            )
        }
    }

    fun processPdfUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(status = ScanStatus.Processing("PDF karnesi sayfalara dönüştürülüyor (300 DPI)..."))
            }

            val pages = pdfRenderer.renderPdfPages(uri)
            if (pages.isEmpty()) {
                _uiState.update {
                    it.copy(status = ScanStatus.Error("PDF dosyası açılamadı veya içi boş."))
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    status = ScanStatus.Processing(
                        "Gemini AI ${pages.size} sayfalık belgeyi inceliyor..."
                    )
                )
            }

            val result = geminiService.parseExamCardPages(pages)
            result.fold(
                onSuccess = { parsed ->
                    _uiState.update {
                        it.copy(
                            status = ScanStatus.Review(
                                parsed,
                                previewBitmap = pages.firstOrNull()
                            )
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            status = ScanStatus.Error(
                                formatUserFriendlyError(error)
                            )
                        )
                    }
                }
            )
        }
    }

    private fun formatUserFriendlyError(error: Throwable): String {
        val msg = error.message ?: ""

        // Eğer JSON içerisindeki message alanı varsa önce onu çıkarmayı dene
        val jsonMessageRegex = Regex("\"message\":\\s*\"([^\"]+)\"")
        val extractedMessage = jsonMessageRegex.find(msg)?.groupValues?.get(1)

        val checkMsg = extractedMessage ?: msg

        return when {
            checkMsg.contains("404") || checkMsg.contains("no longer available") || checkMsg.contains("not found", ignoreCase = true) -> {
                preferences.setSelectedModel(AppPreferences.DEFAULT_MODEL)
                "Seçilen yapay zeka modeli artık sunucuda desteklenmiyor. Model otomatik olarak Gemini 3.8 Flash'a güncellendi. Lütfen 'Tekrar Dene' butonuna dokunun."
            }
            checkMsg.contains("503") || checkMsg.contains("high demand") || checkMsg.contains("UNAVAILABLE") ->
                "Google AI sunucularında anlık yoğunluk yaşanıyor (503). Lütfen birkaç saniye sonra tekrar deneyin veya Ayarlar'dan başka bir model seçin."
            checkMsg.contains("API_KEY_INVALID") || checkMsg.contains("API key not valid") ->
                "Gemini API anahtarı geçersiz. Lütfen Ayarlar sekmesinden API anahtarınızı kontrol edin."
            checkMsg.contains("RESOURCE_EXHAUSTED") || checkMsg.contains("quota") ->
                "API kullanım kotası aşıldı. Lütfen biraz bekleyin veya Ayarlar sekmesinden Flash Lite modelini seçin."
            checkMsg.contains("MAX_TOKENS") ->
                "Karnedeki veri yoğunluğu nedeniyle yanıt kesildi. Lütfen 'Tekrar Dene' butonuna dokunun veya sadeleştirilmiş bir görsel yükleyin."
            extractedMessage != null -> extractedMessage
            else -> "Karne analiz edilirken bir hata oluştu: ${error.localizedMessage?.take(120) ?: "Sunucu yanıt vermedi."}"
        }
    }

    fun saveExam(exam: ExamEntity) {
        viewModelScope.launch {
            try {
                repository.insertExam(exam)
                _uiState.update {
                    it.copy(
                        status = ScanStatus.Success(exam.examName, exam.totalNet),
                        showManualEntry = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(status = ScanStatus.Error("Kaydedilirken hata oluştu: ${e.message}"))
                }
            }
        }
    }
}
