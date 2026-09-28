package com.example.yksaisinavkocu.ui.screen.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yksaisinavkocu.theme.AeroDeepBlue
import com.example.yksaisinavkocu.theme.AeroSkyBlue
import com.example.yksaisinavkocu.theme.ErrorRed
import com.example.yksaisinavkocu.ui.components.GlassCard
import com.example.yksaisinavkocu.ui.components.SectionHeader

@Composable
fun SystemPromptSettingsCard(
    customSystemPrompt: String,
    onPromptChange: (String) -> Unit,
    onSavePrompt: () -> Unit,
    onClearPrompt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val templates = listOf(
        "Disiplinli Koç" to "Bana karşı sert, tavizsiz ve disiplinli bir koç ol. Asla bahaneleri kabul etme ve her mesajda net hedefler koy.",
        "Tıp Hedefi (Sayısal)" to "Hedefim Tıp Fakültesi (Sayısal). AYT Matematik ve Biyoloji netlerimi zirveye çıkarmaya odaklan.",
        "Kısa & Öz" to "Yanıtlarını her zaman maddeler halinde, kısa, öz ve doğrudan uygulanabilir somut eylem planları olarak ver.",
        "Hukuk Hedefi (EA)" to "Hedefim Hukuk Fakültesi (Eşit Ağırlık). TYT Matematik ve AYT Edebiyat-Tarih netlerimi artırmaya odaklan."
    )

    GlassCard(cornerRadius = 20.dp, alpha = 0.1f, modifier = modifier) {
        SectionHeader(
            icon = Icons.Rounded.Psychology,
            title = "Yapay Zeka Sistem Komutları",
            tint = AeroSkyBlue
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Yapay zeka koçunun üslubunu, hedeflerini veya odaklanmasını istediğin konuları kişiselleştir.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Hızlı Şablonlar:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            templates.forEach { (title, promptText) ->
                FilterChip(
                    selected = customSystemPrompt == promptText,
                    onClick = { onPromptChange(promptText) },
                    label = { Text(title, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AeroSkyBlue.copy(alpha = 0.3f),
                        selectedLabelColor = AeroDeepBlue
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = customSystemPrompt,
            onValueChange = onPromptChange,
            placeholder = {
                Text(
                    "Örn: Sayısal ilk 10.000 hedefliyorum. Matematik netlerimi artırmam için tavsiyelerde bulun...",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 3,
            maxLines = 6
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onSavePrompt,
                colors = ButtonDefaults.buttonColors(containerColor = AeroDeepBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Kaydet")
            }
            if (customSystemPrompt.isNotBlank()) {
                OutlinedButton(
                    onClick = onClearPrompt,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Temizle", color = ErrorRed)
                }
            }
        }
    }
}
