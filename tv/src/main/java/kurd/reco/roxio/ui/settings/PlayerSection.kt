package kurd.reco.roxio.ui.settings

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

@Composable
fun PlayerSection(settingsDataStore: SettingsDataStore) {
    val forceHighestQuality by settingsDataStore.forceHighestQualityEnabled.collectAsState(initial = true)
    val context = LocalContext.current

    SettingCard(
        title = stringResource(R.string.force_highest_quality),
        description = stringResource(R.string.open_streams_in_the_highest_quality),
        isChecked = forceHighestQuality,
        onClick = { settingsDataStore.setForceHighestQuality(!forceHighestQuality) }
    )
}