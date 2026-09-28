package com.example.yksaisinavkocu

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.yksaisinavkocu.service.parser.AppAction
import com.example.yksaisinavkocu.theme.YKSAISinavKocuTheme
import com.example.yksaisinavkocu.ui.components.AnimatedBottomBar
import com.example.yksaisinavkocu.ui.components.NavTab
import com.example.yksaisinavkocu.ui.screen.analytics.AnalyticsScreenContent
import com.example.yksaisinavkocu.ui.screen.coach.AiCoachScreenContent
import com.example.yksaisinavkocu.ui.screen.exams.ExamsListScreenContent
import com.example.yksaisinavkocu.ui.screen.scan.ScanExamScreenContent
import com.example.yksaisinavkocu.ui.screen.settings.SettingsScreenContent

@Composable
fun MainNavigation() {
    val prefs = YksApp.instance.preferences
    var isDarkTheme by remember { mutableStateOf(prefs.isDarkTheme()) }
    var selectedTab by remember { mutableStateOf(NavTab.Analytics) }

    fun toggleTheme() {
        val newTheme = !isDarkTheme
        isDarkTheme = newTheme
        prefs.setDarkTheme(newTheme)
    }

    YKSAISinavKocuTheme(darkTheme = isDarkTheme) {
        Scaffold(
            bottomBar = {
                AnimatedBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    isDarkTheme = isDarkTheme
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            ) {
                Crossfade(
                    targetState = selectedTab,
                    animationSpec = tween(250),
                    label = "tab_crossfade"
                ) { tab ->
                    when (tab) {
                        NavTab.Scan -> ScanExamScreenContent(
                            isDarkTheme = isDarkTheme,
                            onNavigateToSettings = { selectedTab = NavTab.Settings },
                            onNavigateToAnalytics = { selectedTab = NavTab.Analytics }
                        )

                        NavTab.Analytics -> AnalyticsScreenContent(
                            isDarkTheme = isDarkTheme
                        )

                        NavTab.Exams -> ExamsListScreenContent(
                            isDarkTheme = isDarkTheme
                        )

                        NavTab.Coach -> AiCoachScreenContent(
                            isDarkTheme = isDarkTheme,
                            onAction = { action ->
                                when (action) {
                                    is AppAction.NavigateTab -> {
                                        selectedTab = when (action.tab.lowercase()) {
                                            "scan" -> NavTab.Scan
                                            "exams" -> NavTab.Exams
                                            "settings" -> NavTab.Settings
                                            "coach" -> NavTab.Coach
                                            else -> NavTab.Analytics
                                        }
                                    }
                                    is AppAction.OpenScanner -> selectedTab = NavTab.Scan
                                    is AppAction.NavigateExams -> selectedTab = NavTab.Exams
                                    is AppAction.SwitchTheme -> toggleTheme()
                                    is AppAction.NavigateSettings -> selectedTab = NavTab.Settings
                                    is AppAction.StudyPlan -> {
                                        // Stay in coach to discuss plan
                                    }
                                }
                            }
                        )

                        NavTab.Settings -> SettingsScreenContent(
                            isDarkTheme = isDarkTheme,
                            onThemeToggle = { toggleTheme() }
                        )
                    }
                }
            }
        }
    }
}
