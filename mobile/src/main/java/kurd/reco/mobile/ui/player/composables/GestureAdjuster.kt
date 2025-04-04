package kurd.reco.mobile.ui.player.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeDown
import androidx.compose.material.icons.automirrored.outlined.VolumeMute
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Brightness5
import androidx.compose.material.icons.outlined.BrightnessHigh
import androidx.compose.material.icons.outlined.BrightnessLow
import androidx.compose.material.icons.outlined.VolumeMute
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    modifier: Modifier
) {
    val context = LocalContext.current

    var initialY by remember { mutableFloatStateOf(0f) }
    var brightnessLevel by remember { mutableFloatStateOf(getCurrentBrightness(context)) }
    var volumeLevel by remember { mutableFloatStateOf(getSystemVolume(context)) }
    var showBrightnessOverlay by remember { mutableStateOf(false) }
    var showVolumeOverlay by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(modifier.padding(36.dp)) {
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
                                brightnessLevel =
                                    (brightnessLevel - brightnessChange).coerceIn(0f, 1f)
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
                                val volumeChange = dragAmount / 600f
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
        }
        if (showVolumeOverlay) {
            LevelOverlay(
                modifier = Modifier.align(Alignment.CenterStart),
                isVolume = true,
                level = volumeLevel
            )
        }

        if (showBrightnessOverlay) {
            LevelOverlay(
                modifier = Modifier.align(Alignment.CenterEnd),
                isVolume = false,
                level = brightnessLevel
            )
        }
    }
}

@Composable
fun LevelOverlay(modifier: Modifier = Modifier, isVolume: Boolean, level: Float) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val icon: ImageVector = when {
                isVolume -> when {
                    level == 0f -> Icons.AutoMirrored.Outlined.VolumeMute
                    level < 0.5f -> Icons.AutoMirrored.Outlined.VolumeDown
                    else -> Icons.AutoMirrored.Outlined.VolumeUp
                }
                else -> when {
                    level < 0.3f -> Icons.Outlined.BrightnessLow
                    level < 0.7f -> Icons.Outlined.Brightness5
                    else -> Icons.Outlined.BrightnessHigh
                }
            }

            Icon(icon, contentDescription = null, tint = Color.White)

            // Vertical slider bar
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(128.dp)
                    .background(Color.Gray.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .width(8.dp)
                        .fillMaxHeight(level)
                        .background(Color.White, shape = MaterialTheme.shapes.small)
                )
            }
        }
    }
}