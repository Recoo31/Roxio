package kurd.reco.roxio

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kurd.reco.core.MainVM
import kurd.reco.roxio.ui.player.VideoPlayerScreen
import kurd.reco.roxio.ui.theme.RoxioTheme
import org.koin.compose.koinInject

class PlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoxioTheme {
                val mainVM: MainVM = koinInject()

                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

                mainVM.playDataModel?.let {
                    VideoPlayerScreen(
                        item = it,
                        onItemChange = { clickedItem ->
                            mainVM.fetchForPlayer = true
                            mainVM.clickedItem = clickedItem
                            finish()
                        },
                        onBackPressed = { finish() }
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }
}
