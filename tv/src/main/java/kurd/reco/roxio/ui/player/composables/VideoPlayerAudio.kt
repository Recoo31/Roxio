package kurd.reco.roxio.ui.player.composables

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import kurd.reco.roxio.R
import kurd.reco.roxio.ui.player.applySelectedTrack
import kurd.reco.roxio.ui.player.getName

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerAudio(
    focusRequester: FocusRequester,
    tracks: Tracks,
    defaultTracks: DefaultTrackSelector,
    onBack: () -> Unit
) {
    val trackName = C.TRACK_TYPE_AUDIO
    val audioTracks = tracks.groups
        .filter { it.type == trackName && it.isSupported }

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
                stringResource(R.string.audio_settings),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            itemsIndexed(audioTracks) { index, format ->
                val selected = format.isSelected
                val title = format.mediaTrackGroup.getName(trackName, index)

                VideoPlayerSettingItem(
                    focusRequester,
                    text = title.replace("Audio #${index+1} -", ""),
                    isSelect = selected
                ) {
                    applySelectedTrack(defaultTracks, format.mediaTrackGroup, 0, C.TRACK_TYPE_AUDIO)
                    onBack()
                }
            }
        }
    }

}