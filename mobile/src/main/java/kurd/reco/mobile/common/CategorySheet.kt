package kurd.reco.mobile.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.generated.destinations.DetailScreenRootDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kurd.reco.core.Global
import kurd.reco.core.api.Resource
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.HomeScreenModel
import kurd.reco.core.data.db.favorite.FavoriteDao
import kurd.reco.core.plugin.PluginManager
import kurd.reco.mobile.data.ErrorModel
import kurd.reco.mobile.ui.detail.composables.CustomIconButton
import kurd.reco.mobile.ui.home.HomeVM
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySheet(
    title: String,
    navigator: DestinationsNavigator,
    homeVM: HomeVM = koinInject(),
    favoriteDao: FavoriteDao = koinInject(),
    pluginManager: PluginManager = koinInject(),
    onDismiss: () -> Unit,
) {
    val categoryList by homeVM.selectedCategoryList.state.collectAsStateWithLifecycle()

    var errorModel by remember { mutableStateOf(ErrorModel("", false)) }
    var showFavoriteDialog by remember { mutableStateOf(false) }
    var currentFavoriteItem by remember { mutableStateOf<HomeItemModel?>(null) }
    val showTitle = !title.contains("TV", false)


    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopStart) {
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
            ) {
                homeVM.resetCategory()
                onDismiss()
            }

            Text(
                text = title,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
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

        when (val response = categoryList) {
            is Resource.Loading -> {
                LazyVerticalGrid(columns = GridCells.Adaptive(140.dp)) {
                    items(10) {
                        ShimmerMovieCard()
                    }
                }
            }

            is Resource.Success -> {
                val list = response.value
                LazyVerticalGrid(columns = GridCells.Adaptive(140.dp)) {
                    items(list) { homeItem ->
                        MovieCard(
                            item = homeItem,
                            showTitle = showTitle,
                            onItemClick = {
                                Global.clickedItem = homeItem

                                if (homeItem.isLiveTv) {
                                    homeVM.getUrl(homeItem.id, homeItem.title)
                                    Global.clickedItemRow = HomeScreenModel("", list)
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
            }

            is Resource.Failure -> {
                LaunchedEffect(response) {
                    errorModel = ErrorModel(response.error, true)
                }
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