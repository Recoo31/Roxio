package kurd.reco.roxio.ui.home

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.DetailScreenRootDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import kurd.reco.core.Global.pluginLoaded
import kurd.reco.core.MainVM
import kurd.reco.core.api.Resource
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.HomeScreenModel
import kurd.reco.core.plugin.PluginManager
import kurd.reco.roxio.PlayerActivity
import kurd.reco.roxio.common.CircularProgressIndicator
import kurd.reco.roxio.common.Error
import kurd.reco.roxio.common.FavoriteDialog
import kurd.reco.roxio.common.MoviesRow
import kurd.reco.roxio.common.TvAlertDialog
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


@Composable
fun HandleHome(
    itemList: List<HomeScreenModel>,
    viewModel: HomeVM,
    navigator: DestinationsNavigator,
    mainVM: MainVM = koinInject()
) {
    val context = LocalContext.current
    val lazyListState = rememberLazyListState()

    var isClicked by remember { mutableStateOf(false) }
    var loadedItemCount by rememberSaveable { mutableIntStateOf(2) }
    val scope = rememberCoroutineScope()
    var showFavoriteDialog by remember { mutableStateOf(false) }
    val clickedItem by viewModel.clickedItem.state.collectAsStateWithLifecycle()
    val fetchForPlayer = mainVM.fetchForPlayer
    var currentFavoriteItem by remember { mutableStateOf<HomeItemModel?>(null) }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize(),
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


        items(itemList) { movie ->
            MoviesRow(
                title = movie.title,
                movieList = movie.contents,
                onMovieSelected = { homeItem ->
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

    when (val resource = clickedItem) {
        is Resource.Success -> {
            LaunchedEffect(resource) {
                isClicked = false

                val playData = resource.value
                mainVM.playDataModel = playData
                val intent = Intent(context, PlayerActivity::class.java)
                context.startActivity(intent)
                viewModel.clearClickedItem()
            }
        }

        is Resource.Failure -> {
            Error(modifier = Modifier.fillMaxSize(), resource.error)
        }

        is Resource.Loading -> {
            if (isClicked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
