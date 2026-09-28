package com.example.yksaisinavkocu.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yksaisinavkocu.theme.*

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        cornerRadius = 20.dp,
        alpha = 0.12f
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = accentColor
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (subtitle.startsWith("+")) Color(0xFF4CAF50)
                    else if (subtitle.startsWith("-")) Color(0xFFF44336)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun TopicWeaknessCard(
    rank: Int,
    topicName: String,
    subjectName: String,
    wrongCount: Int,
    errorRate: Float,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val barColor = when {
        errorRate > 50f -> Color(0xFFF44336)
        errorRate > 30f -> Color(0xFFFF9800)
        else -> Color(0xFFFFC107)
    }

    val isDarkTheme = MaterialTheme.colorScheme.background == AeroDarkBg
    val blockBg = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else AeroMatteLightGreySecondary.copy(alpha = AeroBlockAlpha)
    val blockBorder = if (isDarkTheme) Color.Transparent else AeroMatteBorderLight.copy(alpha = 0.85f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(blockBg, shape)
            .border(1.dp, blockBorder, shape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sıra numarası
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(barColor.copy(alpha = 0.8f), barColor.copy(alpha = 0.4f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = topicName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subjectName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "%.0f%%".format(errorRate),
                style = MaterialTheme.typography.titleMedium,
                color = barColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$wrongCount yanlış",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun ShimmerLoading(modifier: Modifier = Modifier) {
    val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by shimmerTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.05f),
            Color.White.copy(alpha = 0.15f),
            Color.White.copy(alpha = 0.05f)
        ),
        start = androidx.compose.ui.geometry.Offset(translateAnim - 200f, 0f),
        end = androidx.compose.ui.geometry.Offset(translateAnim + 200f, 0f)
    )

    Column(modifier = modifier.padding(16.dp)) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(shimmerBrush)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
