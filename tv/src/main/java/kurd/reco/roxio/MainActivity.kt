package kurd.reco.roxio

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.Surface
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import kurd.reco.core.MainVM
import kurd.reco.core.isProxyDetected
import kurd.reco.core.plugin.PluginManager
import kurd.reco.roxio.ui.RoxioNavigationDrawer
import kurd.reco.roxio.ui.theme.RoxioTheme
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
        setContent {
            val context = LocalContext.current
            val mainVM: MainVM = koinInject()
            val pluginManager: PluginManager = koinInject()

            startProxyMonitoring()

            val navController = rememberNavController()

            val pluginList = pluginManager.getAllPlugins()
            val accessToken = mainVM.accessToken


            LaunchedEffect(accessToken) {
                if (pluginList.isEmpty() && accessToken != null) {
                    Toast.makeText(context, "Downloading Main Plugins...", Toast.LENGTH_SHORT).show()
                    downloadMainPlugins(mainVM, context)
                }
            }

            RoxioTheme {
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
        val url = "https://raw.githubusercontent.com/Recoo31/Roxio-Test-Plugin/refs/heads/main/version.json"
        viewModel.downloadPlugins(url, context)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}