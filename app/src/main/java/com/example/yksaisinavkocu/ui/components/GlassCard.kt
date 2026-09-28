package com.example.yksaisinavkocu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.yksaisinavkocu.theme.*

/**
 * Directional shadow modifier that casts a shadow towards the bottom-left
 * (negative X, positive Y offset) to simulate light originating from the top-right.
 */
fun Modifier.bottomLeftShadow(
    cornerRadius: Dp = 24.dp,
    shadowColor: Color = Color.Black.copy(alpha = 0.08f),
    offsetX: Dp = (-3).dp,
    offsetY: Dp = 4.dp,
    blurRadius: Dp = 8.dp
): Modifier = this.drawBehind {
    if (shadowColor.alpha <= 0.005f) return@drawBehind
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = shadowColor.toArgb()
        if (blurRadius > 0.dp) {
            frameworkPaint.maskFilter = android.graphics.BlurMaskFilter(
                blurRadius.toPx(),
                android.graphics.BlurMaskFilter.Blur.NORMAL
            )
        }
        val left = offsetX.toPx()
        val top = offsetY.toPx()
        val right = size.width + offsetX.toPx()
        val bottom = size.height + offsetY.toPx()
        val radiusPx = cornerRadius.toPx()
        canvas.drawRoundRect(
            left = left,
            top = top,
            right = right,
            bottom = bottom,
            radiusX = radiusPx,
            radiusY = radiusPx,
            paint = paint
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    alpha: Float = AeroBlockAlpha,
    borderAlpha: Float = 0.85f,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val isDarkTheme = MaterialTheme.colorScheme.background == AeroDarkBg

    val targetAlpha = if (alpha < 0.5f || alpha == 0.95f) AeroBlockAlpha else alpha

    val containerColor = if (isDarkTheme) {
        AeroDarkCard.copy(alpha = if (alpha < 0.5f) 0.82f else alpha)
    } else {
        AeroMatteLightGrey.copy(alpha = targetAlpha)
    }

    val borderColor = if (isDarkTheme) {
        Color.White.copy(alpha = if (borderAlpha > 0.5f) 0.15f else borderAlpha)
    } else {
        AeroMatteBorderLight.copy(alpha = if (borderAlpha < 0.5f) 0.85f else borderAlpha)
    }

    val shadowElevation = if (isDarkTheme) 6.dp else 3.dp
    val shadowAmbient = if (isDarkTheme) AeroSkyBlue.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.03f)
    val shadowSpot = if (isDarkTheme) AeroSkyBlue.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.02f)
    val directionalShadowColor = if (isDarkTheme) {
        Color.Black.copy(alpha = 0.40f)
    } else {
        Color(0x18001F2E)
    }

    Card(
        modifier = modifier
            .bottomLeftShadow(
                cornerRadius = cornerRadius,
                shadowColor = directionalShadowColor,
                offsetX = (-3).dp,
                offsetY = 4.dp,
                blurRadius = 8.dp
            )
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = shadowAmbient,
                spotColor = shadowSpot
            )
            .clip(shape)
            .background(
                color = containerColor,
                shape = shape
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    alpha: Float = AeroBlockAlpha,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val isDarkTheme = MaterialTheme.colorScheme.background == AeroDarkBg

    val containerColor = if (isDarkTheme) {
        AeroDarkCard.copy(alpha = 0.82f)
    } else {
        AeroMatteLightGrey.copy(alpha = AeroBlockAlpha)
    }
    val borderColor = if (isDarkTheme) Color.White.copy(alpha = 0.2f) else AeroMatteBorderLight.copy(alpha = 0.85f)
    val directionalShadowColor = if (isDarkTheme) {
        Color.Black.copy(alpha = 0.32f)
    } else {
        Color(0x14001F2E)
    }

    Box(
        modifier = modifier
            .bottomLeftShadow(
                cornerRadius = cornerRadius,
                shadowColor = directionalShadowColor,
                offsetX = (-2.5).dp,
                offsetY = 3.5.dp,
                blurRadius = 6.dp
            )
            .clip(shape)
            .background(
                color = containerColor,
                shape = shape
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            ),
        content = content
    )
}
