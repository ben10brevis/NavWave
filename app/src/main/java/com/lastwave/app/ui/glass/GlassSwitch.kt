package com.lastwave.app.ui.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.lastwave.app.ui.theme.LocalIsDarkTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Capsule Clear Liquid Glass Switch.
 * Sliding liquid glass droplet thumb with overshoot spring physics.
 * Replaces Material 3 Switch.
 */
@Composable
fun GlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val isDark = LocalIsDarkTheme.current

    val springSpec = spring<Float>(dampingRatio = 0.55f, stiffness = 380f)
    val thumbOffsetAnim = remember { Animatable(if (checked) 1f else 0f) }
    val stretchXAnim = remember { Animatable(0f) }

    LaunchedEffect(checked) {
        scope.launch {
            stretchXAnim.animateTo(0.12f, tween(80))
            stretchXAnim.animateTo(0f, springSpec)
        }
        thumbOffsetAnim.animateTo(if (checked) 1f else 0f, springSpec)
    }

    val activeTrackColor = if (isDark) {
        Color.White.copy(alpha = 0.25f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    }

    val trackBgColor by animateColorAsState(
        targetValue = if (checked) activeTrackColor else Color.Transparent,
        animationSpec = tween(200),
        label = "switchTrackColor",
    )

    Box(
        modifier = modifier
            .size(width = 52.dp, height = 32.dp)
            .clearLiquidGlass(
                shape = CircleShape,
                interactive = enabled,
            )
            .background(trackBgColor, CircleShape)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    onCheckedChange(!checked)
                },
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        val maxOffsetPx = 20.dp
        val currentOffsetPx = (maxOffsetPx.value * thumbOffsetAnim.value).dp

        // Sliding liquid droplet thumb
        Box(
            modifier = Modifier
                .offset(x = 4.dp + currentOffsetPx, y = 0.dp)
                .size(24.dp)
                .graphicsLayer {
                    scaleX = 1f + stretchXAnim.value
                    scaleY = 1f - stretchXAnim.value * 0.5f
                }
                .clearLiquidGlass(
                    shape = CircleShape,
                    interactive = false,
                    customBlurDp = 6.dp,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color.White.copy(alpha = if (isDark) 0.70f else 0.90f),
                                Color.Transparent,
                            )
                        )
                    )
            )
        }
    }
}
