package kurd.reco.mobile.ui.plugin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kurd.reco.core.MainVM
import kurd.reco.core.data.db.plugin.Plugin
import kurd.reco.core.plugin.PluginManager
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
    var pluginToDelete by remember { mutableStateOf<Plugin?>(null) }
    val textTitle = if (isDeleteMode) "Delete Plugin" else "Select Plugin"
    var plugins = pluginManager.getAllPlugins()
//
//    if (showAddDialog) {
//        DownloadDialog(viewModel) { showAddDialog = false }
//    }
//
//    pluginToDelete?.let { plugin ->
//        ConfirmDeleteDialog(plugin,
//            onDeleteConfirmed = {
//                viewModel.deletePlugin(plugin)
//                pluginToDelete = null
//                plugins = pluginManager.getAllPlugins()
//            },
//            onDismiss = { pluginToDelete = null }
//        )
//    }

    AlertDialog(
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
                            contentDescription = if (isDeleteMode) "Exit Delete Mode" else "Enter Delete Mode"
                        )
                    }
                    IconButton(
                        onClick = { showAddDialog = true },
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Plugin")
                    }
                }

                LazyColumn {
                    items(plugins) { plugin ->
                        val isSelected = plugin.id == pluginManager.getLastSelectedPlugin()?.id
                        Button(
                            onClick = {
                                if (isDeleteMode) {
                                    pluginToDelete = plugin
                                } else {
                                    onDismiss()
                                    pluginManager.selectPlugin(plugin.id)
                                }
                            },
                            enabled = plugin.active,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isDeleteMode -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                },
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = plugin.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(8.dp),
                                textDecoration = if (!plugin.active) TextDecoration.LineThrough else null
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
