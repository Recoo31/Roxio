package kurd.reco.roxio.ui.player.composables

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import kurd.reco.roxio.ui.player.applySelectedTrack
import kurd.reco.roxio.ui.player.getName


@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerSubtitles(
    focusRequester: FocusRequester,
    tracks: Tracks,
    defaultTrackSelector: DefaultTrackSelector,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val trackName = C.TRACK_TYPE_TEXT

    val textTracks = tracks.groups
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
                "Subtitles",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                val selected = try {
                    !textTracks[0].isSelected
                } catch (e: Exception) {
                    true
                }

                VideoPlayerSettingItem(
                    focusRequester,
                    text = "Off",
                    isSelect = selected,
                    onClick = {
                        setSubtitleTrack(defaultTrackSelector, context, false)
                        onBack()
                    }
                )
            }

            itemsIndexed(textTracks) { index, trackGroup ->
                val selected = trackGroup.isSelected
                val text = trackGroup.mediaTrackGroup.getName(trackName, index)
                println(text)

                VideoPlayerSettingItem(
                    focusRequester,
                    text = text.replace("Subtitle Track #${index + 1} -", ""),
                    isSelect = selected,
                    onClick = {
                        setSubtitleTrack(defaultTrackSelector, context, true)
                        applySelectedTrack(
                            defaultTrackSelector,
                            trackGroup.mediaTrackGroup,
                            0,
                            C.TRACK_TYPE_TEXT
                        )
                        onBack()
                    }
                )
            }
        }
    }
}


@OptIn(UnstableApi::class)
private fun setSubtitleTrack(
    trackSelector: DefaultTrackSelector,
    context: Context,
    isActive: Boolean
) {
    trackSelector.parameters = DefaultTrackSelector.Parameters.Builder(context)
        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, !isActive).build()
}