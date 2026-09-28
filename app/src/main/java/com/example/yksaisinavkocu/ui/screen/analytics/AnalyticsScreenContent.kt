package com.example.yksaisinavkocu.ui.screen.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yksaisinavkocu.theme.*
import com.example.yksaisinavkocu.ui.components.*

@Composable
fun AnalyticsScreenContent(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AeroGradientBackground(isDarkTheme = isDarkTheme) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            // Başlık
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    IconBadge(
                        icon = Icons.Rounded.Insights,
                        tint = AeroDeepBlue,
                        size = 40.dp,
                        iconSize = 22.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Başarı Analizi",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // TYT / AYT Seçici
            item {
                ExamTypeSelector(
                    selectedType = uiState.selectedExamType,
                    onSelect = { viewModel.selectExamType(it) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.isLoading) {
                item { ShimmerLoading() }
            } else if (uiState.exams.isEmpty()) {
                item {
                    EmptyAnalyticsState()
                }
            } else {
                // Metrik Kartları
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "En Yüksek",
                            value = "%.1f".format(uiState.highestNet),
                            subtitle = "net",
                            accentColor = AeroLeafGreen,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Ortalama",
                            value = "%.1f".format(uiState.averageNet),
                            subtitle = "net",
                            accentColor = AeroSkyBlue,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Son Sınav",
                            value = "%.1f".format(uiState.lastNet),
                            subtitle = "%s%.1f".format(
                                if (uiState.netChange >= 0) "+" else "",
                                uiState.netChange
                            ),
                            accentColor = AeroDeepBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Net Değişim Grafiği
                item {
                    GlassCard(cornerRadius = 24.dp, alpha = 0.1f) {
                        SectionHeader(
                            icon = Icons.Rounded.TrendingUp,
                            title = "Net Değişim Eğrisi",
                            iconTint = AeroSkyBlue,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        NetChangeChart(
                            exams = uiState.exams,
                            maxNet = viewModel.getMaxNetFor(uiState.selectedExamType),
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Ders Detay Sekmeleri
                item {
                    val tabs = viewModel.getSubjectTabs()
                    SubjectTabRow(
                        tabs = tabs,
                        selectedTab = uiState.selectedSubjectTab,
                        onTabSelected = { viewModel.selectSubjectTab(it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Ders bazlı grafik
                item {
                    if (uiState.selectedSubjectTab != "Genel") {
                        val subjectData = viewModel.getSubjectNets(uiState.selectedSubjectTab)
                        if (subjectData.isNotEmpty()) {
                            GlassCard(cornerRadius = 20.dp, alpha = 0.1f) {
                                Text(
                                    text = "${uiState.selectedSubjectTab} Net Eğrisi",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                SubjectNetChart(
                                    data = subjectData,
                                    maxNet = viewModel.getMaxNetFor(uiState.selectedExamType, uiState.selectedSubjectTab),
                                    color = SubjectColors[uiState.selectedSubjectTab] ?: AeroSkyBlue,
                                    isDarkTheme = isDarkTheme,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .padding(top = 8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // Konu Zafiyet Analizi
                val weaknessTitle = if (uiState.selectedSubjectTab == "Genel") {
                    "Konu Zafiyet Analizi"
                } else {
                    "${uiState.selectedSubjectTab} Konu Zafiyet Analizi"
                }

                item {
                    SectionHeader(
                        icon = Icons.Rounded.WarningAmber,
                        title = weaknessTitle,
                        iconTint = Color(0xFFFFB74D),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (uiState.weaknesses.isEmpty()) {
                    item {
                        GlassCard(
                            cornerRadius = 16.dp,
                            alpha = 0.08f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                IconBadge(
                                    icon = Icons.Rounded.Info,
                                    tint = AeroSkyBlue,
                                    size = 32.dp,
                                    iconSize = 18.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Bu ders için henüz detaylı konu analiz verisi kaydedilmemiş.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(uiState.weaknesses) { _, weakness ->
                        TopicWeaknessCard(
                            rank = weakness.rank,
                            topicName = weakness.topicName,
                            subjectName = weakness.subjectName,
                            wrongCount = weakness.wrongCount,
                            errorRate = weakness.errorRate,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
