package kurd.reco.mobile.ui.player.composables

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.media3.exoplayer.ExoPlayer
import kurd.reco.core.MainVM
import kurd.reco.mobile.ui.player.createMediaItem
import org.koin.compose.koinInject

@Composable
fun SourceSelectorDialog(
    exoPlayer: ExoPlayer,
    onDismiss: () -> Unit
) {
    val viewModel: MainVM = koinInject()

    val urls = viewModel.playDataModel?.urls
    if (urls.isNullOrEmpty()) return

    Dialog(onDismissRequest = onDismiss) {
        OutlinedCard(
            modifier = Modifier
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select Source",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.Center).padding(top = 16.dp)
                )
            }

            LazyColumn {
                items(urls) { item ->
                    val selected =
                        exoPlayer.currentMediaItem?.localConfiguration?.uri.toString() == item.second
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = {
                                val mediaItem =
                                    createMediaItem(viewModel.playDataModel!!, item.second)
                                exoPlayer.setMediaItem(mediaItem)
                                onDismiss()
                            }
                        )
                        Text(text = item.first, modifier = Modifier.padding(4.dp))
                    }
                }
            }
        }
    }
}