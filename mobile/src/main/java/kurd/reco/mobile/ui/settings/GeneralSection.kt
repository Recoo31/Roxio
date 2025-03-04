package kurd.reco.mobile.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import kurd.reco.core.SettingsDataStore
import kurd.reco.mobile.R
import kurd.reco.mobile.ui.settings.composables.SettingCard

@Composable
fun GeneralSection(settingsDataStore: SettingsDataStore) {
    val isDarkModeEnabled by settingsDataStore.darkThemeEnabled.collectAsState(false)
    val isMaterialThemeEnabled by settingsDataStore.materialThemeEnabled.collectAsState(false)
    val showTitleEnabled by settingsDataStore.showTitleEnabled.collectAsState(true)
    //val useVpnEnabled by settingsDataStore.useVpnEnabled.collectAsState(false)

    SettingCard(
        title = stringResource(R.string.dark_theme),
        description = stringResource(R.string.dark_theme_desc),
        isChecked = isDarkModeEnabled,
        onCheckedChange = { settingsDataStore.setDarkMode(it) }
    )

    SettingCard(
        title = stringResource(R.string.material_theme),
        description = stringResource(R.string.material_theme_desc),
        isChecked = isMaterialThemeEnabled,
        onCheckedChange = { settingsDataStore.saveMaterialTheme(it) }
    )

    SettingCard(
        title = stringResource(R.string.show_title),
        description = stringResource(R.string.show_title_desc),
        isChecked = showTitleEnabled,
        onCheckedChange = { settingsDataStore.setShowTitle(it) }
    )

//    SettingCard(
//        title = "Use VPN",
//        description = "Use VPN to stream content",
//        isChecked = useVpnEnabled,
//        onCheckedChange = { settingsDataStore.setUseVpn(it) }
//    )

}