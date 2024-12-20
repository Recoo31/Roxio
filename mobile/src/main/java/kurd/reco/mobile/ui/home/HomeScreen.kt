package kurd.reco.mobile.ui.home

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.DetailScreenRootDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kurd.reco.core.MainVM
import kurd.reco.core.SettingsDataStore
import kurd.reco.core.api.Resource
import kurd.reco.core.api.model.HomeScreenModel
import kurd.reco.core.plugin.PluginManager
import kurd.reco.mobile.PlayerActivity
import kurd.reco.mobile.R
import kurd.reco.mobile.common.CategorySheet
import kurd.reco.mobile.common.ErrorShower
import kurd.reco.mobile.common.MovieCard
import kurd.reco.mobile.common.MovieCategorySelector
import kurd.reco.mobile.common.ShimmerMovieCard
import kurd.reco.mobile.ui.player.openVideoWithSelectedPlayer
import org.koin.compose.koinInject

@Destination<RootGraph>
@Composable
fun HomeScreenRoot(
    viewModel: HomeVM = koinInject(),
    pluginManager: PluginManager = koinInject(),
    navigator: DestinationsNavigator
) {
    var isError by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }
    val movieList by viewModel.moviesList.state.collectAsState()
    val lastPlugin by pluginManager.getSelectedPluginFlow().collectAsState()
    var currentPlugin by viewModel.selectedPlugin

    LaunchedEffect(Unit) {
        currentPlugin = lastPlugin
    }

    LaunchedEffect(lastPlugin) {
        if (currentPlugin != lastPlugin) {
            viewModel.resetMovies()
            viewModel.loadMovies()
            viewModel.resetCategory()
            currentPlugin = lastPlugin
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val resource = movieList) {
            is Resource.Loading -> {
                ShimmerHomeScreen()
            }

            is Resource.Success -> {
                val list = resource.value.toImmutableList()
                HomeScreen(list, viewModel, navigator)
            }

            is Resource.Failure -> {
                LaunchedEffect(resource) {
                    isError = true
                    errorText = resource.error
                }
            }
        }

        if (isError) {
            ErrorShower(
                errorText = errorText,
                onRetry = {
                    isError = false
                    viewModel.loadMovies()
                },
                onDismiss = { isError = false }
            )
        }
    }
}

@Composable
fun HomeScreen(
    movieList: ImmutableList<HomeScreenModel>,
    viewModel: HomeVM,
    navigator: DestinationsNavigator,
    mainVM: MainVM = koinInject(),
    settingsDataStore: SettingsDataStore = koinInject()
) {
    val context = LocalContext.current
    val clickedItem by viewModel.clickedItem.state.collectAsState()
    val showTitleEnabled by settingsDataStore.showTitleEnabled.collectAsState(false)
    val externalPlayer by settingsDataStore.externalPlayer.collectAsState("")

    var isError by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }
    var isClicked by remember { mutableStateOf(false) }
    val isThereSeeMore = remember { viewModel.isThereSeeMore() }
    val lazyListState = rememberLazyListState()
    var showCategorySheet by remember { mutableStateOf(false) }
    var categoryTitle by remember { mutableStateOf("") }
    var fetchForPlayer = mainVM.fetchForPlayer

    if (isError) {
        ErrorShower(
            errorText = errorText,
            onRetry = {
                isError = false
            },
            onDismiss = { isError = false }
        )
    }

    LazyColumn(state = lazyListState) {
        item {
            viewModel.getCategories()?.let { list ->
                MovieCategorySelector(
                    modifier = Modifier.fillMaxWidth(),
                    categories = list
                ) {
                    viewModel.getCategoryItems(it)
                    categoryTitle = it
                    showCategorySheet = !showCategorySheet
                }
            }
        }

        item {
            viewModel.getPagerList()?.let { list ->
                ViewPager(list) {
                    navigator.navigate(
                        DetailScreenRootDestination(it.id.toString(), it.isSeries)
                    )
                }
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
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.54f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(24.dp),
                    onClick = {
                        if (!isThereSeeMore) {
                            viewModel.setViewAll(movie.contents)
                            categoryTitle = movie.title
                            showCategorySheet = !showCategorySheet
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_right),
                        contentDescription = "See More",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            LazyRow {
                items(movie.contents) { homeItem ->
                    MovieCard(item = homeItem, showTitle = showTitleEnabled) {
                        isClicked = !isClicked
                        mainVM.clickedItem = homeItem

                        if (homeItem.isLiveTv) {
                            viewModel.getUrl(homeItem.id, homeItem.title)
                            mainVM.clickedItemRow = movie
                        } else {
                            navigator.navigate(
                                DetailScreenRootDestination(
                                    homeItem.id.toString(),
                                    homeItem.isSeries
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCategorySheet) {
        CategorySheet(title = categoryTitle, navigator = navigator) {
            showCategorySheet = !showCategorySheet
        }
    }

    when (val resource = clickedItem) {
        is Resource.Success -> {
            isClicked = false

            val playData = resource.value

            if (externalPlayer.isNotEmpty() && playData.drm == null) {
                openVideoWithSelectedPlayer(
                    context = context,
                    videoUri = playData.urls[0].second,
                    playerPackageName = externalPlayer
                )
            } else {
                mainVM.playDataModel = playData
                val intent = Intent(context, PlayerActivity::class.java)
                context.startActivity(intent)
                viewModel.clearClickedItem()
            }
        }

        is Resource.Failure -> {
            LaunchedEffect(resource) {
                isError = true
                errorText = resource.error
            }
        }

        is Resource.Loading -> {
            if (isClicked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (fetchForPlayer) {
        isClicked = true
        val homeItem = mainVM.clickedItem
        homeItem?.let {
            viewModel.getUrl(it.id, it.title)
        }
        mainVM.fetchForPlayer = false
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
