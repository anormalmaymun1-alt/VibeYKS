package com.example.yksaisinavkocu.ui.screen.scan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yksaisinavkocu.theme.AeroSunset
import com.example.yksaisinavkocu.ui.components.GlassCard
import com.example.yksaisinavkocu.ui.components.IconBadge

@Composable
fun ApiKeyWarningCard(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        cornerRadius = 20.dp,
        alpha = 0.18f,
        borderAlpha = 0.5f,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconBadge(
                icon = Icons.Rounded.VpnKey,
                tint = AeroSunset,
                size = 44.dp
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Gemini API Anahtarı Gerekli",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AeroSunset
                )
                Text(
                    text = "OCR ve AI koçluk özelliklerini kullanmak için API anahtarınızı tanımlayın.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onNavigateToSettings,
            colors = ButtonDefaults.buttonColors(containerColor = AeroSunset),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Ayarlara Git", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
