package kurd.reco.mobile

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kurd.reco.core.MainVM
import kurd.reco.mobile.ui.player.VideoPlayerCompose
import kurd.reco.mobile.ui.player.hideSystemBars
import kurd.reco.mobile.ui.theme.RoxioTheme
import org.koin.compose.koinInject


class PlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoxioTheme {
                Surface {
                    val mainVM: MainVM = koinInject()
                    val playDataModel = mainVM.playDataModel
                    val useVpn = mainVM.useVpn

                    var oldOrientation by rememberSaveable { mutableIntStateOf(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) }
                    DisposableEffect(Unit) {
                        oldOrientation = requestedOrientation
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                        onDispose {
                            requestedOrientation = oldOrientation
                        }
                    }

                    hideSystemBars(window)

                    playDataModel?.let {
                        VideoPlayerCompose(it, useVpn) { homeItem ->
                            mainVM.playDataModel = null
                            mainVM.fetchForPlayer = true
                            mainVM.clickedItem = homeItem
                            finishAndRemoveTask()
                        }
                    }
                }
            }
        }
    }
}