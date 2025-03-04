package kurd.reco.mobile.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import kurd.reco.mobile.R
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
        description = stringResource(R.string.app_logs_desc),
        onClick = {
            showLogs = !showLogs
        }
    )
}