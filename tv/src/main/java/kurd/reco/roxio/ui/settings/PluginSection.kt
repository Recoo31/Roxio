package kurd.reco.roxio.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Glow
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import coil.compose.AsyncImage
import kurd.reco.core.plugin.PluginManager
import kurd.reco.roxio.R
import kurd.reco.roxio.extractDominantColor
import org.koin.androidx.compose.koinViewModel

@Composable
fun PluginSection(pluginManager: PluginManager = koinViewModel()) {
    var showHiddenPlugins by remember { mutableStateOf(false) }

    val plugins = remember { pluginManager.getAllPlugins() }.filter { it.active && (it.image != null || showHiddenPlugins) }
    val selectedPlugin by pluginManager.getSelectedPluginFlow().collectAsState()

    LazyHorizontalGrid(
        rows = GridCells.Fixed(4),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            IconButton(
                onClick = { showHiddenPlugins = !showHiddenPlugins },
            ) {
                Icon(
                    if (showHiddenPlugins) Icons.Outlined.RemoveRedEye else Icons.Default.RemoveRedEye,
                    contentDescription = null
                )
            }
        }

        items(plugins) { plugin ->
            val isSelected = plugin.id == selectedPlugin?.id
            val base = MaterialTheme.colorScheme.primary
            var baseColor by remember { mutableStateOf(base) }

            Surface(
                onClick = { pluginManager.selectPlugin(plugin.id) },
                modifier = Modifier.padding(8.dp).padding(start = 8.dp),
                shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(8.dp)),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = if (isSelected) baseColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                ),
                glow = ClickableSurfaceDefaults.glow(
                    focusedGlow = Glow(
                        elevation = 36.dp,
                        elevationColor = baseColor
                    )
                )
            ) {
                AsyncImage(
                    model = plugin.image,
                    contentDescription = plugin.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(128.dp),
                    onSuccess = { success ->
                        extractDominantColor(success.result.drawable, base) { color ->
                            baseColor = color
                        }
                    }
                )
            }
        }
    }
}
