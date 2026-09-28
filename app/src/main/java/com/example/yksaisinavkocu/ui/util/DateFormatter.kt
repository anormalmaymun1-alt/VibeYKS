package com.example.yksaisinavkocu.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {
    private val shortFormat = SimpleDateFormat("dd MMM yyyy", Locale("tr"))
    private val longFormat = SimpleDateFormat("dd MMMM yyyy, EEEE", Locale("tr"))
    private val dayMonth = SimpleDateFormat("dd MMM", Locale("tr"))

    fun formatShort(millis: Long): String = shortFormat.format(Date(millis))
    fun formatLong(millis: Long): String = longFormat.format(Date(millis))
    fun formatDayMonth(millis: Long): String = dayMonth.format(Date(millis))

    fun parseDateToMillis(dateStr: String): Long? {
        val formats = listOf("dd.MM.yyyy", "dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd")
        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale("tr"))
                sdf.isLenient = false
                val parsed = sdf.parse(dateStr.trim())
                if (parsed != null) return parsed.time
            } catch (_: Exception) {}
        }
        return null
    }
}

