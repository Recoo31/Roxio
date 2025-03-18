package kurd.reco.roxio.ui.player.composables

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import kurd.reco.roxio.R
import kurd.reco.roxio.ui.player.applySelectedTrack

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerQuality(
    exoPlayer: ExoPlayer,
    focusRequester: FocusRequester,
    tracks: Tracks,
    defaultTrackSelector: DefaultTrackSelector,
    onBack: () -> Unit
) {
    val videoTracks = tracks.groups
        .filter { it.type == C.TRACK_TYPE_VIDEO && it.isSupported }

    val videoSize by remember { mutableStateOf(exoPlayer.videoSize) }
    val videoBitrate = exoPlayer.videoFormat?.bitrate ?: -1

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { onBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                stringResource(R.string.quality_settings),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(videoTracks) { trackGroup ->
                val formats = (0 until trackGroup.mediaTrackGroup.length)
                    .asSequence()
                    .map { trackGroup.mediaTrackGroup.getFormat(it) }
                    .groupBy { it.height }
                    .map { (_, formats) -> formats.maxByOrNull { it.bitrate } }
                    .filterNotNull()
                    .sortedByDescending { it.height }

                formats.forEach { format ->
                    val width = format.width
                    val height = format.height
                    val bitrate = format.bitrate

                    val selected = width == videoSize.width && height == videoSize.height && bitrate == videoBitrate

                    val index = trackGroup.mediaTrackGroup.indexOf(format)

                    VideoPlayerSettingItem(
                        focusRequester,
                        text = "${width}x${height} | ${formatBitrate(bitrate)}",
                        isSelect = selected
                    ) {
                        applySelectedTrack(defaultTrackSelector, trackGroup.mediaTrackGroup, index, C.TRACK_TYPE_VIDEO)
                        onBack()
                    }
                }
            }
        }
    }
}

private fun formatBitrate(bitrate: Int): String {
    return when {
        bitrate >= 1000000 -> "${bitrate / 1000000}Mbps"
        bitrate >= 1000 -> "${bitrate / 1000}kbps"
        else -> "$bitrate bps"
    }
}