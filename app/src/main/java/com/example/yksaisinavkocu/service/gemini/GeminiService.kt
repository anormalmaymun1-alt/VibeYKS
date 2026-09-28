package com.example.yksaisinavkocu.service.gemini

import android.graphics.Bitmap
import android.util.Log
import com.example.yksaisinavkocu.data.local.entity.SubjectResult
import com.example.yksaisinavkocu.data.local.entity.TopicDetail
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.TextPart
import com.google.ai.client.generativeai.type.content
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

class GeminiService(
    private val modelManager: GeminiModelManager
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * Tek bir karne görselinden sınav verilerini çıkarır.
     */
    suspend fun parseExamCard(bitmap: Bitmap): Result<ParsedExamData> =
        parseExamCardPages(listOf(bitmap))

    /**
     * Çoklu sayfa/fotoğraf halindeki karne görsellerinden sınav verilerini çıkarır.
     */
    suspend fun parseExamCardPages(bitmaps: List<Bitmap>): Result<ParsedExamData> {
        if (!modelManager.isApiKeyConfigured()) {
            return Result.failure(
                IllegalStateException("Gemini API anahtarı tanımlanmamış. Lütfen Ayarlar sekmesinden API anahtarınızı kaydedin.")
            )
        }

        val primaryModel = modelManager.getModelForOcr()
            ?: return Result.failure(IllegalStateException("Aktif model oluşturulamadı."))

        val content = content {
            bitmaps.forEach { image(it) }
            text(PromptTemplates.EXAM_CARD_OCR_PROMPT)
        }

        return try {
            val response = primaryModel.generateContent(content)
            val text = safeExtractText(response)
            val ocrTokens = response.usageMetadata?.totalTokenCount ?: (text.length / 4).coerceAtLeast(600)
            com.example.yksaisinavkocu.YksApp.instance.tokenUsageManager.recordUsage(ocrTokens)
            val parsed = parseStructuredResponse(text)
            Result.success(parsed)
        } catch (e: Exception) {
            Log.w("GeminiService", "Birincil OCR modeli başarısız oldu, yedek deneniyor: ${e.message}")
            try {
                val fallbackModel = modelManager.getFallbackModel(forOcr = true)
                if (fallbackModel != null) {
                    val fallbackResponse = fallbackModel.generateContent(content)
                    val text = safeExtractText(fallbackResponse)
                    val fallbackTokens = fallbackResponse.usageMetadata?.totalTokenCount ?: (text.length / 4).coerceAtLeast(600)
                    com.example.yksaisinavkocu.YksApp.instance.tokenUsageManager.recordUsage(fallbackTokens)
                    val parsed = parseStructuredResponse(text)
                    Result.success(parsed)
                } else {
                    Result.failure(e)
                }
            } catch (fallbackError: Exception) {
                Log.e("GeminiService", "Yedek OCR modeli de başarısız oldu: ${fallbackError.message}")
                val fullErr = "${e.message} ${fallbackError.message}"
                if (fullErr.contains("RESOURCE_EXHAUSTED") || fullErr.contains("quota") || fullErr.contains("429")) {
                    com.example.yksaisinavkocu.YksApp.instance.tokenUsageManager.markQuotaExhausted()
                }
                val errorToReturn = if (e.message?.contains("404") == true || e.message?.contains("no longer available") == true) e else fallbackError
                Result.failure(errorToReturn)
            }
        }
    }

    private fun safeExtractText(response: GenerateContentResponse): String {
        return try {
            response.text ?: ""
        } catch (_: Exception) {
            // response.text finishReason != STOP (örn. MAX_TOKENS) olduğunda InvalidStateException fırlatır.
            // Bu durumda üretilen parçaları (parts) birleştirerek metni kurtarıyoruz.
            response.candidates.firstOrNull()?.content?.parts
                ?.mapNotNull { (it as? TextPart)?.text }
                ?.joinToString("") ?: ""
        }
    }

    /**
     * Modelden dönen metin içerisindeki JSON bloğunu güvenle parse eder.
     */
    fun parseStructuredResponse(rawResponse: String): ParsedExamData {
        val jsonStr = extractJsonString(rawResponse)
        return try {
            val parsed = json.decodeFromString<ParsedExamData>(jsonStr)
            normalizeParsedData(parsed)
        } catch (_: Exception) {
            // Manuel fallback parse
            fallbackManualJsonParse(jsonStr)
        }
    }

    private fun extractJsonString(raw: String): String {
        val trimmed = raw.trim()
        val codeBlockRegex = Regex("```(?:json)?\\s*([\\s\\S]*?)(?:```|$)")
        val match = codeBlockRegex.find(trimmed)
        val extracted = if (match != null) {
            match.groupValues[1].trim()
        } else {
            val firstBrace = trimmed.indexOf('{')
            val lastBrace = trimmed.lastIndexOf('}')
            if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
                trimmed.substring(firstBrace, lastBrace + 1)
            } else if (firstBrace != -1) {
                trimmed.substring(firstBrace)
            } else {
                trimmed
            }
        }

        return repairIncompleteJson(extracted)
    }

    private fun repairIncompleteJson(jsonStr: String): String {
        var str = jsonStr.trim()
        if (!str.startsWith("{")) return str

        var openBraces = 0
        var openBrackets = 0
        var inString = false
        var escape = false

        for (ch in str) {
            if (escape) {
                escape = false
                continue
            }
            if (ch == '\\') {
                escape = true
                continue
            }
            if (ch == '"') {
                inString = !inString
                continue
            }
            if (!inString) {
                when (ch) {
                    '{' -> openBraces++
                    '}' -> openBraces--
                    '[' -> openBrackets++
                    ']' -> openBrackets--
                }
            }
        }

        if (inString) {
            str += "\""
        }
        while (openBrackets > 0) {
            str += "]"
            openBrackets--
        }
        while (openBraces > 0) {
            str += "}"
            openBraces--
        }
        return str
    }

    private fun normalizeParsedData(data: ParsedExamData): ParsedExamData {
        val normalizedType = if (data.examType.contains("AYT", ignoreCase = true)) "AYT" else "TYT"
        val normalizedSubjects = data.subjects.map { subj ->
            val correct = maxOf(0, subj.correct)
            val wrong = maxOf(0, subj.wrong)
            val empty = maxOf(0, subj.empty)
            val calculatedNet = correct - (wrong / 4.0f)
            val net = if (subj.net == 0f && (correct > 0 || wrong > 0)) calculatedNet else subj.net
            subj.copy(
                correct = correct,
                wrong = wrong,
                empty = empty,
                net = net
            )
        }
        return data.copy(
            examType = normalizedType,
            subjects = normalizedSubjects
        )
    }

    private fun fallbackManualJsonParse(jsonStr: String): ParsedExamData {
        return try {
            val element = json.parseToJsonElement(jsonStr).jsonObject
            val examName = element["examName"]?.jsonPrimitive?.contentOrNull ?: "Deneme Sınavı"
            val examDate = element["examDate"]?.jsonPrimitive?.contentOrNull ?: ""
            val rawType = element["examType"]?.jsonPrimitive?.contentOrNull ?: "TYT"
            val examType = if (rawType.contains("AYT", ignoreCase = true)) "AYT" else "TYT"

            val subjectsList = mutableListOf<SubjectResult>()
            element["subjects"]?.jsonArray?.forEach { subElem ->
                try {
                    val subObj = subElem.jsonObject
                    val name = subObj["subjectName"]?.jsonPrimitive?.contentOrNull ?: "Ders"
                    val c = subObj["correct"]?.jsonPrimitive?.intOrNull ?: 0
                    val w = subObj["wrong"]?.jsonPrimitive?.intOrNull ?: 0
                    val e = subObj["empty"]?.jsonPrimitive?.intOrNull ?: 0
                    val net = (subObj["net"]?.jsonPrimitive?.contentOrNull?.toFloatOrNull()) ?: (c - (w / 4.0f))
                    subjectsList.add(SubjectResult(name, c, w, e, net))
                } catch (_: Exception) {}
            }

            val topicsList = mutableListOf<TopicDetail>()
            element["topics"]?.jsonArray?.forEach { topElem ->
                try {
                    val topObj = topElem.jsonObject
                    val sName = topObj["subjectName"]?.jsonPrimitive?.contentOrNull ?: "Ders"
                    val tName = topObj["topicName"]?.jsonPrimitive?.contentOrNull ?: "Konu"
                    val total = topObj["totalQuestions"]?.jsonPrimitive?.intOrNull ?: 0
                    val c = topObj["correctCount"]?.jsonPrimitive?.intOrNull ?: 0
                    val w = topObj["wrongCount"]?.jsonPrimitive?.intOrNull ?: 0
                    val e = topObj["emptyCount"]?.jsonPrimitive?.intOrNull ?: 0
                    topicsList.add(TopicDetail(sName, tName, total, c, w, e))
                } catch (_: Exception) {}
            }

            ParsedExamData(
                examName = examName,
                examDate = examDate,
                examType = examType,
                subjects = subjectsList,
                topics = topicsList
            )
        } catch (_: Exception) {
            ParsedExamData()
        }
    }
}
