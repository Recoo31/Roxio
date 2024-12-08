package kurd.reco.mobile.ui.player

import android.app.Activity
import android.util.TypedValue
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import kotlinx.coroutines.launch
import kurd.reco.core.AppLog
import kurd.reco.core.MainVM
import kurd.reco.core.SettingsDataStore
import kurd.reco.core.api.Cache
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.PlayDataModel
import kurd.reco.mobile.R
import kurd.reco.mobile.ui.player.composables.VideoPlayerItemsRow
import kurd.reco.mobile.ui.player.composables.GestureAdjuster
import kurd.reco.mobile.ui.player.composables.SettingsDialog
import kurd.reco.mobile.ui.player.composables.VideoPlayerBottom
import kurd.reco.mobile.ui.player.composables.VideoSeekControls
import org.koin.compose.koinInject

@OptIn(UnstableApi::class)
@Composable
fun VideoController(
    exoPlayer: ExoPlayer,
    item: PlayDataModel,
    trackSelector: DefaultTrackSelector,
    settingsDataStore: SettingsDataStore = koinInject(),
    mainVM: MainVM = koinInject(),
    onItemChange: (HomeItemModel) -> Unit,
) {
    val context = LocalContext.current
    val TAG = "VideoControlBar"

    var currentTime by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(exoPlayer.duration) }
    var showControls by remember { mutableStateOf(false) }
    var videoSize by remember { mutableStateOf(exoPlayer.videoSize) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_ZOOM) }
    val scope = rememberCoroutineScope()
    var subtitlePadding by remember { mutableIntStateOf(180) }
    var showChannelSelector by remember { mutableStateOf(false) }
    val bottomPadding by animateDpAsState(
        targetValue = if (showChannelSelector) 110.dp else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )
    var isPressing by remember { mutableStateOf(false) }

    val rowItems = mainVM.clickedItemRow
    val lastRowItem = mainVM.clickedItem

    val subtitleSize by settingsDataStore.subtitleSize.collectAsState(initial = 16f)

    LaunchedEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    duration = exoPlayer.duration
                }
            }

            override fun onVideoSizeChanged(size: VideoSize) {
                videoSize = size
            }

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
            currentTime = exoPlayer.currentPosition
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
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
                            showControls = !showControls
                        }
                    )
                }
        ) {
            if (!showControls) {
                GestureAdjuster()
            }


            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                visible = isPressing,
                enter = fadeIn(),
                exit = fadeOut()
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
                        Icon(painterResource(R.drawable.baseline_fast_forward_24), contentDescription = null, tint = Color.White)
                    }
                }
            }


            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(bottom = bottomPadding)
                ) {

                    VideoSeekControls(modifier = Modifier.align(Alignment.Center), exoPlayer)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        IconButton(
                            onClick = {
                                exoPlayer.release()
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
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (!item.title.isNullOrEmpty()) {
                                Text(
                                    text = item.title!!,
                                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                                )
                            }
                            Text(
                                text = "${videoSize.width}x${videoSize.height}",
                                style = MaterialTheme.typography.titleSmall.copy(color = Color.White),
                            )
                        }
                    }

                    VideoPlayerBottom(
                        exoPlayer,
                        currentTime,
                        duration,
                        onResizeClick = {
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
                        },
                        onSettingsClick = { showSettingsDialog = !showSettingsDialog }
                    )


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
            AnimatedVisibility(
                modifier = Modifier.align(Alignment.BottomCenter),
                visible = showChannelSelector && showControls && rowItems != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                VideoPlayerItemsRow(
                    items = rowItems!!,
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