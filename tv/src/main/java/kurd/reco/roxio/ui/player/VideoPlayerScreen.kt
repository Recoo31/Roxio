package kurd.reco.roxio.ui.player

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kurd.reco.core.AppLog
import kurd.reco.core.MainVM
import kurd.reco.core.api.Cache
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.PlayDataModel
import kurd.reco.roxio.R
import kurd.reco.roxio.common.VideoPlayerState
import kurd.reco.roxio.common.rememberVideoPlayerState
import kurd.reco.roxio.ui.player.composables.VideoPlayerControlsIcon
import kurd.reco.roxio.ui.player.composables.VideoPlayerItemsRow
import kurd.reco.roxio.ui.player.composables.VideoPlayerMainFrame
import kurd.reco.roxio.ui.player.composables.VideoPlayerOverlay
import kurd.reco.roxio.ui.player.composables.VideoPlayerPulse
import kurd.reco.roxio.ui.player.composables.VideoPlayerPulseState
import kurd.reco.roxio.ui.player.composables.VideoPlayerSeeker
import kurd.reco.roxio.ui.player.composables.rememberVideoPlayerPulseState
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

private val TAG = "VideoPlayerScreen"

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(item: PlayDataModel, onItemChange: (HomeItemModel) -> Unit, onBackPressed: () -> Unit, mainVM: MainVM = koinInject()) {
    val context = LocalContext.current
    val videoPlayerState = rememberVideoPlayerState(hideSeconds = 4)
    val scope = rememberCoroutineScope()

    val lastRowItem = mainVM.clickedItem

    BackHandler(onBack = onBackPressed)

    val trackSelector = remember {
        DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters()
                    .setAllowVideoMixedMimeTypeAdaptiveness(true)
                    .setAllowVideoNonSeamlessAdaptiveness(true)
                    .setSelectUndeterminedTextLanguage(true)
                    .setAllowAudioMixedMimeTypeAdaptiveness(true)
                    .setAllowMultipleAdaptiveSelections(true)
                    .setPreferredTextRoleFlags(C.ROLE_FLAG_SUBTITLE)
            )
        }
    }
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }


    LaunchedEffect(item) {
        println(item)
        exoPlayer?.release()

        val newExoPlayer = ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .apply {
                if (item.streamHeaders != null) {
                    val httpDataSourceFactory = createHttpDataSourceFactory(item.streamHeaders!!)
                    val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
                    setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory).run {
                        if (item.drm?.clearKey != null) {
                            val drmSessionManager = clearKey(item.drm!!) ?: return@run this
                            setDrmSessionManagerProvider { drmSessionManager }
                        } else this
                    })
                }
            }
            .build()
            .apply {
                val mediaItem = createMediaItem(item, item.urls.first().second)
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }

        exoPlayer = newExoPlayer
    }

    if (exoPlayer != null) {
        val exoPlayer = exoPlayer!!

        var contentCurrentPosition by remember { mutableLongStateOf(0L) }
        var isPlaying: Boolean by remember { mutableStateOf(exoPlayer.isPlaying) }

        // TODO: Update in a more thoughtful manner

        LaunchedEffect(exoPlayer) {
            val listener = object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    error.printStackTrace()
                    scope.launch {
                        AppLog.i(TAG, "Deleting cache")
                        Cache.deleteCache(item.id)
                    }
                    AppLog.e(TAG, error.stackTraceToString())
                    exoPlayer.release()

                    AppLog.i(TAG, "${error.errorCode}")

                    if (error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS) {
                        Toast.makeText(context, "Reloading...", Toast.LENGTH_SHORT).show()
                        lastRowItem?.let {
                            onItemChange(it)
                        }
                    }

//                val intent = Intent(Intent.ACTION_VIEW).apply {
//                    setDataAndType(Uri.parse(item.urls.first().second), "video/*")
//                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                }
//
//                startActivity(context, intent, null)

                }
            }
            exoPlayer.addListener(listener)
            while (true) {
                delay(300)
                contentCurrentPosition = exoPlayer.currentPosition
                isPlaying = exoPlayer.isPlaying
            }
        }

        val pulseState = rememberVideoPlayerPulseState()

        Box(
            Modifier
                .dPadEvents(
                    exoPlayer,
                    videoPlayerState,
                    pulseState
                )
                .focusable()
        ) {
            AndroidView(
                factory = {
                    PlayerView(context).apply { useController = false }
                },
                update = { it.player = exoPlayer },
                onRelease = { exoPlayer.release() }
            )

            val focusRequester = remember { FocusRequester() }
            VideoPlayerOverlay(
                modifier = Modifier.align(Alignment.BottomCenter),
                focusRequester = focusRequester,
                state = videoPlayerState,
                isPlaying = isPlaying,
                centerButton = { VideoPlayerPulse(pulseState) },
                controls = {
                    VideoPlayerControls(
                        item,
                        isPlaying,
                        contentCurrentPosition,
                        exoPlayer,
                        videoPlayerState,
                        focusRequester,
                        trackSelector,
                        onItemChange = onItemChange,
                        mainVM = mainVM
                    )
                }
            )
        }

    }


}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerControls(
    item: PlayDataModel,
    isPlaying: Boolean,
    contentCurrentPosition: Long,
    exoPlayer: ExoPlayer,
    state: VideoPlayerState,
    focusRequester: FocusRequester,
    defaultTrackSelector: DefaultTrackSelector,
    onItemChange: (HomeItemModel) -> Unit,
    mainVM: MainVM = koinInject()
) {
    val focusManager = LocalFocusManager.current

    val rowItems = mainVM.clickedItemRow
    val lastRowItem = mainVM.clickedItem

    val onPlayPauseToggle = { shouldPlay: Boolean ->
        if (shouldPlay) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    var showSettings by remember { mutableStateOf(false) }
    var isMoreFocused by remember { mutableStateOf(false) } // "More" focus kontrolü

    VideoPlayerMainFrame(
        mediaTitle = {
            Text(
                item.title ?: "",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        mediaActions = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                VideoPlayerControlsIcon(
                    modifier = Modifier.padding(start = 12.dp),
                    icon = R.drawable.rounded_video_settings_24,
                    state = state,
                    isPlaying = isPlaying,
                    contentDescription = "Settings"
                ) {
                    showSettings = !showSettings
                    if (showSettings) {
                        focusManager.moveFocus(FocusDirection.Left)
                    } else {
                        focusRequester.requestFocus()
                    }
                }
            }
        },
        seeker = {
            VideoPlayerSeeker(
                focusRequester,
                state,
                isPlaying,
                onPlayPauseToggle,
                onSeek = { exoPlayer.seekTo(exoPlayer.duration.times(it).toLong()) },
                contentProgress = contentCurrentPosition.milliseconds,
                contentDuration = exoPlayer.duration.milliseconds
            )
        },
        settingsOverlay = {
            if (showSettings) {
                SettingsDialog(exoPlayer, defaultTrackSelector, focusRequester)
            }
        },
        more = {
            Box(
                modifier = Modifier
            ) {
                if (!isMoreFocused) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "See More",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .onFocusChanged { focusState ->
                                    if (!isMoreFocused) {
                                        isMoreFocused = focusState.isFocused
                                    }
                                }
                                .focusable()
                        )
                    }
                }

                AnimatedVisibility(
                    modifier = Modifier.onFocusChanged {
                        isMoreFocused = it.hasFocus
                    },
                    visible = isMoreFocused,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    VideoPlayerItemsRow(
                        state = state,
                        items = rowItems!!,
                        selectedItem = lastRowItem,
                        onItemClick = {
                            onItemChange(it)
                            isMoreFocused = false
                        },
                        focusRequester = focusRequester,
                        modifier = Modifier.focusRequester(focusRequester)
                    )
                }
            }
        }
    )
}


private fun Modifier.dPadEvents(
    exoPlayer: ExoPlayer,
    videoPlayerState: VideoPlayerState,
    pulseState: VideoPlayerPulseState
): Modifier = this.handleDPadKeyEvents(
    onLeft = {
        if (!videoPlayerState.controlsVisible) {
            exoPlayer.seekBack()
            pulseState.setType(VideoPlayerPulse.Type.BACK)
        }
    },
    onRight = {
        if (!videoPlayerState.controlsVisible) {
            exoPlayer.seekForward()
            pulseState.setType(VideoPlayerPulse.Type.FORWARD)
        }
    },
    onUp = { videoPlayerState.showControls() },
    onDown = { videoPlayerState.showControls() },
    onEnter = {
        exoPlayer.pause()
        videoPlayerState.showControls()
    }
)


// if (videoPlayerState.controlsVisible) videoPlayerState.hideControls() else