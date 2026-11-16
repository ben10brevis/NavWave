package com.lastwave.app.ui.album

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.lastwave.app.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lastwave.app.ui.glass.GlassButton
import com.lastwave.app.ui.glass.GlassCard
import com.lastwave.app.ui.glass.GlassChip
import com.lastwave.app.ui.glass.GlassIconButton
import com.lastwave.app.ui.glass.clearLiquidGlass
import com.lastwave.app.ui.theme.LocalIsDarkTheme
import com.lastwave.app.data.model.ArtistAlbumItem
import com.lastwave.app.playback.PlayableTrack
import com.lastwave.app.ui.common.ArtworkImage
import com.lastwave.app.ui.common.ExpressiveLoadingIndicator
import com.lastwave.app.ui.common.TrackContextMenuSheet
import com.lastwave.app.ui.common.TrackMenuCapabilities
import com.lastwave.app.ui.common.TrackMenuTarget
import com.lastwave.app.ui.common.safeDrawingBottomPadding
import com.lastwave.app.ui.common.safeHorizontalContentPadding
import com.lastwave.app.ui.common.adaptiveContentWidth
import com.lastwave.app.ui.player.LocalMiniPlayerScrollClearance
import com.lastwave.app.ui.player.LocalMusicPlayer
import com.lastwave.app.ui.player.PlayingWaveBars

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlbumDetailScreen(
    albumTitle: String,
    artistName: String = "",
    browseId: String? = null,
    onBack: () -> Unit,
    onOpenArtist: (name: String, browseId: String?) -> Unit = { _, _ -> },
    onOpenAlbum: (title: String, artist: String, browseId: String?) -> Unit = { _, _, _ -> },
    viewModel: AlbumViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val musicPlayer = LocalMusicPlayer.current
    val playbackState by musicPlayer.listPlaybackState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    // System back / predictive-back gesture is owned by the wrapping
    // PredictiveBackScreen in NavGraph (single handler per screen) — the
    // toolbar button below still pops directly via onBack.

    LaunchedEffect(albumTitle, artistName, browseId) {
        viewModel.loadAlbum(albumTitle, artistName, browseId)
    }

    var selectedTrackMenu by remember { mutableStateOf<PlayableTrack?>(null) }
    val listState = rememberLazyListState()

    val scrollOffset by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) {
                listState.firstVisibleItemScrollOffset.toFloat()
            } else 600f
        }
    }

    val showScrolledHeader by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 240
        }
    }

    val albumData = (uiState as? AlbumUiState.Success)?.data

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 1. Full-Bleed Cover Art Background at Top with smooth parallax physics
        if (albumData != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp)
                    .graphicsLayer {
                        translationY = -scrollOffset * 0.45f
                        alpha = (1f - (scrollOffset / 520f)).coerceIn(0.1f, 1f)
                        val zoom = 1f + (-scrollOffset.coerceAtMost(0f) / 600f)
                        scaleX = zoom
                        scaleY = zoom
                    },
            ) {
                ArtworkImage(
                    name = albumData.title,
                    artist = albumData.artist,
                    embeddedUrl = albumData.artworkUrl,
                    fallbackIcon = Icons.Filled.Album,
                    alignment = Alignment.TopCenter,
                    modifier = Modifier.fillMaxSize(),
                )

                // Multi-stop cinematic dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Black.copy(alpha = 0.55f),
                                    0.30f to Color.Black.copy(alpha = 0.20f),
                                    0.60f to Color.Black.copy(alpha = 0.55f),
                                    0.85f to MaterialTheme.colorScheme.background.copy(alpha = 0.90f),
                                    1.0f to MaterialTheme.colorScheme.background,
                                ),
                            ),
                        ),
                )
            }
        }
        when (val state = uiState) {
            is AlbumUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ExpressiveLoadingIndicator(message = "Loading $albumTitle...")
                }
            }
            is AlbumUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadAlbum(albumTitle, artistName, browseId) }) {
                            Text(stringResource(R.string.ly_retry))
                        }
                    }
                }
            }
            is AlbumUiState.Success -> {
                val data = state.data
                val saveState by viewModel.saveUiState.collectAsStateWithLifecycle()
                val isAlbumPlaying = playbackState.isPlaying &&
                    (playbackState.sourceLabel.contains(data.title, ignoreCase = true) || playbackState.current?.album.equals(data.title, ignoreCase = true))

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 0.dp,
                        bottom = 32.dp + LocalMiniPlayerScrollClearance.current + safeDrawingBottomPadding(),
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .adaptiveContentWidth(maxWidth = 860.dp)
                        .align(Alignment.TopCenter)
                        .safeHorizontalContentPadding(),
                ) {
                    // 1. Album Hero Header (Edge-to-Edge Full Bleed Overlaid Header)
                    item(key = "album_hero") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 150.dp)
                                .padding(horizontal = 4.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom,
                            ) {
                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(
                                        text = data.title,
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    // Clickable Artist Link(s)
                                    val splitArtists = remember(data.artist) {
                                        com.lastwave.app.util.ArtistHelper.splitArtists(data.artist)
                                    }
                                    androidx.compose.foundation.layout.FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        splitArtists.forEach { artName ->
                                            GlassChip(
                                                text = artName,
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                                    onOpenArtist(artName, if (splitArtists.size == 1) data.artistBrowseId else null)
                                                },
                                            )
                                        }
                                    }

                                    // Metadata line (Year, Duration)
                                    val metaText = listOfNotNull(
                                        data.releaseYear,
                                        data.durationText,
                                    ).joinToString(" • ")

                                    if (metaText.isNotBlank()) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = metaText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = (if (LocalIsDarkTheme.current) Color.White else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.65f),
                                        )
                                    }
                                }

                                if (isAlbumPlaying) {
                                    Box(
                                        modifier = Modifier
                                            .padding(start = 12.dp, bottom = 4.dp)
                                            .clearLiquidGlass(shape = RoundedCornerShape(12.dp), interactive = false),
                                    ) {
                                        PlayingWaveBars(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            containerColor = Color.Transparent,
                                            waveColor = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(18.dp))

                            // Hero Action Buttons (Shuffle • Save to Library • Large Play FAB)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    GlassIconButton(
                                        imageVector = Icons.Filled.Shuffle,
                                        contentDescription = stringResource(R.string.alb_shuffle_cd),
                                        size = 50.dp,
                                        iconSize = 22.dp,
                                        enabled = data.tracks.isNotEmpty(),
                                        tint = MaterialTheme.colorScheme.primary,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                            viewModel.playShuffle()
                                        },
                                    )

                                    GlassIconButton(
                                        imageVector = if (saveState.savedToLibrary) Icons.Filled.Check else Icons.Filled.BookmarkAdd,
                                        contentDescription = if (saveState.savedToLibrary) "Saved to library" else "Save album to library",
                                        size = 50.dp,
                                        iconSize = 22.dp,
                                        enabled = !saveState.isSaving && !saveState.savedToLibrary && data.tracks.isNotEmpty(),
                                        tint = if (saveState.savedToLibrary) MaterialTheme.colorScheme.primary else (if (LocalIsDarkTheme.current) Color.White else MaterialTheme.colorScheme.onSurface),
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                            viewModel.saveToLibrary()
                                        },
                                    )
                                }

                                // Large Floating Primary Play Button (56dp circle)
                                GlassIconButton(
                                    imageVector = if (isAlbumPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = stringResource(R.string.alb_play_cd),
                                    size = 56.dp,
                                    iconSize = 30.dp,
                                    enabled = data.tracks.isNotEmpty(),
                                    tint = MaterialTheme.colorScheme.primary,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                        viewModel.playAll()
                                    },
                                )
                            }

                            saveState.saveError?.let { message ->
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }

                    // 2. Tracklist Header
                    item(key = "tracklist_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.alb_tracks),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            data.trackCountText?.let { count ->
                                Text(
                                    text = count,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    // 3. Track Items
                    if (data.tracks.isEmpty()) {
                        item(key = "tracklist_empty") {
                            Text(
                                text = stringResource(R.string.alb_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                    }
                    itemsIndexed(
                        data.tracks,
                        key = { index, track -> "${track.videoId ?: track.title}_$index" },
                        contentType = { _, _ -> "album_track" },
                    ) { index, track ->
                        val isPlayingThis = playbackState.isPlaying &&
                            playbackState.current?.title.equals(track.title, ignoreCase = true) &&
                            (playbackState.current?.artist.isNullOrBlank() || playbackState.current?.artist.equals(track.artist, ignoreCase = true))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clearLiquidGlass(
                                    shape = RoundedCornerShape(16.dp),
                                    tintColor = if (isPlayingThis) MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                                    else Color.White.copy(alpha = 0.05f),
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    viewModel.playAll(startIndex = index)
                                },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Artwork Thumbnail with Wave overlay
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    ArtworkImage(
                                        name = track.title,
                                        artist = track.artist.takeIf { !com.lastwave.app.util.ArtistHelper.isPlayCountOrStat(it) && it.isNotBlank() } ?: data.artist,
                                        embeddedUrl = track.artworkUrl ?: data.artworkUrl,
                                        fallbackIcon = Icons.Filled.Album,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                    if (isPlayingThis) {
                                        PlayingWaveBars(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(2.dp)
                                                .size(24.dp, 18.dp),
                                        )
                                    }
                                }

                                Spacer(Modifier.width(14.dp))

                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isPlayingThis) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isPlayingThis) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = if (track.artist.isNotBlank() &&
                                            !com.lastwave.app.util.ArtistHelper.isPlayCountOrStat(track.artist) &&
                                            !track.artist.equals(data.artist, ignoreCase = true)
                                        ) {
                                            "${track.artist} \u2022 Track ${index + 1}"
                                        } else {
                                            "Track ${index + 1}"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }

                                GlassIconButton(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = stringResource(R.string.dl_options_cd),
                                    size = 36.dp,
                                    iconSize = 20.dp,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                        selectedTrackMenu = track
                                    },
                                )
                            }
                        }
                    }


                    // 4. Description / Wiki Card
                    if (!data.description.isNullOrBlank()) {
                        item(key = "album_description") {
                            GlassCard(
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            ) {
                                Column(Modifier.padding(4.dp)) {
                                    Text(
                                        text = "About this Album",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = data.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = (if (LocalIsDarkTheme.current) Color.White else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.75f),
                                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                                    )
                                }
                            }
                        }
                    }

                    // 5. Other Albums by Artist
                    if (data.otherAlbums.isNotEmpty()) {
                        item(key = "other_albums_header") {
                            Text(
                                text = "More by ${data.artist}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (LocalIsDarkTheme.current) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 10.dp),
                            )
                        }

                        item(key = "other_albums_row") {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                            ) {
                                items(
                                    data.otherAlbums,
                                    key = { it.browseId.ifBlank { it.title } },
                                    contentType = { "other_album" },
                                ) { otherAlbum ->
                                    AlbumCard(
                                        item = otherAlbum,
                                        onClick = {
                                            onOpenAlbum(otherAlbum.title, data.artist, otherAlbum.browseId)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(10f)
                .align(Alignment.TopCenter)
                .clearLiquidGlass(
                    shape = RectangleShape,
                    customBlurDp = if (showScrolledHeader) 24.dp else 0.dp,
                    tintColor = if (showScrolledHeader) null else Color.Transparent,
                    interactive = false,
                ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .adaptiveContentWidth(maxWidth = 860.dp)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassIconButton(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.pd_back_cd),
                    size = 42.dp,
                    iconSize = 22.dp,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onBack()
                    },
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AnimatedVisibility(
                        visible = showScrolledHeader,
                        enter = fadeIn() + scaleIn(initialScale = 0.9f),
                        exit = fadeOut() + scaleOut(targetScale = 0.9f),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))) {
                                ArtworkImage(
                                    name = albumData?.title ?: albumTitle,
                                    artist = albumData?.artist ?: artistName,
                                    embeddedUrl = albumData?.artworkUrl,
                                    fallbackIcon = Icons.Filled.Album,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = albumData?.title ?: albumTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                // Quick Play Button on Scroll
                AnimatedVisibility(
                    visible = showScrolledHeader && albumData?.tracks?.isNotEmpty() == true,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            viewModel.playAll()
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        modifier = Modifier.size(38.dp),
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.dl_play_cd), modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    }

    // Context Menu Sheet for Tracks
    selectedTrackMenu?.let { track ->
        val safeArtist = track.artist.takeIf { !com.lastwave.app.util.ArtistHelper.isPlayCountOrStat(it) && it.isNotBlank() } ?: (uiState as? AlbumUiState.Success)?.data?.artist ?: ""
        val safeTrack = if (track.artist != safeArtist && safeArtist.isNotBlank()) track.copy(artist = safeArtist) else track
        TrackContextMenuSheet(
            target = TrackMenuTarget.Track(safeTrack.title, safeTrack.artist, ""),
            capabilities = TrackMenuCapabilities(showCopyActions = true, showDeleteScrobble = false),
            playableTrack = safeTrack,
            playbackSourceLabel = albumTitle,
            onDismiss = { selectedTrackMenu = null },
        )
    }
}

@Composable
private fun AlbumCard(
    item: ArtistAlbumItem,
    onClick: () -> Unit,
) {
    GlassCard(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        contentPadding = 10.dp,
        modifier = Modifier.width(150.dp),
    ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(16.dp)),
            ) {
                ArtworkImage(
                    name = item.title,
                    artist = "",
                    embeddedUrl = item.artworkUrl,
                    fallbackIcon = Icons.Filled.Album,
                    modifier = Modifier.fillMaxSize(),
                )
                if (!item.year.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp),
                    ) {
                        Text(
                            text = item.year,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.type ?: "Album",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
