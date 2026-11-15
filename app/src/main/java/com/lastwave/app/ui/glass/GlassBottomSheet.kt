package com.lastwave.app.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lastwave.app.ui.theme.LocalIsDarkTheme

/**
 * Clear Liquid Glass Bottom Sheet.
 * Continuous 32dp squircle top corners, 28dp deep blur, and liquid drag handle.
 * Replaces Material 3 ModalBottomSheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable ColumnScope.() -> Unit,
) {
    val isDark = LocalIsDarkTheme.current
    val handleColor = if (isDark) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.25f)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = Color.Transparent,
        scrimColor = Color.Black.copy(alpha = 0.45f),
        dragHandle = null,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clearLiquidGlass(
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    isSheet = true,
                    interactive = false,
                )
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Drag handle
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 4.dp)
                        .background(handleColor, CircleShape)
                )
                Spacer(Modifier.height(16.dp))

                content()
            }
        }
    }
}
