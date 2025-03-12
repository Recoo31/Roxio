package kurd.reco.mobile.ui.player.composables

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.media3.common.C
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import kurd.reco.mobile.R
import kurd.reco.mobile.ui.player.applySelectedTrack

@OptIn(UnstableApi::class)
@Composable
fun QualitySelectorDialog(
    exoPlayer: ExoPlayer,
    tracks: Tracks,
    defaultTrackSelector: DefaultTrackSelector,
    onDismiss: () -> Unit
) {
    val videoTracks = tracks.groups
        .filter { it.type == C.TRACK_TYPE_VIDEO && it.isSupported }

    val videoSize by remember { mutableStateOf(exoPlayer.videoSize) }
    val videoBitrate = exoPlayer.videoFormat?.bitrate ?: -1


    Dialog(onDismissRequest = onDismiss) {
        OutlinedCard(
            modifier = Modifier
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.select_quality),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 16.dp)
                )
            }

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

                        val selected =
                            width == videoSize.width && height == videoSize.height && bitrate == videoBitrate

                        val index = trackGroup.mediaTrackGroup.indexOf(format)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = {
                                    applySelectedTrack(
                                        defaultTrackSelector,
                                        trackGroup.mediaTrackGroup,
                                        index,
                                        C.TRACK_TYPE_VIDEO
                                    )
                                    onDismiss()
                                }
                            )
                            Text(
                                text = "${width}x${height} | ${formatBitrate(bitrate)}",
                                modifier = Modifier.padding(4.dp)
                            )
                        }
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