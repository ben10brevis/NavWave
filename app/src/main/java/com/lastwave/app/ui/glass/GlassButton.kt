package com.lastwave.app.ui.glass

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lastwave.app.ui.theme.LocalIsDarkTheme

enum class GlassButtonVariant {
    FILLED,
    TONAL,
    OUTLINED,
    TEXT,
    Primary,
    Secondary,
    Ghost,
}

/**
 * Capsule Clear Liquid Glass Button.
 * Replaces Material 3 FilledButton, FilledTonalButton, OutlinedButton.
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    variant: GlassButtonVariant = GlassButtonVariant.FILLED,
    tintColor: Color? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val interactionState = rememberGlassInteractionState()
    val isDark = LocalIsDarkTheme.current
    val effectiveTint = tintColor ?: when (variant) {
        GlassButtonVariant.FILLED, GlassButtonVariant.Primary -> MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
        GlassButtonVariant.TONAL, GlassButtonVariant.Secondary -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
        GlassButtonVariant.OUTLINED, GlassButtonVariant.TEXT, GlassButtonVariant.Ghost -> null
    }

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 64.dp, minHeight = 44.dp)
            .clearLiquidGlass(
                shape = shape,
                interactive = enabled,
                interactionState = if (enabled) interactionState else null,
                tintColor = effectiveTint,
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    onClick()
                },
            )
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            leadingIcon?.invoke()
            content()
            trailingIcon?.invoke()
        }
    }
}

/**
 * Convenience text overload for GlassButton.
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    variant: GlassButtonVariant = GlassButtonVariant.FILLED,
    tintColor: Color? = null,
    icon: ImageVector? = null,
    leadingIcon: (@Composable () -> Unit)? = icon?.let { vec ->
        {
            Icon(
                imageVector = vec,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }
    },
) {
    val isDark = LocalIsDarkTheme.current
    GlassButton(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        enabled = enabled,
        variant = variant,
        tintColor = tintColor,
        leadingIcon = leadingIcon,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Circular Clear Liquid Glass Icon Button with 48dp minimum touch target.
 */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    tint: Color = if (LocalIsDarkTheme.current) Color.White else MaterialTheme.colorScheme.onSurface,
    contentDescription: String? = null,
    imageVector: ImageVector? = null,
    icon: ImageVector? = imageVector,
    painter: Painter? = null,
) {
    val haptic = LocalHapticFeedback.current
    val interactionState = rememberGlassInteractionState()
    val effectiveVector = icon ?: imageVector

    Box(
        modifier = modifier
            .size(size)
            .clearLiquidGlass(
                shape = shape,
                interactive = enabled,
                interactionState = if (enabled) interactionState else null,
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (effectiveVector != null) {
            Icon(
                imageVector = effectiveVector,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(iconSize),
            )
        } else if (painter != null) {
            Icon(
                painter = painter,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

/** Convenience overload where icon/imageVector is passed as a positional or named parameter */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    tint: Color = if (LocalIsDarkTheme.current) Color.White else MaterialTheme.colorScheme.onSurface,
) {
    GlassIconButton(
        onClick = onClick,
        modifier = modifier,
        size = size,
        iconSize = iconSize,
        shape = shape,
        enabled = enabled,
        tint = tint,
        contentDescription = contentDescription,
        imageVector = icon,
    )
}
