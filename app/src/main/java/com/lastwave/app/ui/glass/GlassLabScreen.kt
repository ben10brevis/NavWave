package com.lastwave.app.ui.glass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lastwave.app.ui.theme.LocalIsDarkTheme
import kotlin.math.roundToInt

/**
 * Hidden Glass Lab Debug Screen.
 * Opened by tapping the Version row 7 times in Settings > About.
 * Provides real-time sliders for all optical parameters and displays
 * every Clear Liquid Glass component live over the dynamic mesh background.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassLabScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalIsDarkTheme.current
    var selectedTier by remember { mutableStateOf(GlassTier.TierA) }

    // Live tunable parameters
    var blurDp by remember { mutableFloatStateOf(12f) }
    var tintAlpha by remember { mutableFloatStateOf(0.06f) }
    var refraction by remember { mutableFloatStateOf(0.18f) }
    var dispersion by remember { mutableFloatStateOf(0.04f) }
    var rimLight by remember { mutableFloatStateOf(0.55f) }
    var saturation by remember { mutableFloatStateOf(1.60f) }
    var brightness by remember { mutableFloatStateOf(1.05f) }

    val liveParams = GlassParams(
        blurDp = blurDp,
        sheetBlurDp = blurDp * 2.2f,
        tintAlpha = tintAlpha,
        refraction = refraction,
        dispersion = dispersion,
        rimLightAlpha = rimLight,
        saturation = saturation,
        brightness = brightness,
    )

    // Interactive component test states
    var switchChecked by remember { mutableStateOf(true) }
    var sliderValue by remember { mutableFloatStateOf(0.42f) }
    var searchQuery by remember { mutableStateOf("Liquid Glass Optics") }
    var selectedChipIndex by remember { mutableIntStateOf(0) }
    var selectedSegmentIndex by remember { mutableIntStateOf(1) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isMiniPlayerPlaying by remember { mutableStateOf(true) }
    var showSheet by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    CompositionLocalProvider(
        LocalGlassTier provides selectedTier,
        LocalGlassParams provides liveParams,
    ) {
        DynamicMeshBackground(
            primaryColor = Color(0xFF6B4EEA),
            secondaryColor = Color(0xFF1DB954),
            accentColor = Color(0xFFFF5252),
        ) {
            Column(modifier = modifier.fillMaxSize()) {
                GlassTopBar(
                    title = "Glass Lab (iOS 26/27)",
                    onBack = onBack,
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "LIVE OPTICAL CONTROLS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor.copy(alpha = 0.60f),
                        )
                    }

                    // Hardware Tier Selector
                    item {
                        GlassCard {
                            Text(
                                text = "Hardware Tier",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor,
                            )
                            Spacer(Modifier.height(8.dp))
                            GlassSegmentedControl(
                                items = listOf("Tier A (AGSL)", "Tier B (Blur)", "Tier C (Tint)"),
                                selectedIndex = when (selectedTier) {
                                    GlassTier.TierA -> 0
                                    GlassTier.TierB -> 1
                                    GlassTier.TierC -> 2
                                },
                                onSelect = {
                                    selectedTier = when (it) {
                                        0 -> GlassTier.TierA
                                        1 -> GlassTier.TierB
                                        else -> GlassTier.TierC
                                    }
                                },
                            )
                        }
                    }

                    // Sliders Panel
                    item {
                        GlassCard {
                            Text("Optical Parameters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                            Spacer(Modifier.height(12.dp))

                            // Blur
                            Text("Backdrop Blur: ${blurDp.roundToInt()} dp", style = MaterialTheme.typography.labelMedium, color = textColor)
                            GlassSlider(value = blurDp, onValueChange = { blurDp = it }, valueRange = 0f..32f)

                            // Tint Alpha
                            Text("Tint Alpha: ${(tintAlpha * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium, color = textColor)
                            GlassSlider(value = tintAlpha, onValueChange = { tintAlpha = it }, valueRange = 0.01f..0.25f)

                            // Refraction
                            Text("Edge Refraction: ${(refraction * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium, color = textColor)
                            GlassSlider(value = refraction, onValueChange = { refraction = it }, valueRange = 0f..0.50f)

                            // Dispersion
                            Text("Chromatic Dispersion: ${(dispersion * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium, color = textColor)
                            GlassSlider(value = dispersion, onValueChange = { dispersion = it }, valueRange = 0f..0.20f)

                            // Rim Light
                            Text("Rim Light Intensity: ${(rimLight * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium, color = textColor)
                            GlassSlider(value = rimLight, onValueChange = { rimLight = it }, valueRange = 0.10f..1.0f)

                            // Saturation
                            Text("Backdrop Saturation: ${(saturation * 10f).roundToInt() / 10f}x", style = MaterialTheme.typography.labelMedium, color = textColor)
                            GlassSlider(value = saturation, onValueChange = { saturation = it }, valueRange = 1.0f..2.5f)

                            // Brightness
                            Text("Backdrop Brightness: ${(brightness * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium, color = textColor)
                            GlassSlider(value = brightness, onValueChange = { brightness = it }, valueRange = 0.90f..1.30f)
                        }
                    }

                    item {
                        Text(
                            text = "GLASS COMPONENT CATALOG",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor.copy(alpha = 0.60f),
                        )
                    }

                    // Buttons & Icon Buttons
                    item {
                        GlassCard {
                            Text("Buttons & Icon Buttons", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                GlassButton(
                                    text = "Play Mix",
                                    onClick = {},
                                    leadingIcon = { GlassIconButton(onClick = {}, imageVector = Icons.Default.PlayArrow, size = 28.dp, iconSize = 16.dp) },
                                )

                                GlassIconButton(
                                    onClick = {},
                                    imageVector = Icons.Default.Favorite,
                                    tint = Color(0xFFFF4081),
                                )

                                GlassIconButton(
                                    onClick = {},
                                    imageVector = Icons.Default.MoreVert,
                                )

                                GlassProgressLoader(size = 40.dp)
                            }
                        }
                    }

                    // Chips & Switch
                    item {
                        GlassCard {
                            Text("Chips & Switch", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("All", "Music", "Podcasts").forEachIndexed { idx, label ->
                                        GlassChip(
                                            text = label,
                                            selected = selectedChipIndex == idx,
                                            onClick = { selectedChipIndex = idx },
                                        )
                                    }
                                }

                                GlassSwitch(
                                    checked = switchChecked,
                                    onCheckedChange = { switchChecked = it },
                                )
                            }
                        }
                    }

                    // Search Bar
                    item {
                        GlassCard {
                            Text("Glass Search Bar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                            Spacer(Modifier.height(12.dp))
                            GlassSearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                            )
                        }
                    }

                    // Slider & Segmented Control
                    item {
                        GlassCard {
                            Text("Glass Slider & Segmented Control", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                            Spacer(Modifier.height(12.dp))
                            GlassSlider(
                                value = sliderValue,
                                onValueChange = { sliderValue = it },
                            )
                            Spacer(Modifier.height(12.dp))
                            GlassSegmentedControl(
                                items = listOf("Songs", "Artists", "Albums"),
                                selectedIndex = selectedSegmentIndex,
                                onSelect = { selectedSegmentIndex = it },
                            )
                        }
                    }

                    // List Rows
                    item {
                        GlassCard {
                            Text("Glass List Rows", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                            Spacer(Modifier.height(12.dp))
                            GlassListRow(
                                title = "Starboy",
                                subtitle = "The Weeknd, Daft Punk",
                                onClick = {},
                                trailingContent = {
                                    GlassIconButton(onClick = {}, imageVector = Icons.Default.MoreVert, size = 36.dp)
                                },
                            )
                            Spacer(Modifier.height(8.dp))
                            GlassListRow(
                                title = "Blinding Lights",
                                subtitle = "The Weeknd • After Hours",
                                onClick = {},
                                trailingContent = {
                                    GlassIconButton(onClick = {}, imageVector = Icons.Default.MoreVert, size = 36.dp)
                                },
                            )
                        }
                    }

                    // Modal Triggers
                    item {
                        GlassCard {
                            Text("Modals & Overlays", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                GlassButton(text = "Open Sheet", onClick = { showSheet = true })
                                GlassButton(text = "Open Dialog", onClick = { showDialog = true })
                            }
                        }
                    }

                    // Mini Player Preview
                    item {
                        Text(
                            text = "FLOATING MINI PLAYER PREVIEW",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor.copy(alpha = 0.60f),
                        )
                        Spacer(Modifier.height(8.dp))
                        GlassMiniPlayer(
                            title = "Save Your Tears",
                            artist = "The Weeknd",
                            isPlaying = isMiniPlayerPlaying,
                            progress = 0.65f,
                            onTogglePlayPause = { isMiniPlayerPlaying = !isMiniPlayerPlaying },
                            onExpand = {},
                        )
                    }

                    // Bottom Tab Bar Preview
                    item {
                        Text(
                            text = "FLOATING TAB BAR PREVIEW",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor.copy(alpha = 0.60f),
                        )
                        Spacer(Modifier.height(8.dp))
                        GlassBottomTabBar(
                            items = listOf(
                                GlassTabItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
                                GlassTabItem("Search", Icons.Filled.Search, Icons.Outlined.Search),
                                GlassTabItem("Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
                            ),
                            selectedIndex = selectedTabIndex,
                            onSelectTab = { selectedTabIndex = it },
                        )
                        Spacer(Modifier.height(32.dp))
                    }
                }

                // Modal Bottom Sheet Preview
                if (showSheet) {
                    GlassBottomSheet(onDismissRequest = { showSheet = false }) {
                        Text(
                            text = "Clear Liquid Glass Sheet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Rendered with 28dp deep blur, 6% translucent tint, and specular gradient rim.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor.copy(alpha = 0.75f),
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                        Spacer(Modifier.height(24.dp))
                        GlassButton(text = "Done", onClick = { showSheet = false })
                    }
                }

                // Modal Dialog Preview
                if (showDialog) {
                    GlassDialog(
                        onDismissRequest = { showDialog = false },
                        title = "Glass Alert Dialog",
                        text = {
                            Text(
                                "This is a continuous 28dp squircle modal dialog with full backdrop refraction.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor.copy(alpha = 0.85f),
                            )
                        },
                        confirmButton = {
                            GlassButton(text = "Confirm", onClick = { showDialog = false })
                        },
                        dismissButton = {
                            GlassButton(text = "Dismiss", onClick = { showDialog = false })
                        },
                    )
                }
            }
        }
    }
}
