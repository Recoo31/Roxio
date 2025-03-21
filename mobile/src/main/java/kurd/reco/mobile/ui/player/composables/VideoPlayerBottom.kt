package kurd.reco.mobile.ui.player.composables

import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import kurd.reco.mobile.R
import kurd.reco.mobile.ui.player.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerBottom(
    exoPlayer: ExoPlayer,
    currentTime: Long,
    duration: Long
) {
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeDisplay(currentTime)

                if (duration > 0) {
                    val trackHeight = 2.dp
                    val thumbSize = DpSize(15.dp, 15.dp)

                    Slider(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp, end = 8.dp),
                        value = currentTime.toFloat(),
                        onValueChange = { exoPlayer.seekTo(it.toLong()) },
                        valueRange = 0f..duration.toFloat(),
                        interactionSource = interaction,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        ),
                        thumb = {
                            val modifier =
                                Modifier
                                    .size(thumbSize)
                                    .shadow(1.dp, CircleShape, clip = false)
                                    .indication(
                                        interactionSource = interaction,
                                        indication = ripple(bounded = false, radius = 20.dp)
                                    )
                            SliderDefaults.Thumb(
                                interactionSource = interaction,
                                modifier = modifier,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                )
                            )
                        },
                        track = {
                            val modifier = Modifier.height(trackHeight)
                            SliderDefaults.Track(
                                sliderState = it,
                                modifier = modifier,
                                thumbTrackGapSize = 0.dp,
                                trackInsideCornerSize = 0.dp,
                                drawStopIndicator = null,
                                colors = SliderDefaults.colors(
                                    activeTrackColor = Color.White,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                )
                            )
                        }
                    )
                }

                TimeDisplay(duration)
            }
        }
    }
}

@Composable
private fun TimeDisplay(time: Long) {
    Text(
        text = formatTime(time),
        color = Color.White,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}