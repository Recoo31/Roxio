/*
 * Copyright 2024 Google LLC
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

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kurd.reco.roxio.R
import kurd.reco.roxio.common.VideoPlayerState
import kurd.reco.roxio.ui.player.formatDuration
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO

@Composable
fun VideoPlayerSeeker(
    focusRequester: FocusRequester,
    state: VideoPlayerState,
    isPlaying: Boolean,
    bufferedPosition: Long,
    onPlayPauseToggle: (Boolean) -> Unit,
    onSeek: (Float) -> Unit,
    contentProgress: Duration,
    contentDuration: Duration
) {
    val durationMillis = contentDuration.inWholeMilliseconds

    val currentProgressFloat = if (durationMillis > 0) {
        (contentProgress.inWholeMilliseconds.toFloat() / durationMillis).coerceIn(0f, 1f)
    } else 0f

    val bufferedProgressFloat = if (durationMillis > 0) {
        (bufferedPosition.toFloat() / durationMillis).coerceIn(0f, 1f)
    } else 0f

    val contentProgressString = remember(contentProgress) { formatDuration(contentProgress) }
    val contentDurationString = remember(contentDuration) { formatDuration(contentDuration) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        VideoPlayerControlsIcon(
            modifier = Modifier.focusRequester(focusRequester),
            icon = if (!isPlaying) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
            onClick = { onPlayPauseToggle(!isPlaying) },
            state = state,
            isPlaying = isPlaying,
            contentDescription = "Play/Pause"
        )
        VideoPlayerControllerText(text = contentProgressString)
        VideoPlayerControllerIndicator(
            progress = currentProgressFloat,
            bufferedProgress = bufferedProgressFloat,
            onSeek = onSeek,
            state = state,
            contentDuration = contentDuration
        )
        VideoPlayerControllerText(text = contentDurationString)
    }
}