package kurd.reco.mobile.ui.plugin

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kurd.reco.core.MainVM
import kurd.reco.mobile.R

@Composable
fun DownloadDialog(viewModel: MainVM, onDismiss: () -> Unit) {
    var pluginUrl by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.enter_plugin_url)) },
        text = {
            TextField(
                value = pluginUrl,
                onValueChange = { pluginUrl = it },
                label = { Text("URL") },
                shape = RoundedCornerShape(size = 8.dp),
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    viewModel.downloadPlugins(pluginUrl, context)
                    pluginUrl = ""
                }
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}