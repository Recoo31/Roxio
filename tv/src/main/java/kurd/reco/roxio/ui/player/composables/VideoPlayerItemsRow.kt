package kurd.reco.roxio.ui.player.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.CompactCard
import androidx.tv.material3.Glow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.HomeScreenModel
import kurd.reco.roxio.common.VideoPlayerState
import kurd.reco.roxio.defaultBorder

@Composable
fun VideoPlayerItemsRow(
    modifier: Modifier = Modifier,
    items: HomeScreenModel,
    focusRequester: FocusRequester,
    state: VideoPlayerState,
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

        focusRequester.requestFocus()
    }

    LaunchedEffect(true) {
        launch {
            while (true) {
                state.showControls()
                delay(3000)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        LazyRow(
            state = lazyListState,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            items(itemsContents) { item ->
                val isSelected = item.id == selectedItem?.id
                val isFocused by remember { mutableStateOf(false) }

                if (item.isSeries) {
                    CompactCard(
                        modifier = Modifier
                            .sizeIn(maxHeight = 160.dp, maxWidth = 300.dp)
                            .run { if (isSelected) focusRequester(focusRequester) else this },
                        onClick = {
                            onItemClick(item)
                        },
                        title = {
                            Text(
                                text = item.title ?: "",
                                style = MaterialTheme.typography.titleMedium.copy(color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(8.dp)
                            )
                        },
                        image = {
                            AsyncImage(
                                model = item.poster,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        },
                        glow = CardDefaults.glow(
                            focusedGlow = Glow(
                                elevation = 16.dp,
                                elevationColor = MaterialTheme.colorScheme.secondary
                            )
                        ),
                        border = CardDefaults.border(
                            focusedBorder = defaultBorder(),
                            border = Border(
                                border = BorderStroke(
                                    width = 4.dp,
                                    color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent
                                ),
                                shape = MaterialTheme.shapes.medium
                            )
                        ),
                        colors = CardDefaults.colors(
                            containerColor = Color.Transparent
                        )
                    )
                } else {
                    Card(
                        onClick = { onItemClick(item) },
                        glow = CardDefaults.glow(
                            focusedGlow = Glow(
                                elevation = 16.dp,
                                elevationColor = MaterialTheme.colorScheme.secondary
                            )
                        ),
                        border = CardDefaults.border(
                            focusedBorder = defaultBorder(shape = MaterialTheme.shapes.medium),
                        ),
                        colors = CardDefaults.colors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        AsyncImage(
                            model = item.poster,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .sizeIn(minWidth = 128.dp, minHeight = 72.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { onItemClick(item) }
                        )
                    }
                }
            }
        }
    }
}