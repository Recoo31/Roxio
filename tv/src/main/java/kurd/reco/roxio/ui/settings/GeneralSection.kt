package kurd.reco.roxio.ui.settings

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kurd.reco.core.SettingsDataStore
import kurd.reco.roxio.R
import kurd.reco.roxio.common.SettingCard
import kotlin.system.exitProcess

@Composable
fun GeneralSection(settingsDataStore: SettingsDataStore) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    val isDarkModeEnabled by settingsDataStore.darkThemeEnabled.collectAsState(false)
    val showTitleEnabled by settingsDataStore.showTitleEnabled.collectAsState(true)
    val showMorePluginsEnabled by settingsDataStore.showMorePluginsEnabled.collectAsState(false)
    //val useVpnEnabled by settingsDataStore.useVpnEnabled.collectAsState(false)

    if (showAddDialog) {
        DownloadDialog { showAddDialog = false }
    }

    SettingCard(
        title = stringResource(R.string.dark_theme),
        description = stringResource(R.string.dark_theme_desc),
        isChecked = isDarkModeEnabled,
        onClick = { settingsDataStore.setDarkMode(!isDarkModeEnabled) }
    )

    SettingCard(
        title = "Download Plugin",
        onClick = {
            showAddDialog = !showAddDialog
        }
    )

    SettingCard(
        title = "Show More Plugins",
        isChecked = showMorePluginsEnabled,
        onClick = {
            settingsDataStore.showMorePlugins(!showMorePluginsEnabled)
        }
    )

    SettingCard(
        title = stringResource(R.string.close_roxio),
        description = stringResource(R.string.close_roxio_desc),
        onClick = {
            (context as Activity).finishAffinity()
            exitProcess(0)
        }
    )


//    SettingCard(
//        title = "Use VPN",
//        description = "Use VPN to stream content",
//        isChecked = useVpnEnabled,
//        onCheckedChange = { settingsDataStore.setUseVpn(it) }
//    )

}