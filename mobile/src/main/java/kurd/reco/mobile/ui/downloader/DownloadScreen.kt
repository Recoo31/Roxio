package kurd.reco.mobile.ui.downloader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import kurd.reco.core.api.model.PlayDataModel
import org.koin.compose.koinInject

@Destination<RootGraph>
@Composable
fun DownloadScreen(
    mediaDownloadHelper: MediaDownloadHelper = koinInject()
) {

    val playData = PlayDataModel(
        urls = listOf("ada" to "https://live-hls-abr-cdn.livepush.io/live/bigbuckbunnyclip/index.m3u8"),
        title = "Big Buck Bunny",
        drm = null,
        streamHeaders = null,
        subtitles = null,
        id = "big_duck",
    )

    LaunchedEffect(Unit) {
        mediaDownloadHelper.downloadContent(playData)
    }

}