package com.lastwave.app.ui.glass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lastwave.app.ui.theme.LocalIsDarkTheme
import kotlin.math.roundToInt

data class GlassTabItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

/**
 * Floating Clear Liquid Glass Bottom Tab Bar.
 * Features a liquid glass selection bubble that morphs and slides
 * between tabs with gentle overshoot spring physics.
 */
@Composable
fun GlassBottomTabBar(
    items: List<GlassTabItem>,
    selectedIndex: Int,
    onSelectTab: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    val haptic = LocalHapticFeedback.current
    val isDark = LocalIsDarkTheme.current
    val springSpec = spring<Float>(dampingRatio = 0.55f, stiffness = 380f)
    val bubblePosition = remember { Animatable(selectedIndex.toFloat()) }

    LaunchedEffect(selectedIndex) {
        bubblePosition.animateTo(selectedIndex.toFloat(), springSpec)
    }

    BoxWithConstraints(
        modifier = modifier
            .height(64.dp)
            .clearLiquidGlass(
                shape = CircleShape,
                interactive = false,
                customBlurDp = 14.dp,
            )
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        val count = items.size
        val tabWidthPx = (constraints.maxWidth.toFloat() - 8f) / count
        val tabWidthDp = (maxWidth - 8.dp) / count

        // Liquid Selection Bubble with dynamic liquid stretch
        val delta = if (bubblePosition.isRunning) {
            bubblePosition.targetValue - bubblePosition.value
        } else 0f
        val stretchX = 1f + (kotlin.math.abs(delta) * 0.12f).coerceAtMost(0.18f)
        val squashY = 1f - (kotlin.math.abs(delta) * 0.06f).coerceAtMost(0.09f)

        Box(
            modifier = Modifier
                .offset { IntOffset((bubblePosition.value * tabWidthPx).roundToInt(), 0) }
                .width(tabWidthDp)
                .fillMaxHeight()
                .graphicsLayer {
                    scaleX = stretchX
                    scaleY = squashY
                }
                .clearLiquidGlass(
                    shape = CircleShape,
                    interactive = false,
                    customBlurDp = 8.dp,
                )
                .background(
                    if (isDark) Color.White.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.08f),
                    CircleShape,
                )
        )

        // Tab Items
        Row(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val tint = when {
                    isSelected && isDark -> Color.White
                    isSelected && !isDark -> MaterialTheme.colorScheme.primary
                    isDark -> Color.White.copy(alpha = 0.60f)
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f)
                }

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
                                    onSelectTab(index)
                                }
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title,
                            tint = tint,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = tint,
                        )
                    }
                }
            }
        }
    }
}
