package kurd.reco.roxio.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.tv.material3.Text

@Composable
fun Error(modifier: Modifier = Modifier, error: String) {
    Text(
        text = error,
        modifier = modifier
    )
}
