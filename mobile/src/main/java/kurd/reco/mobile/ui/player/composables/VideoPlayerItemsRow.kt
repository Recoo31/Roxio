package kurd.reco.mobile.ui.player.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.HomeScreenModel
import kurd.reco.mobile.common.ImageOverlayCard

@Composable
fun VideoPlayerItemsRow(
    modifier: Modifier = Modifier,
    items: HomeScreenModel,
    selectedItem: HomeItemModel? = null,
    onItemClick: (HomeItemModel) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val itemsContents = items.contents
    val selected = itemsContents.find { it == selectedItem }

    LaunchedEffect(Unit) {
        selected?.let {
            lazyListState.scrollToItem(itemsContents.indexOf(selected))
        }
    }

    Box(
        modifier = modifier
            .padding(8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        LazyRow(
            state = lazyListState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(itemsContents) { item ->
                val isSelected = item == selectedItem

                if (item.isSeries) {
                    ImageOverlayCard(
                        modifier = Modifier
                            .sizeIn(maxHeight = 110.dp, maxWidth = 200.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onItemClick(item) }
                            .run {
                                if (isSelected) border(4.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium) else this
                            },
                        title = item.title ?: "",
                        imageUrl = item.poster
                    )
                } else {
                    AsyncImage(
                        model = item.poster,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .heightIn(min = 90.dp, max = 110.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onItemClick(item) }
                            .run {
                                if (isSelected) border(4.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium) else this
                            }
                    )
                }
            }
        }
    }
}