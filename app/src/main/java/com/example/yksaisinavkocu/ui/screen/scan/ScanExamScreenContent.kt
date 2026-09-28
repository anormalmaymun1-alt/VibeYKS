package com.example.yksaisinavkocu.ui.screen.scan

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yksaisinavkocu.theme.AeroSkyBlue
import com.example.yksaisinavkocu.ui.components.AeroGradientBackground
import com.example.yksaisinavkocu.ui.components.GlassCard
import com.example.yksaisinavkocu.ui.components.IconBadge
import com.example.yksaisinavkocu.ui.components.SectionHeader

@Composable
fun ScanExamScreenContent(
    isDarkTheme: Boolean,
    onNavigateToSettings: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScanViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.checkApiKey()
    }

    // Launchers
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.processCapturedBitmap(bitmap)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.processImageUri(context, uri)
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.processPdfUri(uri)
        }
    }

    AeroGradientBackground(isDarkTheme = isDarkTheme) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(bottom = 90.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconBadge(
                    icon = Icons.Rounded.DocumentScanner,
                    tint = AeroSkyBlue
                )
                Text(
                    text = "Akıllı Karne Tarama",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Gemini Multimodal yapay zeka ile karnenizi anında dijitalleştirin",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // API Key Warning
            if (!uiState.isApiKeyConfigured) {
                ApiKeyWarningCard(onNavigateToSettings = onNavigateToSettings)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Status Card
            when (val status = uiState.status) {
                is ScanStatus.Processing -> {
                    ScanProcessingCard(message = status.message)
                }
                is ScanStatus.Success -> {
                    ScanSuccessCard(
                        examName = status.examName,
                        totalNet = status.totalNet,
                        onNewScan = { viewModel.resetStatus() },
                        onNavigateToAnalytics = onNavigateToAnalytics
                    )
                }
                is ScanStatus.Error -> {
                    ScanErrorCard(
                        message = status.message,
                        onRetry = { viewModel.resetStatus() },
                        onManualEntry = { viewModel.openManualEntry() }
                    )
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Cards Grid
            UploadSourceGrid(
                onCameraClick = { cameraLauncher.launch(null) },
                onGalleryClick = { galleryLauncher.launch("image/*") },
                onPdfClick = { pdfLauncher.launch("application/pdf") },
                onManualClick = { viewModel.openManualEntry() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // OCR Tips Card
            GlassCard(cornerRadius = 20.dp, alpha = 0.1f) {
                SectionHeader(
                    icon = Icons.Rounded.Lightbulb,
                    title = "En İyi OCR Sonuçları İçin İpuçları",
                    tint = AeroSkyBlue
                )
                Spacer(modifier = Modifier.height(10.dp))
                listOf(
                    "Karnenin tamamının ve ders tablosunun net kadrajda olduğundan emin olun.",
                    "Yansıma ve gölgeleri önlemek için iyi aydınlatılmış bir ortamda çekin.",
                    "PDF formatındaki dijital karneler en yüksek doğruluk oranını sağlar.",
                    "Okunan sonuçları onay ekranında dilediğiniz gibi düzenleyebilirsiniz."
                ).forEach { tip ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("•", color = AeroSkyBlue, fontWeight = FontWeight.Bold)
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }

    // Review Dialog
    val status = uiState.status
    if (status is ScanStatus.Review) {
        ReviewExamDialog(
            initialData = status.examData,
            sourcePreview = status.previewBitmap,
            onDismiss = { viewModel.dismissReview() },
            onSave = { examEntity ->
                viewModel.saveExam(examEntity)
            }
        )
    }

    // Manual Entry Dialog
    if (uiState.showManualEntry) {
        ManualEntryDialog(
            onDismiss = { viewModel.closeManualEntry() },
            onSave = { examEntity ->
                viewModel.saveExam(examEntity)
            }
        )
    }
}
