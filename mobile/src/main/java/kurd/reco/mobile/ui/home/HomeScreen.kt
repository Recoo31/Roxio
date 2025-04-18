package kurd.reco.mobile.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.DetailScreenRootDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kurd.reco.core.Global
import kurd.reco.core.Global.errorModel
import kurd.reco.core.SettingsDataStore
import kurd.reco.core.api.Resource
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.HomeScreenModel
import kurd.reco.core.data.db.favorite.FavoriteDao
import kurd.reco.core.data.db.watched.WatchedItemDao
import kurd.reco.core.plugin.PluginManager
import kurd.reco.core.viewmodels.HomeVM
import kurd.reco.mobile.R
import kurd.reco.mobile.common.CategorySheet
import kurd.reco.mobile.common.ErrorShower
import kurd.reco.mobile.common.FavoriteDialog
import kurd.reco.mobile.common.MovieCard
import kurd.reco.mobile.common.ShimmerMovieCard
import kurd.reco.mobile.common.VideoPlaybackHandler
import kurd.reco.core.data.ErrorModel
import kurd.reco.mobile.common.MultiSourceDialog
import org.koin.compose.koinInject

@Destination<RootGraph>
@Composable
fun HomeScreenRoot(
    viewModel: HomeVM = koinInject(),
    pluginManager: PluginManager = koinInject(),
    navigator: DestinationsNavigator
) {
    val movieList by viewModel.moviesList.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val resource = movieList) {
            is Resource.Loading -> {
                ShimmerHomeScreen()
            }

            is Resource.Success -> {
                val list = resource.value.toImmutableList()
                HomeScreen(list, viewModel, navigator, pluginManager)
            }

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
                    viewModel.loadMovies()
                },
                onDismiss = { errorModel = errorModel.copy(isError = false) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    movieList: ImmutableList<HomeScreenModel>,
    viewModel: HomeVM,
    navigator: DestinationsNavigator,
    pluginManager: PluginManager = koinInject(),
    settingsDataStore: SettingsDataStore = koinInject(),
    favoriteDao: FavoriteDao = koinInject(),
    watchedItemDao: WatchedItemDao = koinInject()
) {
    val context = LocalContext.current
    val clickedItem by viewModel.clickedItem.state.collectAsState()
    val showTitleEnabled by settingsDataStore.showTitleEnabled.collectAsState(true)
    val externalPlayer by settingsDataStore.externalPlayer.collectAsState("")
    val watchedItems by watchedItemDao.getAllWatchedItems().collectAsState(emptyList())

    var isClicked by remember { mutableStateOf(false) }
    val isThereSeeMore = remember { viewModel.isThereSeeMore() }
    val lazyListState = rememberLazyListState()
    var showCategorySheet by remember { mutableStateOf(false) }
    var categoryTitle by remember { mutableStateOf("") }
    val fetchForPlayer = Global.fetchForPlayer
    var showMultiSelect by remember { mutableStateOf(false) }
    var showFavoriteDialog by remember { mutableStateOf(false) }
    var currentFavoriteItem by remember { mutableStateOf<HomeItemModel?>(null) }

    if (errorModel.isError) {
        ErrorShower(
            errorText = errorModel.errorText,
            onRetry = {
                errorModel = errorModel.copy(isError = false)
                Global.clickedItem?.let { viewModel.getUrl(id = it.id, title = it.title) }
                isClicked = !isClicked
            },
            onDismiss = {
                errorModel = errorModel.copy(isError = false)
            }
        )
    }

    if (showMultiSelect) {
        Dialog(
            onDismissRequest = {
                showMultiSelect = false
                viewModel.clearClickedItem()
            }
        ) {
            MultiSourceDialog(Global.playDataModel, context) {
                Global.playDataModel = it
                showMultiSelect = false
                viewModel.clearClickedItem()
            }
        }
    }

    LazyColumn(state = lazyListState) {
        item {
            viewModel.getPagerList()?.let { list ->
                ViewPager(list) {
                    navigator.navigate(
                        DetailScreenRootDestination(it.id.toString(), it.isSeries)
                    )
                }
            }
        }

        item {
            if (watchedItems.isNotEmpty()) {
                val items = watchedItems
                    .filter { it.pluginId == Global.currentPlugin?.id }
                    .reversed()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.continue_watching),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.54f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    IconButton(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(36.dp),
                        onClick = {
                            viewModel.setViewAll(items.map { it.toHomeItemModel() })
                            categoryTitle = context.getString(R.string.continue_watching)
                            showCategorySheet = !showCategorySheet
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            modifier = Modifier.size(36.dp),
                            contentDescription = "See More",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                LazyRow {
                    items(items) { item ->
                        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            val width = if (item.isSeries) 280.dp else 150.dp
                            val itemModifier = if (item.isSeries) {
                                Modifier.sizeIn(minHeight = 90.dp, maxWidth = width)
                            } else {
                                Modifier.sizeIn(maxHeight = 200.dp, maxWidth = width, minHeight = 90.dp)
                            }
                            Box(
                                modifier = itemModifier
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .combinedClickable(
                                        onClick = {
                                            Global.clickedItem = item.toHomeItemModel()
                                            viewModel.getUrl(item.id, item.title)
                                        },
                                        onLongClick = {
                                            watchedItemDao.deleteWatchedItemById(item.id)
                                        }
                                    )
                            ) {
                                AsyncImage(
                                    model = item.poster,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = itemModifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(18.dp))
                                )

                                val progress = if (item.totalDuration > 0) {
                                    item.resumePosition.toFloat() / item.totalDuration
                                } else {
                                    0f
                                }

//                                if (progress > 0.96f && !item.isSeries) {
//                                    watchedItemDao.deleteWatchedItemById(item.id)
//                                }

                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(50))
                                        .height(4.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }

                            Text(
                                text = item.title!!,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .widthIn(max = width),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
            LaunchedEffect(watchedItems) {
                lazyListState.animateScrollToItem(0)
            }
        }

        items(movieList, key = { it.title }) { movie ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.54f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(36.dp),
                    onClick = {
                        if (isThereSeeMore && movie.id != null) {
                            viewModel.getViewAll(movie.id!!)
                        } else {
                            viewModel.setViewAll(movie.contents)
                        }
                        categoryTitle = movie.title
                        showCategorySheet = !showCategorySheet
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        modifier = Modifier.size(36.dp),
                        contentDescription = "See More",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            LazyRow {
                items(movie.contents) { homeItem ->
                    MovieCard(
                        item = homeItem,
                        showTitle = showTitleEnabled,
                        onItemClick = {
                            isClicked = !isClicked

                            if (homeItem.isLiveTv) {
                                viewModel.getUrl(homeItem.id, homeItem.title)
                                Global.clickedItemRow = movie
                                Global.clickedItem = homeItem
                            } else {
                                navigator.navigate(
                                    DetailScreenRootDestination(
                                        homeItem.id.toString(),
                                        homeItem.isSeries
                                    )
                                )
                            }
                        },
                        onLongPress = {
                            showFavoriteDialog = true
                            currentFavoriteItem = homeItem
                        }
                    )
                }
            }

            if (showFavoriteDialog && currentFavoriteItem != null) {
                FavoriteDialog(
                    item = currentFavoriteItem!!,
                    favoriteDao = favoriteDao,
                    pluginManager = pluginManager,
                    onDismiss = { showFavoriteDialog = false }
                )
            }

        }
    }

    if (showCategorySheet) {
        CategorySheet(
            title = categoryTitle,
            navigator = navigator,
            viewModel,
            favoriteDao,
            pluginManager
        ) {
            showCategorySheet = !showCategorySheet
        }
    }

    VideoPlaybackHandler(
        clickedItem = clickedItem,
        isClicked = isClicked,
        externalPlayer = externalPlayer,
        clearClickedItem = { viewModel.clearClickedItem() },
        onDone = { isClicked = false },
    )

    if (fetchForPlayer) {
        isClicked = true
        val homeItem = Global.clickedItem
        homeItem?.let {
            viewModel.getUrl(it.id, it.title)
        }
        Global.fetchForPlayer = false
    }
}

@Composable
fun ShimmerHomeScreen() {
    LazyColumn {
        items(3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(24.dp)
                        .background(Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp))
                )

                // "See More"
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.2f)
                        .height(24.dp)
                        .background(Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp))
                )
            }

            LazyRow(
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                items(5) {
                    ShimmerMovieCard()
                }
            }
        }
    }
}
