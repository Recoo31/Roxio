package kurd.reco.mobile.ui.player.composables

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.media3.common.C
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import kurd.reco.core.removeWatermark
import kurd.reco.mobile.R
import kurd.reco.mobile.ui.player.applySelectedTrack
import kurd.reco.mobile.ui.player.getName

@OptIn(UnstableApi::class)
@Composable
fun AudioSelectorDialog(
    tracks: Tracks,
    defaultTracks: DefaultTrackSelector,
    onDismiss: () -> Unit
) {
    val trackName = C.TRACK_TYPE_AUDIO
    val audioTracks = tracks.groups
        .filter { it.type == trackName && it.isSupported }

    Dialog(onDismissRequest = onDismiss) {
        OutlinedCard(
            modifier = Modifier
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.select_audio),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 16.dp)
                )
            }
            LazyColumn {
                itemsIndexed(audioTracks) { index, format ->
                    val selected = format.isSelected
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = {
                                applySelectedTrack(defaultTracks, format.mediaTrackGroup, 0, C.TRACK_TYPE_AUDIO)
                                onDismiss()
                            }
                        )
                        Text(
                            text = format.mediaTrackGroup.getName(trackName, index).removeWatermark(),
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}
