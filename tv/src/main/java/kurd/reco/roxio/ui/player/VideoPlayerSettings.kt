package kurd.reco.roxio.ui.player

import androidx.annotation.OptIn
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Hd
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import kurd.reco.roxio.R
import kurd.reco.roxio.ui.player.composables.VideoPlayerAudio
import kurd.reco.roxio.ui.player.composables.VideoPlayerQuality
import kurd.reco.roxio.ui.player.composables.VideoPlayerSources
import kurd.reco.roxio.ui.player.composables.VideoPlayerSubtitles

enum class SettingsScreen {
    MAIN,
    SOURCES,
    QUALITY,
    AUDIO,
    SUBTITLE
}

@OptIn(UnstableApi::class)
@Composable
fun SettingsDialog(
    exoPlayer: ExoPlayer,
    defaultTrackSelector: DefaultTrackSelector,
    focusRequester: FocusRequester,
    onErrorFlag: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(SettingsScreen.MAIN) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(0.3f)
            .wrapContentHeight(),
        shape = CardDefaults.shape(shape = MaterialTheme.shapes.small),
        onClick = { focusRequester.requestFocus() },
        colors = CardDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Box {
            when (currentScreen) {
                SettingsScreen.MAIN -> {
                    MainSettings(
                        focusRequester = focusRequester,
                        onSettingSelected = { setting ->
                            when (setting) {
                                SettingsScreen.SOURCES -> currentScreen = SettingsScreen.SOURCES
                                SettingsScreen.QUALITY -> currentScreen = SettingsScreen.QUALITY
                                SettingsScreen.AUDIO -> currentScreen = SettingsScreen.AUDIO
                                SettingsScreen.SUBTITLE -> currentScreen = SettingsScreen.SUBTITLE
                                else -> currentScreen == SettingsScreen.MAIN
                            }
                        },
                        onErrorFlag = { onErrorFlag() }
                    )
                }

                SettingsScreen.SOURCES -> {
                    VideoPlayerSources(
                        exoPlayer = exoPlayer,
                        focusRequester = focusRequester,
                        onBack = { currentScreen = SettingsScreen.MAIN }
                    )
                }

                SettingsScreen.QUALITY -> {
                    VideoPlayerQuality(
                        exoPlayer = exoPlayer,
                        focusRequester = focusRequester,
                        tracks = exoPlayer.currentTracks,
                        defaultTrackSelector = defaultTrackSelector,
                        onBack = { currentScreen = SettingsScreen.MAIN }
                    )
                }

                SettingsScreen.AUDIO -> {
                    VideoPlayerAudio(
                        focusRequester = focusRequester,
                        tracks = exoPlayer.currentTracks,
                        defaultTracks = defaultTrackSelector,
                        onBack = { currentScreen = SettingsScreen.MAIN }
                    )
                }

                SettingsScreen.SUBTITLE -> {
                    VideoPlayerSubtitles(
                        focusRequester = focusRequester,
                        tracks = exoPlayer.currentTracks,
                        defaultTrackSelector = defaultTrackSelector,
                        onBack = { currentScreen = SettingsScreen.MAIN }
                    )
                }
            }
        }
    }
}


@Composable
fun MainSettings(
    focusRequester: FocusRequester,
    onSettingSelected: (SettingsScreen) -> Unit,
    onErrorFlag: () -> Unit
) {
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column {
        VideoSettingItem(Icons.Outlined.Flag, stringResource(R.string.report), focusRequester) {
            onErrorFlag()
        }
        VideoSettingItem(Icons.Outlined.Cloud, stringResource(R.string.sources_settings), focusRequester) {
            onSettingSelected(SettingsScreen.SOURCES)
        }
        VideoSettingItem(Icons.Outlined.Hd, stringResource(R.string.quality_settings), focusRequester) {
            onSettingSelected(SettingsScreen.QUALITY)
        }
        VideoSettingItem(Icons.Outlined.Audiotrack, stringResource(R.string.audio_settings), focusRequester) {
            onSettingSelected(SettingsScreen.AUDIO)
        }
        VideoSettingItem(Icons.Outlined.Subtitles, stringResource(R.string.subtitle_settings), focusRequester) {
            onSettingSelected(SettingsScreen.SUBTITLE)
        }
    }
}

@Composable
private fun VideoSettingItem(
    icon: ImageVector,
    title: String,
    focusRequester: FocusRequester,
    onSettingsClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        modifier = Modifier
            .padding(4.dp)
            .border(2.dp, borderColor, shape = MaterialTheme.shapes.small)
            .focusRequester(focusRequester)
            .onFocusChanged {
                isFocused = it.isFocused
            }
            .focusable(),
        onClick = onSettingsClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
