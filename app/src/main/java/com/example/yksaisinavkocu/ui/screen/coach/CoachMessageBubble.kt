package com.example.yksaisinavkocu.ui.screen.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.yksaisinavkocu.service.parser.AppAction
import com.example.yksaisinavkocu.theme.*

import com.example.yksaisinavkocu.ui.components.RichTextVisualizer

@Composable
fun ChatBubble(
    message: ChatMessage,
    onAction: (AppAction) -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = MaterialTheme.colorScheme.background == AeroDarkBg
) {
    val alignment = if (message.isFromUser) Alignment.End else Alignment.Start
    val bubbleShape = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = if (message.isFromUser) 20.dp else 4.dp,
        bottomEnd = if (message.isFromUser) 4.dp else 20.dp
    )
    val bgColor = if (message.isFromUser) {
        AeroDeepBlue.copy(alpha = 0.9f)
    } else {
        if (isDarkTheme) AeroDarkCard.copy(alpha = 0.85f) else AeroMatteLightGrey.copy(alpha = AeroBlockAlpha)
    }
    val borderColor = if (message.isFromUser) {
        Color.Transparent
    } else {
        if (isDarkTheme) Color.White.copy(alpha = 0.15f) else AeroMatteBorderLight.copy(alpha = 0.85f)
    }
    val textColor = if (message.isFromUser) Color.White else MaterialTheme.colorScheme.onSurface

    val bubbleModifier = if (message.isFromUser) {
        Modifier.widthIn(max = 300.dp)
    } else {
        Modifier.fillMaxWidth(0.96f)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = bubbleModifier
                .clip(bubbleShape)
                .background(bgColor)
                .border(1.dp, borderColor, bubbleShape)
                .padding(14.dp)
        ) {
            RichTextVisualizer(
                text = message.text,
                textColor = textColor,
                isDarkTheme = isDarkTheme,
                isUserMessage = message.isFromUser
            )
        }

        // Aksiyon butonları
        if (message.actions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                message.actions.forEach { action ->
                    ActionChip(action = action, onClick = { onAction(action) })
                }
            }
        }
    }
}

@Composable
fun ActionChip(
    action: AppAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, label) = when (action) {
        is AppAction.NavigateTab -> Icons.Rounded.Analytics to "${action.subject ?: "Analiz"} Grafiği"
        is AppAction.OpenScanner -> Icons.Rounded.DocumentScanner to "Karne Tara"
        is AppAction.NavigateExams -> Icons.AutoMirrored.Rounded.ListAlt to "Sınav Listesi"
        is AppAction.SwitchTheme -> Icons.Rounded.DarkMode to "Tema Değiştir"
        is AppAction.StudyPlan -> Icons.Rounded.CalendarMonth to "Çalışma Planı"
        is AppAction.NavigateSettings -> Icons.Rounded.Settings to "Ayarlar"
    }

    AssistChip(
        onClick = onClick,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = AeroSkyBlue
            )
        },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = AeroSkyBlue.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    )
}
