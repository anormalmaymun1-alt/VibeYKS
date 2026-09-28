package com.example.yksaisinavkocu.ui.screen.coach

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.yksaisinavkocu.theme.AeroDeepBlue

@Composable
fun CoachChatInput(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isTyping: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputTextChange,
            placeholder = { Text("Mesajınızı yazın...") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            singleLine = false,
            maxLines = 3
        )
        Spacer(modifier = Modifier.width(8.dp))
        FilledIconButton(
            onClick = onSendMessage,
            enabled = inputText.isNotBlank() && !isTyping,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = AeroDeepBlue
            )
        ) {
            Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Gönder", tint = Color.White)
        }
    }
}
