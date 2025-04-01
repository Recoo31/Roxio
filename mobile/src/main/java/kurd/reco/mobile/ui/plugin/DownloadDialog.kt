package kurd.reco.mobile.ui.plugin

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kurd.reco.core.viewmodels.MainVM
import kurd.reco.mobile.R
import kurd.reco.mobile.ui.theme.RoxioTheme

@Composable
fun DownloadDialog(viewModel: MainVM, onDismiss: () -> Unit) {
    var pluginUrl by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.enter_plugin_url)) },
        text = {
            OutlinedTextField(
                value = pluginUrl,
                onValueChange = { pluginUrl = it },
                label = { Text("URL") },
                shape = MaterialTheme.shapes.medium,
                singleLine = true
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