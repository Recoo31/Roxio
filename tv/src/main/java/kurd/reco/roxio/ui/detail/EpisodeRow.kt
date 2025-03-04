package kurd.reco.roxio.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import kurd.reco.core.api.model.SeriesItem
import kurd.reco.roxio.common.MovieCard
import kurd.reco.roxio.common.PosterImage


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EpisodeRow(
    modifier: Modifier = Modifier,
    episodes: List<SeriesItem>,
    onSelected: (SeriesItem) -> Unit,
    focusRequester: FocusRequester = remember { FocusRequester() },
    lazyListState: LazyListState = remember(episodes) { LazyListState() }
) {
    val firstItem = remember { FocusRequester() }
    var previousEpisodeListHash by remember { mutableIntStateOf(episodes.hashCode()) }
    val isSameList = previousEpisodeListHash == episodes.hashCode()

    LazyRow(
        state = lazyListState,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .focusProperties {
                enter = {
                    when {
                        lazyListState.layoutInfo.visibleItemsInfo.isEmpty() -> FocusRequester.Cancel
                        isSameList && focusRequester.restoreFocusedChild() -> FocusRequester.Cancel
                        else -> firstItem
                    }
                }
                exit = {
                    previousEpisodeListHash = episodes.hashCode()
                    focusRequester.saveFocusedChild()
                    FocusRequester.Default
                }
            }
            .then(modifier)
    ) {

        itemsIndexed(episodes) { index, item ->
            val cardModifier = if (index == 0) {
                Modifier.focusRequester(firstItem)
            } else {
                Modifier
            }

            EpisodeCard(
                playerEpisode = item,
                onClick = { onSelected(item) },
                modifier = cardModifier
            )
        }
    }
}


@Composable
internal fun EpisodeCard(
    playerEpisode: SeriesItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    MovieCard(
        image = {
            PosterImage(playerEpisode.poster, modifier = Modifier.widthIn(min = 200.dp, max = 300.dp).heightIn(max = 180.dp))
        },
        title = {
            if (isFocused){
                Text(
                    text = playerEpisode.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 4.dp)
                        .widthIn(max = 300.dp)
                )

            }
        },
        modifier = modifier.focusRequester(focusRequester).onFocusChanged { isFocused = it.isFocused },
        onClick = onClick
    )
}