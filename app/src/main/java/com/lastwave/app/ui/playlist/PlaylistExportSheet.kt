package com.lastwave.app.ui.playlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lastwave.app.R
import com.lastwave.app.ui.glass.GlassBottomSheet
import com.lastwave.app.ui.glass.GlassButton
import com.lastwave.app.ui.glass.clearLiquidGlass
import com.lastwave.app.ui.theme.LocalIsDarkTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExportBottomSheet(
    onDismiss: () -> Unit,
    onSaveCsv: () -> Unit,
    onSaveM3u: () -> Unit,
    onShareCsv: () -> Unit,
    onShareM3u: () -> Unit,
) {
    var selected by remember { mutableStateOf(ExportFormat.CSV) }
    val isDark = LocalIsDarkTheme.current
    val titleColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    GlassBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
            Text(
                stringResource(R.string.pl_export_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = titleColor,
            )
            Spacer(Modifier.height(16.dp))

            FormatOptionRow(
                title = "CSV",
                description = stringResource(R.string.pl_format_csv_desc),
                selected = selected == ExportFormat.CSV,
                onClick = { selected = ExportFormat.CSV },
            )
            Spacer(Modifier.height(10.dp))
            FormatOptionRow(
                title = "M3U",
                description = stringResource(R.string.pl_format_m3u_desc),
                selected = selected == ExportFormat.M3U,
                onClick = { selected = ExportFormat.M3U },
            )

            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassButton(
                    text = stringResource(R.string.common_cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                GlassButton(
                    text = stringResource(R.string.common_save),
                    onClick = { if (selected == ExportFormat.CSV) onSaveCsv() else onSaveM3u() },
                    modifier = Modifier.weight(1f),
                )
                GlassButton(
                    text = stringResource(R.string.common_share),
                    onClick = { if (selected == ExportFormat.CSV) onShareCsv() else onShareM3u() },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
internal fun FormatOptionRow(title: String, description: String, selected: Boolean, onClick: () -> Unit) {
    val isDark = LocalIsDarkTheme.current
    val shape = RoundedCornerShape(18.dp)
    val tintColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f) else null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clearLiquidGlass(
                shape = shape,
                interactive = true,
                tintColor = tintColor,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = (if (isDark) Color.White else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.7f),
                )
            }
        }
    }
}

internal fun formatDate(millis: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))
