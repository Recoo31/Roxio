package kurd.reco.mobile

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import kurd.reco.core.FridaUtil
import kurd.reco.core.Global
import kurd.reco.core.Global.showPluginDialog
import kurd.reco.core.viewmodels.MainVM
import kurd.reco.core.SettingsDataStore
import kurd.reco.core.User
import kurd.reco.core.api.Api.PLUGIN_URL
import kurd.reco.core.api.app
import kurd.reco.core.api.appWithDpi
import kurd.reco.core.plugin.PluginManager
import kurd.reco.core.viewmodels.ByeDpiProxyVM
import kurd.reco.core.viewmodels.HomeVM
import kurd.reco.mobile.common.AppUpdateDialog
import kurd.reco.mobile.ui.plugin.PluginDialog
import kurd.reco.mobile.ui.theme.RoxioTheme
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            Firebase.crashlytics.isCrashlyticsCollectionEnabled = !Global.isDebugMode

            val mainVM: MainVM = koinInject()
            val settingsDataStore: SettingsDataStore = koinInject()
            val proxyVM: ByeDpiProxyVM = koinInject()
            FridaUtil.isFridaEnabled(context)

            val isDarkModeEnabled by settingsDataStore.darkThemeEnabled.collectAsState(true)
            val isMaterialThemeEnabled by settingsDataStore.materialThemeEnabled.collectAsState(true)
            val pluginManager: PluginManager = koinInject()

            val navController = rememberNavController()
            val pluginList = pluginManager.getAllPlugins()
            var showFab by remember { mutableStateOf(true) }
            var showSideBar by remember { mutableStateOf(true) }
            val lastPlugin by pluginManager.getSelectedPluginFlow().collectAsState()
            val configuration = LocalConfiguration.current
            val isWideScreen = configuration.screenWidthDp > 600

            val accessToken = User.accessToken

            LaunchedEffect(accessToken) {
                if (pluginList.isEmpty() && accessToken != null) {
                    Toast.makeText(
                        context,
                        getString(R.string.downloading_main_plugins),
                        Toast.LENGTH_SHORT
                    ).show()
                    mainVM.downloadPlugins(PLUGIN_URL, context)
                }
                mainVM.checkOldPlugins(context)
            }


            LaunchedEffect(navController) {
                navController.currentBackStackEntryFlow.collect {
                    showFab =
                        it.destination.route == "home_screen_root" || it.destination.route == "search_screen"
                    showSideBar = it.destination.route != "detail_screen_root/{id}/{isSeries}"
                }
            }

            accessToken?.let {
                val homeVM: HomeVM = koinInject()
                var currentPlugin by homeVM.selectedPlugin

                LaunchedEffect(Unit) {
                    mainVM.checkAppUpdate(context, isMobile = true)
                    currentPlugin = lastPlugin
                }

                LaunchedEffect(lastPlugin) {
                    if (currentPlugin != lastPlugin) {
                        homeVM.resetMovies()
                        homeVM.loadMovies()
                        homeVM.resetCategory()
                        currentPlugin = lastPlugin
                    }
                }
            }

            RoxioTheme(
                darkTheme = isDarkModeEnabled,
                dynamicColor = isMaterialThemeEnabled,
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (accessToken != null && !isWideScreen) {
                            BottomBar(navController)
                        }
                    },
                    floatingActionButton = {
                        if (showFab) {
                            ExtendedFloatingActionButton(
                                text = { Text(text = lastPlugin?.name ?: "None") },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = null,
                                    )
                                },
                                onClick = { showPluginDialog = !showPluginDialog },
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                ) { innerPadding ->

                    if (mainVM.showUpdateDialog) {
                        AppUpdateDialog(mainVM) {
                            mainVM.showUpdateDialog = false
                        }
                    }

                    if (showPluginDialog) {
                        PluginDialog {
                            showPluginDialog = false
                        }
                    }

                    if (isWideScreen && showSideBar && accessToken != null) {
                        SideBar(navController)
                    }

                    Box(modifier = Modifier
                        .padding(innerPadding)
                        .padding(start = if (isWideScreen && showSideBar) 80.dp else 0.dp)) {
                        DestinationsNavHost(
                            navGraph = NavGraphs.root,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}