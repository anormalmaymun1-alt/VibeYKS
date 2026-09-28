package com.example.yksaisinavkocu.ui.screen.exams

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yksaisinavkocu.YksApp
import com.example.yksaisinavkocu.data.local.entity.ExamEntity
import com.example.yksaisinavkocu.theme.*
import com.example.yksaisinavkocu.ui.components.*
import com.example.yksaisinavkocu.ui.util.DateFormatter

@Composable
fun ExamsListScreenContent(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    viewModel: ExamsListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AeroGradientBackground(isDarkTheme = isDarkTheme) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconBadge(
                    icon = Icons.Rounded.Assignment,
                    tint = AeroSkyBlue
                )
                Text(
                    text = "Deneme Sınavları",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Arama
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Sınav ara...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Filtre chipları
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.filterType == null,
                    onClick = { viewModel.setFilterType(null) },
                    label = { Text("Tümü") }
                )
                FilterChip(
                    selected = uiState.filterType == "TYT",
                    onClick = { viewModel.setFilterType(if (uiState.filterType == "TYT") null else "TYT") },
                    label = { Text("TYT") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AeroSkyBlue.copy(alpha = 0.2f)
                    )
                )
                FilterChip(
                    selected = uiState.filterType == "AYT",
                    onClick = { viewModel.setFilterType(if (uiState.filterType == "AYT") null else "AYT") },
                    label = { Text("AYT") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AeroPurple.copy(alpha = 0.2f)
                    )
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading) {
                ShimmerLoading()
            } else if (uiState.exams.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlowIconContainer(
                            icon = Icons.Rounded.AssignmentLate,
                            tint = AeroSkyBlue
                        )
                        Text(
                            text = "Henüz sınav kaydı bulunmuyor",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Karne tarayarak veya manuel girişle sınav ekleyebilirsiniz",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(uiState.exams, key = { it.id }) { exam ->
                        ExamCard(
                            exam = exam,
                            onDelete = { viewModel.deleteExam(exam) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamCard(
    exam: ExamEntity,
    onDelete: () -> Unit
) {
    val repo = YksApp.instance.examRepository
    val subjects = remember(exam) { repo.parseSubjectResults(exam.subjectResults) }
    var showDetail by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val typeColor = if (exam.examType == "TYT") AeroSkyBlue else AeroPurple

    GlassCard(
        cornerRadius = 20.dp,
        alpha = 0.1f,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDetail = !showDetail }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tip badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(typeColor.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = exam.examType,
                    style = MaterialTheme.typography.labelMedium,
                    color = typeColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exam.examName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = DateFormatter.formatShort(exam.examDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Text(
                text = "%.1f".format(exam.totalNet),
                style = MaterialTheme.typography.headlineMedium,
                color = typeColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = " net",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = "Sil",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }

        // Detay genişletme
        AnimatedVisibility(visible = showDetail) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                val isDark = MaterialTheme.colorScheme.background == AeroDarkBg
                HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.1f) else AeroMatteBorderLight)
                Spacer(modifier = Modifier.height(8.dp))
                subjects.forEach { subject ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = subject.subjectName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${subject.correct}D ${subject.wrong}Y → %.1f net".format(subject.net),
                            style = MaterialTheme.typography.bodySmall,
                            color = SubjectColors[subject.subjectName] ?: AeroSkyBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Sınavı Sil") },
            text = { Text("\"${exam.examName}\" silinecek. Bu işlem geri alınamaz.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Sil", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}
