package kurd.reco.roxio.ui.favorite

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.DetailScreenRootDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kurd.reco.core.Global
import kurd.reco.core.Global.pluginLoaded
import kurd.reco.core.api.Resource
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.data.db.favorite.FavoriteDao
import kurd.reco.core.plugin.PluginManager
import kurd.reco.roxio.PlayerActivity
import kurd.reco.roxio.common.CircularProgressIndicator
import kurd.reco.roxio.common.FavoriteDialog
import kurd.reco.roxio.common.ImageOverlayCard
import kurd.reco.roxio.defaultBorder
import kurd.reco.roxio.ui.home.HomeVM
import org.koin.compose.koinInject

@Destination<RootGraph>
@Composable
fun FavoritesScreen(
    navigator: DestinationsNavigator,
    favoriteDao: FavoriteDao = koinInject(),
    pluginManager: PluginManager = koinInject(),
    homeVM: HomeVM = koinInject()
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
                    text = "No favorites yet",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    } else {
        val clickedItem by homeVM.clickedItem.state.collectAsState()

        var isError by remember { mutableStateOf(false) }
        var errorText by remember { mutableStateOf("") }
        var isClicked by remember { mutableStateOf(false) }
        var showFavoriteDialog by remember { mutableStateOf(false) }
        var currentFavoriteItem by remember { mutableStateOf<HomeItemModel?>(null) }
        val scope = rememberCoroutineScope()

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            Text(
                text = "Favorites",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyRow {
                items(favoriteList) { favorite ->
                    Surface(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(150.dp, 240.dp),
                        shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.medium),
                        onClick = {
                            isClicked = true
                            Global.clickedItem = favorite.toHomeItemModel()

                            if (favorite.pluginID != currentPlugin?.id) {
                                pluginManager.selectPlugin(favorite.pluginID)
                                pluginLoaded = false
                            }

                            scope.launch {
                                while (!pluginLoaded) {
                                    delay(100)
                                }

                                if (favorite.isLiveTv) {
                                    homeVM.getUrl(favorite.id, favorite.title)
                                } else {
                                    navigator.navigate(
                                        DetailScreenRootDestination(
                                            favorite.id,
                                            favorite.isSeries
                                        )
                                    )
                                }
                            }
                        },
                        onLongClick = {
                            showFavoriteDialog = true
                            currentFavoriteItem = favorite.toHomeItemModel()
                        },
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                        border = ClickableSurfaceDefaults.border(focusedBorder = defaultBorder())
                    ) {
                        ImageOverlayCard(
                            title = favorite.title,
                            imageUrl = favorite.image,
                            isLiveTv = favorite.isLiveTv
                        )
                    }
                }
            }
        }

        when (val resource = clickedItem) {
            is Resource.Success -> {
                isClicked = false

                val playData = resource.value

                Global.playDataModel = playData

                LaunchedEffect(resource) {
                    Global.playDataModel = playData
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