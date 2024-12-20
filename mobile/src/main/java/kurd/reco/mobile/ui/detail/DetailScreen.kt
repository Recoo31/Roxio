package kurd.reco.mobile.ui.detail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kurd.reco.core.MainVM
import kurd.reco.core.SettingsDataStore
import kurd.reco.core.api.Resource
import kurd.reco.core.api.model.DetailScreenModel
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.HomeScreenModel
import kurd.reco.core.api.model.SeriesItem
import kurd.reco.core.data.db.favorite.Favorite
import kurd.reco.core.data.db.favorite.FavoriteDao
import kurd.reco.core.data.db.plugin.PluginDao
import kurd.reco.mobile.PlayerActivity
import kurd.reco.mobile.common.ErrorShower
import kurd.reco.mobile.ui.detail.composables.BackImage
import kurd.reco.mobile.ui.detail.composables.CustomIconButton
import kurd.reco.mobile.ui.detail.composables.DescriptionSection
import kurd.reco.mobile.ui.detail.composables.MovieDetails
import kurd.reco.mobile.ui.detail.composables.MultiSourceDialog
import kurd.reco.mobile.ui.detail.composables.SeasonItem
import kurd.reco.mobile.ui.detail.composables.SeasonsSelector
import kurd.reco.mobile.ui.player.openVideoWithSelectedPlayer
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

    var isError by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }

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
                    viewModel.getMovie(id, isSeries)
                },
                onDismiss = { isError = false }
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
    mainVM: MainVM = koinInject(),
    pluginDao: PluginDao = koinInject(),
    favoriteDao: FavoriteDao = koinInject(),
    settingsDataStore: SettingsDataStore = koinInject()
) {
    val context = LocalContext.current
    val clickedItem by viewModel.clickedItem.state.collectAsState()
    val externalPlayer by settingsDataStore.externalPlayer.collectAsState("")

    val fetchForPlayer = mainVM.fetchForPlayer

    var expanded by remember { mutableStateOf(false) }
    var selectedSeason by rememberSaveable { mutableIntStateOf(0) }
    var isError by remember { mutableStateOf(false) }
    var showMultiSelect by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }
    var selectedEpisode by remember { mutableStateOf<SeriesItem?>(null) }
    var isLoading by remember { mutableStateOf(false) }


    if (isError) {
        ErrorShower(
            errorText = errorText,
            onRetry = {
                isError = false
            },
            onDismiss = { isError = false }
        )
    }

    if (showMultiSelect) {
        Dialog(onDismissRequest = { showMultiSelect = false }) {
            MultiSourceDialog(mainVM.playDataModel, context) { mainVM.playDataModel = it }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        SeasonItem(item = episode) { item ->
                            isLoading = true
                            viewModel.getUrl(item.id, item.title)

                            val contents = episodes.map {
                                HomeItemModel(
                                    id = it.id,
                                    title = it.title,
                                    poster = it.poster,
                                    isSeries = true,
                                    isLiveTv = false
                                )
                            }
                            mainVM.clickedItem = HomeItemModel(
                                id = item.id,
                                title = item.title,
                                poster = item.poster,
                                isSeries = true,
                                isLiveTv = false
                            )
                            mainVM.clickedItemRow = HomeScreenModel("Episodes", contents)
                            selectedEpisode = episode
                        }
                    }
                }
            } else {
                item {
                    Button(
                        onClick = {
                            isLoading = true
                            viewModel.getUrl(item.id, item.title)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Play",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
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
                        pluginID = pluginDao.getSelectedPlugin()!!.id
                    )
                )
            }
            isFavorite = !isFavorite
        }
    }

    when (val resource = clickedItem) {
        is Resource.Success -> {
            val playData = resource.value

            if (externalPlayer.isNotEmpty() && playData.drm == null) {
                openVideoWithSelectedPlayer(
                    context = context,
                    videoUri = playData.urls[0].second,
                    playerPackageName = externalPlayer
                )
            } else {
                mainVM.playDataModel = if (item.isSeries) {
                    playData.copy(title = "${item.title} | ${selectedEpisode?.title}")
                } else playData.copy(title = item.title)

                LaunchedEffect(resource) {
                    if (playData.urls.size > 1) {
                        showMultiSelect = true
                    } else {
                        val intent = Intent(context, PlayerActivity::class.java)
                        context.startActivity(intent)
                        viewModel.clearClickedItem()
                        isLoading = false
                    }
                }
            }
        }

        is Resource.Failure -> {
            LaunchedEffect(resource) {
                isError = true
                errorText = resource.error
            }
        }

        is Resource.Loading -> {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxSize()
                        .clickable { }
                ) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }

    if (fetchForPlayer) {
        isLoading = true
        val selectedItem = mainVM.clickedItem
        selectedItem?.let {
            viewModel.getUrl(it.id, it.title)
            selectedEpisode = SeriesItem(
                id = it.id,
                title = it.title ?: "",
                poster = it.poster,
                description = null
            )
        }

        mainVM.fetchForPlayer = false
    }
}