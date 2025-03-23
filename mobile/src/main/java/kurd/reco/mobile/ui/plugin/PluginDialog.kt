package kurd.reco.mobile.ui.plugin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kurd.reco.core.viewmodels.MainVM
import kurd.reco.core.data.db.plugin.Plugin
import kurd.reco.core.plugin.PluginManager
import kurd.reco.mobile.R
import org.koin.androidx.compose.koinViewModel

private val TAG = "PluginBottomSheet"

@Composable
fun PluginDialog(
    viewModel: MainVM = koinViewModel(),
    pluginManager: PluginManager = koinViewModel(),
    onDismiss: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var isDeleteMode by remember { mutableStateOf(false) }
    var showHiddenPlugins by remember { mutableStateOf(false) }
    var pluginToDelete by remember { mutableStateOf<Plugin?>(null) }
    val textTitle = if (isDeleteMode) stringResource(R.string.delete_plugin) else stringResource(R.string.select_plugin)
    val pluginsState by pluginManager.getAllPluginsFlow().collectAsState(initial = pluginManager.getAllPlugins())
    var plugins = pluginsState.filter { it.active }

    if (showAddDialog) {
        DownloadDialog(viewModel) { showAddDialog = false }
    }

    pluginToDelete?.let { plugin ->
        ConfirmDeleteDialog(plugin,
            onDeleteConfirmed = {
                viewModel.deletePlugin(plugin)
                pluginToDelete = null
                plugins = pluginManager.getAllPlugins()
            },
            onDismiss = { pluginToDelete = null }
        )
    }

    AlertDialog(
        modifier = Modifier.fillMaxHeight(0.8f),
        onDismissRequest = { onDismiss() },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = textTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = { isDeleteMode = !isDeleteMode },
                    ) {
                        Icon(
                            imageVector = if (isDeleteMode) Icons.Default.Check else Icons.Default.Delete,
                            contentDescription = if (isDeleteMode) stringResource(R.string.exit_delete_mode) else stringResource(R.string.enter_delete_mode)
                        )
                    }
                    IconButton(
                        onClick = { showAddDialog = true },
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_plugin))
                    }
                    IconButton(
                        onClick = { showHiddenPlugins = !showHiddenPlugins },
                    ) {
                        Icon(
                            painterResource(if (showHiddenPlugins) R.drawable.outline_remove_eye_24 else R.drawable.baseline_remove_eye_24),
                            contentDescription = null
                        )
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2)
                ) {
                    items(plugins) { plugin ->
                        val isSelected = plugin.id == pluginManager.getLastSelectedPlugin()?.id

                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable {
                                    if (isDeleteMode) {
                                        pluginToDelete = plugin
                                    } else {
                                        onDismiss()
                                        pluginManager.selectPlugin(plugin.id)
                                    }
                                }
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.secondary.copy(0.6f)
                                        isDeleteMode -> MaterialTheme.colorScheme.errorContainer
                                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (plugin.image != null) {
                                AsyncImage(
                                    model = plugin.image,
                                    contentDescription = plugin.name,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(64.dp)
                                )
                            } else if (showHiddenPlugins) {
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
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}
