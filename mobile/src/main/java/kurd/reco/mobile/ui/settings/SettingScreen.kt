package kurd.reco.mobile.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import kurd.reco.core.SettingsDataStore
import kurd.reco.mobile.R
import kurd.reco.mobile.ui.settings.composables.SectionHeader
import org.koin.compose.koinInject

@Destination<RootGraph>
@Composable
fun SettingScreenRoot(settingsDataStore: SettingsDataStore = koinInject()) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        SectionHeader(title = stringResource(R.string.general))
        GeneralSection(settingsDataStore)

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader(title = stringResource(R.string.player))
        PlayerSection(settingsDataStore)

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader(title = stringResource(R.string.developer))
        DeveloperSettings()
    }
}