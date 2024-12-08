package kurd.reco.roxio.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Glow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.ShapeDefaults
import androidx.tv.material3.StandardCardContainer
import androidx.tv.material3.Surface

@Composable
fun MovieCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    title: @Composable () -> Unit = {},
    image: @Composable BoxScope.() -> Unit,
) {
    StandardCardContainer(
        modifier = modifier,
        title = title,
        imageCard = {
            Surface(
                onClick = onClick,
                shape = ClickableSurfaceDefaults.shape(ShapeDefaults.Small),
                border = ClickableSurfaceDefaults.border(
                    focusedBorder = Border(
                        border = BorderStroke(
                            width = 4.dp,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        shape = ShapeDefaults.Small
                    )
                ),
                glow = ClickableSurfaceDefaults.glow(
                    focusedGlow = Glow(
                        elevation = 24.dp,
                        elevationColor = MaterialTheme.colorScheme.primary
                    )
                ),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
                content = image
            )
        },
    )
}