package com.example.yksaisinavkocu

import android.app.Application
import com.example.yksaisinavkocu.data.local.AppDatabase
import com.example.yksaisinavkocu.data.preferences.AppPreferences
import com.example.yksaisinavkocu.data.repository.ChatRepository
import com.example.yksaisinavkocu.data.repository.ExamRepository
import com.example.yksaisinavkocu.service.gemini.CoachContextBuilder
import com.example.yksaisinavkocu.service.gemini.GeminiModelManager
import com.example.yksaisinavkocu.service.gemini.GeminiService
import com.example.yksaisinavkocu.service.scanner.PdfBitmapRenderer

class YksApp : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var examRepository: ExamRepository
        private set
    lateinit var chatRepository: ChatRepository
        private set
    lateinit var preferences: AppPreferences
        private set
    lateinit var geminiModelManager: GeminiModelManager
        private set
    lateinit var geminiService: GeminiService
        private set
    lateinit var pdfBitmapRenderer: PdfBitmapRenderer
        private set
    lateinit var coachContextBuilder: CoachContextBuilder
        private set
    lateinit var tokenUsageManager: com.example.yksaisinavkocu.service.gemini.TokenUsageManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        examRepository = ExamRepository(database.examDao())
        chatRepository = ChatRepository(database.chatDao())
        preferences = AppPreferences(this)
        tokenUsageManager = com.example.yksaisinavkocu.service.gemini.TokenUsageManager(preferences)
        geminiModelManager = GeminiModelManager(preferences)
        geminiService = GeminiService(geminiModelManager)
        pdfBitmapRenderer = PdfBitmapRenderer(this)
        coachContextBuilder = CoachContextBuilder(examRepository)
    }

    companion object {
        lateinit var instance: YksApp
            private set
    }
}
