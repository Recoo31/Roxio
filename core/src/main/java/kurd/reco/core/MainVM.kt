package kurd.reco.core

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.Keep
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kurd.reco.core.api.app
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.HomeScreenModel
import kurd.reco.core.api.model.PlayDataModel
import kurd.reco.core.data.VersionData
import kurd.reco.core.data.db.plugin.DeletedPlugin
import kurd.reco.core.data.db.plugin.DeletedPluginDao
import kurd.reco.core.data.db.plugin.Plugin
import kurd.reco.core.data.db.plugin.PluginDao
import kurd.reco.core.plugin.downloadPlugins
import java.io.File
import java.io.FileOutputStream

class MainVM(
    private val pluginDao: PluginDao,
    private val deletedPluginDao: DeletedPluginDao
) : ViewModel() {

    var showUpdateDialog by mutableStateOf(false)
    var downloadProgress by mutableFloatStateOf(0f)
    var changeLog: List<String> = emptyList()
    private var appLink: String? = null

    private val versionLink = ""

    //var useVpn by mutableStateOf(false)

    fun checkAppUpdate(context: Context) {
        val currentVersion = getCurrentAppVersion(context).toDoubleOrNull() ?: return
        viewModelScope.launch {
            runCatching { app.get(versionLink).parsed<VersionData>() }
                .onSuccess { response ->
                    changeLog = response.changeLog
                    appLink = response.downloadUrl
                    if (response.version > currentVersion) showUpdateDialog = true
                }
        }
    }

    private fun getCurrentAppVersion(context: Context): String {
        return context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "Unknown"
    }


    fun downloadApk(outputFilePath: File) {
        viewModelScope.launch(Dispatchers.IO) {
            appLink?.let { link ->
                app.get(link).body.use { response ->
                    FileOutputStream(outputFilePath).use { output ->
                        val totalBytes = response.contentLength()
                        var bytesCopied: Long = 0
                        val input = response.byteStream()

                        val buffer = ByteArray(8 * 1024)
                        var bytes = input.read(buffer)
                        while (bytes >= 0) {
                            output.write(buffer, 0, bytes)
                            bytesCopied += bytes
                            downloadProgress = (bytesCopied / totalBytes.toFloat()) * 100
                            bytes = input.read(buffer)
                        }
                    }
                }
            }
        }
    }

    fun installUpdate(apkFile: File, context: Context) {
        val apkUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(installIntent)
    }

    fun downloadPlugins(url: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            downloadPlugins(url, pluginDao, context.filesDir.path)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Plugins Downloaded", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deletePlugin(plugin: Plugin) {
        viewModelScope.launch(Dispatchers.IO) {
            pluginDao.deletePlugin(plugin.id)
            deletedPluginDao.insertDeletedPlugin(DeletedPlugin(plugin.id))
            File(plugin.filePath).delete()
        }
    }
}
