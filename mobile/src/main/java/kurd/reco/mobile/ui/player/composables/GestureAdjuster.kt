package kurd.reco.mobile.ui.player.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Brightness5
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kurd.reco.mobile.R
import kurd.reco.mobile.ui.player.adjustVolume
import kurd.reco.mobile.ui.player.getCurrentBrightness
import kurd.reco.mobile.ui.player.getSystemVolume
import kurd.reco.mobile.ui.player.setTemporaryBrightness


@Composable
fun GestureAdjuster(
) {
    val context = LocalContext.current

    var initialY by remember { mutableFloatStateOf(0f) }
    var brightnessLevel by remember { mutableFloatStateOf(getCurrentBrightness(context)) }
    var volumeLevel by remember { mutableFloatStateOf(getSystemVolume(context)) }
    var showBrightnessOverlay by remember { mutableStateOf(false) }
    var showVolumeOverlay by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Row {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            initialY = it.y
                            showBrightnessOverlay = true
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            val brightnessChange = dragAmount / 500f
                            brightnessLevel = (brightnessLevel - brightnessChange).coerceIn(0f, 1f)
                            setTemporaryBrightness(context, brightnessLevel)
                            showBrightnessOverlay = true
                        },
                        onDragEnd = {
                            scope.launch {
                                delay(500L)
                                showBrightnessOverlay = false
                            }
                        }
                    )
                }
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            initialY = it.y
                            showVolumeOverlay = true
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            val volumeChange = dragAmount / 800f
                            volumeLevel = (volumeLevel - volumeChange).coerceIn(0f, 1f)
                            adjustVolume(context, volumeLevel)
                            showVolumeOverlay = true
                        },
                        onDragEnd = {
                            scope.launch {
                                delay(500L)
                                showVolumeOverlay = false
                            }
                        }
                    )
                }
        )

        // Brightness overlay
        if (showBrightnessOverlay) {
            val level = "${(brightnessLevel * 100).toInt()}"
            LevelOverlay(isVolume = false, level = level)
        }

        // Volume overlay
        if (showVolumeOverlay) {
            val level = "${(volumeLevel * 100).toInt()}"
            LevelOverlay(isVolume = true, level = level)
        }
    }
}

@Composable
fun LevelOverlay(modifier: Modifier = Modifier, isVolume: Boolean, level: String) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = if (isVolume) Icons.AutoMirrored.Outlined.VolumeUp else Icons.Outlined.Brightness5

            Icon(icon, contentDescription = null)
            Text(text = level, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}