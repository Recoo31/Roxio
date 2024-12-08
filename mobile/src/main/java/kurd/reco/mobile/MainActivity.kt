package kurd.reco.mobile

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.AuthScreenDestination
import kurd.reco.core.AuthVM
import kurd.reco.core.MainVM
import kurd.reco.core.SettingsDataStore
import kurd.reco.core.api.Resource
import kurd.reco.core.isProxyDetected
import kurd.reco.core.plugin.PluginManager
import kurd.reco.mobile.ui.login.AuthScreen
import kurd.reco.mobile.ui.login.AuthScreenRoot
import kurd.reco.mobile.ui.plugin.PluginDialog
import kurd.reco.mobile.ui.theme.RoxioTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private val proxyCheckInterval = 2000L
    val isDebugMode = BuildConfig.DEBUG

    private fun startProxyMonitoring() {
        handler.post(object : Runnable {
            override fun run() {
                if (isProxyDetected() && !isDebugMode) {
                    finishAffinity()
                } else {
                    handler.postDelayed(this, proxyCheckInterval)
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current

            startProxyMonitoring()

            val mainVM: MainVM = koinInject()
            val pluginManager: PluginManager = koinInject()
            val settingsDataStore: SettingsDataStore = koinInject()

            val navController = rememberNavController()
            val pluginList = pluginManager.getAllPlugins()
            var showFab by remember { mutableStateOf(true) }
            var showPluginDialog by remember { mutableStateOf(false) }
            val lastPlugin by pluginManager.getSelectedPluginFlow().collectAsState()

            val isDarkModeEnabled by settingsDataStore.darkThemeEnabled.collectAsState(true)
            val isMaterialThemeEnabled by settingsDataStore.materialThemeEnabled.collectAsState(true)

            val accessToken = mainVM.accessToken

            LaunchedEffect(accessToken) {
                if (pluginList.isEmpty() && accessToken != null) {
                    Toast.makeText(context, "Downloading Main Plugins...", Toast.LENGTH_SHORT)
                        .show()
                    downloadMainPlugins(mainVM, context)
                }
            }


            LaunchedEffect(navController) {
                navController.currentBackStackEntryFlow.collect {
                    showFab = it.destination.route == "home_screen_root"
                }
            }

            RoxioTheme(
                darkTheme = isDarkModeEnabled,
                dynamicColor = isMaterialThemeEnabled,
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { accessToken?.let { BottomBar(navController) } },
                    floatingActionButton = {
                        if (showFab) {
                            ExtendedFloatingActionButton(
                                text = { Text(text = lastPlugin?.name ?: "None") },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_baseline_filter_list_24),
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

                    if (showPluginDialog) {
                        PluginDialog {
                            showPluginDialog = false
                        }
                    }

                    Box(modifier = Modifier.padding(innerPadding)) {
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
        val url =
            "https://raw.githubusercontent.com/Recoo31/Roxio-Test-Plugin/refs/heads/main/version.json"
        viewModel.downloadPlugins(url, context)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

}