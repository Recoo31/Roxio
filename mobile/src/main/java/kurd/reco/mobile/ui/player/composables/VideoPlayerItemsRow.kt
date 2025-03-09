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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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

    LaunchedEffect(selectedItem) {
        selectedItem?.id?.let { selectedItemId ->
            val selectedIndex = itemsContents.indexOfFirst { it.id == selectedItemId }

            if (selectedIndex != -1) {
                val middleItemOffset = (lazyListState.layoutInfo.viewportEndOffset - lazyListState.layoutInfo.viewportStartOffset) / 3
                lazyListState.scrollToItem(
                    index = selectedIndex,
                    scrollOffset = -middleItemOffset
                )
            }
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
            items(itemsContents, key = {item -> item.id}) { item ->
                val isSelected = item.id == selectedItem?.id

                ImageOverlayCard(
                    modifier = Modifier
                        .run {
                            if (item.isSeries) {
                                sizeIn(maxHeight = 110.dp, maxWidth = 200.dp)
                            } else {
                                heightIn(min = 90.dp, max = 110.dp)
                            }
                        }
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { onItemClick(item) }
                        .run {
                            if (isSelected) border(4.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium) else this
                        },
                    title = if (item.isSeries) item.title else null,
                    imageUrl = item.poster,
                    bgColor = Color.Black
                )
            }
        }
    }
}