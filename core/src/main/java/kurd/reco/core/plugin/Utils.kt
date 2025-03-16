package kurd.reco.core.plugin

import android.content.Context
import androidx.annotation.Keep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kurd.reco.core.AppLog
import kurd.reco.core.api.app
import kurd.reco.core.data.db.plugin.DeletedPluginDao
import kurd.reco.core.data.db.plugin.Plugin
import kurd.reco.core.data.db.plugin.PluginDao
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

@Keep
data class PluginResponseRoot(val plugins: List<PluginResponse>)

@Keep
data class PluginResponse(
    val id: String,
    val name: String,
    val url: String,
    val image: String?,
    val version: String,
    val active: Boolean
)

private val TAG = "PluginManagerUtils"

fun getPluginFromManifest(
    filePath: String,
    url: String,
    image: String?,
    version: String,
    active: Boolean
): Plugin? {
    return try {
        val zip = ZipFile(File(filePath))
        zip.getInputStream(zip.getEntry("manifest.json")).bufferedReader().use {
            val jsonObj = JSONObject(it.readText())
            Plugin(
                jsonObj.getString("plugin_id"),
                jsonObj.getString("plugin_name"),
                jsonObj.getString("package"),
                jsonObj.getString("package_name"),
                filePath,
                version,
                url,
                image,
                active
            )
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }
}

suspend fun checkUpdate(plugin: Plugin, pluginDao: PluginDao, remotePlugins: List<PluginResponse>) {
    remotePlugins.firstOrNull { it.id == plugin.id }?.let { outdated ->
        if (outdated.version != plugin.version) {
            if (plugin.id == pluginDao.getSelectedPlugin()?.id) pluginDao.clearSelectedPlugin()
            updatePlugin(plugin.copy(version = outdated.version), pluginDao, outdated.url)
        }
        if (outdated.active != plugin.active) {
            pluginDao.updatePluginActiveStatus(plugin.id, outdated.active)
        }
    }
}

suspend fun checkAndDownloadNewPlugins(
    downloadUrl: String,
    remotePlugins: List<PluginResponse>,
    pluginDao: PluginDao,
    deletedPlugin: DeletedPluginDao,
    outputDir: String
) {
    val localPlugins = pluginDao.getAllPlugins().filter { it.downloadUrl == downloadUrl }
    val deletedPlugins = deletedPlugin.getAllDeletedPlugins().map { it.id }

    remotePlugins.forEach { remotePlugin ->
        if (localPlugins.none { it.id == remotePlugin.id.lowercase() } && !deletedPlugins.contains(
                remotePlugin.id.lowercase()
            )) {
            val uri = remotePlugin.url
            val filename = uri.substring(uri.lastIndexOf('/') + 1)
            val filePath = "$outputDir/$filename"
            val result = downloadPlugin(uri, filePath)
            if (result) {
                val newPlugin = getPluginFromManifest(
                    filePath,
                    downloadUrl,
                    remotePlugin.image,
                    remotePlugin.version,
                    remotePlugin.active
                )
                newPlugin?.let { pluginDao.insertPlugin(it) }
            }
        }
    }
}

suspend fun updatePlugin(plugin: Plugin, pluginDao: PluginDao, url: String) {
    val directory = File(plugin.filePath)
    if (directory.exists() && directory.delete()) {
        pluginDao.deletePlugin(plugin.id)
        if (downloadPlugin(url, plugin.filePath)) {
            pluginDao.insertPlugin(plugin)
        }
    }
}

suspend fun downloadPlugin(uri: String, outputFilePath: String): Boolean {
    return try {
        app.get(uri).body.byteStream().use { input ->
            withContext(Dispatchers.IO) {
                FileOutputStream(File(outputFilePath)).use { output ->
                    input.copyTo(output)
                }
            }
        }
        true
    } catch (t: Throwable) {
        t.printStackTrace()
        false
    }
}

fun extractDexFileFromZip(context: Context, plugin: Plugin): File? {
    val pluginFile = File(plugin.filePath)
    if (!pluginFile.exists()) return null

    val outputDir = context.getDir("dex", Context.MODE_PRIVATE)
    val outputFile = File(outputDir, "classes.dex")

    if (outputFile.exists()) {
        if (!outputFile.setWritable(true) || !outputFile.delete()) return null
    }

    ZipInputStream(FileInputStream(pluginFile)).use { zipInputStream ->
        var entry: ZipEntry? = zipInputStream.nextEntry
        while (entry != null) {
            if (entry.name == "classes.dex") {
                FileOutputStream(outputFile).use { outputStream ->
                    try {
                        zipInputStream.copyTo(outputStream)
                        outputFile.setReadOnly()
                        return outputFile
                    } catch (t: Throwable) {
                        println("Error writing to file: ${t.message}")
                    }
                }
            }
            entry = zipInputStream.nextEntry
        }
    }
    return null
}

suspend fun downloadPlugins(url: String, pluginDao: PluginDao, outputDir: String) {
    try {
        val response = app.get(url).parsed<PluginResponseRoot>()
        val plugins = pluginDao.getAllPlugins()

        response.plugins.forEach {
            val uri = it.url
            val filename = uri.substring(uri.lastIndexOf('/') + 1)
            val filePath = "$outputDir/$filename"
            val result = downloadPlugin(uri, filePath)
            if (result) {
                val plugin = getPluginFromManifest(filePath, url, it.image, it.version, it.active)
                plugin?.let { pl ->
                    if (plugins.any { p -> p.id == pl.id }) {
                        AppLog.d(TAG, "Plugin ${pl.id} already exists, deleting...")
                        pluginDao.deletePlugin(pl.id)
                    }
                    pluginDao.insertPlugin(pl)
                }
            }
        }
    } catch (t: Throwable) {
        t.printStackTrace()
        AppLog.e(TAG, "Error downloading plugins: ${t.message}")
    }
}