package kurd.reco.mobile.ui.player

import android.app.Activity
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.VideoSettings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kurd.reco.core.AppLog
import kurd.reco.core.Global
import kurd.reco.core.SettingsDataStore
import kurd.reco.core.api.Cache
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.PlayDataModel
import kurd.reco.mobile.R
import kurd.reco.mobile.ui.InfoDialog
import kurd.reco.mobile.ui.player.composables.GestureAdjuster
import kurd.reco.mobile.ui.player.composables.SettingsDialog
import kurd.reco.mobile.ui.player.composables.VideoPlayerBottom
import kurd.reco.mobile.ui.player.composables.VideoPlayerItemsRow
import kurd.reco.mobile.ui.player.composables.VideoSeekControls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

// Helper function to format time
private fun formatDuration(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoController(
    exoPlayer: ExoPlayer,
    item: PlayDataModel,
    trackSelector: DefaultTrackSelector,
    settingsDataStore: SettingsDataStore,
    onItemChange: (HomeItemModel) -> Unit
) {
    val context = LocalContext.current
    val TAG = "VideoControlBar"

    val rowItems = Global.clickedItemRow
    val lastRowItem = Global.clickedItem

    val scope = rememberCoroutineScope()

    var currentTime by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(exoPlayer.duration) }
    var bufferedPosition by remember { mutableLongStateOf(exoPlayer.bufferedPosition) }
    var showControls by remember { mutableStateOf(false) }
    var videoSize by remember { mutableStateOf(exoPlayer.videoSize) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var subtitlePadding by remember { mutableIntStateOf(50) }
    var showChannelSelector by remember { mutableStateOf(false) }
    val bottomPadding by animateDpAsState(
        targetValue = if (showChannelSelector && rowItems != null) 110.dp else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )
    var isPressing by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    val subtitleSize by settingsDataStore.subtitleSize.collectAsState(initial = 16f)
    var isBuffering by remember { mutableStateOf(true) }
    var showSeekIndicator by remember { mutableStateOf(false) }
    var totalDragAmount by remember { mutableFloatStateOf(0f) }
    var seekSeconds by remember { mutableIntStateOf(0) }
    var isLocked by remember { mutableStateOf(false) }
    var showLockIcon by remember { mutableStateOf(false) }
    var clickCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        isBuffering = false
                        duration = exoPlayer.duration
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

            override fun onVideoSizeChanged(size: VideoSize) {
                videoSize = size
            }

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
            withContext(Dispatchers.Main) {
                currentTime = exoPlayer.currentPosition
                bufferedPosition = exoPlayer.bufferedPosition
            }
            delay(1000L)
        }
    }


    // TODO: Update subtitle position according to Resize mode
    // It's like this for now

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < 0) {
                        showChannelSelector = true
                    } else if (dragAmount > 0) {
                        showChannelSelector = false
                    }
                }
            }
    ) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    useController = false
                    this.resizeMode = resizeMode
                    player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    subtitleView?.apply {
                        setPadding(0, 0, 0, subtitlePadding)
                        setFixedTextSize(TypedValue.COMPLEX_UNIT_SP, subtitleSize)

                    }
                }
            },
            update = {
                it.resizeMode = resizeMode
                it.subtitleView?.apply {
                    setPadding(0, 0, 0, subtitlePadding)
                    setFixedTextSize(TypedValue.COMPLEX_UNIT_SP, subtitleSize)
                }
            }
        )

        if (isBuffering) {
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
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .run {
                    if (isLocked) {
                        clickable {
                            if (clickCount > 2) {
                                showLockIcon = !showLockIcon
                                clickCount = 0
                            } else {
                                clickCount += 1
                            }

                            scope.launch {
                                delay(1400)
                                clickCount = 0
                            }
                        }
                    } else this
                }
                .run {
                    if (!isLocked) {
                        pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    try {
                                        awaitRelease()
                                        exoPlayer.setPlaybackSpeed(1f)
                                    } finally {
                                        isPressing = false
                                    }
                                },
                                onLongPress = {
                                    exoPlayer.setPlaybackSpeed(2f)
                                    isPressing = true
                                },
                                onTap = {
                                    showChannelSelector = false
                                    showControls = !showControls
                                }
                            )
                        }
                    } else this
                }
                .run {
                    if (!isLocked) {
                        pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (!showControls && seekSeconds != 0) {
                                        val currentPosition = exoPlayer.currentPosition
                                        val newPosition = currentPosition + (seekSeconds * 1000L)
                                        val duration = exoPlayer.duration
                                        if (duration > 0) {
                                            exoPlayer.seekTo(
                                                newPosition.coerceIn(
                                                    0,
                                                    duration
                                                )
                                            )
                                        } else {
                                            exoPlayer.seekTo(newPosition.coerceAtLeast(0))
                                        }
                                    }
                                    scope.launch {
                                        delay(200)
                                        showSeekIndicator = false
                                        totalDragAmount = 0f
                                        seekSeconds = 0
                                    }
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    if (!showControls) {
                                        totalDragAmount += dragAmount
                                        val sensitivity = 0.06f
                                        seekSeconds = (totalDragAmount * sensitivity).toInt()
                                        showSeekIndicator = true
                                    }
                                }
                            )
                        }
                    } else this
                }
        ) {
            if (!showControls && !isLocked) {
                GestureAdjuster(modifier = Modifier.fillMaxSize())
            }

            AnimatedVisibility(
                visible = showSeekIndicator && !showControls,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-80).dp),
                enter = fadeIn(animationSpec = tween(150)),
                exit = fadeOut(animationSpec = tween(150))
            ) {
                val scale by animateFloatAsState(
                    targetValue = if (showSeekIndicator) 1f else 0.8f,
                    animationSpec = tween(durationMillis = 200), label = "SeekTextScale"
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val currentPosition = exoPlayer.currentPosition
                    val targetTimeMillis = (currentPosition + (seekSeconds * 1000L)).coerceAtLeast(0L)
                    val formattedTargetTime = formatDuration(targetTimeMillis)
                    val formattedSeekSeconds = "${if (seekSeconds >= 0) "+" else ""}${seekSeconds}s"

                    Text(
                        text = "$formattedTargetTime [$formattedSeekSeconds]",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = Color.White
                        ),
                        modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
                    )
                }
            }

            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                visible = isPressing
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                            shape = MaterialTheme.shapes.medium
                        )
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        Text("2x", color = Color.White)
                        Icon(Icons.Default.FastForward, contentDescription = null, tint = Color.White)
                    }
                }
            }

            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                if (showLockIcon) {
                    IconButton(onClick = {
                        isLocked = false
                        showLockIcon = false
                        showControls = !showControls
                    }) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }

            if (showControls) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(bottom = bottomPadding)
                ) {

                    VideoSeekControls(modifier = Modifier.align(Alignment.Center), exoPlayer, isBuffering)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        IconButton(
                            onClick = {
                                exoPlayer.release()
                                Global.clickedItem = null
                                Global.clickedItemRow = null
                                Global.fetchForPlayer = false
                                (context as? Activity)?.finishAndRemoveTask()
                            },
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(.5f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (!item.title.isNullOrEmpty()) {
                                Text(
                                    text = item.title!!,
                                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "${videoSize.width}x${videoSize.height}",
                                style = MaterialTheme.typography.titleSmall.copy(color = Color.White),
                            )
                        }
                    }

                    VideoPlayerBottom(exoPlayer, currentTime, duration, bufferedPosition)

                    Row(modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)) {
                        if (Global.clickedItem?.isLiveTv == true) {
                            IconButton(onClick = { showErrorDialog = true }) {
                                Icon(
                                    Icons.Outlined.Flag,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }

                        IconButton(onClick = {
                            isLocked = true
                            showLockIcon = false
                            showControls = false
                        }) {
                            Icon(
                                Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        IconButton(onClick = {
                            resizeMode = when (resizeMode) {
                                AspectRatioFrameLayout.RESIZE_MODE_FIT -> {
                                    subtitlePadding = 140
                                    AspectRatioFrameLayout.RESIZE_MODE_FILL
                                }

                                AspectRatioFrameLayout.RESIZE_MODE_FILL -> {
                                    subtitlePadding = 160
                                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                }

                                AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> {
                                    subtitlePadding = 50
                                    AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }

                                else -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                            }
                        }) {
                            Icon(
                                Icons.Outlined.AspectRatio,
                                contentDescription = null,
                                tint = Color.White,
                            )
                        }

                        IconButton(onClick = { showSettingsDialog = !showSettingsDialog }) {
                            Icon(
                                Icons.Outlined.VideoSettings,
                                contentDescription = null,
                                tint = Color.White,
                            )
                        }
                    }

                    if (showErrorDialog) {
                        InfoDialog(
                            title = stringResource(R.string.reload_stream),
                            message = stringResource(R.string.reload_stream_desc),
                            onDismiss = { showErrorDialog = false },
                            onConfirm = {
                                if (Global.fetchRetryCount < 2) {
                                    Global.fetchRetryCount++

                                    runBlocking {
                                        AppLog.i(TAG, "Deleting cache")
                                        Cache.deleteCache(item.id)
                                    }
                                    exoPlayer.release()
                                    Toast.makeText(context, "Reloading...", Toast.LENGTH_SHORT).show()
                                    lastRowItem?.let {
                                        onItemChange(it)
                                    }
                                }
                            }
                        )
                    }

                    if (showSettingsDialog) {
                        SettingsDialog(
                            exoPlayer,
                            trackSelector,
                            onDismiss = { showSettingsDialog = false },
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                    }
                }
            }

            if (rowItems != null && rowItems.contents.isNotEmpty()) {
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    visible = showChannelSelector && showControls,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    VideoPlayerItemsRow(
                        items = rowItems,
                        selectedItem = lastRowItem,
                        onItemClick = {
                            onItemChange(it)
                            showChannelSelector = false
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}