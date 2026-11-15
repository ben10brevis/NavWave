package com.lastwave.app.ui.glass

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lastwave.app.ui.theme.LocalIsDarkTheme

/**
 * Floating Clear Liquid Glass Mini Player Capsule.
 * Displays artwork, title/artist, play/pause, and a progress hairline.
 */
@Composable
fun GlassMiniPlayer(
    title: String,
    artist: String,
    isPlaying: Boolean,
    progress: Float,
    onTogglePlayPause: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
    onNext: (() -> Unit)? = null,
    artworkContent: (@Composable () -> Unit)? = null,
) {
    val isDark = LocalIsDarkTheme.current
    val titleColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val artistColor = titleColor.copy(alpha = 0.65f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clearLiquidGlass(
                shape = CircleShape,
                interactive = true,
                customBlurDp = 14.dp,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onExpand,
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Artwork slot
                if (artworkContent != null) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        artworkContent()
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.10f)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("♪", color = titleColor, fontSize = 20.sp)
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Title & Artist
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = artistColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlassIconButton(
                    onClick = onTogglePlayPause,
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    size = 40.dp,
                    iconSize = 22.dp,
                )

                if (onNext != null) {
                    Spacer(Modifier.width(4.dp))
                    GlassIconButton(
                        onClick = onNext,
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        size = 36.dp,
                        iconSize = 20.dp,
                    )
                }
            }
        }

        // Progress hairline along bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.92f)
                .height(2.dp)
                .clip(CircleShape)
                .background(if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.10f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(2.dp)
                    .background(if (isDark) Color.White else MaterialTheme.colorScheme.primary)
            )
        }
    }
}
