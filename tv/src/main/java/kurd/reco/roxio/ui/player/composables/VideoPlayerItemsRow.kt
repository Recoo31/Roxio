package kurd.reco.roxio.ui.player.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.CompactCard
import androidx.tv.material3.Glow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.ShapeDefaults
import androidx.tv.material3.StandardCardContainer
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.HomeScreenModel
import kurd.reco.roxio.common.VideoPlayerState

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
    val selected = itemsContents.find { it == selectedItem }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()

        selected?.let {
            val index = itemsContents.indexOf(selected)
            lazyListState.scrollToItem(index)
        }
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
            .padding(8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        LazyRow(
            state = lazyListState,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(itemsContents) { item ->
                val isSelected = item == selectedItem
                var isFocused by remember { mutableStateOf(false) }

                if (item.isSeries) {
                    CompactCard(
                        modifier = Modifier
                            .heightIn(max = 160.dp),
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
                            focusedBorder = Border(
                                border = BorderStroke(
                                    width = 4.dp,
                                    color = MaterialTheme.colorScheme.secondary
                                ),
                                shape = MaterialTheme.shapes.medium
                            ),
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
                            focusedBorder = Border(
                                border = BorderStroke(
                                    width = 4.dp,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                shape = MaterialTheme.shapes.medium
                            ),
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