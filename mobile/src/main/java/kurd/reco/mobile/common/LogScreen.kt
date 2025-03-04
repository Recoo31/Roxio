package kurd.reco.mobile.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kurd.reco.core.AppLog.logs
import kurd.reco.core.LogItem
import kurd.reco.core.LogType
import kurd.reco.core.shareLogs


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row {
                Text(text = "Logs", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { shareLogs(context) }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Logs",
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = { logs = emptyList() ; onDismiss() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear Logs",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

            SelectionContainer {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 16.dp, start = 8.dp, end = 8.dp)
                ) {
                    items(logs) { log ->
                        LogItemView(log)
                    }
                }
            }
        }
    }
}


@Composable
fun LogItemView(log: LogItem) {
    val color = when (log.type) {
        LogType.INFO -> MaterialTheme.colorScheme.primary
        LogType.DEBUG -> MaterialTheme.colorScheme.secondary
        LogType.WARNING -> MaterialTheme.colorScheme.onBackground
        LogType.ERROR -> MaterialTheme.colorScheme.error
    }
    Text(
        text = log.message,
        color = color,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 4.dp)
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
}