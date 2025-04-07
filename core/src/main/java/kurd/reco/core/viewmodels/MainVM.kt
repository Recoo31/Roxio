package kurd.reco.core.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
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
import kurd.reco.core.api.Api.PLUGIN_URL
import kurd.reco.core.api.app
import kurd.reco.core.data.UpdateResponse
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

    var currentVersion = 1.0
    var showUpdateDialog by mutableStateOf(false)
    var downloadProgress by mutableFloatStateOf(0f)
    var changeLog: List<String> = emptyList()
    private var appLink: String? = null

    private val versionLink = "https://raw.githubusercontent.com/Recoo31/apis/refs/heads/main/version.json"

    //var useVpn by mutableStateOf(false)

    fun checkAppUpdate(context: Context, isMobile: Boolean) {
        currentVersion = getCurrentAppVersion(context).toDoubleOrNull() ?: return
        viewModelScope.launch {
            runCatching { app.get(versionLink).parsed<UpdateResponse>() }
                .onSuccess { response ->
                    if (currentVersion < (if (isMobile) response.versions.mobile else response.versions.tv)) {
                        changeLog = response.changeLog
                        appLink = if (isMobile) response.downloads.mobile else response.downloads.tv
                        showUpdateDialog = true
                    }
                }
        }
    }

    private fun checkInstallAppPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                    intent.data = Uri.parse("package:${context.packageName}")
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // If the intent is not available, try the alternative approach
                    val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                    context.startActivity(intent)
                }
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
        checkInstallAppPermission(context)

        val apkUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(installIntent)
    }

    fun checkOldPlugins(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val existingPlugins = pluginDao.getAllPlugins()
                .filter { it.downloadUrl == "https://raw.githubusercontent.com/Recoo31/Roxio-Test-Plugin/refs/heads/main/version.json" }

            if (existingPlugins.isNotEmpty()) {
                existingPlugins.forEach { plugin ->
                    pluginDao.deletePlugin(plugin.id)
                    File(plugin.filePath).delete()
                }
                downloadPlugins(PLUGIN_URL, context)
            }
        }
    }

    fun downloadPlugins(url: String, context: Context) {
        val uri = if (url.startsWith("http")) url else "https://raw.githubusercontent.com/Recoo31/RoxioPlugins/main/$url.json"
        viewModelScope.launch(Dispatchers.IO) {
            downloadPlugins(uri, pluginDao, context.filesDir.path)
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
