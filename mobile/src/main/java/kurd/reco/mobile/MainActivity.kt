package kurd.reco.mobile

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import kurd.reco.core.MainVM
import kurd.reco.core.SettingsDataStore
import kurd.reco.core.SGCheck
import kurd.reco.core.User
import kurd.reco.core.api.Api.PLUGIN_URL
import kurd.reco.core.plugin.PluginManager
import kurd.reco.mobile.ui.AppUpdateDialog
import kurd.reco.mobile.ui.home.HomeVM
import kurd.reco.mobile.ui.plugin.PluginDialog
import kurd.reco.mobile.ui.theme.RoxioTheme
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
/*
    private val handler = Handler(Looper.getMainLooper())
    private val proxyCheckInterval = 500L

    private fun startProxyMonitoring() {
        handler.post(object : Runnable {
            override fun run() {
                if (isProxyDetected() && !isDebugMode) {
                    finishAndRemoveTask()
                } else {
                    handler.postDelayed(this, proxyCheckInterval)
                }
            }
        })
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            Firebase.crashlytics.isCrashlyticsCollectionEnabled = !Global.isDebugMode

            val mainVM: MainVM = koinInject()
            val settingsDataStore: SettingsDataStore = koinInject()
            FridaUtil.isFridaEnabled(context)
            SGCheck.checkSGIntegrity()


            /*            val playData = PlayDataModel(
                            urls = listOf("Test" to "https://abfr3rdaaaaaaaamntdci7rsigkn5.s3-dub-2.cf.dash.row.aiv-cdn.net/dm/3\$0CiEIAhIFCgMlbSUgbTABUgaAwAKB8AN6A4C4F4IBAQGIAQQYAQ/1@aa25ca561655f4e7b1b384f00dd4502a/5861/37fe/8553/4a7f-94bf-8369afc717ac/614879e9-e2a7-4140-ad86-1e654aa4808c_corrected.mpd?custom=true&encoding=segmentBase&amznDtid=AOAGZA014O5RE&encoding=segmentBase"),
                            id = "1",
                            title = "Test",
                            drm = DrmDataModel(
                                licenseUrl = "https://atv-ps-eu.primevideo.com/cdp/catalog/GetPlaybackResources?deviceID=8b92c9cc-e507-4dd5-a85b-3228b22c7896&deviceTypeID=AOAGZA014O5RE&gascEnabled=true&marketplaceID=A3K6Y4MI8GDYMT&uxLocale=tr_TR&firmware=1&playerType=xp&operatingSystemName=Windows&operatingSystemVersion=10.0&deviceApplicationName=Chrome&asin=amzn1.dv.gti.4cffac3b-0ca5-4410-8d9e-c96c06ca2394&consumptionType=Streaming&desiredResources=Widevine2License&resourceUsage=ImmediateConsumption&videoMaterialType=Feature&clientId=f22dbddb-ef2c-48c5-8876-bed0d47594fd&userWatchSessionId=95d1932c-023d-434d-b1ec-f5f004aa7fb7&displayWidth=826&displayHeight=957&supportsVariableAspectRatio=true&supportsEmbeddedTimedTextForVod=true&deviceProtocolOverride=Https&vodStreamSupportOverride=Auxiliary&deviceStreamingTechnologyOverride=DASH&deviceDrmOverride=CENC&deviceAdInsertionTypeOverride=SSAI&deviceHdrFormatsOverride=None&deviceVideoCodecOverride=H264&deviceVideoQualityOverride=HD&deviceBitrateAdaptationsOverride=CVBR%2CCBR&supportsEmbeddedTrickplayForVod=false&playerAttributes=%7B%22middlewareName%22%3A%22Chrome%22%2C%22middlewareVersion%22%3A%22132.0.0.0%22%2C%22nativeApplicationName%22%3A%22Chrome%22%2C%22nativeApplicationVersion%22%3A%22132.0.0.0%22%2C%22supportedAudioCodecs%22%3A%22AAC%22%2C%22frameRate%22%3A%22HFR%22%2C%22H264.codecLevel%22%3A%224.2%22%2C%22H265.codecLevel%22%3A%220.0%22%2C%22AV1.codecLevel%22%3A%220.0%22%7D&nerid=ZS3T9vBntdUx3dVqq47o7e00",
                                headers = null,
                                clearKey = null
                            ),
                            subtitles = null,
                            streamHeaders = null
                        )
                        Global.playDataModel = playData
                        val intent = Intent(context, PlayerActivity::class.java)
                        context.startActivity(intent)*/




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
                    )
                        .show()
                    downloadMainPlugins(mainVM, context)
                }
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

                    mainVM.checkAppUpdate(context)

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

    private fun downloadMainPlugins(viewModel: MainVM, context: Context) {
        viewModel.downloadPlugins(PLUGIN_URL, context)
    }

}