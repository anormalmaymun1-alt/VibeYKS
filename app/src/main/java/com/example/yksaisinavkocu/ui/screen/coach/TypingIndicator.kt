package com.example.yksaisinavkocu.ui.screen.coach

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.yksaisinavkocu.theme.AeroSkyBlue

@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "typing")
    val alpha1 by transition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "d1")
    val alpha2 by transition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(600, 200), RepeatMode.Reverse), label = "d2")
    val alpha3 by transition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(600, 400), RepeatMode.Reverse), label = "d3")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = "yazıyor",
            style = MaterialTheme.typography.labelSmall,
            color = AeroSkyBlue.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        listOf(alpha1, alpha2, alpha3).forEach { alpha ->
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(AeroSkyBlue.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.width(2.dp))
        }
    }
}
