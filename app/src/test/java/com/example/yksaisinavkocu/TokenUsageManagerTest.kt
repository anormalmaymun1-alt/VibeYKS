package com.example.yksaisinavkocu

import com.example.yksaisinavkocu.service.gemini.TokenUsageManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenUsageManagerTest {

    @Test
    fun testColorThresholds() {
        // >= 50% remaining -> Green
        assertEquals(TokenUsageManager.COLOR_GREEN, TokenUsageManager.getBarColor(1.0f))
        assertEquals(TokenUsageManager.COLOR_GREEN, TokenUsageManager.getBarColor(0.85f))
        assertEquals(TokenUsageManager.COLOR_GREEN, TokenUsageManager.getBarColor(0.50f))

        // under 50% down to 20% -> Yellow
        assertEquals(TokenUsageManager.COLOR_YELLOW, TokenUsageManager.getBarColor(0.499f))
        assertEquals(TokenUsageManager.COLOR_YELLOW, TokenUsageManager.getBarColor(0.35f))
        assertEquals(TokenUsageManager.COLOR_YELLOW, TokenUsageManager.getBarColor(0.20f))

        // under 20% -> Red
        assertEquals(TokenUsageManager.COLOR_RED, TokenUsageManager.getBarColor(0.199f))
        assertEquals(TokenUsageManager.COLOR_RED, TokenUsageManager.getBarColor(0.10f))
        assertEquals(TokenUsageManager.COLOR_RED, TokenUsageManager.getBarColor(0.0f))
    }

    @Test
    fun testCountdownFormatting() {
        // 1 hour, 2 minutes, 3 seconds = (3600 + 120 + 3) * 1000 = 3,723,000 ms
        val formatted1 = TokenUsageManager.formatCountdown(3_723_000L)
        assertEquals("01:02:03", formatted1)

        // 45 seconds = 45,000 ms
        val formatted2 = TokenUsageManager.formatCountdown(45_000L)
        assertEquals("00:00:45", formatted2)

        // 0 ms
        val formattedZero = TokenUsageManager.formatCountdown(0L)
        assertEquals("00:00:00", formattedZero)

        // Negative ms -> coerced to 0
        val formattedNegative = TokenUsageManager.formatCountdown(-1000L)
        assertEquals("00:00:00", formattedNegative)
    }

    @Test
    fun testCalculateNextResetTimestamp() {
        val now = System.currentTimeMillis()
        val nextReset = TokenUsageManager.calculateNextResetTimestamp(now)

        assertTrue("Reset timestamp must be in the future", nextReset > now)
        val diffHours = (nextReset - now) / (1000 * 60 * 60)
        assertTrue("Reset timestamp must be within 24 hours", diffHours in 0..24)
    }
}
