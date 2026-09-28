package com.example.yksaisinavkocu.ui.screen.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yksaisinavkocu.theme.*

@Composable
fun ExamTypeSelector(
    selectedType: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val isDarkTheme = MaterialTheme.colorScheme.background == AeroDarkBg
    val containerBg = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else AeroMatteLightGreySecondary.copy(alpha = AeroBlockAlpha)
    val containerBorder = if (isDarkTheme) Color.Transparent else AeroMatteBorderLight.copy(alpha = 0.85f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerBg, shape)
            .border(1.dp, containerBorder, shape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf("TYT", "AYT").forEach { type ->
            val isSelected = selectedType == type
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) AeroDeepBlue else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelect(type) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
