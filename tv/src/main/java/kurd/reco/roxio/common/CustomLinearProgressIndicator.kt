package kurd.reco.roxio.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.SurfaceDefaults.shape
import kurd.reco.roxio.ui.theme.RoxioTheme

@Composable
fun CustomLinearProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = Color.Gray.copy(alpha = 0.3f),
    height: Dp = 4.dp,
    shape: Shape = RoundedCornerShape(percent = 50)
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(color)
        )
    }
}

@Preview
@Composable
private fun CustomLinearProgressIndicatorPreview() {
    RoxioTheme {
        CustomLinearProgressIndicator(
            progress = 0.5f, modifier = Modifier.height(4.dp).fillMaxWidth()
        )
    }
}
