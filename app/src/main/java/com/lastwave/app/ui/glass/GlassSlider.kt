package com.lastwave.app.ui.glass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.lastwave.app.ui.theme.LocalIsDarkTheme
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Clear Liquid Glass Slider.
 * The thumb is a small glass lens that magnifies the track and stretches along the axis
 * up to 8% while dragged. Produces haptic ticks at 25%, 50%, and 75% milestones.
 * Used for Seekbar, Volume, 15-band EQ bands, and Tempo/Pitch.
 */
@Composable
fun GlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    trackHeight: Dp = 6.dp,
    thumbSize: Dp = 22.dp,
    activeColor: Color = if (LocalIsDarkTheme.current) Color.White else MaterialTheme.colorScheme.primary,
    inactiveColor: Color = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.20f) else Color.Black.copy(alpha = 0.15f),
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val isDark = LocalIsDarkTheme.current

    val rangeSpan = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.0001f)
    val normalizedFraction = ((value - valueRange.start) / rangeSpan).coerceIn(0f, 1f)

    var isDragging by remember { mutableStateOf(false) }
    val stretchXAnim = remember { Animatable(0f) }
    val thumbScaleAnim = remember { Animatable(1f) }
    val springSpec = spring<Float>(dampingRatio = 0.55f, stiffness = 380f)

    // Tracks 25%, 50%, 75% milestone crossings for haptic feedback
    var lastMilestone by remember { mutableIntStateOf(-1) }

    fun checkHapticMilestone(frac: Float) {
        val currentMilestone = when {
            frac >= 0.74f && frac <= 0.76f -> 3
            frac >= 0.49f && frac <= 0.51f -> 2
            frac >= 0.24f && frac <= 0.26f -> 1
            else -> 0
        }
        if (currentMilestone > 0 && currentMilestone != lastMilestone) {
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            lastMilestone = currentMilestone
        } else if (currentMilestone == 0) {
            lastMilestone = -1
        }
    }

    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnFinished by rememberUpdatedState(onValueChangeFinished)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbSize + 16.dp)
            .pointerInput(enabled, rangeSpan) {
                if (!enabled) return@pointerInput
                detectTapGestures { tapOffset ->
                    val width = size.width.toFloat()
                    val newFrac = (tapOffset.x / width).coerceIn(0f, 1f)
                    val newValue = valueRange.start + newFrac * rangeSpan
                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    currentOnValueChange(newValue)
                    currentOnFinished?.invoke()
                }
            }
            .pointerInput(enabled, rangeSpan) {
                if (!enabled) return@pointerInput
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        scope.launch { thumbScaleAnim.animateTo(1.15f, springSpec) }
                    },
                    onDragEnd = {
                        isDragging = false
                        scope.launch {
                            thumbScaleAnim.animateTo(1f, springSpec)
                            stretchXAnim.animateTo(0f, springSpec)
                        }
                        currentOnFinished?.invoke()
                    },
                    onDragCancel = {
                        isDragging = false
                        scope.launch {
                            thumbScaleAnim.animateTo(1f, springSpec)
                            stretchXAnim.animateTo(0f, springSpec)
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val width = size.width.toFloat()
                        val currentX = normalizedFraction * width
                        val newX = (currentX + dragAmount.x).coerceIn(0f, width)
                        val newFrac = (newX / width).coerceIn(0f, 1f)
                        checkHapticMilestone(newFrac)

                        // Liquid horizontal stretch (up to 8%)
                        val stretch = (dragAmount.x / 120f).coerceIn(-0.08f, 0.08f)
                        scope.launch { stretchXAnim.animateTo(stretch, springSpec) }

                        val newValue = valueRange.start + newFrac * rangeSpan
                        currentOnValueChange(newValue)
                    },
                )
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        val totalWidthPx = constraints.maxWidth.toFloat()
        val density = LocalDensity.current
        val thumbRadiusPx = with(density) { thumbSize.toPx() / 2f }
        val trackHeightPx = with(density) { trackHeight.toPx() }

        // Draw track
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
        ) {
            val corner = CornerRadius(trackHeightPx / 2f, trackHeightPx / 2f)
            // Inactive track
            drawRoundRect(
                color = inactiveColor,
                size = Size(totalWidthPx, trackHeightPx),
                cornerRadius = corner,
            )
            // Active track
            val activeWidth = (totalWidthPx * normalizedFraction).coerceIn(0f, totalWidthPx)
            drawRoundRect(
                color = activeColor,
                size = Size(activeWidth, trackHeightPx),
                cornerRadius = corner,
            )
        }

        // Lens Thumb (Liquid Glass Magnifier)
        val thumbOffsetPx = (totalWidthPx * normalizedFraction - thumbRadiusPx).coerceIn(0f, totalWidthPx - thumbRadiusPx * 2f)

        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffsetPx.roundToInt(), 0) }
                .size(thumbSize)
                .graphicsLayer {
                    scaleX = (thumbScaleAnim.value + abs(stretchXAnim.value)).coerceIn(0.9f, 1.35f)
                    scaleY = (thumbScaleAnim.value - abs(stretchXAnim.value) * 0.5f).coerceIn(0.9f, 1.35f)
                }
                .clearLiquidGlass(
                    shape = CircleShape,
                    interactive = false,
                    customBlurDp = 8.dp,
                ),
            contentAlignment = Alignment.Center,
        ) {
            // Lens center specular glint
            Box(
                modifier = Modifier
                    .size(thumbSize * 0.45f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color.White.copy(alpha = if (isDark) 0.65f else 0.85f),
                                Color.Transparent,
                            )
                        )
                    )
            )
        }
    }
}
