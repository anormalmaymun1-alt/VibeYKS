package com.example.yksaisinavkocu.ui.screen.scan

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.yksaisinavkocu.YksApp
import com.example.yksaisinavkocu.data.local.entity.ExamEntity
import com.example.yksaisinavkocu.service.gemini.ParsedExamData
import com.example.yksaisinavkocu.theme.*
import com.example.yksaisinavkocu.ui.components.GlassCard
import com.example.yksaisinavkocu.ui.components.IconBadge
import com.example.yksaisinavkocu.ui.components.SectionHeader
import com.example.yksaisinavkocu.ui.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewExamDialog(
    initialData: ParsedExamData,
    sourcePreview: Bitmap? = null,
    onDismiss: () -> Unit,
    onSave: (ExamEntity) -> Unit
) {
    var examName by remember { mutableStateOf(initialData.examName.ifBlank { "Deneme Sınavı" }) }
    var examType by remember { mutableStateOf(if (initialData.examType.contains("AYT", ignoreCase = true)) "AYT" else "TYT") }
    var examDateStr by remember {
        mutableStateOf(
            initialData.examDate.ifBlank {
                DateFormatter.formatShort(System.currentTimeMillis())
            }
        )
    }

    var subjects by remember {
        mutableStateOf(
            if (initialData.subjects.isNotEmpty()) initialData.subjects
            else getDefaultSubjectsFor(examType)
        )
    }

    val totalNet = remember(subjects) {
        subjects.sumOf { it.net.toDouble() }.toFloat()
    }

    val repo = YksApp.instance.examRepository
    val isDarkTheme = MaterialTheme.colorScheme.background == AeroDarkBg

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
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
                            icon = Icons.Rounded.FactCheck,
                            tint = AeroSkyBlue
                        )
                        Column {
                            Text(
                                text = "Karne İnceleme & Onay",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "OCR tarafından okunan sonuçları kontrol edip düzenleyebilirsiniz",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Kapat")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Kaynak görsel önizlemesi (varsa)
                    if (sourcePreview != null) {
                        item {
                            GlassCard(cornerRadius = 16.dp, alpha = 0.1f) {
                                SectionHeader(
                                    icon = Icons.Rounded.Image,
                                    title = "Taranan Belge Önizlemesi",
                                    tint = AeroSkyBlue
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Image(
                                    bitmap = sourcePreview.asImageBitmap(),
                                    contentDescription = "Karne Önizleme",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }
                    }

                    // Sınav Bilgileri Kartı
                    item {
                        GlassCard(cornerRadius = 20.dp, alpha = 0.12f) {
                            SectionHeader(
                                icon = Icons.Rounded.Description,
                                title = "Sınav Genel Bilgileri",
                                tint = AeroSkyBlue
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = examName,
                                onValueChange = { examName = it },
                                label = { Text("Sınav / Yayın Adı") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Sınav Türü Seçimi
                                val typeSelectorBg = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else AeroMatteLightGreySecondary.copy(alpha = AeroBlockAlpha)
                                val typeSelectorBorder = if (isDarkTheme) Color.Transparent else AeroMatteBorderLight.copy(alpha = 0.85f)

                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(typeSelectorBg)
                                        .border(1.dp, typeSelectorBorder, RoundedCornerShape(14.dp))
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("TYT", "AYT").forEach { type ->
                                        val isSel = examType == type
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSel) AeroDeepBlue else Color.Transparent)
                                                .clickable {
                                                    if (examType != type) {
                                                        examType = type
                                                        subjects = getDefaultSubjectsFor(type)
                                                    }
                                                }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = type,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }

                                // Tarih
                                OutlinedTextField(
                                    value = examDateStr,
                                    onValueChange = { examDateStr = it },
                                    label = { Text("Tarih") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    // Toplam Net Özeti
                    item {
                        GlassCard(
                            cornerRadius = 20.dp,
                            alpha = 0.18f,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Toplam Net",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "Doğru - (Yanlış / 4)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                                Text(
                                    text = "%.2f".format(totalNet),
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AeroSkyBlue
                                )
                            }
                        }
                    }

                    // Dersler Başlığı
                    item {
                        SectionHeader(
                            icon = Icons.Rounded.MenuBook,
                            title = "Ders Bazlı Dağılım",
                            tint = AeroPurple
                        )
                    }

                    // Ders Kartları
                    itemsIndexed(subjects) { index, subj ->
                        SubjectEditCard(
                            subject = subj,
                            onUpdate = { updated ->
                                val list = subjects.toMutableList()
                                list[index] = updated
                                subjects = list
                            }
                        )
                    }

                    // Konu Detayları (varsa)
                    if (initialData.topics.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            SectionHeader(
                                icon = Icons.Rounded.Checklist,
                                title = "Tespit Edilen Konular (${initialData.topics.size} adet)",
                                tint = AeroLeafGreen
                            )
                        }

                        itemsIndexed(initialData.topics) { _, topic ->
                            val topicRowBg = if (isDarkTheme) Color.White.copy(alpha = 0.06f) else AeroMatteLightGreySecondary.copy(alpha = AeroBlockAlpha)
                            val topicRowBorder = if (isDarkTheme) Color.Transparent else AeroMatteBorderLight.copy(alpha = 0.85f)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(topicRowBg)
                                    .border(1.dp, topicRowBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = topic.topicName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = topic.subjectName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                                Text(
                                    text = "${topic.correctCount}D ${topic.wrongCount}Y (${topic.totalQuestions} S)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (topic.wrongCount > 0) ErrorRed else AeroLeafGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Alt Aksiyon Butonları
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Vazgeç")
                    }

                    Button(
                        onClick = {
                            val epochDate = DateFormatter.parseDateToMillis(examDateStr)
                                ?: System.currentTimeMillis()
                            val examEntity = ExamEntity(
                                examName = examName.trim().ifBlank { "Deneme Sınavı" },
                                examDate = epochDate,
                                examType = examType,
                                totalNet = totalNet,
                                subjectResults = repo.encodeSubjectResults(subjects),
                                topicDetails = repo.encodeTopicDetails(initialData.topics),
                                aiAnalysisNote = initialData.rawNotes
                            )
                            onSave(examEntity)
                        },
                        modifier = Modifier.weight(2f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AeroDeepBlue)
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Onayla ve Kaydet")
                    }
                }
            }
        }
    }
}
