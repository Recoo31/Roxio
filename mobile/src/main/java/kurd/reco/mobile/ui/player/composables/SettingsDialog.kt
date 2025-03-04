package kurd.reco.mobile.ui.player.composables

import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import kurd.reco.mobile.R
import kurd.reco.mobile.common.VideoSettingItem

@OptIn(UnstableApi::class)
@Composable
fun SettingsDialog(
    exoPlayer: ExoPlayer,
    defaultTrackSelector: DefaultTrackSelector,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSetting by remember { mutableStateOf("") }
    val trackSelector = exoPlayer.currentTracks
    var onSettingsDismiss by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 64.dp, end = 8.dp)
            .clickable { onDismiss() },
        contentAlignment = Alignment.TopEnd
    ) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth(0.3f)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.outlinedCardColors().copy(containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
        ) {
            VideoSettingItem(R.drawable.cloud, stringResource(R.string.sources_settings)) {
                selectedSetting = "Sources"
                onSettingsDismiss = false
            }
            VideoSettingItem(R.drawable.hd_icon, stringResource(R.string.quality_settings)) {
                selectedSetting = "Quality"
                onSettingsDismiss = false
            }
            VideoSettingItem(R.drawable.audio, stringResource(R.string.audio_settings)) {
                selectedSetting = "Audio"
                onSettingsDismiss = false
            }
            VideoSettingItem(R.drawable.subtitles, stringResource(R.string.subtitle_settings)) {
                selectedSetting = "Subtitle"
                onSettingsDismiss = false
            }
        }

        if (!onSettingsDismiss) {
            when (selectedSetting) {
                "Sources" -> {
                    SourceSelectorDialog(exoPlayer) { onSettingsDismiss = true }
                }
                "Quality" -> {
                    QualitySelectorDialog(exoPlayer, trackSelector, defaultTrackSelector) { onSettingsDismiss = true }
                }
                "Audio" -> {
                    AudioSelectorDialog(trackSelector, defaultTrackSelector) { onSettingsDismiss = true }
                }
                "Subtitle" -> {
                    SubtitleSelectorDialog(trackSelector, defaultTrackSelector) { onSettingsDismiss = true }
                }
            }
        }
    }
}

