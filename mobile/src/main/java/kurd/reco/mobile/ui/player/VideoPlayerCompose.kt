package kurd.reco.mobile.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.PlayDataModel


@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerCompose(item: PlayDataModel, onItemChange: (HomeItemModel) -> Unit) {
    val context = LocalContext.current

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

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
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
    }

    DisposableEffect(item) {
        onDispose {
            exoPlayer.release()
        }
    }

    VideoController(exoPlayer = exoPlayer, item, trackSelector, onItemChange = onItemChange)
}
