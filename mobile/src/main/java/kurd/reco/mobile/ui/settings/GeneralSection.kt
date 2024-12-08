package kurd.reco.mobile.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kurd.reco.core.SettingsDataStore
import kurd.reco.mobile.ui.settings.composables.SettingCard

@Composable
fun GeneralSection(settingsDataStore: SettingsDataStore) {
    val isDarkModeEnabled by settingsDataStore.darkThemeEnabled.collectAsState(false)
    val isMaterialThemeEnabled by settingsDataStore.materialThemeEnabled.collectAsState(false)
    val showTitleEnabled by settingsDataStore.showTitleEnabled.collectAsState(false)

    SettingCard(
        title = "Dark Theme",
        description = "Enable dark theme",
        isChecked = isDarkModeEnabled,
        onCheckedChange = { settingsDataStore.setDarkMode(it) }
    )

    SettingCard(
        title = "Material Theme",
        description = "Use material theme for the app",
        isChecked = isMaterialThemeEnabled,
        onCheckedChange = { settingsDataStore.saveMaterialTheme(it) },
    )

    SettingCard(
        title = "Show Title",
        description = "Show title in the home screen",
        isChecked = showTitleEnabled,
        onCheckedChange = { settingsDataStore.setShowTitle(it) },
        )

}