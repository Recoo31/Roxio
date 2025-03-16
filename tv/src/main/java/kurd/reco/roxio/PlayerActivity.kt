package kurd.reco.roxio

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kurd.reco.core.Global
import kurd.reco.roxio.ui.player.VideoPlayerScreen
import kurd.reco.roxio.ui.theme.RoxioTheme

class PlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoxioTheme {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

                Global.playDataModel?.let {
                    VideoPlayerScreen(
                        item = it,
                        onItemChange = { clickedItem ->
                            Global.fetchForPlayer = true
                            Global.clickedItem = clickedItem
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
