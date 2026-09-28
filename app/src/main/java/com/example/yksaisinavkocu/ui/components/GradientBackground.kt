package com.example.yksaisinavkocu.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.yksaisinavkocu.theme.*

@Composable
fun AeroGradientBackground(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )

    val gradientColors = if (isDarkTheme) {
        listOf(AeroDarkBg, AeroDarkSurface, Color(0xFF1A3A5C))
    } else {
        // -8% parlaklık: #E3F2FD -> #D1DFE9, #B3E5FC -> #A5D3E8, #E8F5E9 -> #D5E1D6
        listOf(Color(0xFFD1DFE9), Color(0xFFA5D3E8), Color(0xFFD5E1D6))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(animatedOffset * 500f, 0f),
                    end = Offset(500f + animatedOffset * 200f, 1500f)
                )
            )
    ) {
        // Dekoratif baloncuklar (açık modda) / 4 köşeli Frutiger Aero yıldızları (karanlık modda)
        BubbleDecorations(isDarkTheme = isDarkTheme, animatedOffset = animatedOffset)
        content()
    }
}

@Composable
private fun BubbleDecorations(isDarkTheme: Boolean, animatedOffset: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val elements = listOf(
            Triple(Offset(w * 0.15f, h * 0.1f + animatedOffset * 30f), 160f + animatedOffset * 35f, 1.0f),
            Triple(Offset(w * 0.85f, h * 0.35f - animatedOffset * 20f), 115f + animatedOffset * 22f, 0.85f),
            Triple(Offset(w * 0.28f, h * 0.7f + animatedOffset * 25f), 80f + animatedOffset * 15f, 0.70f),
            Triple(Offset(w * 0.78f, h * 0.8f - animatedOffset * 15f), 60f + animatedOffset * 10f, 0.75f),
            Triple(Offset(w * 0.65f, h * 0.14f - animatedOffset * 18f), 48f + animatedOffset * 8f, 0.65f)
        )

        elements.forEach { (center, radius, scaleAlpha) ->
            if (isDarkTheme) {
                drawRealisticFourEdgedStar(
                    center = center,
                    radius = radius * 0.92f,
                    scaleAlpha = scaleAlpha
                )
            } else {
                drawRealisticBubble(
                    center = center,
                    radius = radius,
                    scaleAlpha = scaleAlpha
                )
            }
        }
    }
}

/**
 * Renders a realistic 3D bubble in Light Mode with:
 * 1. Fresnel edge glass body (transparent interior, glowing perimeter)
 * 2. Perimeter rim stroke
 * 3. Caustic internal refraction (bottom-left inner rim reflection)
 * 4. Top-right specular gloss oval highlight
 * 5. Secondary pinpoint sparkle highlight
 */
private fun DrawScope.drawRealisticBubble(
    center: Offset,
    radius: Float,
    scaleAlpha: Float = 1.0f
) {
    if (radius <= 0f) return

    // 1. Spherical Volume Body (Fresnel glass effect: transparent center, glowing rim)
    val bodyEdgeColor = Color.White.copy(alpha = 0.45f * scaleAlpha)
    val bodyCenterColor = Color.White.copy(alpha = 0.04f * scaleAlpha)

    drawCircle(
        brush = Brush.radialGradient(
            0.0f to bodyCenterColor,
            0.70f to bodyCenterColor,
            0.95f to bodyEdgeColor,
            1.0f to bodyEdgeColor.copy(alpha = bodyEdgeColor.alpha * 0.8f),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )

    // 2. Perimeter Rim
    val rimColor = Color.White.copy(alpha = 0.60f * scaleAlpha)
    val rimStrokeWidth = (radius * 0.022f).coerceIn(1.2f, 2.5f)
    drawCircle(
        color = rimColor,
        radius = radius,
        center = center,
        style = Stroke(width = rimStrokeWidth)
    )

    // 3. Caustic Internal Refraction (subtle glowing light rim on bottom-left)
    val causticCenter = center + Offset(-radius * 0.45f, radius * 0.45f)
    val causticColor = Color.White.copy(alpha = 0.35f * scaleAlpha)
    drawCircle(
        brush = Brush.radialGradient(
            0.0f to causticColor,
            0.50f to causticColor.copy(alpha = 0.20f),
            1.0f to Color.Transparent,
            center = causticCenter,
            radius = radius * 0.45f
        ),
        radius = radius,
        center = center
    )

    // 4. Primary Specular Gloss Highlight (faces top-right: dx > 0, dy < 0)
    val hlCenter = center + Offset(radius * 0.38f, -radius * 0.38f)
    val hlWidth = radius * 0.45f
    val hlHeight = radius * 0.22f
    val hlAlpha = 0.75f * scaleAlpha

    withTransform({
        rotate(degrees = -45f, pivot = hlCenter)
    }) {
        drawOval(
            brush = Brush.radialGradient(
                0.0f to Color.White.copy(alpha = hlAlpha),
                0.55f to Color.White.copy(alpha = hlAlpha * 0.45f),
                1.0f to Color.Transparent,
                center = hlCenter,
                radius = hlWidth * 0.5f
            ),
            topLeft = Offset(hlCenter.x - hlWidth * 0.5f, hlCenter.y - hlHeight * 0.5f),
            size = Size(hlWidth, hlHeight)
        )
    }

    // 5. Secondary Pinpoint Specular Highlight (smaller sparkle near upper-right rim)
    val pinCenter = center + Offset(radius * 0.58f, -radius * 0.18f)
    val pinRadius = (radius * 0.065f).coerceIn(2.5f, 6.0f)
    val pinAlpha = 0.80f * scaleAlpha
    drawCircle(
        color = Color.White.copy(alpha = pinAlpha),
        radius = pinRadius,
        center = pinCenter
    )
}

/**
 * Creates an elegant 4-edged star path with concave sweeping curves.
 */
private fun createFourEdgedStarPath(center: Offset, radius: Float): Path {
    val path = Path()
    val cx = center.x
    val cy = center.y
    val r = radius
    val indent = r * 0.18f

    path.moveTo(cx, cy - r)
    path.quadraticTo(cx + indent, cy - indent, cx + r, cy)
    path.quadraticTo(cx + indent, cy + indent, cx, cy + r)
    path.quadraticTo(cx - indent, cy + indent, cx - r, cy)
    path.quadraticTo(cx - indent, cy - indent, cx, cy - r)
    path.close()
    return path
}

/**
 * Renders a 4-edged star on Dark Mode:
 * - Completely shine-free (no specular gloss highlights or sparkle glints)
 * - Blended 20% deeper into the background for a soft, ambient aesthetic
 */
private fun DrawScope.drawRealisticFourEdgedStar(
    center: Offset,
    radius: Float,
    scaleAlpha: Float = 1.0f
) {
    if (radius <= 0f) return

    // 1. Soft Ambient Halo Bloom (blended 20% deeper into background)
    drawCircle(
        brush = Brush.radialGradient(
            0.0f to Color(0x184FC3F7).copy(alpha = 0.12f * scaleAlpha),
            0.65f to Color(0x064FC3F7).copy(alpha = 0.04f * scaleAlpha),
            1.0f to Color.Transparent,
            center = center,
            radius = radius * 1.2f
        ),
        radius = radius * 1.2f,
        center = center
    )

    val starPath = createFourEdgedStarPath(center, radius)

    // 2. Star Volume Body (subtle, soft matte gradient - 20% blended)
    drawPath(
        path = starPath,
        brush = Brush.radialGradient(
            0.0f to Color(0x084FC3F7).copy(alpha = 0.045f * scaleAlpha),
            0.60f to Color(0x184FC3F7).copy(alpha = 0.12f * scaleAlpha),
            0.90f to Color(0x2A80D8FF).copy(alpha = 0.20f * scaleAlpha),
            1.0f to Color(0x3880D8FF).copy(alpha = 0.24f * scaleAlpha),
            center = center,
            radius = radius
        )
    )

    // 3. Delicate Perimeter Rim (soft outline - 20% blended, non-reflective)
    val rimColor = Color(0x4580D8FF).copy(alpha = 0.25f * scaleAlpha)
    val rimStrokeWidth = (radius * 0.018f).coerceIn(1.0f, 2.0f)
    drawPath(
        path = starPath,
        color = rimColor,
        style = Stroke(width = rimStrokeWidth)
    )
}
