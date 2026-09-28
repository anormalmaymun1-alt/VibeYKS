package com.example.yksaisinavkocu.service.gemini

import androidx.compose.ui.graphics.Color
import com.example.yksaisinavkocu.data.preferences.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

data class TokenUsageState(
    val usedTokens: Long = 0L,
    val totalLimit: Long = TokenUsageManager.DEFAULT_DAILY_TOKEN_LIMIT,
    val remainingFraction: Float = 1.0f,
    val resetTimestamp: Long = 0L
)

class TokenUsageManager(private val preferences: AppPreferences) {

    private val _usageState = MutableStateFlow(loadInitialState())
    val usageState: StateFlow<TokenUsageState> = _usageState.asStateFlow()

    companion object {
        const val DEFAULT_DAILY_TOKEN_LIMIT = 1_000_000L // 1 Million tokens daily capacity (Free tier Flash)

        val COLOR_GREEN = Color(0xFF22C55E)
        val COLOR_YELLOW = Color(0xFFEAB308)
        val COLOR_RED = Color(0xFFEF4444)

        fun getBarColor(remainingFraction: Float): Color {
            return when {
                remainingFraction >= 0.50f -> COLOR_GREEN
                remainingFraction >= 0.20f -> COLOR_YELLOW
                else -> COLOR_RED
            }
        }

        fun calculateNextResetTimestamp(nowMillis: Long = System.currentTimeMillis()): Long {
            // Google AI Studio resets free tier quotas daily at midnight Pacific Time (00:00 America/Los_Angeles)
            val zone = ZoneId.of("America/Los_Angeles")
            val nowZoned = Instant.ofEpochMilli(nowMillis).atZone(zone)
            val nextMidnight = nowZoned.toLocalDate().plusDays(1).atStartOfDay(zone)
            return nextMidnight.toInstant().toEpochMilli()
        }

        fun formatCountdown(remainingMillis: Long): String {
            val totalSeconds = (remainingMillis / 1000).coerceAtLeast(0)
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        }
    }

    private fun loadInitialState(): TokenUsageState {
        val now = System.currentTimeMillis()
        var resetTime = preferences.getTokenResetTimestamp()
        var used = preferences.getTokenUsageCount()

        if (resetTime == 0L || now >= resetTime) {
            resetTime = calculateNextResetTimestamp(now)
            used = 0L
            preferences.setTokenResetTimestamp(resetTime)
            preferences.setTokenUsageCount(used)
        }

        val remainingFraction = calculateRemainingFraction(used, DEFAULT_DAILY_TOKEN_LIMIT)
        return TokenUsageState(
            usedTokens = used,
            totalLimit = DEFAULT_DAILY_TOKEN_LIMIT,
            remainingFraction = remainingFraction,
            resetTimestamp = resetTime
        )
    }

    @Synchronized
    fun recordUsage(tokens: Int) {
        if (tokens <= 0) return
        val now = System.currentTimeMillis()
        var resetTime = preferences.getTokenResetTimestamp()
        var used = preferences.getTokenUsageCount()

        if (resetTime == 0L || now >= resetTime) {
            resetTime = calculateNextResetTimestamp(now)
            used = 0L
        }

        used += tokens
        preferences.setTokenUsageCount(used)
        preferences.setTokenResetTimestamp(resetTime)

        val remainingFraction = calculateRemainingFraction(used, DEFAULT_DAILY_TOKEN_LIMIT)
        _usageState.value = TokenUsageState(
            usedTokens = used,
            totalLimit = DEFAULT_DAILY_TOKEN_LIMIT,
            remainingFraction = remainingFraction,
            resetTimestamp = resetTime
        )
    }

    @Synchronized
    fun checkAndResetIfExpired(): TokenUsageState {
        val now = System.currentTimeMillis()
        val currentResetTime = preferences.getTokenResetTimestamp()
        if (currentResetTime != 0L && now >= currentResetTime) {
            val nextReset = calculateNextResetTimestamp(now)
            preferences.setTokenUsageCount(0L)
            preferences.setTokenResetTimestamp(nextReset)
            val newState = TokenUsageState(
                usedTokens = 0L,
                totalLimit = DEFAULT_DAILY_TOKEN_LIMIT,
                remainingFraction = 1.0f,
                resetTimestamp = nextReset
            )
            _usageState.value = newState
            return newState
        }
        return _usageState.value
    }

    private fun calculateRemainingFraction(used: Long, totalLimit: Long): Float {
        val remaining = (totalLimit - used).coerceAtLeast(0L)
        return (remaining.toFloat() / totalLimit.toFloat()).coerceIn(0.0f, 1.0f)
    }

    @Synchronized
    fun markQuotaExhausted() {
        val now = System.currentTimeMillis()
        var resetTime = preferences.getTokenResetTimestamp()
        if (resetTime == 0L || now >= resetTime) {
            resetTime = calculateNextResetTimestamp(now)
            preferences.setTokenResetTimestamp(resetTime)
        }
        preferences.setTokenUsageCount(DEFAULT_DAILY_TOKEN_LIMIT)
        _usageState.value = TokenUsageState(
            usedTokens = DEFAULT_DAILY_TOKEN_LIMIT,
            totalLimit = DEFAULT_DAILY_TOKEN_LIMIT,
            remainingFraction = 0.0f,
            resetTimestamp = resetTime
        )
    }

    // For testing / debug
    fun setSimulatedRemainingFraction(fraction: Float) {
        val remaining = (DEFAULT_DAILY_TOKEN_LIMIT * fraction.coerceIn(0f, 1f)).toLong()
        val used = DEFAULT_DAILY_TOKEN_LIMIT - remaining
        _usageState.value = _usageState.value.copy(
            usedTokens = used,
            remainingFraction = fraction.coerceIn(0f, 1f)
        )
    }
}
