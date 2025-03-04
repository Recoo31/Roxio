package kurd.reco.mobile.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kurd.reco.mobile.ui.theme.RoxioTheme


@Composable
fun InfoDialog(
    title: String,
    message: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Confirm", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Dismiss", color = MaterialTheme.colorScheme.onSurface)
            }
        },
        title = {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
        },
        text = {
            message?.let { Text(text = message, style = MaterialTheme.typography.bodyMedium) }
        },
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    )
}


@Preview
@Composable
fun InfoDialogPreview() {
    RoxioTheme {
        InfoDialog(
            title = "Title",
            message = "Message",
            onConfirm = { /*TODO*/ },
            onDismiss = { /*TODO*/ }
        )
    }
}