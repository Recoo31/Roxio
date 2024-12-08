package kurd.reco.mobile.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kurd.reco.mobile.common.LogScreen
import kurd.reco.mobile.ui.settings.composables.SettingCard

@Composable
fun DeveloperSettings() {
    var showLogs by remember { mutableStateOf(false) }

    if (showLogs) {
        LogScreen {
            showLogs = false
        }
    }

    SettingCard(
        title = "Logs",
        description = "App logs and errors",
        onClick = {
            showLogs = !showLogs
        }
    )
}