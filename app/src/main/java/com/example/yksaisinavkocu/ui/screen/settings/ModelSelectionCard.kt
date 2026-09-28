package com.example.yksaisinavkocu.ui.screen.settings

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.yksaisinavkocu.YksApp
import com.example.yksaisinavkocu.data.preferences.AppPreferences
import com.example.yksaisinavkocu.service.gemini.TokenUsageManager
import com.example.yksaisinavkocu.theme.AeroPurple
import com.example.yksaisinavkocu.theme.AeroSkyBlue
import com.example.yksaisinavkocu.ui.components.GlassCard
import com.example.yksaisinavkocu.ui.components.SectionHeader
import kotlinx.coroutines.delay

@Composable
fun ModelSelectionCard(
    selectedModel: String,
    onModelSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokenManager = YksApp.instance.tokenUsageManager
    val tokenState by tokenManager.usageState.collectAsStateWithLifecycle()

    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTime = System.currentTimeMillis()
            tokenManager.checkAndResetIfExpired()
        }
    }

    val millisUntilReset = (tokenState.resetTimestamp - currentTime).coerceAtLeast(0L)
    val countdownText = TokenUsageManager.formatCountdown(millisUntilReset)
    val remainingFraction = tokenState.remainingFraction
    val barColor = TokenUsageManager.getBarColor(remainingFraction)

    val animatedProgress by animateFloatAsState(
        targetValue = remainingFraction,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "token_usage_progress"
    )

    GlassCard(cornerRadius = 20.dp, alpha = 0.1f, modifier = modifier) {
        SectionHeader(
            icon = Icons.Rounded.AutoAwesome,
            title = "Gemini Modeli",
            tint = AeroPurple
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Görsel karne OCR ve yapay zeka koçluk yanıtlarında kullanılır.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(12.dp))

        AppPreferences.AVAILABLE_MODELS.forEach { (modelId, modelLabel) ->
            val isSelected = selectedModel == modelId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onModelSelected(modelId) }
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { onModelSelected(modelId) },
                    colors = RadioButtonDefaults.colors(selectedColor = AeroSkyBlue)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = modelLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // ── Kalan Token Kullanım Çubuğu ve Sıfırlanma Sayacı ──
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
            thickness = 1.dp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(barColor)
                )
                Text(
                    text = "Kalan Kota",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // Sayısal token/yüzde değeri OLMADAN, yalnızca sıfırlanma geri sayımı
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = countdownText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Kalan token çubuğu: >= %50 yeşil, < %50 sarı, < %20 kırmızı
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                barColor.copy(alpha = 0.85f),
                                barColor
                            )
                        )
                    )
            )
        }
    }
}
