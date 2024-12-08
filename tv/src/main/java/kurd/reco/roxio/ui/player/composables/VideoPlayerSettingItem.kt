package kurd.reco.roxio.ui.player.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.SelectableSurfaceDefaults
import androidx.tv.material3.ShapeDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

@Composable
fun VideoPlayerSettingItem(
    focusRequester: FocusRequester,
    text: String,
    isSelect: Boolean = false,
    onClick: () -> Unit
) {
    val color =
        if (isSelect) MaterialTheme.colorScheme.onSurface else Color.Transparent

    var isFocused by remember { mutableStateOf(false) }
    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        modifier = Modifier
            .padding(4.dp)
            .size(202.dp, 40.dp)
            .border(2.dp, borderColor, shape = MaterialTheme.shapes.small)
            .focusRequester(focusRequester)
            .onFocusChanged {
                isFocused = it.isFocused
            }
            .focusable(),
        selected = isSelect,
        onClick = onClick,
        shape = SelectableSurfaceDefaults.shape(ShapeDefaults.Small),
        colors = SelectableSurfaceDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    color = if (isSelect) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )

                if (isSelect) {
                    Icon(
                        Icons.Default.Done,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 8.dp),
                        tint = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }
    }
}