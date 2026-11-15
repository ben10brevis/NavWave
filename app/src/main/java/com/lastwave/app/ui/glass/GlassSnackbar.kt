package com.lastwave.app.ui.glass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lastwave.app.ui.theme.LocalIsDarkTheme

/**
 * Clear Liquid Glass Floating Snackbar Pill.
 * Replaces Material 3 Snackbar.
 */
@Composable
fun GlassSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalIsDarkTheme.current
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clearLiquidGlass(
                shape = CircleShape,
                interactive = false,
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = snackbarData.visuals.message,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                modifier = Modifier.weight(1f),
            )

            val actionLabel = snackbarData.visuals.actionLabel
            if (actionLabel != null) {
                GlassButton(
                    text = actionLabel,
                    onClick = { snackbarData.performAction() },
                )
            }
        }
    }
}
