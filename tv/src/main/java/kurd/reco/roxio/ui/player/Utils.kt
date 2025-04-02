package kurd.reco.roxio.ui.player

import android.net.Uri
import android.util.Base64
import android.view.KeyEvent
import androidx.annotation.OptIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
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
fun createHttpDataSourceFactory(headers: Map<String, String>): HttpDataSource.Factory {
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

/*
Thx to https://github.com/androidx/media/issues/1208
*/
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

/**
 * Handles horizontal (Left & Right) D-Pad Keys and consumes the event(s) so that the focus doesn't
 * accidentally move to another element.
 * */
fun Modifier.handleDPadKeyEvents(
    onLeft: (() -> Unit)? = null,
    onRight: (() -> Unit)? = null,
    onEnter: (() -> Unit)? = null
) = onPreviewKeyEvent {
    fun onActionUp(block: () -> Unit) {
        if (it.nativeKeyEvent.action == KeyEvent.ACTION_UP) block()
    }

    when (it.nativeKeyEvent.keyCode) {
        KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_SYSTEM_NAVIGATION_LEFT -> {
            onLeft?.apply {
                onActionUp(::invoke)
                return@onPreviewKeyEvent true
            }
        }

        KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_SYSTEM_NAVIGATION_RIGHT -> {
            onRight?.apply {
                onActionUp(::invoke)
                return@onPreviewKeyEvent true
            }
        }

        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
            onEnter?.apply {
                onActionUp(::invoke)
                return@onPreviewKeyEvent true
            }
        }
    }

    false
}

/**
 * Handles all D-Pad Keys
 * */
fun Modifier.handleDPadKeyEvents(
    onLeft: (() -> Unit)? = null,
    onRight: (() -> Unit)? = null,
    onUp: (() -> Unit)? = null,
    onDown: (() -> Unit)? = null,
    onEnter: (() -> Unit)? = null
) = onKeyEvent {

    if (it.nativeKeyEvent.action == KeyEvent.ACTION_UP) {
        when (it.nativeKeyEvent.keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_SYSTEM_NAVIGATION_LEFT -> {
                onLeft?.invoke().also { return@onKeyEvent true }
            }

            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_SYSTEM_NAVIGATION_RIGHT -> {
                onRight?.invoke().also { return@onKeyEvent true }
            }

            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_SYSTEM_NAVIGATION_UP -> {
                onUp?.invoke().also { return@onKeyEvent true }
            }

            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_SYSTEM_NAVIGATION_DOWN -> {
                onDown?.invoke().also { return@onKeyEvent true }
            }

            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                onEnter?.invoke().also { return@onKeyEvent true }
            }
        }
    }
    false
}


/**
 * Used to apply modifiers conditionally.
 */
fun Modifier.ifElse(
    condition: () -> Boolean,
    ifTrueModifier: Modifier,
    ifFalseModifier: Modifier = Modifier
): Modifier = then(if (condition()) ifTrueModifier else ifFalseModifier)

/**
 * Used to apply modifiers conditionally.
 */
fun Modifier.ifElse(
    condition: Boolean,
    ifTrueModifier: Modifier,
    ifFalseModifier: Modifier = Modifier
): Modifier = ifElse({ condition }, ifTrueModifier, ifFalseModifier)
