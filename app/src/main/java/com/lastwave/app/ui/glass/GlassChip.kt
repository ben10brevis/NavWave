package com.lastwave.app.ui.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lastwave.app.ui.theme.LocalIsDarkTheme

/**
 * Capsule Clear Liquid Glass Filter Chip.
 * Replaces Material 3 FilterChip, AssistChip, SuggestionChip.
 */
@Composable
fun GlassChip(
    selected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    label: @Composable () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val isDark = LocalIsDarkTheme.current
    val interactionState = rememberGlassInteractionState()

    val selectedBgColor by animateColorAsState(
        targetValue = if (selected) {
            if (isDark) Color.White.copy(alpha = 0.22f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(250),
        label = "chipSelectedBg",
    )

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 36.dp)
            .clearLiquidGlass(
                shape = shape,
                interactive = enabled,
                interactionState = if (enabled) interactionState else null,
            )
            .background(selectedBgColor, shape)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    onClick()
                },
            )
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            leadingIcon?.invoke()
            label()
        }
    }
}

@Composable
fun GlassChip(
    text: String,
    selected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val isDark = LocalIsDarkTheme.current
    val textColor = when {
        selected && isDark -> Color.White
        selected && !isDark -> MaterialTheme.colorScheme.primary
        isDark -> Color.White.copy(alpha = 0.85f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
    }

    GlassChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
        )
    }
}

@Composable
fun GlassChip(
    selected: Boolean = false,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
) = GlassChip(
    text = label,
    selected = selected,
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    leadingIcon = leadingIcon ?: icon?.let { ic ->
        {
            Icon(
                imageVector = ic,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (selected) Color.White else Color.White.copy(alpha = 0.85f),
            )
        }
    },
)
