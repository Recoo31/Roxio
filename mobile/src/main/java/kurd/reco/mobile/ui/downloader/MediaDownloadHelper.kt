package kurd.reco.mobile.ui.downloader

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kurd.reco.core.api.model.PlayDataModel
import java.io.File
import java.util.concurrent.Executor

@OptIn(UnstableApi::class)
class MediaDownloadHelper(
    context: Context,
    databaseProvider: StandaloneDatabaseProvider,
) {
    private val downloadFolder = File(context.filesDir, "download")
    private val downloadCache = SimpleCache(downloadFolder, NoOpCacheEvictor(), databaseProvider)

    private val downloadExecutor = Executor(Runnable::run)
    private val dataSourceFactory = DefaultHttpDataSource.Factory()
    private val downloadManager =
        DownloadManager(context, databaseProvider, downloadCache, dataSourceFactory, downloadExecutor)


    suspend fun downloadContent(playData: PlayDataModel) = withContext(Dispatchers.IO) {
//        val mediaItem = buildMediaItem(playData)
        val url = Uri.parse(playData.urls.first().second)
        val downloadRequest = DownloadRequest.Builder(playData.id, url).build()
        downloadManager.addDownload(downloadRequest)
        downloadManager.resumeDownloads()
    }

//    // Build MediaItem, including DRM if present
//    private fun buildMediaItem(playData: PlayDataModel): MediaItem {
//        val mediaItemBuilder = MediaItem.Builder()
//            .setUri(playData.urls.first().first)
//            .setMediaId(playData.id)
//
//        playData.drm?.let { drmData ->
//            val drmCallback = HttpMediaDrmCallback(drmData.licenseUrl, DefaultHttpDataSource.Factory().apply {
//                playData.streamHeaders?.forEach { setHeader(it.key, it.value) }
//            })
//            val drmSessionManager = DefaultDrmSessionManagerProvider().get(drmCallback)
//            mediaItemBuilder.setDrmConfiguration(
//                MediaItem.DrmConfiguration.Builder(drmSessionManager).build()
//            )
//        }
//
//        return mediaItemBuilder.build()
//    }
}
