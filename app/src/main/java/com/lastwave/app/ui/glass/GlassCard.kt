package com.lastwave.app.ui.glass

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Continuous-corner (28dp squircle) Clear Liquid Glass Card.
 * Replaces Material 3 Card, OutlinedCard, ElevatedCard, and LiquidGlassCard.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    contentPadding: Dp = 16.dp,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val interactionState = rememberGlassInteractionState()

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            enabled = enabled,
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                onClick()
            },
        )
    } else Modifier

    Box(
        modifier = modifier
            .clearLiquidGlass(
                shape = shape,
                interactive = enabled && onClick != null,
                interactionState = if (enabled && onClick != null) interactionState else null,
            )
            .then(clickModifier)
            .padding(contentPadding),
    ) {
        Column(content = content)
    }
}
