package kurd.reco.roxio.ui.detail

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.ShapeDefaults
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kurd.reco.core.Global
import kurd.reco.core.api.Resource
import kurd.reco.core.api.model.DetailScreenModel
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.HomeScreenModel
import kurd.reco.core.api.model.SeriesItem
import kurd.reco.core.viewmodels.DetailVM
import kurd.reco.roxio.PlayerActivity
import kurd.reco.roxio.R
import kurd.reco.roxio.common.CircularProgressIndicator
import kurd.reco.roxio.common.Error
import kurd.reco.roxio.common.VideoPlaybackHandler
import org.koin.androidx.compose.koinViewModel

@Destination<RootGraph>
@Composable
fun DetailScreenRoot(
    id: String,
    isSeries: Boolean,
    navigator: DestinationsNavigator,
    viewModel: DetailVM = koinViewModel()
) {
    val item by viewModel.item.state.collectAsState()

    LaunchedEffect(id) {
        viewModel.getMovie(id, isSeries)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val resource = item) {
            is Resource.Loading -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            is Resource.Success -> DetailScreen(id, resource.value, viewModel, navigator)
            is Resource.Failure -> {}
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(
    homeID: String,
    item: DetailScreenModel,
    viewModel: DetailVM,
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    var selectedEpisode by remember { mutableStateOf<SeriesItem?>(null) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var selectedSeason by rememberSaveable { mutableIntStateOf(0) }
    val focus = remember { FocusRequester() }
    var isLoading by remember { mutableStateOf(false) }
    var showMultiSelect by remember { mutableStateOf(false) }

    val clickedItem by viewModel.clickedItem.state.collectAsStateWithLifecycle()
    val seasons = viewModel.seriesList
    val fetchForPlayer = Global.fetchForPlayer
    val isSeries = item.isSeries

    LaunchedEffect(Unit) {
        focus.requestFocus()
    }

    if (showMultiSelect) {
        Dialog(onDismissRequest = { showMultiSelect = false }) {
            MultiSourceDialog(Global.playDataModel, context) { Global.playDataModel = it }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val boxHeight = maxHeight
        val paddingTitle = if (isSeries) 0.15f else 0.30f

        Box(modifier = Modifier.fillMaxSize().bringIntoViewRequester(bringIntoViewRequester)) {
            item.backImage?.let {
                MovieImageWithGradients(posterUri = it, modifier = Modifier.fillMaxSize())
            }

            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .padding(top = boxHeight * paddingTitle, start = 16.dp)
                ) {
                    MovieLargeTitle(movieTitle = item.title)

                    item.description?.let {
                        MovieDescription(
                            modifier = Modifier.alpha(0.75f).padding(top = 8.dp),
                            description = it
                        )
                    }
                }

                if (isSeries && seasons != null) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(top = boxHeight * 0.05f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            SeasonsSelector(seasons, selectedSeason = selectedSeason) {
                                selectedSeason = it
                            }
                        }

                        item {
                            val episodes = seasons[selectedSeason].episodes
                            EpisodeRow(
                                episodes = episodes,
                                onSelected = { episode ->
                                    isLoading = true
                                    viewModel.getUrl(episode.id, episode.title)

                                    val contents = episodes.map {
                                        HomeItemModel(
                                            id = it.id,
                                            title = it.title,
                                            poster = it.poster,
                                            isSeries = true,
                                            isLiveTv = false
                                        )
                                    }
                                    Global.clickedItem = HomeItemModel(
                                        id = episode.id,
                                        title = episode.title,
                                        poster = episode.poster,
                                        isSeries = true,
                                        isLiveTv = false
                                    )

                                    selectedEpisode = episode
                                    Global.clickedItemRow = HomeScreenModel("Episodes", contents)
                                },
                                focusRequester = focus,
                            )
                        }
                    }
                } else {
                    WatchTrailerButton(
                        modifier = Modifier
                            .padding(16.dp)
                            .focusRequester(focus),
                        goToMoviePlayer = {
                            Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show()
                            viewModel.getUrl(item.id, item.title)
                            isLoading = true
                        }
                    )
                }
            }
        }

        VideoPlaybackHandler(
            clickedItem = clickedItem,
            isClicked = isLoading,
            clearClickedItem = { viewModel.clearClickedItem() },
            onSuccess = { isLoading = false },
        )
    }

    if (fetchForPlayer) {
        isLoading = true
        val selectedItem = Global.clickedItem
        selectedItem?.let {
            viewModel.getUrl(it.id, it.title)
            selectedEpisode = SeriesItem(
                id = it.id,
                title = it.title ?: "",
                poster = it.poster,
                description = null
            )
        }
        Global.fetchForPlayer = false
    }
}


@Composable
private fun MovieImageWithGradients(
    modifier: Modifier = Modifier,
    posterUri: String,
    gradientColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
) {
    Box(modifier) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(posterUri)
                .crossfade(true).build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    drawRect(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, gradientColor),
                            startY = 600f
                        )
                    )
                    drawRect(
                        Brush.horizontalGradient(
                            colors = listOf(gradientColor, Color.Transparent),
                            endX = 1200f,
                            startX = 600f
                        )
                    )
                    drawRect(
                        Brush.linearGradient(
                            colors = listOf(gradientColor, Color.Transparent),
                            start = Offset(x = 500f, y = 600f),
                            end = Offset(x = 1000f, y = 0f)
                        )
                    )
                }
        )
    }

}

@Composable
private fun MovieLargeTitle(movieTitle: String) {
    Text(
        text = movieTitle,
        style = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.Bold
        ),
        maxLines = 1,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun MovieDescription(modifier: Modifier = Modifier, description: String) {
    Text(
        text = description,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier.padding(top = 8.dp),
        maxLines = 4,
    )
}

@Composable
private fun WatchTrailerButton(
    modifier: Modifier = Modifier,
    goToMoviePlayer: () -> Unit
) {
    Button(
        onClick = goToMoviePlayer,
        modifier = modifier.padding(top = 16.dp),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        shape = ButtonDefaults.shape(shape = ShapeDefaults.ExtraSmall)
    ) {
        Icon(
            imageVector = Icons.Outlined.PlayArrow,
            contentDescription = null
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = stringResource(R.string.watch_now),
            style = MaterialTheme.typography.titleSmall
        )
    }
}