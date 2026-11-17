package com.lastwave.app.ui.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.lastwave.app.ui.theme.LocalIsDarkTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DynamicMeshBackground(
    artworkColors: List<Color>,
    modifier: Modifier = Modifier,
    isPureBlack: Boolean = false,
    content: @Composable () -> Unit = {},
) {
    val primary = artworkColors.getOrNull(0) ?: MaterialTheme.colorScheme.primary
    val secondary = artworkColors.getOrNull(1) ?: MaterialTheme.colorScheme.tertiary
    val accent = artworkColors.getOrNull(2) ?: MaterialTheme.colorScheme.secondary
    DynamicMeshBackground(
        modifier = modifier,
        primaryColor = primary,
        secondaryColor = secondary,
        accentColor = accent,
        isPureBlack = isPureBlack,
        content = content,
    )
}

/**
 * Animated dynamic mesh gradient background.
 * Drifts 3-4 blurred color blobs (20-30s loops) extracted from artwork
 * or Monet color scheme over a rich dark/light base.
 * Ensures glass surfaces always have vibrant colors to refract and magnify.
 */
@Composable
fun DynamicMeshBackground(
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiary,
    accentColor: Color = MaterialTheme.colorScheme.secondary,
    isPureBlack: Boolean = false,
    content: @Composable () -> Unit = {},
) {
    val isDark = LocalIsDarkTheme.current

    // Smooth 600ms crossfade between colors when tracks change
    val animatedPrimary by animateColorAsState(primaryColor, tween(600), label = "meshPrimary")
    val animatedSecondary by animateColorAsState(secondaryColor, tween(600), label = "meshSecondary")
    val animatedAccent by animateColorAsState(accentColor, tween(600), label = "meshAccent")

    // Slow drifting animations (24s, 28s, 32s loops)
    val transition1 = rememberInfiniteTransition(label = "blob1")
    val transition2 = rememberInfiniteTransition(label = "blob2")
    val transition3 = rememberInfiniteTransition(label = "blob3")

    val t1 by transition1.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(24_000, easing = LinearEasing), RepeatMode.Restart),
        label = "t1",
    )
    val t2 by transition2.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(28_000, easing = LinearEasing), RepeatMode.Restart),
        label = "t2",
    )
    val t3 by transition3.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(32_000, easing = LinearEasing), RepeatMode.Restart),
        label = "t3",
    )

    // Base surface color
    val baseColor = when {
        isPureBlack -> Color(0xFF000000)
        isDark -> Color(0xFF0C0C0E)
        else -> Color(0xFFF6F6F9)
    }

    val blobAlphaMultiplier = when {
        isPureBlack -> 0.22f // Dimmer in Pure Black AMOLED
        isDark -> 0.45f
        else -> 0.28f
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(baseColor)

            val w = size.width
            val h = size.height

            // Blob 1: Upper-left drifting orbit
            val b1X = w * (0.35f + 0.22f * cos(t1))
            val b1Y = h * (0.28f + 0.18f * sin(t1 * 1.2f))
            val r1 = (w * 0.75f).coerceAtLeast(300f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedPrimary.copy(alpha = 0.85f * blobAlphaMultiplier),
                        animatedPrimary.copy(alpha = 0.35f * blobAlphaMultiplier),
                        Color.Transparent,
                    ),
                    center = Offset(b1X, b1Y),
                    radius = r1,
                ),
                radius = r1,
                center = Offset(b1X, b1Y),
                blendMode = BlendMode.SrcOver,
            )

            // Blob 2: Lower-right counter-drifting orbit
            val b2X = w * (0.68f + 0.20f * cos(t2 + 1.5f))
            val b2Y = h * (0.65f + 0.22f * sin(t2 * 0.9f))
            val r2 = (w * 0.85f).coerceAtLeast(320f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedSecondary.copy(alpha = 0.75f * blobAlphaMultiplier),
                        animatedSecondary.copy(alpha = 0.25f * blobAlphaMultiplier),
                        Color.Transparent,
                    ),
                    center = Offset(b2X, b2Y),
                    radius = r2,
                ),
                radius = r2,
                center = Offset(b2X, b2Y),
                blendMode = BlendMode.SrcOver,
            )

            // Blob 3: Center-floating accent
            val b3X = w * (0.50f + 0.25f * sin(t3))
            val b3Y = h * (0.45f + 0.16f * cos(t3 * 1.1f))
            val r3 = (w * 0.65f).coerceAtLeast(260f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedAccent.copy(alpha = 0.65f * blobAlphaMultiplier),
                        Color.Transparent,
                    ),
                    center = Offset(b3X, b3Y),
                    radius = r3,
                ),
                radius = r3,
                center = Offset(b3X, b3Y),
                blendMode = BlendMode.SrcOver,
            )
        }

        content()
    }
}
