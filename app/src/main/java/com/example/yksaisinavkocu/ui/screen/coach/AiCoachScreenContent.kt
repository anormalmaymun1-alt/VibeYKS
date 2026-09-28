package com.example.yksaisinavkocu.ui.screen.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yksaisinavkocu.service.parser.AppAction
import com.example.yksaisinavkocu.theme.*
import com.example.yksaisinavkocu.ui.components.AeroGradientBackground

@Composable
fun AiCoachScreenContent(
    isDarkTheme: Boolean,
    onAction: (AppAction) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AiCoachViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Otomatik scroll
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    AeroGradientBackground(isDarkTheme = isDarkTheme) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .imePadding()
        ) {
            // Başlık
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(AeroSkyBlue, AeroPurple)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SmartToy,
                        contentDescription = "AI Koç",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "YKS AI Sınav Koçu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (uiState.isTyping) {
                        TypingIndicator()
                    }
                }
            }

            // Mesajlar
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    ChatBubble(
                        message = message,
                        isDarkTheme = isDarkTheme,
                        onAction = onAction
                    )
                }

                // Hata mesajı
                if (uiState.errorMessage != null) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WarningAmber,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = uiState.errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = ErrorRed
                            )
                        }
                    }
                }
            }

            // Hızlı istem butonları
            QuickPromptRow(
                onPromptSelected = { viewModel.sendMessage(it) }
            )

            // Mesaj girişi
            CoachChatInput(
                inputText = uiState.inputText,
                onInputTextChange = { viewModel.updateInputText(it) },
                onSendMessage = { viewModel.sendMessage() },
                isTyping = uiState.isTyping
            )
        }
    }
}
