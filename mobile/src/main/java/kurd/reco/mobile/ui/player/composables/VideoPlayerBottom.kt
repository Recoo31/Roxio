package kurd.reco.mobile.ui.player.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
    duration: Long,
    bufferedPosition: Long
) {
    val interaction = remember { MutableInteractionSource() }
    var sliderPosition by remember { mutableFloatStateOf(currentTime.toFloat()) }
    var isSliding by remember { mutableStateOf(false) }

    // Update slider position when not sliding
    LaunchedEffect(currentTime) {
        if (!isSliding) {
            sliderPosition = currentTime.toFloat()
        }
    }

    val onSeekBarValueChange: (Float) -> Unit = { value ->
        sliderPosition = value
        isSliding = true
    }

    val onSeekBarValueChangeFinished: () -> Unit = {
        if (duration > 0) {
            exoPlayer.seekTo(sliderPosition.toLong())
        }
        isSliding = false
    }

    val activeTrackColor = Color.White
    val inactiveTrackColor = Color.White.copy(alpha = 0.2f)
    val bufferedTrackColor = Color.White.copy(alpha = 0.5f)
    val thumbColor = Color.White

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
                TimeDisplay(if (isSliding) sliderPosition.toLong() else currentTime)

                if (duration > 0) {
                    val trackHeight = 3.dp
                    val thumbSize = DpSize(15.dp, 15.dp)

                    val sliderValue = if (isSliding) sliderPosition else currentTime.toFloat()

                    val coercedSliderValue = sliderValue.coerceIn(0f, duration.toFloat())
                    val coercedBufferedValue = bufferedPosition.toFloat().coerceIn(0f, duration.toFloat())

                    Slider(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        value = coercedSliderValue,
                        onValueChange = onSeekBarValueChange,
                        onValueChangeFinished = onSeekBarValueChangeFinished,
                        valueRange = 0f..duration.toFloat(),
                        interactionSource = interaction,
                        thumb = {
                            SliderDefaults.Thumb(
                                interactionSource = interaction,
                                modifier = Modifier
                                    .size(thumbSize)
                                    .shadow(1.dp, CircleShape, clip = false)
                                    .indication(
                                        interactionSource = interaction,
                                        indication = ripple(bounded = false, radius = 20.dp)
                                    ),
                                colors = SliderDefaults.colors(thumbColor = thumbColor)
                            )
                        },
                        track = { sliderState ->
                            val activeFraction = if (duration > 0) (sliderState.value / duration) else 0f
                            val bufferedFraction = if (duration > 0) (coercedBufferedValue / duration) else 0f

                            Box(
                                modifier = Modifier
                                    .height(trackHeight)
                                    .fillMaxWidth()
                                    .background(inactiveTrackColor)
                            ) {
                                // Buffered portion
                                Box(
                                    modifier = Modifier
                                        .height(trackHeight)
                                        .fillMaxWidth(bufferedFraction.coerceIn(0f, 1f))
                                        .background(bufferedTrackColor)
                                )
                                // Active portion (played)
                                Box(
                                    modifier = Modifier
                                        .height(trackHeight)
                                        .fillMaxWidth(activeFraction.coerceIn(0f, 1f))
                                        .background(activeTrackColor)
                                )
                            }
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