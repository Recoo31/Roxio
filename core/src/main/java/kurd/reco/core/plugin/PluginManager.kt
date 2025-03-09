package kurd.reco.core.plugin

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import dalvik.system.PathClassLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kurd.reco.core.AppLog
import kurd.reco.core.Global
import kurd.reco.core.MainVM
import kurd.reco.core.api.RemoteRepo
import kurd.reco.core.api.app
import kurd.reco.core.data.db.plugin.DeletedPluginDao
import kurd.reco.core.data.db.plugin.Plugin
import kurd.reco.core.data.db.plugin.PluginDao

@Keep class PluginManager(
    private val pluginDao: PluginDao,
    private val deletedPluginDao: DeletedPluginDao,
    private val context: Context,
    private val mainVM: MainVM
) : ViewModel() {

    private var pluginInstance: RemoteRepo? = null
    private val outputDir = context.filesDir.path

    private val _selectedPlugin = MutableStateFlow<Plugin?>(null)
    //val selectedPlugin: StateFlow<Plugin?> get() = _selectedPlugin

    init {
        runBlocking {
            checkPluginUpdates()
            _selectedPlugin.value = getLastSelectedPlugin()
        }
    }

    private suspend fun checkPluginUpdates() {
        coroutineScope {
            val pluginGroups = pluginDao.getAllPlugins().groupBy { it.downloadUrl }

            pluginGroups.entries.map { (url, plugins) ->
                launch(Dispatchers.IO) {
                    runCatching {
                        val response = app.get(url).parsed<PluginResponseRoot>()
                        val remotePlugins = response.plugins

                        plugins.map { plugin ->
                            checkUpdate(plugin, pluginDao, remotePlugins)
                        }

                        checkAndDownloadNewPlugins(
                            url,
                            remotePlugins,
                            pluginDao,
                            deletedPluginDao,
                            outputDir
                        )
                    }
                }
            }
        }
    }

    fun selectPlugin(pluginID: String) {
        pluginDao.clearSelectedPlugin()
        pluginDao.selectPlugin(pluginID)
        pluginDao.getPluginById(pluginID)?.let { plugin ->
            pluginInstance = loadPlugin(plugin)
            _selectedPlugin.value = plugin
        }
    }

    private fun loadLastSelectedPlugin() {
        pluginDao.getSelectedPlugin()?.let { pluginInstance = loadPlugin(it) }
    }

    fun getSelectedPluginFlow(): StateFlow<Plugin?> = _selectedPlugin

    fun getLastSelectedPlugin(): Plugin? = pluginDao.getSelectedPlugin()


    private fun loadPlugin(plugin: Plugin): RemoteRepo? {
        val dexFile = extractDexFileFromZip(context, plugin) ?: return null
        val className = "${plugin.classPath}.${plugin.className}"
        val loader = PathClassLoader(dexFile.absolutePath, context.classLoader)
        return runCatching {
            val loadClass = loader.loadClass(className).getDeclaredConstructor().newInstance() as? RemoteRepo

            runCatching {
                val token = mainVM.accessToken
                token?.let {
                    loadClass?.getAccessToken(it)
                }
            }
            Global.currentPlugin = plugin

            loadClass
        }.onFailure { e ->
            Log.e("PluginManager", "Error loading plugin", e)
            AppLog.e("Error loading plugin", e.toString())
        }.getOrNull()
    }

    fun getSelectedPlugin(): RemoteRepo {
        return pluginInstance ?: run {
            loadLastSelectedPlugin()
            pluginInstance!!
        }
    }

    fun getAllPlugins(): List<Plugin> = pluginDao.getAllPlugins()
}

