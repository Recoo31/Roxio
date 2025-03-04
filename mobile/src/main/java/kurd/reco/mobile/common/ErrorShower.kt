package kurd.reco.mobile.common

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kurd.reco.mobile.ui.theme.RoxioTheme

@Composable
fun ErrorShower(
    errorText: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            println("onDismissRequest")
            onDismiss()
        },
        confirmButton = {
            Text(
                text = "Retry",
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { onRetry() }
            )
        },
        title = { Text(text = "Error") },
        text = { Text(text = errorText) },
        icon = { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
    )
}
@Preview(showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun ErrorShowerPreview() {
    RoxioTheme {
        ErrorShower(
            errorText = "Lorem ipsum dolor sit amet, excepteur eiusmod consequat mollit proident commodo duis lorem qui magna proident proident reprehenderit officia commodo culpa lorem pariatur exercitation velit non minim culpa ullamco tempor",
            {},
            {}
        )
    }
}