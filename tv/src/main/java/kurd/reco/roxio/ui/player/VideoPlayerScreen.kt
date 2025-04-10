package kurd.reco.roxio.ui.player

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.VideoSettings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kurd.reco.core.AppLog
import kurd.reco.core.Global
import kurd.reco.core.SettingsDataStore
import kurd.reco.core.api.Cache
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.HomeScreenModel
import kurd.reco.core.api.model.PlayDataModel
import kurd.reco.core.data.db.watched.WatchedItemDao
import kurd.reco.core.data.db.watched.WatchedItemModel
import kurd.reco.roxio.R
import kurd.reco.roxio.common.CircularProgressIndicator
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
    settingsDataStore: SettingsDataStore = koinInject(),
    watchedItemDao: WatchedItemDao = koinInject()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val videoPlayerState = rememberVideoPlayerState(hideSeconds = 4)
    val forceHighestQualityEnabled by settingsDataStore.forceHighestQualityEnabled.collectAsStateWithLifecycle(true)

    var shouldObserve by remember { mutableStateOf(false) }

    val lastItem = Global.clickedItem
    var itemRow: HomeScreenModel? = null
    val isWatched = watchedItemDao.getWatchedItemById(lastItem?.id.toString())

    if (isWatched?.itemsRow != null && isWatched.isSeries) {
        Global.clickedItemRow = isWatched.itemsRow
        itemRow = isWatched.itemsRow
    } else if (lastItem != null && !lastItem.isSeries && !lastItem.isLiveTv) {
        Global.clickedItemRow = null
    }

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
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .apply {
                if (item.streamHeaders != null) {
                    val httpDataSourceFactory = createHttpDataSourceFactory(item.streamHeaders!!)
                    val dataSourceFactory =
                        DefaultDataSource.Factory(context, httpDataSourceFactory)
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
        if (isWatched != null) exoPlayer.seekTo(isWatched.resumePosition)
        exoPlayer.playWhenReady = true
    }

    LaunchedEffect(Unit) {
        delay(30_000)
        shouldObserve = true
    }


    DisposableEffect(item) {
        onDispose {
            exoPlayer.release()
        }
    }


    if (shouldObserve) {
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                    if (lastItem != null && !lastItem.isLiveTv) {
                        val currentPosition = exoPlayer.currentPosition
                        val duration = exoPlayer.duration
                        val isCompleted = duration > 0 && currentPosition >= duration * 0.95 // Watched 95%

                        val currentRow = itemRow ?: Global.clickedItemRow

                        if (lastItem.isSeries && isCompleted) {
                            watchedItemDao.deleteWatchedItemById(lastItem.id.toString()) // Delete the completed one

                            if (currentRow != null) {
                                val currentIndex = currentRow.contents.indexOfFirst { it.id == lastItem.id }
                                if (currentIndex != -1 && currentIndex < currentRow.contents.size - 1) {
                                    // Not the last episode, add the next one
                                    val nextEpisode = currentRow.contents[currentIndex + 1]
                                    watchedItemDao.insertOrUpdateWatchedItem(
                                        WatchedItemModel(
                                            id = nextEpisode.id.toString(),
                                            title = nextEpisode.title ?: item.title,
                                            poster = nextEpisode.poster,
                                            isSeries = true,
                                            resumePosition = 0L,
                                            totalDuration = 0L,
                                            pluginId = Global.currentPlugin!!.id,
                                            itemsRow = currentRow
                                        )
                                    )
                                }
                            }
                        } else {
                            // Not completed or not a series, just update progress
                            watchedItemDao.insertOrUpdateWatchedItem(
                                WatchedItemModel(
                                    id = lastItem.id.toString(),
                                    title = item.title,
                                    poster = lastItem.poster,
                                    isSeries = lastItem.isSeries,
                                    resumePosition = currentPosition,
                                    totalDuration = duration,
                                    pluginId = Global.currentPlugin!!.id,
                                    itemsRow = if (lastItem.isSeries) currentRow else null
                                )
                            )
                        }
                    }
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                exoPlayer.release()
            }
        }
    }


    var contentCurrentPosition by remember { mutableLongStateOf(0L) }
    var isPlaying: Boolean by remember { mutableStateOf(exoPlayer.isPlaying) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var isBuffering by remember { mutableStateOf(false) }
    var isInitialLoading by remember { mutableStateOf(true) }
    var bufferedPosition by remember { mutableLongStateOf(exoPlayer.bufferedPosition) }

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
                    Global.clickedItem?.let {
                        onItemChange(it)
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        isInitialLoading = false
                        isBuffering = false
                    }
                    Player.STATE_BUFFERING -> {
                        isBuffering = true
                    }
                    Player.STATE_ENDED -> {
                        isBuffering = false
                    }

                    Player.STATE_IDLE -> {
                        isBuffering = false
                    }
                }
            }
        }
        exoPlayer.addListener(listener)
        while (true) {
            withContext(Dispatchers.Main) {
                contentCurrentPosition = exoPlayer.currentPosition
                bufferedPosition = exoPlayer.bufferedPosition
                isPlaying = exoPlayer.isPlaying
            }
            delay(300L)
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
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    useController = false
                    this.resizeMode = resizeMode
                }
            },
            update = {
                it.player = exoPlayer
                it.resizeMode = resizeMode
            },
            onRelease = { exoPlayer.release() }
        )

        if (isInitialLoading || isBuffering) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                    if (isInitialLoading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading...",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

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
                    bufferedPosition,
                    exoPlayer,
                    videoPlayerState,
                    focusRequester,
                    trackSelector,
                    resizeMode,
                    onItemChange = onItemChange,
                    onResizeClick = {
                        resizeMode = it
                    }
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
    bufferedPosition: Long,
    exoPlayer: ExoPlayer,
    state: VideoPlayerState,
    focusRequester: FocusRequester,
    defaultTrackSelector: DefaultTrackSelector,
    resizeMode: Int,
    onResizeClick: (Int) -> Unit = {},
    onItemChange: (HomeItemModel) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val videoSize by remember { mutableStateOf(exoPlayer.videoSize) }

    val rowItems = Global.clickedItemRow
    val lastRowItem = Global.clickedItem

    val onPlayPauseToggle = { shouldPlay: Boolean ->
        if (shouldPlay) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    var showSettings by remember { mutableStateOf(false) }
    var isMoreFocused by remember { mutableStateOf(false) }

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
                    icon = Icons.Outlined.VideoSettings,
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

                VideoPlayerControlsIcon(
                    modifier = Modifier.padding(start = 12.dp),
                    icon = Icons.Outlined.AspectRatio,
                    state = state,
                    isPlaying = isPlaying
                ) {
                    when (resizeMode) {
                        AspectRatioFrameLayout.RESIZE_MODE_FIT -> onResizeClick(
                            AspectRatioFrameLayout.RESIZE_MODE_FILL
                        )

                        AspectRatioFrameLayout.RESIZE_MODE_FILL -> onResizeClick(
                            AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        )

                        AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> onResizeClick(
                            AspectRatioFrameLayout.RESIZE_MODE_FIT
                        )

                        else -> onResizeClick(AspectRatioFrameLayout.RESIZE_MODE_FILL)
                    }
                }
            }
        },
        seeker = {
            VideoPlayerSeeker(
                focusRequester,
                state,
                isPlaying,
                bufferedPosition,
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
                if (!isMoreFocused && rowItems != null) {
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
                rowItems?.let {
                    AnimatedVisibility(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged {
                                isMoreFocused = it.hasFocus
                            },
                        visible = isMoreFocused,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                    ) {
                        VideoPlayerItemsRow(
                            state = state,
                            items = rowItems,
                            selectedItem = lastRowItem,
                            onItemClick = {
                                onItemChange(it)
                                isMoreFocused = false
                            },
                            focusRequester = focusRequester,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )
                    }
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