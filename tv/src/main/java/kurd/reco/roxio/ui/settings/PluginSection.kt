package kurd.reco.roxio.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Glow
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import kurd.reco.core.SettingsDataStore
import kurd.reco.core.plugin.PluginManager
import kurd.reco.core.viewmodels.MainVM
import kurd.reco.roxio.R
import kurd.reco.roxio.common.CustomTextField
import kurd.reco.roxio.common.TvAlertDialog
import kurd.reco.roxio.extractDominantColor
import kurd.reco.roxio.ui.theme.RoxioTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun PluginSection(
    mainVM: MainVM = koinInject(),
    pluginManager: PluginManager = koinViewModel(),
    settingsDataStore: SettingsDataStore = koinInject(),
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val showMorePluginsEnabled by settingsDataStore.showMorePluginsEnabled.collectAsState(false)

    val plugins = remember { pluginManager.getAllPlugins() }.filter { it.active && (it.image != null || showMorePluginsEnabled) }
    val selectedPlugin by pluginManager.getSelectedPluginFlow().collectAsState()

    if (showAddDialog) {
        DownloadDialog(mainVM) { showAddDialog = false }
    }

    LazyHorizontalGrid(
        rows = GridCells.Fixed(4),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(plugins) { plugin ->
            val isSelected = plugin.id == selectedPlugin?.id
            val base = MaterialTheme.colorScheme.primary
            var baseColor by remember { mutableStateOf(base) }

            Surface(
                onClick = { pluginManager.selectPlugin(plugin.id) },
                modifier = Modifier
                    .padding(8.dp)
                    .padding(start = 8.dp),
                shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(8.dp)),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = if (isSelected) baseColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                ),
                glow = ClickableSurfaceDefaults.glow(
                    focusedGlow = Glow(
                        elevation = 36.dp,
                        elevationColor = baseColor
                    )
                )
            ) {
                if (plugin.image != null) {
                    AsyncImage(
                        model = plugin.image,
                        contentDescription = plugin.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(128.dp),
                        onSuccess = { success ->
                            extractDominantColor(success.result.drawable, base) { color ->
                                baseColor = color
                            }
                        }
                    )
                } else if (showMorePluginsEnabled) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Extension,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(width = 128.dp, height = 100.dp)
                        )
                        Text(
                            text = plugin.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadDialog(viewModel: MainVM = koinInject(), onDismiss: () -> Unit) {
    val context = LocalContext.current
    var pluginUrl by remember { mutableStateOf("") }

    TvAlertDialog(
        title = "Download Plugin",
        message = {
            CustomTextField(
                value = pluginUrl,
                onValueChange = { pluginUrl = it },
                placeholder = "Enter plugin URL",
                surfaceContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                singleLine = true,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        },
        onConfirm = {
            onDismiss()
            viewModel.downloadPlugins(pluginUrl, context)
            pluginUrl = ""
        },
        onDismiss = { onDismiss() }
    )
}