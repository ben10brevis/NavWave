package com.lastwave.app.ui.glass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lastwave.app.ui.theme.LocalIsDarkTheme
import kotlin.math.roundToInt

/**
 * Capsule Clear Liquid Glass Segmented Control.
 * Houses a sliding liquid selection bubble with overshoot spring physics.
 */
@Composable
fun GlassSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    val haptic = LocalHapticFeedback.current
    val isDark = LocalIsDarkTheme.current
    val springSpec = spring<Float>(dampingRatio = 0.55f, stiffness = 380f)
    val indicatorPosition = remember { Animatable(selectedIndex.toFloat()) }

    LaunchedEffect(selectedIndex) {
        indicatorPosition.animateTo(selectedIndex.toFloat(), springSpec)
    }

    BoxWithConstraints(
        modifier = modifier
            .height(44.dp)
            .clearLiquidGlass(
                shape = CircleShape,
                interactive = false,
            )
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        val segmentWidthPx = (constraints.maxWidth.toFloat() - 8f) / items.size
        val segmentWidthDp = (maxWidth - 8.dp) / items.size

        // Sliding liquid selection bubble
        Box(
            modifier = Modifier
                .offset { IntOffset((indicatorPosition.value * segmentWidthPx).roundToInt(), 0) }
                .width(segmentWidthDp)
                .fillMaxHeight()
                .clearLiquidGlass(
                    shape = CircleShape,
                    interactive = false,
                    customBlurDp = 6.dp,
                )
                .background(
                    if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f),
                    CircleShape,
                )
        )

        // Labels
        Row(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (index != selectedIndex) {
                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    onSelect(index)
                                }
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = item,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = when {
                            isSelected && isDark -> Color.White
                            isSelected && !isDark -> MaterialTheme.colorScheme.primary
                            isDark -> Color.White.copy(alpha = 0.65f)
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        },
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
