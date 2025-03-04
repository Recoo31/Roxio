package kurd.reco.mobile.ui.favorite

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.DetailScreenRootDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kurd.reco.core.Global
import kurd.reco.core.Global.pluginLoaded
import kurd.reco.core.MainVM
import kurd.reco.core.api.Resource
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.HomeScreenModel
import kurd.reco.core.data.db.favorite.FavoriteDao
import kurd.reco.core.plugin.PluginManager
import kurd.reco.mobile.PlayerActivity
import kurd.reco.mobile.R
import kurd.reco.mobile.common.FavoriteDialog
import kurd.reco.mobile.common.ImageOverlayCard
import kurd.reco.mobile.ui.home.HomeVM
import kurd.reco.mobile.ui.player.openVideoWithSelectedPlayer
import org.koin.compose.koinInject

@OptIn(ExperimentalFoundationApi::class)
@Destination<RootGraph>
@Composable
fun FavoritesScreen(
    navigator: DestinationsNavigator,
    favoriteDao: FavoriteDao = koinInject(),
    pluginManager: PluginManager = koinInject(),
    homeVM: HomeVM = koinInject(),
    mainVM: MainVM = koinInject()
) {
    val context = LocalContext.current
    val favoriteList = favoriteDao.getAllFavorites()
    val currentPlugin by pluginManager.getSelectedPluginFlow().collectAsState()

    if (favoriteList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.no_favorites_yet),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    } else {
        val clickedItem by homeVM.clickedItem.state.collectAsState()
        val scope = rememberCoroutineScope()
        var isError by remember { mutableStateOf(false) }
        var errorText by remember { mutableStateOf("") }
        var isClicked by remember { mutableStateOf(false) }
        var showFavoriteDialog by remember { mutableStateOf(false) }
        var currentFavoriteItem by remember { mutableStateOf<HomeItemModel?>(null) }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(favoriteList) { movie ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.elevatedCardElevation(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .animateContentSize()
                        .combinedClickable(
                            onClick = {
                                isClicked = true

                                if (movie.pluginID != currentPlugin?.id) {
                                    pluginManager.selectPlugin(movie.pluginID)
                                    pluginLoaded = false
                                }

                                scope.launch {
                                    while (!pluginLoaded) {
                                        delay(100)
                                    }

                                    if (movie.isLiveTv) {
                                        homeVM.getUrl(movie.id, movie.title)
                                    } else {
                                        navigator.navigate(
                                            DetailScreenRootDestination(
                                                movie.id,
                                                movie.isSeries
                                            )
                                        )
                                    }
                                }
                            },
                            onLongClick = {
                                showFavoriteDialog = true
                                currentFavoriteItem = movie.toHomeItemModel()
                            }
                        )
                ) {
                    ImageOverlayCard(
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                        title = movie.title,
                        imageUrl = movie.image,
                        isLiveTv = movie.isLiveTv
                    )
                }
            }
        }

        when (val resource = clickedItem) {
            is Resource.Success -> {
                isClicked = false

                val playData = resource.value

                mainVM.playDataModel = playData

                LaunchedEffect(resource) {
                    mainVM.playDataModel = playData
                    val intent = Intent(context, PlayerActivity::class.java)
                    context.startActivity(intent)
                    homeVM.clearClickedItem()
                }
            }

            is Resource.Failure -> {
                LaunchedEffect(resource) {
                    isClicked = false
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

        if (!pluginLoaded) {
            CircularProgressIndicator()
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
