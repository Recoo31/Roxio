package kurd.reco.roxio.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import kurd.reco.core.MainVM
import kurd.reco.roxio.R
import java.io.File

@Composable
fun AppUpdateDialog(viewModel: MainVM, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val outputFilePath = File(context.filesDir, "roxio_${viewModel.currentVersion}.apk")
    val downloadProgress = viewModel.downloadProgress
    val changeLog = viewModel.changeLog
    
    val updateButtonFocusRequester = remember { FocusRequester() }

    Dialog(
        onDismissRequest = { onDismiss() }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(32.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.app_update),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (changeLog.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.change_log),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    changeLog.forEach { log ->
                        Text(
                            text = "• $log",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                when (downloadProgress) {
                    0f -> {
                        Button(
                            onClick = { viewModel.downloadApk(outputFilePath) },
                            modifier = Modifier
                                .padding(8.dp)
                                .focusRequester(updateButtonFocusRequester)
                                .fillMaxWidth()
                        ) {
                            Text(text = stringResource(R.string.update))
                        }

                        Button(
                            onClick = { onDismiss() },
                            modifier = Modifier
                                .padding(8.dp)
                                .focusRequester(updateButtonFocusRequester)
                                .fillMaxWidth()
                        ) {
                            Text(text = stringResource(R.string.cancel))
                        }
                    }
                    100f -> {
                        Button(
                            onClick = { viewModel.installUpdate(outputFilePath, context) },
                            modifier = Modifier
                                .focusRequester(updateButtonFocusRequester)
                                .fillMaxWidth()
                        ) {
                            Text(text = stringResource(R.string.install))
                        }
                    }
                    else -> {
                        Text(
                            text = "Downloading: ${downloadProgress.toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
            }
        }
    }
}