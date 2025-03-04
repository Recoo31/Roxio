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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.anilbeesetti.nextlib.media3ext.ffdecoder.NextRenderersFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kurd.reco.core.Global
import kurd.reco.core.AppLog
import kurd.reco.core.MainVM
import kurd.reco.core.SettingsDataStore
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
fun VideoPlayerScreen(
    item: PlayDataModel,
    onItemChange: (HomeItemModel) -> Unit,
    onBackPressed: () -> Unit,
    mainVM: MainVM = koinInject(),
    settingsDataStore: SettingsDataStore = koinInject()
) {
    val context = LocalContext.current
    val videoPlayerState = rememberVideoPlayerState(hideSeconds = 4)
    val forceHighestQualityEnabled by settingsDataStore.forceHighestQualityEnabled.collectAsStateWithLifecycle(true)

    val lastRowItem = mainVM.clickedItem

    BackHandler(onBack = onBackPressed)


    // Ensure trackSelector parameters update dynamically when forceHighestQualityEnabled changes
    val trackSelector = remember { DefaultTrackSelector(context) }

    trackSelector.parameters = trackSelector.buildUponParameters()
        .setAllowVideoMixedMimeTypeAdaptiveness(true)
        .setAllowVideoNonSeamlessAdaptiveness(true)
        .setSelectUndeterminedTextLanguage(true)
        .setAllowAudioMixedMimeTypeAdaptiveness(true)
        .setAllowMultipleAdaptiveSelections(true)
        .setPreferredAudioLanguages("tr")
        .setPreferredTextLanguage(null)
        .setForceHighestSupportedBitrate(forceHighestQualityEnabled)
        .build()


    val renderersFactory = NextRenderersFactory(context).apply {
        setEnableDecoderFallback(true)
        setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .setRenderersFactory(renderersFactory)
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
    }

    LaunchedEffect(exoPlayer) {
        val mediaItem = createMediaItem(item, item.urls.first().second)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }


    var contentCurrentPosition by remember { mutableLongStateOf(0L) }
    var isPlaying: Boolean by remember { mutableStateOf(exoPlayer.isPlaying) }

    // TODO: Update in a more thoughtful manner

    LaunchedEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                AppLog.e(TAG, error.stackTraceToString())
                AppLog.e(TAG, "${error.errorCode}")

                Toast.makeText(context, "Error: ${error.errorCodeName}", Toast.LENGTH_SHORT).show()

                if (error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS && Global.fetchRetryCount < 2) {
                    Global.fetchRetryCount++

                    Toast.makeText(context, "Reloading...", Toast.LENGTH_SHORT).show()
                    AppLog.i(TAG, "Deleting cache")
                    runBlocking {
                        Cache.deleteCache(item.id)
                    }
                    lastRowItem?.let {
                        onItemChange(it)
                    }
                }
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
                PlayerView(context).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                }
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
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val videoSize by remember { mutableStateOf(exoPlayer.videoSize) }

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
            Column {
                Text(
                    item.title ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${videoSize.width}x${videoSize.height}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
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
                SettingsDialog(exoPlayer, defaultTrackSelector, focusRequester) {
                    Toast.makeText(context, "Reloading...", Toast.LENGTH_SHORT).show()
                    exoPlayer.release()
                    lastRowItem?.let {
                        onItemChange(it)
                    }
                }
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
                            context.getString(R.string.see_more),
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
                    modifier = Modifier.fillMaxWidth().onFocusChanged {
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
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
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