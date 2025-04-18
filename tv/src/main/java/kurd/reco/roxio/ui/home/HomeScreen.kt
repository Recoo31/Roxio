package kurd.reco.roxio.ui.home

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.DetailScreenRootDestination
import com.ramcosta.composedestinations.generated.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import kurd.reco.core.Global
import kurd.reco.core.api.Resource
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.HomeScreenModel
import kurd.reco.core.data.ItemDirection
import kurd.reco.core.data.db.watched.WatchedItemDao
import kurd.reco.core.viewmodels.HomeVM
import kurd.reco.roxio.PlayerActivity
import kurd.reco.roxio.R
import kurd.reco.roxio.common.CircularProgressIndicator
import kurd.reco.roxio.common.CustomLinearProgressIndicator
import kurd.reco.roxio.common.Error
import kurd.reco.roxio.common.FavoriteDialog
import kurd.reco.roxio.common.MoviesRow
import kurd.reco.roxio.common.VideoPlaybackHandler
import kurd.reco.roxio.defaultBorder
import kurd.reco.roxio.ui.detail.MultiSourceDialog
import org.koin.compose.koinInject
import kotlin.system.exitProcess

@Destination<RootGraph>
@Composable
fun HomeScreen(navigator: DestinationsNavigator) {
    val viewModel: HomeVM = koinInject()
    val movieList by viewModel.moviesList.state.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val resource = movieList) {
            is Resource.Loading -> {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                println("Loading state")
            }

            is Resource.Success -> {
                HandleHome(resource.value, viewModel, navigator)
                println("resource success")
            }

            is Resource.Failure -> {
                Error(modifier = Modifier.fillMaxSize(), resource.error)
                LaunchedEffect(resource) {
                    delay(1000)
                    viewModel.loadMovies()
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HandleHome(
    itemList: List<HomeScreenModel>,
    viewModel: HomeVM,
    navigator: DestinationsNavigator,
    watchedItemDao: WatchedItemDao = koinInject()
) {
    val context = LocalContext.current
    val lazyListState = rememberLazyListState()

    var isClicked by remember { mutableStateOf(false) }
    var showFavoriteDialog by remember { mutableStateOf(false) }
    val clickedItem by viewModel.clickedItem.state.collectAsStateWithLifecycle()
    val watchedItems by watchedItemDao.getAllWatchedItems().collectAsState(emptyList())
    val fetchForPlayer = Global.fetchForPlayer
    var currentFavoriteItem by remember { mutableStateOf<HomeItemModel?>(null) }
    var showMultiSelect by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }


    if (showSettings) {
        navigator.navigate(SettingsScreenDestination)
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

    BackHandler {
        showExitDialog = !showExitDialog
    }

    if (showExitDialog) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Dialog(
                onDismissRequest = { showExitDialog = false }
            ) {
                Surface(
                    modifier = Modifier.size(width = 400.dp, height = 200.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.exit_app_dialog),
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(16.dp)
                        )
                        Row(modifier = Modifier.padding(16.dp)) {
                            Button(
                                onClick = { showExitDialog = false },
                                modifier = Modifier.padding(end = 16.dp)
                            ) {
                                Text(stringResource(R.string.cancel))
                            }
                            Button(onClick = {
                                (context as Activity).finish()
                                exitProcess(0)
                            }) {
                                Text(stringResource(R.string.exit))
                            }
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
    ) {
//        item {
//            viewModel.getCategories()?.let { list ->
//                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
//                    TopCategory(list) {
//                        viewModel.getCategoryItems(it)
//                        //navigator.navigate(CategoryScreenDestination(category = it))
//                    }
//                }
//            }
//        }

//        item {
//            viewModel.getPagerList()?.let { list ->
//                ViewPager(
//                    items = list,
//                    onFocus = {
//                        scope.launch {
//                            lazyListState.scrollToItem(0)
//                        }
//                    },
//                    onItemClicked = {
//                        navigator.navigate(
//                            DetailScreenRootDestination(it.id.toString(), it.isSeries)
//                        )
//                    }
//                )
//            }
//        }

        item {
            if (watchedItems.isNotEmpty()) {
                val items = watchedItems
                    .filter { it.pluginId == Global.currentPlugin?.id }

                LazyRow {
                    items(items) { item ->
                        val configuration = LocalConfiguration.current
                        val screenWidth = configuration.screenWidthDp.dp

                        val imageWidth = screenWidth / 6
                        val imageHeight = imageWidth * if (item.isSeries) 1f else 1.5f

                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                onClick = {
                                    isClicked = !isClicked
                                    Global.clickedItem = item.toHomeItemModel()
                                    viewModel.getUrl(item.id, item.title)
                                },
                                onLongClick = {
                                    watchedItemDao.deleteWatchedItemById(item.id)
                                },
                                border = ClickableSurfaceDefaults.border(focusedBorder = defaultBorder()),
                                scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .sizeIn(
                                            minHeight = imageHeight * 0.75f,
                                            maxHeight = imageHeight
                                        )
                                        .aspectRatio(if (item.isSeries) ItemDirection.Horizontal.aspectRatio else ItemDirection.Vertical.aspectRatio)
                                ) {
                                    AsyncImage(
                                        model = item.poster,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    val progress = if (item.totalDuration > 0) {
                                        item.resumePosition.toFloat() / item.totalDuration
                                    } else {
                                        0f
                                    }

                                    if (progress > 0.96f && !item.isSeries) {
                                        watchedItemDao.deleteWatchedItemById(item.id)
                                    }

                                    CustomLinearProgressIndicator(
                                        progress = progress,
                                        modifier = Modifier
                                            .height(4.dp)
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                    )

                                }
                            }
                            Text(
                                text = item.title!!,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .widthIn(max = imageWidth),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                LaunchedEffect(watchedItems) {
                    lazyListState.animateScrollToItem(0)
                }
            }
        }

        items(itemList) { movie ->
            MoviesRow(
                title = movie.title,
                movieList = movie.contents,
                onMovieSelected = { homeItem ->
                    isClicked = !isClicked
                    Global.clickedItem = homeItem

                    if (homeItem.isLiveTv) {
                        viewModel.getUrl(homeItem.id, homeItem.title)
                        Global.clickedItemRow = movie
                    } else {
                        navigator.navigate(
                            DetailScreenRootDestination(
                                homeItem.id.toString(),
                                homeItem.isSeries
                            )
                        )
                    }
                },
                onLongClick = {
                    currentFavoriteItem = it
                    showFavoriteDialog = true
                }
            )
        }
    }

    if (showFavoriteDialog && currentFavoriteItem != null) {
        FavoriteDialog(
            item = currentFavoriteItem!!,
            onDismiss = { showFavoriteDialog = false }
        )
    }

    VideoPlaybackHandler(
        clickedItem = clickedItem,
        isClicked = isClicked,
        clearClickedItem = { viewModel.clearClickedItem() },
        onSuccess = { isClicked = false },
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
