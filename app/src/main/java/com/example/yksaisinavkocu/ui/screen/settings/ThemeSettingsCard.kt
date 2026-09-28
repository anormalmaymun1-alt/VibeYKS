package com.example.yksaisinavkocu.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yksaisinavkocu.theme.AeroDeepBlue
import com.example.yksaisinavkocu.theme.AeroPurple
import com.example.yksaisinavkocu.theme.AeroSkyBlue
import com.example.yksaisinavkocu.ui.components.GlassCard
import com.example.yksaisinavkocu.ui.components.IconBadge

@Composable
fun ThemeSettingsCard(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(cornerRadius = 20.dp, alpha = 0.1f, modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconBadge(
                    icon = Icons.Rounded.DarkMode,
                    tint = AeroPurple,
                    size = 36.dp
                )
                Column {
                    Text(
                        text = "Karanlık Tema",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isDarkTheme) "Aktif" else "Pasif",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { onThemeToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AeroSkyBlue,
                    checkedTrackColor = AeroDeepBlue.copy(alpha = 0.3f)
                )
            )
        }
    }
}
