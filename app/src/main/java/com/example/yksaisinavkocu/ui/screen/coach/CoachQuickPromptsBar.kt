package com.example.yksaisinavkocu.ui.screen.coach

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.yksaisinavkocu.theme.*

@Composable
fun QuickPromptRow(
    onPromptSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = MaterialTheme.colorScheme.background == AeroDarkBg
    val chipBg = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else AeroMatteLightGreySecondary.copy(alpha = AeroBlockAlpha)
    val chipBorder = if (isDarkTheme) Color.White.copy(alpha = 0.15f) else AeroMatteBorderLight.copy(alpha = 0.85f)

    val prompts = listOf(
        "Genel net durumumu analiz et",
        "En zayıf konularıma çalışma taktiği ver",
        "TYT Türkçe nasıl 35+ net yapılır?",
        "Haftalık çalışma planı oluştur",
        "Son sınavımı değerlendir"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        prompts.forEach { prompt ->
            SuggestionChip(
                onClick = { onPromptSelected(prompt) },
                label = {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = chipBg
                ),
                border = SuggestionChipDefaults.suggestionChipBorder(
                    enabled = true,
                    borderColor = chipBorder,
                    borderWidth = 1.dp
                )
            )
        }
    }
}
