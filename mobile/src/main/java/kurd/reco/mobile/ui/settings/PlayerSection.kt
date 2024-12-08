package kurd.reco.mobile.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kurd.reco.core.SettingsDataStore
import kurd.reco.mobile.ui.settings.composables.PlayerSelectionDialog
import kurd.reco.mobile.ui.settings.composables.SettingCard
import kurd.reco.mobile.ui.settings.composables.SettingSliderItem

@Composable
fun PlayerSection(settingsDataStore: SettingsDataStore) {
    var openPlayerSelection by remember { mutableStateOf(false) }
    val currentPlayer by settingsDataStore.externalPlayer.collectAsState(initial = "")
    val savedSubtitleSize by settingsDataStore.subtitleSize.collectAsState(initial = 16f)
    val context = LocalContext.current

    if (openPlayerSelection) {
        PlayerSelectionDialog(
            context = context,
            selectedPlayer = currentPlayer,
            onDismiss = { openPlayerSelection = false },
            onSelectPlayer = { packageName ->
                settingsDataStore.setExternalPlayer(packageName)
                openPlayerSelection = false
            }
        )
    }

    SettingCard(
        title = "External Player",
        description = currentPlayer.ifEmpty { "Most players don't support DRM" },
        onClick = { openPlayerSelection = true }
    )

    SettingSliderItem(
        title = "Subtitle Size",
        description = "Adjust the subtitle size",
        value = savedSubtitleSize,
        valueRange = 10f..40f,
        steps = 12,
        onValueChange = { newSize ->
            settingsDataStore.setSubtitleSize(newSize)
        }
    )
}