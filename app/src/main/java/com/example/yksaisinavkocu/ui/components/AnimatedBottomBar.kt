package com.example.yksaisinavkocu.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yksaisinavkocu.theme.*

enum class NavTab(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    Scan("scan", "Tara", Icons.Rounded.DocumentScanner),
    Analytics("analytics", "Analiz", Icons.Rounded.Analytics),
    Exams("exams", "Sınavlar", Icons.Rounded.ListAlt),
    Coach("coach", "AI Koç", Icons.Rounded.Psychology),
    Settings("settings", "Ayarlar", Icons.Rounded.Settings)
}

@Composable
fun AnimatedBottomBar(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val barShape = RoundedCornerShape(28.dp)
    val barBackground = if (isDarkTheme) {
        Color(0xCC132F4C)
    } else {
        AeroMatteLightGrey.copy(alpha = AeroBlockAlpha)
    }
    val barBorder = if (isDarkTheme) {
        Color(0x334FC3F7)
    } else {
        AeroMatteBorderLight.copy(alpha = 0.85f)
    }
    val barShadowColor = if (isDarkTheme) {
        Color.Black.copy(alpha = 0.40f)
    } else {
        Color(0x18001F2E)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .bottomLeftShadow(
                    cornerRadius = 28.dp,
                    shadowColor = barShadowColor,
                    offsetX = (-3).dp,
                    offsetY = 4.dp,
                    blurRadius = 10.dp
                )
                .shadow(
                    elevation = 12.dp,
                    shape = barShape,
                    ambientColor = AeroSkyBlue.copy(alpha = 0.12f),
                    spotColor = AeroDeepBlue.copy(alpha = 0.16f)
                )
                .clip(barShape)
                .background(barBackground)
                .border(1.dp, barBorder, barShape)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                val interactionSource = remember { MutableInteractionSource() }

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.08f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "tab_scale"
                )

                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    label = "tab_color"
                )

                val iconShadowColor = if (isSelected) {
                    Color.Black.copy(alpha = 0.35f)
                } else if (isDarkTheme) {
                    Color.Black.copy(alpha = 0.45f)
                } else {
                    Color.Black.copy(alpha = 0.15f)
                }

                val pillBackground = if (isSelected) {
                    Brush.linearGradient(
                        colors = listOf(AeroSkyBlue, AeroDeepBlue)
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(Color.Transparent, Color.Transparent)
                    )
                }

                Box(
                    modifier = Modifier
                        .scale(scale)
                        .clip(RoundedCornerShape(20.dp))
                        .background(pillBackground)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            onTabSelected(tab)
                        }
                        .padding(horizontal = if (isSelected) 14.dp else 10.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.size(22.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Underlaid shadow facing left and bottom (dx = -1.5.dp, dy = 1.5.dp)
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                tint = iconShadowColor,
                                modifier = Modifier
                                    .size(22.dp)
                                    .offset(x = (-1.5).dp, y = 1.5.dp)
                            )
                            // Foreground icon
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = contentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab.title,
                                color = contentColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
