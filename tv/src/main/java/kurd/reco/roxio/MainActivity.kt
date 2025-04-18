package kurd.reco.roxio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.Surface
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import kurd.reco.core.FridaUtil
import kurd.reco.core.Global
import kurd.reco.core.Global.pluginLoaded
import kurd.reco.core.viewmodels.MainVM
import kurd.reco.core.SGCheck
import kurd.reco.core.SettingsDataStore
import kurd.reco.core.User
import kurd.reco.core.api.Api.PLUGIN_URL
import kurd.reco.core.api.model.PlayDataModel
import kurd.reco.core.plugin.PluginManager
import kurd.reco.core.viewmodels.ByeDpiProxyVM
import kurd.reco.core.viewmodels.HomeVM
import kurd.reco.roxio.common.AppUpdateDialog
import kurd.reco.roxio.ui.RoxioNavigationDrawer
import kurd.reco.roxio.ui.theme.RoxioTheme
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            Global.platform = "tv"

            val proxyVM: ByeDpiProxyVM = koinInject()
            val mainVM: MainVM = koinInject()
            val pluginManager: PluginManager = koinInject()
            val settingsDataStore: SettingsDataStore = koinInject()

            FridaUtil.isFridaEnabled(context)

            val navController = rememberNavController()

            val pluginList = pluginManager.getAllPlugins()
            val accessToken by remember { derivedStateOf { User.accessToken } }

            val isDarkModeEnabled by settingsDataStore.darkThemeEnabled.collectAsState(true)
            val lastPlugin by pluginManager.getSelectedPluginFlow().collectAsState()

            LaunchedEffect(accessToken) {
                if (pluginList.isEmpty() && accessToken != null) {
                    Toast.makeText(context, "Downloading Main Plugins...", Toast.LENGTH_SHORT).show()
                    downloadMainPlugins(mainVM, context)
                }
                mainVM.checkOldPlugins(context)
            }

            accessToken?.let {
                val homeVM: HomeVM = koinInject()
                var currentPlugin by homeVM.selectedPlugin

                LaunchedEffect(Unit) {
                    if (currentPlugin == null) {
                        currentPlugin = lastPlugin
                    }
                    mainVM.checkAppUpdate(context, isMobile = false)
                }

                LaunchedEffect(lastPlugin) {
                    if (currentPlugin != lastPlugin) {
                        homeVM.loadMovies()
                        homeVM.resetCategory()
                        pluginLoaded = false
                        currentPlugin = lastPlugin
                    }
                }
            }

            RoxioTheme(
                darkTheme = isDarkModeEnabled,
            ) {
                if (mainVM.showUpdateDialog) {
                    AppUpdateDialog(mainVM) {
                        mainVM.showUpdateDialog = false
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    RoxioNavigationDrawer(navController = navController) {
                        DestinationsNavHost(
                            navGraph = NavGraphs.root,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
    private fun downloadMainPlugins(viewModel: MainVM, context: Context) {
        viewModel.downloadPlugins(PLUGIN_URL, context)
    }
}