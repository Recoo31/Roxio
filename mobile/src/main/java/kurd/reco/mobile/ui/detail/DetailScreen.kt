package kurd.reco.mobile.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kurd.reco.core.Global
import kurd.reco.core.api.Resource
import kurd.reco.core.api.model.DetailScreenModel
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.HomeScreenModel
import kurd.reco.core.api.model.SeriesItem
import kurd.reco.core.data.db.favorite.Favorite
import kurd.reco.core.data.db.favorite.FavoriteDao
import kurd.reco.core.data.db.plugin.PluginDao
import kurd.reco.core.viewmodels.DetailVM
import kurd.reco.mobile.R
import kurd.reco.mobile.common.ErrorShower
import kurd.reco.mobile.common.VideoPlaybackHandler
import kurd.reco.core.data.ErrorModel
import kurd.reco.mobile.ui.detail.composables.BackImage
import kurd.reco.mobile.ui.detail.composables.CustomIconButton
import kurd.reco.mobile.ui.detail.composables.DescriptionSection
import kurd.reco.mobile.ui.detail.composables.MovieDetails
import kurd.reco.mobile.common.MultiSourceDialog
import kurd.reco.mobile.ui.detail.composables.SeasonItem
import kurd.reco.mobile.ui.detail.composables.SeasonsSelector
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject


private val TAG = "DetailScreenRoot"

@Destination<RootGraph>
@Composable
fun DetailScreenRoot(
    id: String,
    isSeries: Boolean,
    navigator: DestinationsNavigator,
    viewModel: DetailVM = koinViewModel()
) {
    val context = LocalContext.current
    val item by viewModel.item.state.collectAsState()
    var errorModel by remember { mutableStateOf(ErrorModel("", false)) }

    LaunchedEffect(id) {
        //AppLog.d(TAG, "LaunchedEffect: $id")
        viewModel.getMovie(id, isSeries)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val resource = item) {
            is Resource.Loading -> CircularProgressIndicator()
            is Resource.Success -> DetailScreen(id, resource.value, viewModel, navigator)
            is Resource.Failure -> {
                LaunchedEffect(resource) {
                    errorModel = ErrorModel(resource.error, true)
                }
            }
        }

        if (errorModel.isError) {
            ErrorShower(
                errorText = errorModel.errorText,
                onRetry = {
                    errorModel = errorModel.copy(isError = false)
                    viewModel.getMovie(id, isSeries)
                },
                onDismiss = { errorModel = errorModel.copy(isError = false) }
            )
        }
    }
}

@Composable
fun DetailScreen(
    homeID: String,
    item: DetailScreenModel,
    viewModel: DetailVM,
    navigator: DestinationsNavigator,
    pluginDao: PluginDao = koinInject(),
    favoriteDao: FavoriteDao = koinInject()
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isWideScreen = screenWidth > 600

    val clickedItem by viewModel.clickedItem.state.collectAsState()

    val fetchForPlayer = Global.fetchForPlayer

    var expanded by remember { mutableStateOf(false) }
    var selectedSeason by rememberSaveable { mutableIntStateOf(0) }
    var showMultiSelect by remember { mutableStateOf(false) }
    var selectedEpisode by remember { mutableStateOf<SeriesItem?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorModel by remember { mutableStateOf(ErrorModel("", false)) }

    val onPlayClick = {
        isLoading = true
        Global.clickedItem = HomeItemModel(
            id = item.id,
            title = item.title,
            poster = item.image,
            isSeries = false,
            isLiveTv = false
        )
        viewModel.getUrl(item.id, item.title)
    }

    val onEpisodeClick: (SeriesItem) -> Unit = { clicked ->
        isLoading = true
        viewModel.getUrl(clicked.id, clicked.title)

        val episodes = viewModel.seriesList?.get(selectedSeason)?.episodes ?: emptyList()
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
            id = clicked.id,
            title = clicked.title,
            poster = clicked.poster,
            isSeries = true,
            isLiveTv = false
        )
        Global.clickedItemRow = HomeScreenModel(context.getString(R.string.episodes), contents)
        selectedEpisode = clicked
    }

    if (errorModel.isError) {
        ErrorShower(
            errorText = errorModel.errorText,
            onRetry = {
                errorModel = errorModel.copy(isError = false)
            },
            onDismiss = { errorModel = errorModel.copy(isError = false) }
        )
    }

    if (showMultiSelect) {
        Dialog(onDismissRequest = { showMultiSelect = false }) {
            MultiSourceDialog(Global.playDataModel, context) { Global.playDataModel = it }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                item.backImage?.let { imageUrl ->
                    Box(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        contentAlignment = Alignment.TopStart
                    ) {
                        BackImage(imageUrl)

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                                            MaterialTheme.colorScheme.background
                                        ),
                                        startX = 0f,
                                        endX = Float.POSITIVE_INFINITY
                                    )
                                )
                        )
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item(span = { GridItemSpan(2) }) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Text(
                                text = item.title,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = true,
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )

                            item.description?.let { description ->
                                DescriptionSection(
                                    item = description,
                                    expanded = expanded,
                                    onExpandClick = { expanded = !expanded }
                                )
                            }

                            if (item.isSeries) {
                                viewModel.seriesList?.let { season ->
                                    SeasonsSelector(season, selectedSeason) { selectedSeason = it }
                                }
                            } else {
                                Button(
                                    onClick = onPlayClick,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.play_text),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }

                    if (item.isSeries) {
                        viewModel.seriesList?.let { season ->
                            val episodes = season[selectedSeason].episodes
                            items(episodes) { episode ->
                                SeasonItem(item = episode) { clicked ->
                                    onEpisodeClick(clicked)
                                    selectedEpisode = episode
                                }
                            }
                        }
                    }
                }

            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item.backImage?.let {
                    item {
                        BackImage(it)
                    }
                }

                item {
                    MovieDetails(item)
                }

                item.description?.let {
                    item {
                        DescriptionSection(
                            item = it,
                            expanded = expanded,
                            onExpandClick = { expanded = !expanded }
                        )
                    }
                }

                if (item.isSeries) {
                    viewModel.seriesList?.let { season ->
                        val episodes = season[selectedSeason].episodes

                        item {
                            SeasonsSelector(season, selectedSeason) {
                                selectedSeason = it
                            }
                        }

                        items(episodes) { episode ->
                            SeasonItem(item = episode) { clicked ->
                                onEpisodeClick(clicked)
                            }
                        }
                    }
                } else {
                    item {
                        Button(
                            onClick = onPlayClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.play_text),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

        }

        CustomIconButton(
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back"
                )
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) { navigator.navigateUp() }

        val favoriteIcon = favoriteDao.getFavoriteById(homeID) != null
        var isFavorite by remember { mutableStateOf(favoriteIcon) }

        CustomIconButton(
            icon = {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite"
                )
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            if (isFavorite) {
                favoriteDao.deleteFavoriteById(homeID)
            } else {
                favoriteDao.insertFavorite(
                    Favorite(
                        id = homeID,
                        title = item.title,
                        image = item.image,
                        isSeries = item.isSeries,
                        pluginID = pluginDao.getSelectedPlugin()!!.id,
                        isLiveTv = false
                    )
                )
            }
            isFavorite = !isFavorite
        }
    }

    VideoPlaybackHandler(
        clickedItem = clickedItem,
        isClicked = isLoading,
        clearClickedItem = {
            viewModel.clearClickedItem()
            isLoading = false
        },
        onDone = { isLoading = false },
        customTitle = if (item.isSeries) "${item.title} | ${selectedEpisode?.title}" else item.title
    )

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