package kurd.reco.mobile.ui.player

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.util.Base64
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.drm.FrameworkMediaDrm
import androidx.media3.exoplayer.drm.LocalMediaDrmCallback
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import kurd.reco.core.api.model.DrmDataModel
import kurd.reco.core.api.model.PlayDataModel
import kurd.reco.core.api.model.SubtitleDataModel
import java.util.Locale

@OptIn(UnstableApi::class)
fun createHttpDataSourceFactory(headers: Map<String, String>, useProxy: Boolean): HttpDataSource.Factory {
    return DefaultHttpDataSource.Factory().apply {
        setDefaultRequestProperties(headers)
    }
}

fun createMediaItem(item: PlayDataModel, url: String): MediaItem {
    val mediaItemBuilder = MediaItem.Builder().apply {
        setUri(Uri.parse(url))
        when {
            url.contains(".mpd", true) -> setMimeType(MimeTypes.APPLICATION_MPD)
            url.contains(".m3u8", true) -> setMimeType(MimeTypes.APPLICATION_M3U8)
            url.contains("Manifest", true) -> setMimeType(MimeTypes.APPLICATION_SS)
        }

        if (item.drm != null && item.drm!!.clearKey == null) {
            setDrmConfiguration(createDrmConfiguration(item.drm!!))
        }
        if (item.subtitles != null) {
            val subtitleConfigurations = item.subtitles!!.map { setSubtitle(it) }
            setSubtitleConfigurations(subtitleConfigurations)
        }
    }
    return mediaItemBuilder.build()
}



fun setSubtitle(item: SubtitleDataModel): MediaItem.SubtitleConfiguration {
    val assetSrtUri = Uri.parse((item.url))
    val subtitle = MediaItem.SubtitleConfiguration.Builder(assetSrtUri)
        .setMimeType(MimeTypes.TEXT_VTT)
        .setLanguage(item.language)
        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
        .build()
    return subtitle
}

//fun createDrmConfiguration(drm: DrmDataModel): MediaItem.DrmConfiguration {
//    return MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID).apply {
//        setLicenseUri(drm.licenseUrl)
//        if (drm.headers != null) {
//            setLicenseRequestHeaders(drm.headers!!)
//        }
//    }.build()
//}


@OptIn(UnstableApi::class)
fun createDrmConfiguration(drm: DrmDataModel): MediaItem.DrmConfiguration {
    return MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID).apply {
        setLicenseUri(drm.licenseUrl)
        drm.headers?.let { setLicenseRequestHeaders(it) }
    }.build()
}

@UnstableApi
fun TrackGroup.getName(trackType: @C.TrackType Int, index: Int): String {
    val format = this.getFormat(0)
    val language = format.language
    val label = format.label
    return buildString {
        if (label != null) {
            append(label)
        }
        if (isEmpty()) {
            if (trackType == C.TRACK_TYPE_TEXT) {
                append("Subtitle #${index + 1}")
            } else {
                append("Audio #${index + 1}")
            }
        }
        if (language != null && language != "und") {
            append(" - ")
            append(Locale(language).displayLanguage)
        }
    }
}

fun hideSystemBars(window: Window) {
    WindowInsetsControllerCompat(window, window.decorView).let {
        it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        it.hide(WindowInsetsCompat.Type.systemBars())
    }
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
}

@OptIn(UnstableApi::class)
fun applySelectedTrack(
    trackSelector: DefaultTrackSelector,
    trackGroup: TrackGroup,
    trackIndex: Int,
    trackType: Int
) {
    val parametersBuilder = trackSelector.parameters.buildUpon()
    val trackSelectionOverride = TrackSelectionOverride(trackGroup, trackIndex)

    parametersBuilder.clearOverridesOfType(trackType)
    parametersBuilder.addOverride(trackSelectionOverride)

    trackSelector.setParameters(parametersBuilder)
}


fun getSystemVolume(context: Context): Float {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    return currentVolume / maxVolume.toFloat()
}

fun adjustVolume(context: Context, volume: Float) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val newVolume = (volume * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)).toInt().coerceIn(0, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
}

fun setTemporaryBrightness(context: Context, brightness: Float) {
    val activity = context as? Activity
    val layoutParams = activity?.window?.attributes
    layoutParams?.screenBrightness = brightness.coerceIn(0f, 1f)
    activity?.window?.attributes = layoutParams
}

fun getCurrentBrightness(context: Context): Float {
    val activity = context as? Activity
    return activity?.window?.attributes?.screenBrightness ?: 0.5f
}

fun openVideoWithSelectedPlayer(context: Context, videoUri: String, playerPackageName: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(Uri.parse(videoUri), "video/*")
        setPackage(playerPackageName)
    }
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "Selected player is not installed", Toast.LENGTH_SHORT).show()
    }
}


@OptIn(UnstableApi::class)
fun clearKey(drm: DrmDataModel): DefaultDrmSessionManager? {
    val clearKey = drm.clearKey!!
    try {
        val (kid, key) = clearKey.split(":")
        val drmKeyBytes = key.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val encodedDrmKey = Base64.encodeToString(drmKeyBytes,
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)

        val drmKeyIdBytes = kid.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val encodedDrmKeyId = Base64.encodeToString(drmKeyIdBytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)

        val drmBody = "{\"keys\":[{\"kty\":\"oct\",\"k\":\"${encodedDrmKey}\",\"kid\":\"${encodedDrmKeyId}\"}],\"type\":\"temporary\"}"
        val drmCallback = LocalMediaDrmCallback(drmBody.toByteArray())

        val drmSessionManager = DefaultDrmSessionManager.Builder()
            .setPlayClearSamplesWithoutKeys(true)
            .setMultiSession(false)
            .setKeyRequestParameters(HashMap())
            .setUuidAndExoMediaDrmProvider(C.CLEARKEY_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
            .build(drmCallback)

        return drmSessionManager
    } catch (t: Throwable) {
        return null
    }
}

fun formatTime(ms: Long, locale: Locale = Locale.getDefault()): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(locale, "%02d:%02d", minutes, seconds)
}
