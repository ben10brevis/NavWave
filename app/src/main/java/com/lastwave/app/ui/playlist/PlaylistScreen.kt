package com.lastwave.app.ui.playlist

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lastwave.app.R
import com.lastwave.app.data.generate.GeneratedTrack
import com.lastwave.app.data.playlist.LOCAL_SONGS_MODE
import com.lastwave.app.data.playlist.SavedPlaylist
import com.lastwave.app.playback.toPlayableTrack
import com.lastwave.app.ui.common.ExpressiveHeader
import com.lastwave.app.ui.common.PlaylistCover
import com.lastwave.app.ui.common.TopCurvedSectionClipShape
import com.lastwave.app.ui.common.TrackContextMenuSheet
import com.lastwave.app.ui.common.TrackMenuCapabilities
import com.lastwave.app.ui.common.TrackMenuTarget
import com.lastwave.app.ui.common.adaptiveContentWidth
import com.lastwave.app.ui.glass.GlassBottomSheet
import com.lastwave.app.ui.glass.GlassButton
import com.lastwave.app.ui.glass.GlassChip
import com.lastwave.app.ui.glass.GlassDialog
import com.lastwave.app.ui.glass.GlassIconButton
import com.lastwave.app.ui.glass.GlassListRow
import com.lastwave.app.ui.glass.clearLiquidGlass
import com.lastwave.app.ui.player.LocalMusicPlayer
import com.lastwave.app.ui.shell.FloatingNavDefaults
import com.lastwave.app.ui.theme.LocalIsDarkTheme

/**
 * Spotify-style Your Library Screen rendered with Clear Liquid Glass.
 * Features:
 * - Library filter chips (Playlists, Albums, Artists, Downloaded, Local)
 * - Sort and Grid/List toggle toolbar
 * - Pinned Liked Songs entry
 * - Glass Create Bottom Sheet menu (Playlist, Generator, Local Folder)
 * - Clear Liquid Glass cards and dialogs
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    onOpenPlaylist: (Long) -> Unit = {},
    onOpenFeedPlaylist: (String) -> Unit = {},
    onOpenGenerator: () -> Unit = {},
    viewModel: PlaylistViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val musicPlayer = LocalMusicPlayer.current
    val playbackState by musicPlayer.listPlaybackState.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var activeFilter by remember { mutableStateOf<String?>(null) }
    var isGridView by remember { mutableStateOf(false) }
    var showCreateMenu by remember { mutableStateOf(false) }

    var coverEditorPlaylistId by remember { mutableStateOf<Long?>(null) }
    var coverPickerPlaylistId by remember { mutableStateOf<Long?>(null) }
    val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val playlistId = coverPickerPlaylistId
        if (uri != null && playlistId != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            viewModel.setCustomCover(playlistId, uri.toString())
        }
        coverPickerPlaylistId = null
    }

    LifecycleResumeEffect(Unit) {
        viewModel.load()
        onPauseOrDispose { }
    }

    var menuTarget by remember { mutableStateOf<Pair<Long, GeneratedTrack>?>(null) }
    var selectedPlaylists by remember { mutableStateOf(setOf<Long>()) }
    var showMultipleDeleteConfirm by remember { mutableStateOf(false) }

    val downloadedCount by viewModel.downloadedCount.collectAsStateWithLifecycle()

    val localSongsPlaylist = remember(state.playlists, searchQuery) {
        state.playlists.firstOrNull {
            it.mode == LOCAL_SONGS_MODE && it.matchesPlaylistSearch(searchQuery)
        }
    }

    val filteredPlaylists = remember(state.playlists, searchQuery, activeFilter) {
        state.playlists.filter { playlist ->
            val matchesSearch = playlist.mode != LOCAL_SONGS_MODE && playlist.matchesPlaylistSearch(searchQuery)
            val matchesChip = when (activeFilter) {
                "downloaded" -> false // local playlists unless downloaded
                "local" -> false
                "albums", "artists" -> false
                else -> true
            }
            matchesSearch && matchesChip
        }
    }

    val filteredSpotifyPlaylists = remember(state.spotifyPlaylists, searchQuery, activeFilter) {
        if (activeFilter == "local" || activeFilter == "downloaded") {
            emptyList()
        } else if (searchQuery.isBlank()) {
            state.spotifyPlaylists
        } else {
            state.spotifyPlaylists.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    val effectiveSpotify = if (state.isSpotifySyncEnabled) filteredSpotifyPlaylists else emptyList()

    val showDownloadsCard = (activeFilter == null || activeFilter == "downloaded") &&
        (searchQuery.isBlank() || "downloads".contains(searchQuery.trim(), ignoreCase = true))

    val showLocalHero = (activeFilter == null || activeFilter == "local") && localSongsPlaylist != null

    val showLikedSongsCard = (activeFilter == null || activeFilter == "playlists") &&
        (searchQuery.isBlank() || "liked songs".contains(searchQuery.trim(), ignoreCase = true))

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .adaptiveContentWidth(maxWidth = 860.dp)
                .align(Alignment.TopCenter),
        ) {
            var sortMenuExpanded by remember { mutableStateOf(false) }

            // ── Top Header Bar ──
            ExpressiveHeader(
                title = if (selectedPlaylists.isNotEmpty()) {
                    stringResource(R.string.pl_selected_count, selectedPlaylists.size)
                } else {
                    "Your Library"
                },
                subtitle = if (selectedPlaylists.isNotEmpty()) {
                    stringResource(R.string.pl_select_more_hint)
                } else {
                    val totalPlaylistsCount = state.playlists.size + (if (state.isSpotifySyncEnabled) state.spotifyPlaylists.size else 0)
                    val totalTracks = state.playlists.sumOf { it.remoteTrackCount ?: it.tracks.size } +
                        (if (state.isSpotifySyncEnabled) state.spotifyPlaylists.sumOf { it.trackCount ?: 0 } else 0)
                    stringResource(R.string.pl_count_tracks, totalPlaylistsCount, totalTracks)
                },
                actions = {
                    if (selectedPlaylists.isNotEmpty()) {
                        GlassIconButton(
                            icon = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.pl_delete_selected_cd),
                            onClick = { showMultipleDeleteConfirm = true },
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.width(6.dp))
                        GlassIconButton(
                            icon = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.pl_clear_selection_cd),
                            onClick = { selectedPlaylists = setOf() },
                        )
                    } else {
                        GlassIconButton(
                            icon = Icons.Filled.Search,
                            contentDescription = "Search Library",
                            onClick = { isSearchVisible = !isSearchVisible },
                        )
                        Spacer(Modifier.width(6.dp))
                        GlassIconButton(
                            icon = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.pl_create_cd),
                            onClick = { showCreateMenu = true },
                        )
                    }
                },
            )

            // ── Search Input Capsule (Expandable) ──
            AnimatedVisibility(
                visible = isSearchVisible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search in Your Library",
                                style = MaterialTheme.typography.bodyMedium,
                                color = (if (isDark) Color.White else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.6f),
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clearLiquidGlass(shape = CircleShape, customBlurDp = 10.dp),
                    )
                }
            }

            // ── Spotify Filter Chips Row ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassChip(
                    text = "Playlists",
                    selected = activeFilter == "playlists",
                    onClick = { activeFilter = if (activeFilter == "playlists") null else "playlists" },
                )
                GlassChip(
                    text = "Albums",
                    selected = activeFilter == "albums",
                    onClick = { activeFilter = if (activeFilter == "albums") null else "albums" },
                )
                GlassChip(
                    text = "Artists",
                    selected = activeFilter == "artists",
                    onClick = { activeFilter = if (activeFilter == "artists") null else "artists" },
                )
                GlassChip(
                    text = "Downloaded",
                    selected = activeFilter == "downloaded",
                    onClick = { activeFilter = if (activeFilter == "downloaded") null else "downloaded" },
                )
                GlassChip(
                    text = "Local",
                    selected = activeFilter == "local",
                    onClick = { activeFilter = if (activeFilter == "local") null else "local" },
                )
            }

            // ── Sort & View Mode Sub-Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Sort Button Capsule
                Box {
                    Row(
                        modifier = Modifier
                            .clearLiquidGlass(shape = CircleShape, interactive = true)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { sortMenuExpanded = true },
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.pl_sort_cd),
                            modifier = Modifier.size(16.dp),
                            tint = if (isDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.pl_sort_label),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false },
                        shape = RoundedCornerShape(22.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 4.dp,
                        shadowElevation = 10.dp,
                    ) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.pl_sort_newest)) }, onClick = { viewModel.setSortMode(PlaylistSortMode.DATE_DESC); sortMenuExpanded = false })
                        DropdownMenuItem(text = { Text(stringResource(R.string.pl_sort_oldest)) }, onClick = { viewModel.setSortMode(PlaylistSortMode.DATE_ASC); sortMenuExpanded = false })
                        DropdownMenuItem(text = { Text(stringResource(R.string.pl_sort_name)) }, onClick = { viewModel.setSortMode(PlaylistSortMode.NAME); sortMenuExpanded = false })
                        DropdownMenuItem(text = { Text(stringResource(R.string.pl_sort_track_count)) }, onClick = { viewModel.setSortMode(PlaylistSortMode.TRACK_COUNT); sortMenuExpanded = false })
                    }
                }

                // Grid / List Toggle
                IconButton(
                    onClick = { isGridView = !isGridView },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.Filled.ViewAgenda else Icons.Filled.GridView,
                        contentDescription = "Toggle View",
                        tint = if (isDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // ── Main Content List ──
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
            ) {
                when {
                    state.isLoading && state.playlists.isEmpty() && !state.isGenerating -> LoadingState()
                    !showLocalHero && filteredPlaylists.isEmpty() && !state.isGenerating &&
                        effectiveSpotify.isEmpty() && (!showDownloadsCard || downloadedCount == 0) && !showLikedSongsCard -> EmptyState()
                    else -> LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = FloatingNavDefaults.contentBottomPadding()),
                        verticalArrangement = Arrangement.spacedBy(com.lastwave.app.ui.common.GroupGap),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(TopCurvedSectionClipShape(horizontalInset = 16.dp, cornerRadius = 28.dp)),
                    ) {
                        // 1. Spotify Pinned Liked Songs Card
                        if (showLikedSongsCard) {
                            item(key = "liked_songs_hero", contentType = "liked_songs_hero") {
                                LikedSongsHeroCard(
                                    onClick = { onOpenFeedPlaylist("yt_liked") },
                                    onPlay = null,
                                )
                            }
                        }

                        // 2. Local Songs Hero Card
                        if (showLocalHero && localSongsPlaylist != null) {
                            item(key = "local_songs_hero", contentType = "local_songs_hero") {
                                LocalSongsHeroCard(
                                    playlist = localSongsPlaylist,
                                    isPlaying = playbackState.isPlaying,
                                    playbackSource = playbackState.sourceLabel,
                                    onClick = { onOpenPlaylist(localSongsPlaylist.id) },
                                    onEditCover = { coverEditorPlaylistId = localSongsPlaylist.id },
                                    onPlay = {
                                        musicPlayer.playQueue(
                                            localSongsPlaylist.tracks.map(GeneratedTrack::toPlayableTrack),
                                            startIndex = 0,
                                            sourceLabel = localSongsPlaylist.title,
                                            enableRadioContinuation = false,
                                        )
                                    },
                                )
                            }
                        }

                        // 3. Downloads Hero Card
                        if (showDownloadsCard) {
                            item(key = "downloads_hero", contentType = "downloads_hero") {
                                DownloadsHeroCard(
                                    downloadedCount = downloadedCount,
                                    onClick = viewModel::openDownloads,
                                )
                            }
                        }

                        // 4. Generating Indicator
                        if (state.isGenerating) {
                            item(key = "generationProgress", contentType = "generationProgress") {
                                com.lastwave.app.ui.common.GenerationProgressCard(message = state.generatingMessage)
                            }
                        }

                        // 5. Just Saved Banner
                        if (state.justSavedBannerVisible) {
                            item(key = "banner") {
                                LaunchedEffect(state.justSavedBannerVisible) {
                                    kotlinx.coroutines.delay(3000)
                                    viewModel.dismissJustSavedBanner()
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clearLiquidGlass(
                                            shape = RoundedCornerShape(16.dp),
                                            interactive = false,
                                            tintColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                                        )
                                        .padding(16.dp),
                                ) {
                                    Text(
                                        "Playlist saved!",
                                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }

                        val groupedPlaylistCount = effectiveSpotify.size + filteredPlaylists.size

                        // 6. Spotify Playlists
                        if (effectiveSpotify.isNotEmpty()) {
                            itemsIndexed(
                                items = effectiveSpotify,
                                key = { _, pl -> "sp_${pl.id}" },
                            ) { index, pl ->
                                Box(Modifier.animateItem()) {
                                    SpotifyPlaylistCard(
                                        playlist = pl,
                                        position = com.lastwave.app.ui.common.groupPositionFor(index, groupedPlaylistCount),
                                        onClick = {
                                            val routeId = if (pl.id == "liked") "sp_liked" else "sp_${pl.id}"
                                            onOpenFeedPlaylist(routeId)
                                        },
                                    )
                                }
                            }
                        }

                        // 7. LastWave Playlists
                        itemsIndexed(
                            items = filteredPlaylists,
                            key = { _, playlist -> "pl_${playlist.id}_${playlist.remotePlaylistId ?: "local"}" },
                        ) { index, playlist ->
                            val isNewest = playlist.id == state.newestId
                            val globalIndex = effectiveSpotify.size + index
                            Box(Modifier.animateItem()) {
                                PlaylistCard(
                                    playlist = playlist,
                                    isNewest = isNewest,
                                    position = com.lastwave.app.ui.common.groupPositionFor(globalIndex, groupedPlaylistCount),
                                    isRegenerating = state.regeneratingId == playlist.id,
                                    currentTrack = playbackState.current,
                                    isPlaying = playbackState.isPlaying,
                                    playbackSource = playbackState.sourceLabel,
                                    onClick = {
                                        if (selectedPlaylists.isNotEmpty()) {
                                            selectedPlaylists = if (selectedPlaylists.contains(playlist.id)) selectedPlaylists - playlist.id else selectedPlaylists + playlist.id
                                        } else {
                                            onOpenPlaylist(playlist.id)
                                        }
                                    },
                                    onExport = { viewModel.openExportSheet(playlist.id) },
                                    onRename = { viewModel.requestRename(playlist.id) },
                                    onEditCover = { coverEditorPlaylistId = playlist.id },
                                    onTogglePin = { viewModel.togglePinned(playlist.id) },
                                    onDownload = { viewModel.downloadPlaylist(playlist.id) },
                                    onRegenerate = {
                                        viewModel.regenerate(playlist.id) { newId ->
                                            onOpenPlaylist(newId)
                                        }
                                    },
                                    onDelete = { viewModel.requestDelete(playlist.id) },
                                    onMakeLocal = { viewModel.makeLocal(playlist.id) },
                                    onPlay = {
                                        musicPlayer.playQueue(
                                            playlist.tracks.map(GeneratedTrack::toPlayableTrack),
                                            startIndex = 0,
                                            sourceLabel = playlist.title,
                                        )
                                    },
                                    isSelected = selectedPlaylists.contains(playlist.id),
                                    onLongClick = {
                                        selectedPlaylists = if (selectedPlaylists.contains(playlist.id)) selectedPlaylists - playlist.id else selectedPlaylists + playlist.id
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Multiple Delete Confirmation Dialog ──
        if (showMultipleDeleteConfirm) {
            GlassDialog(
                onDismissRequest = { showMultipleDeleteConfirm = false },
                title = stringResource(R.string.pl_delete_multi_title),
                text = { Text(stringResource(R.string.pl_delete_multi_text, selectedPlaylists.size)) },
                confirmButton = {
                    GlassButton(
                        text = stringResource(R.string.common_delete),
                        onClick = {
                            viewModel.deleteMultiple(selectedPlaylists)
                            selectedPlaylists = setOf()
                            showMultipleDeleteConfirm = false
                        },
                    )
                },
                dismissButton = {
                    GlassButton(
                        text = stringResource(R.string.common_cancel),
                        onClick = { showMultipleDeleteConfirm = false },
                    )
                },
            )
        }

        // ── Floating Toast Pill ──
        state.toastMessage?.let { msg ->
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(3000)
                viewModel.dismissToast()
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = FloatingNavDefaults.contentBottomPadding() + 12.dp)
                    .clearLiquidGlass(shape = CircleShape, interactive = false)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                Text(
                    text = msg,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }

    // ── Glass Create Menu Sheet ──
    if (showCreateMenu) {
        GlassBottomSheet(onDismissRequest = { showCreateMenu = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    "Create",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                )
                GlassListRow(
                    title = "Playlist",
                    subtitle = "Create a new custom playlist",
                    leadingContent = {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    onClick = {
                        showCreateMenu = false
                        viewModel.openCreateDialog()
                    },
                )
                GlassListRow(
                    title = "AI Playlist Generator",
                    subtitle = "Generate smart mixes with LastWave AI",
                    leadingContent = {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    onClick = {
                        showCreateMenu = false
                        onOpenGenerator()
                    },
                )
                GlassListRow(
                    title = "Local Music Folders",
                    subtitle = "Scan device storage for audio tracks",
                    leadingContent = {
                        Icon(Icons.Filled.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    onClick = {
                        showCreateMenu = false
                        localSongsPlaylist?.let { onOpenPlaylist(it.id) }
                    },
                )
            }
        }
    }

    // ── Export Bottom Sheet ──
    state.exportSheetForPlaylistId?.let { id ->
        ExportBottomSheet(
            onDismiss = viewModel::dismissExportSheet,
            onSaveCsv = { viewModel.exportSave(id, ExportFormat.CSV) },
            onSaveM3u = { viewModel.exportSave(id, ExportFormat.M3U) },
            onShareCsv = { viewModel.exportShare(id, ExportFormat.CSV) },
            onShareM3u = { viewModel.exportShare(id, ExportFormat.M3U) },
        )
    }

    // ── Delete Confirm Dialog ──
    if (state.deleteConfirmForPlaylistId != null) {
        GlassDialog(
            onDismissRequest = viewModel::dismissDeleteConfirm,
            title = stringResource(R.string.pl_delete_single_title),
            text = { Text(stringResource(R.string.pl_delete_undone)) },
            confirmButton = {
                GlassButton(
                    text = stringResource(R.string.common_delete),
                    onClick = viewModel::confirmDelete,
                )
            },
            dismissButton = {
                GlassButton(
                    text = stringResource(R.string.common_cancel),
                    onClick = viewModel::dismissDeleteConfirm,
                )
            },
        )
    }

    // ── Create Dialog ──
    if (state.createDialogVisible) {
        var title by remember(state.createDialogVisible) { mutableStateOf("") }
        GlassDialog(
            onDismissRequest = viewModel::dismissCreateDialog,
            title = stringResource(R.string.pl_create_title),
            text = {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.pl_name_label)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                GlassButton(
                    text = stringResource(R.string.common_create),
                    enabled = title.isNotBlank(),
                    onClick = { viewModel.createCustomPlaylist(title) },
                )
            },
            dismissButton = {
                GlassButton(
                    text = stringResource(R.string.common_cancel),
                    onClick = viewModel::dismissCreateDialog,
                )
            },
        )
    }

    // ── Rename Dialog ──
    state.renamePlaylistId?.let { playlistId ->
        val playlist = state.playlists.firstOrNull { it.id == playlistId }
        var title by remember(playlistId) { mutableStateOf(playlist?.title.orEmpty()) }
        GlassDialog(
            onDismissRequest = viewModel::dismissRename,
            title = stringResource(R.string.pl_rename_title),
            text = {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.pl_name_label)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                GlassButton(
                    text = stringResource(R.string.common_rename),
                    enabled = title.isNotBlank(),
                    onClick = { viewModel.renamePlaylist(title) },
                )
            },
            dismissButton = {
                GlassButton(
                    text = stringResource(R.string.common_cancel),
                    onClick = viewModel::dismissRename,
                )
            },
        )
    }

    // ── Cover Editor Dialog ──
    coverEditorPlaylistId?.let { playlistId ->
        state.playlists.firstOrNull { it.id == playlistId }?.let { playlist ->
            GlassDialog(
                onDismissRequest = { coverEditorPlaylistId = null },
                title = stringResource(R.string.pl_cover_title),
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        PlaylistCover(playlist = playlist, modifier = Modifier.size(132.dp), cornerRadius = 26.dp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (playlist.customCoverUri.isNullOrBlank()) {
                                stringResource(R.string.pl_cover_auto_desc)
                            } else {
                                stringResource(R.string.pl_cover_custom_desc)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = (if (isDark) Color.White else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.75f),
                            textAlign = TextAlign.Center,
                        )
                    }
                },
                confirmButton = {
                    GlassButton(
                        text = if (playlist.customCoverUri.isNullOrBlank()) stringResource(R.string.pl_choose_image) else stringResource(R.string.pl_change_image),
                        onClick = {
                            coverPickerPlaylistId = playlist.id
                            coverEditorPlaylistId = null
                            coverPicker.launch(arrayOf("image/*"))
                        },
                    )
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!playlist.customCoverUri.isNullOrBlank()) {
                            GlassButton(
                                text = stringResource(R.string.pl_use_automatic),
                                onClick = {
                                    viewModel.setCustomCover(playlist.id, null)
                                    coverEditorPlaylistId = null
                                },
                            )
                        }
                        GlassButton(
                            text = stringResource(R.string.common_done),
                            onClick = { coverEditorPlaylistId = null },
                        )
                    }
                },
            )
        }
    }

    // ── Scrobble Auth Required Dialog ──
    if (state.deleteScrobbleAuthRequired) {
        GlassDialog(
            onDismissRequest = viewModel::dismissDeleteScrobbleAuthRequired,
            title = stringResource(R.string.pl_scrobble_auth_title),
            text = { Text(stringResource(R.string.pl_scrobble_auth_text)) },
            confirmButton = {
                GlassButton(
                    text = stringResource(R.string.pl_scrobble_auth_ok),
                    onClick = viewModel::dismissDeleteScrobbleAuthRequired,
                )
            },
        )
    }

    // ── Shared Track Context Menu ──
    menuTarget?.let { (playlistId, track) ->
        TrackContextMenuSheet(
            target = TrackMenuTarget.Track(track.name, track.artist, track.url),
            capabilities = TrackMenuCapabilities(showCopyActions = true, showDeleteScrobble = true),
            playbackSourceLabel = state.playlists.firstOrNull { it.id == playlistId }?.title ?: "Playlists",
            onDismiss = { menuTarget = null },
            onDeleteScrobble = { name, artist -> viewModel.deleteScrobble(name, artist) },
            onRefreshArtwork = { viewModel.refreshArtwork(track.name, track.artist) },
        )
    }
}

private fun SavedPlaylist.matchesPlaylistSearch(query: String): Boolean =
    query.isBlank() ||
        title.contains(query, ignoreCase = true) ||
        tracks.any { t ->
            t.name.contains(query, ignoreCase = true) ||
                t.artist.contains(query, ignoreCase = true)
        }
