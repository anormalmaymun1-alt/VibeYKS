package com.example.yksaisinavkocu.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AeroLightColorScheme = lightColorScheme(
    primary = AeroDeepBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB3E5FC),
    onPrimaryContainer = Color(0xFF001F2A),
    secondary = AeroLeafGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8E6C9),
    onSecondaryContainer = Color(0xFF002106),
    tertiary = AeroPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE8DEF8),
    onTertiaryContainer = Color(0xFF1D192B),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = AeroLightBg,
    onBackground = TextPrimaryLight,
    surface = AeroMatteLightGrey.copy(alpha = AeroBlockAlpha),
    onSurface = TextPrimaryLight,
    surfaceVariant = AeroMatteLightGreySecondary.copy(alpha = AeroBlockAlpha),
    onSurfaceVariant = TextSecondaryLight,
    outline = AeroMatteBorderLight.copy(alpha = 0.85f),
    outlineVariant = Color(0xFFB0BEC5),
    inverseSurface = AeroDarkSurface,
    inverseOnSurface = TextPrimaryDark,
)

private val AeroDarkColorScheme = darkColorScheme(
    primary = AeroSkyBlue,
    onPrimary = Color(0xFF003547),
    primaryContainer = Color(0xFF004D65),
    onPrimaryContainer = Color(0xFFB3E5FC),
    secondary = AeroMint,
    onSecondary = Color(0xFF003822),
    secondaryContainer = Color(0xFF005234),
    onSecondaryContainer = Color(0xFFA5D6A7),
    tertiary = Color(0xFFBB86FC),
    onTertiary = Color(0xFF332D41),
    tertiaryContainer = Color(0xFF4A4458),
    onTertiaryContainer = Color(0xFFE8DEF8),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = AeroDarkBg,
    onBackground = TextPrimaryDark,
    surface = AeroDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = AeroDarkCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF546E7A),
    outlineVariant = Color(0xFF37474F),
    inverseSurface = Color(0xFFE3F2FD),
    inverseOnSurface = TextPrimaryLight,
)

@Composable
fun YKSAISinavKocuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AeroDarkColorScheme else AeroLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AeroTypography,
        content = content
    )
}
