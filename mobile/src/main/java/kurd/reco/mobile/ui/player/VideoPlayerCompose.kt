package kurd.reco.mobile.ui.player

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import io.github.anilbeesetti.nextlib.media3ext.ffdecoder.NextRenderersFactory
import kotlinx.coroutines.delay
import kurd.reco.core.Global
import kurd.reco.core.SettingsDataStore
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.HomeScreenModel
import kurd.reco.core.api.model.PlayDataModel
import kurd.reco.core.data.db.watched.WatchedItemDao
import kurd.reco.core.data.db.watched.WatchedItemModel
import org.koin.compose.koinInject


@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerCompose(
    item: PlayDataModel,
    useVpn: Boolean,
    settingsDataStore: SettingsDataStore = koinInject(),
    watchedItemDao: WatchedItemDao = koinInject(),
    onItemChange: (HomeItemModel) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
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

    val renderersFactory = NextRenderersFactory(context)
        .setEnableDecoderFallback(true)
        .setExtensionRendererMode(EXTENSION_RENDERER_MODE_ON)


    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .setRenderersFactory(renderersFactory)
            .apply {
                val httpDataSourceFactory = createHttpDataSourceFactory(item.streamHeaders?: emptyMap(), useVpn)
                val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
                setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory).run {
                    if (item.drm?.clearKey != null) {
                        val drmSessionManager = clearKey(item.drm!!) ?: return@run this
                        setDrmSessionManagerProvider { drmSessionManager }
                    } else this
                })
            }
            .build()
            .apply {
                val mediaItem = createMediaItem(item, item.urls.first().second)
                setMediaItem(mediaItem)
                prepare()
                if (isWatched != null) seekTo(isWatched.resumePosition)
                playWhenReady = true
            }
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

    VideoController(exoPlayer = exoPlayer, item, trackSelector, settingsDataStore = settingsDataStore, onItemChange = onItemChange)
}
