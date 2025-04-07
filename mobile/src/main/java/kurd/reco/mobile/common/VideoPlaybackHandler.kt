package kurd.reco.mobile.common

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import kurd.reco.core.Global
import kurd.reco.core.Global.errorModel
import kurd.reco.core.api.Resource
import kurd.reco.core.api.model.PlayDataModel
import kurd.reco.core.data.ErrorModel
import kurd.reco.mobile.PlayerActivity
import kurd.reco.mobile.ui.player.openVideoWithSelectedPlayer

@Composable
fun VideoPlaybackHandler(
    clickedItem: Resource<PlayDataModel>,
    isClicked: Boolean,
    externalPlayer: String = "",
    clearClickedItem: () -> Unit,
    onDone: () -> Unit = {},
    customTitle: String? = null
) {
    val context = LocalContext.current
    var showMultiSelect by remember { mutableStateOf(false) }

    when (clickedItem) {
        is Resource.Success -> {
            LaunchedEffect(clickedItem) {
                val playData = clickedItem.value
                
                Global.playDataModel = customTitle?.let {
                    playData.copy(title = it) 
                } ?: playData

                if (playData.urls.size > 1) {
                    showMultiSelect = true
                } else {
                    if (externalPlayer.isNotEmpty() && playData.drm == null) {
                        openVideoWithSelectedPlayer(
                            context = context,
                            videoUri = playData.urls[0].second,
                            playerPackageName = externalPlayer
                        )
                    } else {
                        val intent = Intent(context, PlayerActivity::class.java)
                        context.startActivity(intent)
                        clearClickedItem()
                    }
                }
                onDone()
            }
        }

        is Resource.Failure -> {
            LaunchedEffect(clickedItem) {
                errorModel = ErrorModel(clickedItem.error, true)
                clearClickedItem()
                onDone()
            }
        }

        is Resource.Loading -> {
            if (isClicked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (showMultiSelect) {
        Dialog(
            onDismissRequest = { 
                showMultiSelect = false
                clearClickedItem()
            }
        ) {
            MultiSourceDialog(Global.playDataModel, context) {
                Global.playDataModel = it
                showMultiSelect = false
                clearClickedItem()
                onDone()
            }
        }
    }
} 