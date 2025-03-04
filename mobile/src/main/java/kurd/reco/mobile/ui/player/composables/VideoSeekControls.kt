package kurd.reco.mobile.ui.player.composables

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kurd.reco.mobile.R

@Composable
fun VideoSeekControls(modifier: Modifier = Modifier, exoPlayer: ExoPlayer) {
    var isRewinding by remember { mutableStateOf(false) }
    var isForwarding by remember { mutableStateOf(false) }
    var exoPlayStatus by remember { mutableStateOf(exoPlayer.isPlaying) }

    val rewindRotation by animateFloatAsState(
        targetValue = if (isRewinding) -50f else 0f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
    )
    val forwardRotation by animateFloatAsState(
        targetValue = if (isForwarding) 50f else 0f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
    )

    val textTranslation by animateFloatAsState(
        targetValue = if (isForwarding) 300f else if (isRewinding) -300f else 0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
    )

    val isForwardingText = if (isForwarding) "+" else ""
    val isRewindingText = if (isRewinding) "-" else ""

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {

            IconButton(
                onClick = {
                    isRewinding = true
                    exoPlayer.seekTo(exoPlayer.currentPosition - 10000)
                },
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    modifier = Modifier.graphicsLayer {
                        this.rotationZ = rewindRotation
                    },
                    painter = painterResource(id = R.drawable.netflix_skip_rewind),
                    contentDescription = "Rewind 10 Seconds",
                    tint = Color.White
                )
            }

            Text(
                text = "${isRewindingText}10",
                modifier = Modifier.graphicsLayer {
                    if (isRewinding) {
                        this.translationX = textTranslation
                    }
                },
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.padding(horizontal = 40.dp))

        IconButton(
            onClick = {
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                    exoPlayStatus = false
                } else {
                    exoPlayer.play()
                    exoPlayStatus = true
                }
            },
            modifier = Modifier.size(80.dp)
        ) {
            val playIcon =
                if (exoPlayStatus) R.drawable.netflix_pause else R.drawable.netflix_play

            Icon(
                painter = painterResource(id = playIcon),
                contentDescription = "Pause/Play",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.padding(horizontal = 40.dp))

        Box(contentAlignment = Alignment.Center) {
            IconButton(
                onClick = {
                    isForwarding = true
                    exoPlayer.seekTo(exoPlayer.currentPosition + 10000)
                },
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.netflix_skip_forward),
                    contentDescription = "Forward",
                    modifier = Modifier.graphicsLayer {
                        this.rotationZ = forwardRotation
                    },
                    tint = Color.White
                )
            }
            Text(
                text = "${isForwardingText}10",
                modifier = Modifier.graphicsLayer {
                    if (isForwarding) {
                        this.translationX = textTranslation
                    }
                },
                color = Color.White
            )
        }
    }

    LaunchedEffect(isForwarding, isRewinding) {
        if (isForwarding || isRewinding)
            delay(150)
        isForwarding = false
        isRewinding = false
    }
}