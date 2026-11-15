package com.lastwave.app.ui.playlist

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lastwave.app.R
import com.lastwave.app.data.generate.GeneratedTrack
import com.lastwave.app.data.playlist.LIKED_SONGS_MODE
import com.lastwave.app.data.playlist.SavedPlaylist
import com.lastwave.app.data.playlist.SpotifyUserPlaylist
import com.lastwave.app.data.playlist.isYouTubeOnly
import com.lastwave.app.playback.PlayableTrack
import com.lastwave.app.ui.common.ExpressiveMotion
import com.lastwave.app.ui.common.GroupOuterRadius
import com.lastwave.app.ui.common.GroupPosition
import com.lastwave.app.ui.common.PlaylistCover
import com.lastwave.app.ui.common.groupShape
import com.lastwave.app.ui.glass.clearLiquidGlass
import com.lastwave.app.ui.player.PlayingWaveBars
import com.lastwave.app.ui.theme.LocalIsDarkTheme

/**
 * Clear Liquid Glass Playlist Card with Spotify design aesthetics.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PlaylistCard(
    playlist: SavedPlaylist,
    isNewest: Boolean,
    position: GroupPosition,
    isRegenerating: Boolean,
    currentTrack: PlayableTrack?,
    isPlaying: Boolean,
    playbackSource: String,
    onClick: () -> Unit,
    onExport: () -> Unit,
    onRename: () -> Unit,
    onEditCover: () -> Unit,
    onTogglePin: () -> Unit,
    onDownload: () -> Unit,
    onRegenerate: () -> Unit,
    onDelete: () -> Unit,
    onMakeLocal: () -> Unit,
    onPlay: () -> Unit,
    isSelected: Boolean = false,
    onLongClick: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    val isDark = LocalIsDarkTheme.current
    val titleColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val isThisPlaylistPlaying = isPlaying && playbackSource == playlist.title
    var menuExpanded by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val playInteractionSource = remember { MutableInteractionSource() }
    val isPlayPressed by playInteractionSource.collectIsPressedAsState()

    val playScale by animateFloatAsState(
        targetValue = if (isPlayPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "playScale",
    )
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.975f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "cardPress",
    )

    val cardTint = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
        isThisPlaylistPlaying -> MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .clearLiquidGlass(
                shape = groupShape(position),
                interactive = true,
                tintColor = cardTint,
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onClick()
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            )
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(60.dp)) {
                PlaylistCover(playlist = playlist, modifier = Modifier.fillMaxSize(), cornerRadius = 14.dp)
                if (isThisPlaylistPlaying) {
                    PlayingWaveBars(
                        Modifier.align(Alignment.BottomEnd).padding(4.dp).size(24.dp, 18.dp),
                    )
                }
                if (isRegenerating) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clearLiquidGlass(
                                shape = RoundedCornerShape(14.dp),
                                tintColor = Color.Black.copy(alpha = 0.5f),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        com.lastwave.app.ui.common.ExpressiveInlineLoadingIndicator(
                            size = 26.dp,
                            color = Color.White,
                        )
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        playlist.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (playlist.isPinned) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Filled.PushPin,
                            contentDescription = stringResource(R.string.pl_pinned_cd),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    if (playlist.isYouTubeOnly) {
                        "YouTube Music • ${playlist.remoteTrackCount?.let { stringResource(R.string.pl_yt_tracks, it) } ?: stringResource(R.string.pl_yt_connected)}"
                    } else {
                        stringResource(R.string.pl_subtitle_local, playlist.tracks.size, formatDate(playlist.createdAtMillis))
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = titleColor.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Quick play button
            if (playlist.tracks.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .graphicsLayer {
                            scaleX = playScale
                            scaleY = playScale
                        }
                        .clearLiquidGlass(
                            shape = CircleShape,
                            interactive = true,
                            tintColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        )
                        .clickable(
                            interactionSource = playInteractionSource,
                            indication = null,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                onPlay()
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = stringResource(R.string.pl_play_cd),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.width(4.dp))
            }

            // More options menu
            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.pl_options_cd),
                        tint = titleColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    shape = RoundedCornerShape(22.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 4.dp,
                    shadowElevation = 10.dp,
                ) {
                    DropdownMenuItem(
                        text = { Text(if (playlist.isPinned) stringResource(R.string.pl_unpin) else stringResource(R.string.pl_pin)) },
                        leadingIcon = { Icon(Icons.Filled.PushPin, contentDescription = null) },
                        onClick = { onTogglePin(); menuExpanded = false },
                    )
                    val isLocalSongs = playlist.mode == com.lastwave.app.data.playlist.LOCAL_SONGS_MODE
                    if (!isLocalSongs) DropdownMenuItem(
                        text = { Text(stringResource(R.string.pl_download_all)) },
                        leadingIcon = { Icon(Icons.Filled.Download, contentDescription = null) },
                        onClick = { onDownload(); menuExpanded = false },
                    )
                    if (playlist.isYouTubeOnly) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.pl_make_local)) },
                            leadingIcon = { Icon(Icons.Filled.BookmarkAdd, contentDescription = null) },
                            onClick = { onMakeLocal(); menuExpanded = false },
                        )
                    } else {
                        if (playlist.mode != LIKED_SONGS_MODE && !isLocalSongs) {
                            DropdownMenuItem(
                                text = { Text(if (isRegenerating) stringResource(R.string.pl_regenerating) else stringResource(R.string.pl_regenerate)) },
                                leadingIcon = { Icon(Icons.Filled.Refresh, contentDescription = null) },
                                enabled = !isRegenerating,
                                onClick = { onRegenerate(); menuExpanded = false },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.pl_change_cover)) },
                            leadingIcon = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null) },
                            onClick = { onEditCover(); menuExpanded = false },
                        )
                        if (playlist.mode != LIKED_SONGS_MODE) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.common_rename)) },
                                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                                onClick = { onRename(); menuExpanded = false },
                            )
                        }
                    }
                    if (!playlist.isYouTubeOnly) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.common_export)) },
                            leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                            onClick = { onExport(); menuExpanded = false },
                        )
                    }
                    if (!isLocalSongs) DropdownMenuItem(
                        text = { Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { onDelete(); menuExpanded = false },
                    )
                }
            }
        }
    }
}

/** Spotify-style Liked Songs Pinned Card */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LikedSongsHeroCard(
    onClick: () -> Unit,
    onPlay: (() -> Unit)? = null,
    trackCount: Int? = null,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = ExpressiveMotion.spatialFast(),
        label = "likedHeroScale",
    )
    val isDark = LocalIsDarkTheme.current
    val titleColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .clearLiquidGlass(
                shape = RoundedCornerShape(GroupOuterRadius),
                interactive = true,
                tintColor = Color(0xFF450AF5).copy(alpha = 0.14f),
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onClick()
                },
            )
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF450AF5),
                                Color(0xFF8E8EE5),
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = Color.White,
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.spotify_liked_songs_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Filled.PushPin,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (trackCount != null && trackCount > 0) {
                        "Pinned • Playlist • $trackCount songs"
                    } else {
                        "Pinned • Playlist"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = titleColor.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (onPlay != null) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clearLiquidGlass(
                            shape = CircleShape,
                            interactive = true,
                            tintColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                        )
                        .clickable(onClick = onPlay),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = stringResource(R.string.pl_play_cd),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

/** Library's Downloads entry: Clear Liquid Glass card opening the Downloads screen. */
@Composable
internal fun DownloadsHeroCard(
    downloadedCount: Int,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val isDark = LocalIsDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = ExpressiveMotion.spatialFast(),
        label = "downloadsHeroScale",
    )
    val titleColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .clearLiquidGlass(
                shape = RoundedCornerShape(GroupOuterRadius),
                interactive = true,
                tintColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f),
            )
            .clickable(interactionSource = interactionSource, indication = null) {
                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                onClick()
            }
            .padding(horizontal = 18.dp, vertical = 18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clearLiquidGlass(
                        shape = RoundedCornerShape(18.dp),
                        interactive = false,
                        tintColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(30.dp),
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = "Saved for offline",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Downloads",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (downloadedCount > 0) {
                        stringResource(R.string.pd_track_count, downloadedCount)
                    } else {
                        "Nothing downloaded yet"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = titleColor.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = titleColor.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LocalSongsHeroCard(
    playlist: SavedPlaylist,
    isPlaying: Boolean,
    playbackSource: String,
    onClick: () -> Unit,
    onEditCover: () -> Unit,
    onPlay: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val isDark = LocalIsDarkTheme.current
    val isThisPlaylistPlaying = isPlaying && playbackSource == playlist.title
    var menuExpanded by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val playInteractionSource = remember { MutableInteractionSource() }
    val isPlayPressed by playInteractionSource.collectIsPressedAsState()

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = ExpressiveMotion.spatialFast(),
        label = "localHeroScale",
    )
    val playScale by animateFloatAsState(
        targetValue = if (isPlayPressed) 0.88f else 1f,
        animationSpec = ExpressiveMotion.spatialFast(),
        label = "localHeroPlayScale",
    )
    val titleColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .clearLiquidGlass(
                shape = RoundedCornerShape(GroupOuterRadius),
                interactive = true,
                tintColor = if (isThisPlaylistPlaying) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                } else {
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                },
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onClick()
                },
            )
            .padding(horizontal = 18.dp, vertical = 18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(68.dp)) {
                if (!playlist.customCoverUri.isNullOrBlank() || playlist.tracks.isNotEmpty()) {
                    PlaylistCover(playlist = playlist, modifier = Modifier.fillMaxSize(), cornerRadius = 18.dp)
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clearLiquidGlass(
                                shape = RoundedCornerShape(18.dp),
                                interactive = false,
                                tintColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp),
                        )
                    }
                }
                if (isThisPlaylistPlaying) {
                    PlayingWaveBars(
                        Modifier.align(Alignment.BottomEnd).padding(4.dp).size(24.dp, 18.dp),
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.local_panel_title),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = playlist.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (playlist.tracks.isNotEmpty()) {
                        stringResource(R.string.pd_track_count, playlist.tracks.size)
                    } else {
                        stringResource(R.string.local_choose_folder)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = titleColor.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (playlist.tracks.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer {
                            scaleX = playScale
                            scaleY = playScale
                        }
                        .clearLiquidGlass(
                            shape = CircleShape,
                            interactive = true,
                            tintColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                        )
                        .clickable(
                            interactionSource = playInteractionSource,
                            indication = null,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                onPlay()
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = stringResource(R.string.pl_play_cd),
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.pl_options_cd),
                            tint = titleColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        shape = RoundedCornerShape(22.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 4.dp,
                        shadowElevation = 10.dp,
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.pl_change_cover)) },
                            leadingIcon = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null) },
                            onClick = { onEditCover(); menuExpanded = false },
                        )
                    }
                }
            } else {
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = titleColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SpotifyPlaylistCard(
    playlist: SpotifyUserPlaylist,
    position: GroupPosition = GroupPosition.SINGLE,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val isDark = LocalIsDarkTheme.current
    val isLiked = playlist.id == "liked"
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.975f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "spCardScale",
    )
    val titleColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .clearLiquidGlass(
                shape = groupShape(position),
                interactive = true,
                tintColor = if (isLiked) Color(0xFF1DB954).copy(alpha = 0.12f) else null,
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onClick()
                },
            )
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (isLiked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF1DB954),
                                        Color(0xFF191414),
                                    ),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                            tint = Color.White,
                        )
                    }
                } else if (!playlist.artworkUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = playlist.artworkUrl,
                        contentDescription = playlist.name,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clearLiquidGlass(shape = RoundedCornerShape(14.dp), interactive = false),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                            tint = titleColor.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                val trackCount = playlist.trackCount
                Text(
                    text = when {
                        isLiked -> "Spotify • ${trackCount?.let { stringResource(R.string.pl_yt_tracks, it) } ?: stringResource(R.string.spotify_liked_songs_sub)}"
                        trackCount != null -> "Spotify • ${stringResource(R.string.pl_yt_tracks, trackCount)}"
                        else -> "Spotify"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = titleColor.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
