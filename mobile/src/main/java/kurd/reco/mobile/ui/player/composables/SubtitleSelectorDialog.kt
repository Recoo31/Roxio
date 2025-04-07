package kurd.reco.mobile.ui.player.composables

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
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
fun SubtitleSelectorDialog(
    tracks: Tracks,
    defaultTrackSelector: DefaultTrackSelector,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val trackName = C.TRACK_TYPE_TEXT

    val textTracks = tracks.groups
        .filter { it.type == trackName && it.isSupported }

    println("textTracks: ${textTracks.size}")

    Dialog(onDismissRequest = onDismiss) {
        OutlinedCard(
            modifier = Modifier
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.select_subtitle),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 16.dp)
                )
            }
            LazyColumn {
                item {
                    val selected = try {
                        !textTracks[0].isSelected
                    } catch (e: Exception) {
                        true
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = {
                                setSubtitleTrack(defaultTrackSelector, context, false)
                                onDismiss()
                            }
                        )
                        Text(text = stringResource(R.string.disable_subtitles))
                    }
                }

                itemsIndexed(textTracks) { index, trackGroup ->
                    val selected = trackGroup.isSelected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = {
                                setSubtitleTrack(defaultTrackSelector, context, true)
                                applySelectedTrack(
                                    defaultTrackSelector,
                                    trackGroup.mediaTrackGroup,
                                    0,
                                    C.TRACK_TYPE_TEXT
                                )
                                onDismiss()
                            }
                        )
                        Text(text = trackGroup.mediaTrackGroup.getName(trackName, index).removeWatermark())
                    }
                }
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