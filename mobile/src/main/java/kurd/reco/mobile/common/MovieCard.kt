package kurd.reco.mobile.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.valentinilk.shimmer.shimmer
import kurd.reco.core.api.model.HomeItemModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MovieCard(
    item: HomeItemModel,
    showTitle: Boolean = true,
    onLongPress: () -> Unit = {},
    onItemClick: () -> Unit
) {
    val itemModifier = if (item.isLiveTv) {
        Modifier.sizeIn(minHeight = 90.dp, maxWidth = 200.dp)
    } else {
        Modifier.sizeIn(maxHeight = 200.dp, maxWidth = 150.dp, minHeight = 90.dp)
    }

    Column(
        modifier = Modifier
            .padding(8.dp)
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = onLongPress
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = item.poster,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = itemModifier
                .fillMaxSize()
                .clip(RoundedCornerShape(18.dp))
        )

        if (!item.title.isNullOrEmpty() && showTitle) {
            Text(
                text = item.title!!,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .widthIn(max = 165.dp),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
        }
    }
}


@Composable
fun ShimmerMovieCard() {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .widthIn(130.dp, 165.dp)
            .heightIn(180.dp, 215.dp)
            .shimmer()
            .clip(RoundedCornerShape(18.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Poster yer tutucu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color.Gray.copy(alpha = 0.5f), shape = RoundedCornerShape(18.dp))
        )

        // Başlık yer tutucu
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(0.7f)
                .height(16.dp)
                .background(Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp))
        )
    }
}




//@Preview(
//    showBackground = true,
//    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
//)
//@Composable
//private fun MovieCardPreview() {
//    RoxioTheme(
//        dynamicColor = false,
//    ) {
//        MovieCard(
//            item = HomeItemModel("adasda", "", false, false)
//        )
//    }
//}