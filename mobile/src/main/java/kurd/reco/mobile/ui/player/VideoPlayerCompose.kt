package kurd.reco.mobile.ui.player

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import io.github.anilbeesetti.nextlib.media3ext.ffdecoder.NextRenderersFactory
import kurd.reco.core.SettingsDataStore
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.PlayDataModel
import org.koin.compose.koinInject


@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerCompose(
    item: PlayDataModel,
    useVpn: Boolean,
    settingsDataStore: SettingsDataStore = koinInject(),
    onItemChange: (HomeItemModel) -> Unit
) {
    val context = LocalContext.current
    val forceHighestQualityEnabled by settingsDataStore.forceHighestQualityEnabled.collectAsStateWithLifecycle(true)

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
                playWhenReady = true
            }
    }

    DisposableEffect(item) {
        onDispose {
            exoPlayer.release()
        }
    }

    VideoController(exoPlayer = exoPlayer, item, trackSelector, settingsDataStore = settingsDataStore, onItemChange = onItemChange)
}
