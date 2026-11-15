package com.lastwave.app.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lastwave.app.R
import com.lastwave.app.ui.common.adaptiveContentWidth
import com.lastwave.app.ui.glass.DynamicMeshBackground
import com.lastwave.app.ui.glass.GlassButton
import com.lastwave.app.ui.glass.GlassButtonVariant
import com.lastwave.app.ui.glass.GlassCard
import com.lastwave.app.ui.glass.clearLiquidGlass

/**
 * Clear Liquid Glass Spotify-style onboarding.
 *
 * Renders an animated mesh gradient background with a centered Clear Liquid
 * Glass card offering YouTube Music login, Spotify account sync, and Guest mode.
 */
@Composable
fun LoginScreen(
    onLoginWithYouTube: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onLoginWithSpotify: (() -> Unit)? = null,
    onRestoreBackupAndSignIn: ((String) -> Unit)? = null,
    onDismissError: (() -> Unit)? = null,
    errorMessage: String? = null,
    isBusy: Boolean = false,
    onOpenDownloads: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    var restoreReadError by remember {
        mutableStateOf(
            if (com.lastwave.app.di.DataStoreModule.consumePrefsReset()) context.getString(R.string.login_prefs_reset) else null,
        )
    }
    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null && onRestoreBackupAndSignIn != null) {
            val content = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: error("Could not read this backup")
            }
            content.onSuccess {
                restoreReadError = null
                onRestoreBackupAndSignIn(it)
            }.onFailure {
                restoreReadError = it.message ?: "Could not read this backup"
            }
        }
    }

    DynamicMeshBackground(
        modifier = Modifier.fillMaxSize(),
        artworkColors = listOf(Color(0xFF1DB954), Color(0xFFE50914), Color(0xFF6200EE)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            GlassCard(
                modifier = Modifier
                    .adaptiveContentWidth(maxWidth = 440.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clearLiquidGlass(shape = CircleShape, customBlurDp = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp),
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.set_about_title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp,
                        ),
                        color = Color.White,
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.login_tagline),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.70f),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(32.dp))

                    // 1. YouTube Music login
                    GlassButton(
                        text = stringResource(R.string.login_yt),
                        onClick = onLoginWithYouTube,
                        enabled = !isBusy,
                        variant = GlassButtonVariant.Primary,
                        icon = Icons.Filled.PlayCircle,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    )

                    if (onLoginWithSpotify != null) {
                        Spacer(Modifier.height(12.dp))
                        // 2. Spotify sync
                        GlassButton(
                            text = "Sync with Spotify",
                            onClick = onLoginWithSpotify,
                            enabled = !isBusy,
                            variant = GlassButtonVariant.Secondary,
                            icon = Icons.Filled.Sync,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // 3. Guest / Skip
                    GlassButton(
                        text = stringResource(R.string.login_guest),
                        onClick = onContinueAsGuest,
                        enabled = !isBusy,
                        variant = GlassButtonVariant.Ghost,
                        icon = Icons.Filled.PersonOutline,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.login_guest_desc),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color.White.copy(alpha = 0.55f),
                        textAlign = TextAlign.Center,
                    )

                    if (onRestoreBackupAndSignIn != null) {
                        Spacer(Modifier.height(16.dp))
                        GlassButton(
                            text = stringResource(R.string.login_restore),
                            onClick = { restoreBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                            enabled = !isBusy,
                            variant = GlassButtonVariant.Ghost,
                            icon = Icons.Filled.CloudDownload,
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                        )
                    }

                    if (onOpenDownloads != null) {
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = onOpenDownloads) {
                            Icon(
                                imageVector = Icons.Filled.Download,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.padding(end = 6.dp).size(18.dp),
                            )
                            Text(
                                stringResource(R.string.login_downloads),
                                color = Color.White.copy(alpha = 0.8f),
                            )
                        }
                    }

                    val visibleError = errorMessage ?: restoreReadError
                    if (visibleError != null) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = visibleError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(4.dp))
                        if (onDismissError != null) {
                            TextButton(onClick = {
                                restoreReadError = null
                                onDismissError()
                            }) {
                                Text(
                                    stringResource(R.string.gen_dismiss),
                                    color = Color.White.copy(alpha = 0.9f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
