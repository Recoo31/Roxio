package kurd.reco.roxio.ui.player.composables

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import kurd.reco.core.Global
import kurd.reco.roxio.R
import kurd.reco.roxio.ui.player.createMediaItem

@Composable
fun VideoPlayerSources(
    exoPlayer: ExoPlayer,
    focusRequester: FocusRequester,
    onBack: () -> Unit
) {

    val urls = Global.playDataModel?.urls
    if (urls.isNullOrEmpty()) return
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
                stringResource(R.string.sources_settings),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(urls) { item ->
                val selected = exoPlayer.currentMediaItem?.localConfiguration?.uri.toString() == item.second

                VideoPlayerSettingItem(focusRequester, text = item.first, isSelect = selected) {
                    val mediaItem =
                        createMediaItem(Global.playDataModel!!, item.second)
                    exoPlayer.setMediaItem(mediaItem)
                    onBack()
                }
            }
        }

    }

}
