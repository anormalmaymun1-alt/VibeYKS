package com.example.yksaisinavkocu.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.yksaisinavkocu.YksApp
import com.example.yksaisinavkocu.theme.AeroSkyBlue
import com.example.yksaisinavkocu.ui.components.AeroGradientBackground
import com.example.yksaisinavkocu.ui.components.IconBadge
import com.example.yksaisinavkocu.ui.util.SampleData
import kotlinx.coroutines.launch

@Composable
fun SettingsScreenContent(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val preferences = YksApp.instance.preferences
    val repo = YksApp.instance.examRepository
    val scope = rememberCoroutineScope()

    var apiKey by remember { mutableStateOf(preferences.getApiKey() ?: "") }
    var selectedModel by remember { mutableStateOf(preferences.getSelectedModel()) }
    var customSystemPrompt by remember { mutableStateOf(preferences.getCustomSystemPrompt()) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showLoadSampleDialog by remember { mutableStateOf(false) }
    var snackMessage by remember { mutableStateOf<String?>(null) }

    AeroGradientBackground(isDarkTheme = isDarkTheme) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(bottom = 100.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconBadge(
                    icon = Icons.Rounded.Settings,
                    tint = AeroSkyBlue
                )
                Text(
                    text = "Ayarlar",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            // 1. API Key Card
            ApiKeySettingsCard(
                apiKey = apiKey,
                onApiKeyChange = { apiKey = it },
                onSaveKey = {
                    preferences.setApiKey(apiKey)
                    YksApp.instance.geminiModelManager.clearCache()
                    snackMessage = "API anahtarı başarıyla kaydedildi"
                },
                onDeleteKey = {
                    preferences.clearApiKey()
                    YksApp.instance.geminiModelManager.clearCache()
                    apiKey = ""
                    snackMessage = "API anahtarı silindi"
                },
                hasSavedKey = preferences.getApiKey() != null
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Model Selection Card
            ModelSelectionCard(
                selectedModel = selectedModel,
                onModelSelected = { modelId ->
                    selectedModel = modelId
                    preferences.setSelectedModel(modelId)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Custom System Prompt Card
            SystemPromptSettingsCard(
                customSystemPrompt = customSystemPrompt,
                onPromptChange = { customSystemPrompt = it },
                onSavePrompt = {
                    preferences.setCustomSystemPrompt(customSystemPrompt.trim())
                    snackMessage = "Sistem komutları başarıyla kaydedildi"
                },
                onClearPrompt = {
                    preferences.clearCustomSystemPrompt()
                    customSystemPrompt = ""
                    snackMessage = "Sistem komutları temizlendi"
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 4. Theme Card
            ThemeSettingsCard(
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 5. Data Management Card
            DataManagementCard(
                onLoadSampleClick = { showLoadSampleDialog = true },
                onClearDataClick = { showClearDialog = true }
            )
            Spacer(modifier = Modifier.height(20.dp))

            // App Version
            Text(
                text = "YKS AI Sınav Koçu v1.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        // Snackbar
        snackMessage?.let { msg ->
            Snackbar(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                action = {
                    TextButton(onClick = { snackMessage = null }) {
                        Text("Tamam")
                    }
                }
            ) {
                Text(msg)
            }
        }
    }

    // Dialogs
    if (showClearDialog) {
        ClearDataDialog(
            onConfirm = {
                scope.launch {
                    repo.deleteAllExams()
                    snackMessage = "Tüm veriler silindi"
                }
                showClearDialog = false
            },
            onDismiss = { showClearDialog = false }
        )
    }

    if (showLoadSampleDialog) {
        LoadSampleDataDialog(
            onConfirm = {
                scope.launch {
                    SampleData.generateSampleTytExams().forEach { repo.insertExam(it) }
                    SampleData.generateSampleAytExams().forEach { repo.insertExam(it) }
                    snackMessage = "16 örnek sınav başarıyla yüklendi"
                }
                showLoadSampleDialog = false
            },
            onDismiss = { showLoadSampleDialog = false }
        )
    }
}
