package kurd.reco.roxio.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import kurd.reco.core.plugin.PluginManager
import org.koin.androidx.compose.koinViewModel

@Destination<RootGraph>
@Composable
fun SettingsScreen(pluginManager: PluginManager = koinViewModel()) {
    var plugins = remember { pluginManager.getAllPlugins() }
    val selectedPlugin by pluginManager.getSelectedPluginFlow().collectAsState()

    LazyColumn {
        items(plugins) { plugin ->
            val isSelected = plugin.id == selectedPlugin?.id
            val pluginActive = plugin.active
            Button(
                onClick = {
                    pluginManager.selectPlugin(plugin.id)
                },
                enabled = pluginActive,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 64.dp, vertical = 8.dp),
                colors = ButtonDefaults.colors(
                    containerColor = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    },
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                shape = ButtonDefaults.shape(
                    shape = RoundedCornerShape(8.dp)
                )
            ) {
                Text(
                    text = plugin.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (!plugin.active) TextDecoration.LineThrough else null,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}