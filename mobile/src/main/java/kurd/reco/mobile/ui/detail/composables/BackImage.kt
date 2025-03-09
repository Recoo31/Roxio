package kurd.reco.mobile.ui.detail.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.valentinilk.shimmer.shimmer

@Composable
fun BackImage(item: String, modifier: Modifier = Modifier) {
    var isLoading by remember { mutableStateOf(false) }


    Box(modifier = modifier) {
        AsyncImage(
            model = item,
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            onLoading = {
                isLoading = true
            },
            onSuccess = {
                isLoading = false
            },
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp).shimmer().background(Color.Gray.copy(alpha = 0.6f), shape = RoundedCornerShape(4.dp))){}
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.background
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )
    }
}