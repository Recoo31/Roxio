package kurd.reco.mobile.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import kurd.reco.core.LogItem
import kurd.reco.core.LogType
import kurd.reco.core.SettingsDataStore
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
        SectionHeader(title = "General")
        GeneralSection(settingsDataStore)

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader(title = "Player")
        PlayerSection(settingsDataStore)

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader(title = "Developer")
        DeveloperSettings()
    }
}


@Composable
fun LogItemView(log: LogItem) {
    val color = when (log.type) {
        LogType.INFO -> MaterialTheme.colorScheme.primary
        LogType.DEBUG -> MaterialTheme.colorScheme.secondary
        LogType.WARNING -> MaterialTheme.colorScheme.onBackground
        LogType.ERROR -> MaterialTheme.colorScheme.error
    }
    Text(
        text = log.message,
        color = color,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 4.dp)
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
}