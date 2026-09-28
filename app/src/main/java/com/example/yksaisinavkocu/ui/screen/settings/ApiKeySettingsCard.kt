package com.example.yksaisinavkocu.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.yksaisinavkocu.theme.AeroDeepBlue
import com.example.yksaisinavkocu.theme.AeroSunset
import com.example.yksaisinavkocu.theme.ErrorRed
import com.example.yksaisinavkocu.ui.components.GlassCard
import com.example.yksaisinavkocu.ui.components.SectionHeader

@Composable
fun ApiKeySettingsCard(
    apiKey: String,
    onApiKeyChange: (String) -> Unit,
    onSaveKey: () -> Unit,
    onDeleteKey: () -> Unit,
    hasSavedKey: Boolean,
    modifier: Modifier = Modifier
) {
    var showApiKey by remember { mutableStateOf(false) }

    GlassCard(cornerRadius = 20.dp, alpha = 0.1f, modifier = modifier) {
        SectionHeader(
            icon = Icons.Rounded.VpnKey,
            title = "Gemini API Anahtarı",
            tint = AeroSunset
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Google AI Studio'dan alınır. Cihazda şifreli saklanır.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = apiKey,
            onValueChange = onApiKeyChange,
            placeholder = { Text("API anahtarını yapıştırın") },
            visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showApiKey = !showApiKey }) {
                    Icon(
                        if (showApiKey) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onSaveKey,
                colors = ButtonDefaults.buttonColors(containerColor = AeroDeepBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Kaydet")
            }
            if (hasSavedKey) {
                OutlinedButton(
                    onClick = onDeleteKey,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sil", color = ErrorRed)
                }
            }
        }
    }
}
