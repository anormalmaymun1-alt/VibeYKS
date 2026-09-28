package com.example.yksaisinavkocu.ui.screen.scan

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.yksaisinavkocu.YksApp
import com.example.yksaisinavkocu.data.local.entity.ExamEntity
import com.example.yksaisinavkocu.data.local.entity.SubjectResult
import com.example.yksaisinavkocu.theme.*
import com.example.yksaisinavkocu.ui.components.*
import com.example.yksaisinavkocu.ui.util.DateFormatter

@Composable
fun ManualEntryDialog(
    onDismiss: () -> Unit,
    onSave: (ExamEntity) -> Unit
) {
    var examName by remember { mutableStateOf("") }
    var examType by remember { mutableStateOf("TYT") }
    var examDateStr by remember { mutableStateOf(DateFormatter.formatShort(System.currentTimeMillis())) }
    var subjects by remember { mutableStateOf(getDefaultSubjectsFor("TYT")) }

    val totalNet = remember(subjects) {
        subjects.sumOf { it.net.toDouble() }.toFloat()
    }

    val repo = YksApp.instance.examRepository

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
                            icon = Icons.Rounded.EditNote,
                            tint = AeroSkyBlue
                        )
                        Column {
                            Text(
                                text = "Manuel Sınav Girişi",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Derslerin doğru, yanlış ve boş sayılarını girin",
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
                    // Sınav Bilgileri Kartı
                    item {
                        GlassCard(cornerRadius = 20.dp, alpha = 0.12f) {
                            SectionHeader(
                                icon = Icons.Rounded.Description,
                                title = "Sınav Detayları",
                                tint = AeroSkyBlue
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = examName,
                                onValueChange = { examName = it },
                                label = { Text("Sınav Adı (örn: Özdebir TYT 1)") },
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
                                val isDarkTheme = MaterialTheme.colorScheme.background == AeroDarkBg
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

                    // Canlı Net Özeti
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
                                        text = "Hesaplanan Toplam Net",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "4 Yanlış 1 Doğruyu Götürür",
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

                    // Dersler
                    item {
                        SectionHeader(
                            icon = Icons.Rounded.MenuBook,
                            title = "Ders Netleri",
                            tint = AeroPurple
                        )
                    }

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
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Alt Butonlar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("İptal")
                    }

                    Button(
                        onClick = {
                            val epochDate = DateFormatter.parseDateToMillis(examDateStr)
                                ?: System.currentTimeMillis()
                            val examEntity = ExamEntity(
                                examName = examName.trim().ifBlank { "$examType Deneme Sınavı" },
                                examDate = epochDate,
                                examType = examType,
                                totalNet = totalNet,
                                subjectResults = repo.encodeSubjectResults(subjects),
                                topicDetails = "[]",
                                aiAnalysisNote = "Manuel olarak girildi."
                            )
                            onSave(examEntity)
                        },
                        modifier = Modifier.weight(2f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AeroDeepBlue)
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sınavı Kaydet")
                    }
                }
            }
        }
    }
}
