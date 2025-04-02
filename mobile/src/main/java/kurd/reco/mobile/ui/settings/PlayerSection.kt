package kurd.reco.mobile.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kurd.reco.core.SettingsDataStore
import kurd.reco.mobile.R
import kurd.reco.mobile.ui.settings.composables.PlayerSelectionDialog
import kurd.reco.mobile.ui.settings.composables.SettingCard
import kurd.reco.mobile.ui.settings.composables.SettingSliderItem

@Composable
fun PlayerSection(settingsDataStore: SettingsDataStore) {
    var openPlayerSelection by remember { mutableStateOf(false) }
    val currentPlayer by settingsDataStore.externalPlayer.collectAsState(initial = "")
    val savedSubtitleSize by settingsDataStore.subtitleSize.collectAsState(initial = 21f)
    val forceHighestQuality by settingsDataStore.forceHighestQualityEnabled.collectAsState(initial = true)
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
        title = stringResource(R.string.external_player),
        description = currentPlayer.ifEmpty { stringResource(R.string.external_player_desc) },
        onClick = { openPlayerSelection = true }
    )

    SettingCard(
        title = stringResource(R.string.force_highest_quality),
        description = stringResource(R.string.open_streams_in_the_highest_quality),
        isChecked = forceHighestQuality,
        onCheckedChange = {
            settingsDataStore.setForceHighestQuality(it)
        }
    )

    SettingSliderItem(
        title = stringResource(R.string.subtitle_size),
        description = stringResource(R.string.subtitle_size_desc),
        value = savedSubtitleSize,
        valueRange = 10f..40f,
        steps = 12,
        onValueChange = { newSize ->
            settingsDataStore.setSubtitleSize(newSize)
        }
    )
}