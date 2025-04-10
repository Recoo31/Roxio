/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kurd.reco.roxio.ui.player.composables

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import kurd.reco.roxio.common.VideoPlayerState
import kurd.reco.roxio.ui.player.formatDuration
import kurd.reco.roxio.ui.player.handleDPadKeyEvents
import kurd.reco.roxio.ui.player.ifElse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun RowScope.VideoPlayerControllerIndicator(
    progress: Float,
    bufferedProgress: Float,
    onSeek: (seekProgress: Float) -> Unit,
    state: VideoPlayerState,
    contentDuration: Duration = Duration.ZERO
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isSelected by remember { mutableStateOf(false) }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val color by rememberUpdatedState(
        newValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    )
    val bufferedColor by rememberUpdatedState(
        newValue = color.copy(alpha = 0.4f)
    )
    val inactiveColor by rememberUpdatedState(
        newValue = color.copy(alpha = 0.2f)
    )

    val animatedIndicatorHeight by animateDpAsState(
        targetValue = 4.dp.times((if (isFocused) 2.5f else 1f))
    )
    var seekProgress by remember { mutableFloatStateOf(0f) }
    
    // For time bubble positioning
    var barWidth by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    
    // Format seek time
    val seekTime = remember(seekProgress, contentDuration) {
        val millis = (contentDuration.inWholeMilliseconds * seekProgress).toLong()
        formatDuration(millis.milliseconds)
    }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            state.showControls(seconds = Int.MAX_VALUE)
        } else {
            state.showControls()
        }
    }

    val handleSeekEventModifier = Modifier.handleDPadKeyEvents(
        onEnter = {
            isSelected = !isSelected
            onSeek(seekProgress)
        },
        onLeft = {
            seekProgress = (seekProgress - 0.03f).coerceAtLeast(0f)
        },
        onRight = {
            seekProgress = (seekProgress + 0.03f).coerceAtMost(1f)
        },
    )

    val handleDpadCenterClickModifier = Modifier.handleDPadKeyEvents(
        onEnter = {
            seekProgress = progress
            isSelected = !isSelected
        }
    )

    Box(
        modifier = Modifier.weight(1f),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Seekbar
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(animatedIndicatorHeight)
                .padding(horizontal = 4.dp)
                .ifElse(
                    condition = isSelected,
                    ifTrueModifier = handleSeekEventModifier,
                    ifFalseModifier = handleDpadCenterClickModifier
                )
                .focusable(interactionSource = interactionSource)
                .onGloballyPositioned { coordinates ->
                    barWidth = coordinates.size.width.toFloat()
                },
            onDraw = {
                val yOffset = size.height / 2
                val barWidth = size.width
                val barHeight = size.height
                val bufferedEndX = (barWidth * bufferedProgress).coerceIn(0f, barWidth)

                drawLine(
                    color = inactiveColor,
                    start = Offset(x = 0f, y = yOffset),
                    end = Offset(x = barWidth, y = yOffset),
                    strokeWidth = barHeight,
                    cap = StrokeCap.Round
                )
                if (bufferedEndX > 0f) {
                    drawLine(
                        color = bufferedColor,
                        start = Offset(x = 0f, y = yOffset),
                        end = Offset(x = bufferedEndX, y = yOffset),
                        strokeWidth = barHeight,
                        cap = StrokeCap.Round
                    )
                }
                drawLine(
                    color = color,
                    start = Offset(x = 0f, y = yOffset),
                    end = Offset(
                        x = barWidth.times(if (isSelected) seekProgress else progress),
                        y = yOffset
                    ),
                    strokeWidth = barHeight,
                    cap = StrokeCap.Round
                )
            }
        )
        
        // Time bubble
        if (isSelected) {
            val bubbleWidth = with(density) { 70.dp.toPx() }
            val seekX = barWidth * seekProgress
            val bubbleX = seekX - (bubbleWidth / 2)
            val clampedX = bubbleX.coerceIn(0f, barWidth - bubbleWidth)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp) // Match the padding of the seekbar
                    .offset(y = (-28).dp) // Move up above the seekbar
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = with(density) { clampedX.toDp() })
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = seekTime,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}
