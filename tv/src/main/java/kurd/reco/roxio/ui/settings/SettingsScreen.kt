package kurd.reco.roxio.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ClickableSurfaceDefaults
import kurd.reco.core.SettingsDataStore
import org.koin.compose.koinInject

enum class Setting {
    Plugins,
    General,
    Player,
    Developer
}

@Destination<RootGraph>
@Composable
fun SettingsScreen() {
    var selectedSetting by remember { mutableStateOf(Setting.Plugins) }

    Row(modifier = Modifier.fillMaxSize()) {
        SettingsMenu(
            selectedSetting = selectedSetting,
            onSettingSelected = { selectedSetting = it },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
        SettingsDetail(
            setting = selectedSetting,
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
        )
    }
}

@Composable
fun SettingsMenu(
    selectedSetting: Setting,
    onSettingSelected: (Setting) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = Setting.entries.toTypedArray()

    LazyColumn(modifier = modifier.padding(16.dp)) {
        items(items) { item ->
            val isSelected = item == selectedSetting

            Surface(
                onClick = { onSettingSelected(item) },
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(8.dp)),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                )
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun SettingsDetail(setting: Setting, modifier: Modifier = Modifier, settingsDataStore: SettingsDataStore = koinInject()) {
    Column(modifier = modifier.padding(16.dp)) {
        when (setting) {
            Setting.Plugins -> PluginSection()
            Setting.General -> GeneralSection(settingsDataStore)
            Setting.Player -> PlayerSection(settingsDataStore)
            else -> {
                Text(text = "Settings Detail for $setting")
            }
        }
    }

}
