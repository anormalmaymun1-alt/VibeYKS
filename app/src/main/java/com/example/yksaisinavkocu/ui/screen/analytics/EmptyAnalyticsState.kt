package com.example.yksaisinavkocu.ui.screen.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.yksaisinavkocu.theme.AeroSkyBlue
import com.example.yksaisinavkocu.ui.components.GlassCard
import com.example.yksaisinavkocu.ui.components.GlowIconContainer

@Composable
fun EmptyAnalyticsState(modifier: Modifier = Modifier) {
    GlassCard(
        cornerRadius = 24.dp,
        alpha = 0.08f,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            GlowIconContainer(
                icon = Icons.Rounded.Insights,
                tint = AeroSkyBlue,
                size = 64.dp,
                iconSize = 32.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Henüz sınav verisi yok",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Karne tarayarak veya ayarlardan örnek veri yükleyerek başlayabilirsiniz",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}
