package com.lastwave.app.ui.glass

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lastwave.app.ui.theme.LocalIsDarkTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * Liquid droplet progress loader.
 * Replaces Material 3 CircularProgressIndicator with a fluid liquid droplet spinner.
 */
@Composable
fun GlassProgressLoader(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val isDark = LocalIsDarkTheme.current
    val transition = rememberInfiniteTransition(label = "liquidDropletLoader")

    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "dropletRotation",
    )

    val pulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dropletPulse",
    )

    Box(
        modifier = modifier
            .size(size)
            .clearLiquidGlass(
                shape = CircleShape,
                interactive = false,
                customBlurDp = 6.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .size(size * 0.70f)
                .graphicsLayer {
                    rotationZ = rotation
                    scaleX = pulse
                    scaleY = pulse
                }
        ) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.minDimension / 4f

            // Orbiting liquid droplet 1
            val d1X = center.x + radius * cos(0f)
            val d1Y = center.y + radius * sin(0f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = if (isDark) 0.85f else 0.95f),
                        color.copy(alpha = 0.35f),
                        Color.Transparent,
                    ),
                    center = Offset(d1X, d1Y),
                    radius = radius * 1.2f,
                ),
                radius = radius,
                center = Offset(d1X, d1Y),
                blendMode = if (isDark) BlendMode.Plus else BlendMode.SrcOver,
            )

            // Orbiting liquid droplet 2
            val d2X = center.x + radius * cos(Math.PI.toFloat())
            val d2Y = center.y + radius * sin(Math.PI.toFloat())
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDark) 0.75f else 0.85f),
                        Color.Transparent,
                    ),
                    center = Offset(d2X, d2Y),
                    radius = radius * 0.8f,
                ),
                radius = radius * 0.7f,
                center = Offset(d2X, d2Y),
                blendMode = if (isDark) BlendMode.Plus else BlendMode.SrcOver,
            )
        }
    }
}
