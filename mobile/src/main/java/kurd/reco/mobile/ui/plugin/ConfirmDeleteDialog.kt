package kurd.reco.mobile.ui.plugin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kurd.reco.core.data.db.plugin.Plugin
import kurd.reco.mobile.R

@Composable
fun ConfirmDeleteDialog(plugin: Plugin, onDeleteConfirmed: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.delete_plugin)) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(R.string.delete_plugin_des) + plugin.name)
                AsyncImage(model = plugin.image, contentDescription = plugin.name, modifier = Modifier.size(128.dp))
            }
        },
        confirmButton = {
            Button(onClick = onDeleteConfirmed) { Text(stringResource(R.string.delete)) }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}