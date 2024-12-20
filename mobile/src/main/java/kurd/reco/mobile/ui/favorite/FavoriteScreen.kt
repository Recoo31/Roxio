package kurd.reco.mobile.ui.favorite

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.DetailScreenRootDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kurd.reco.core.data.db.favorite.FavoriteDao
import kurd.reco.core.plugin.PluginManager
import kurd.reco.mobile.common.ImageOverlayCard
import org.koin.compose.koinInject

@Destination<RootGraph>
@Composable
fun FavoritesScreen(
    navigator: DestinationsNavigator,
    favoriteDao: FavoriteDao = koinInject(),
    pluginManager: PluginManager = koinInject()
) {
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
                    onClick = {
                        if (movie.pluginID != currentPlugin?.id) {
                            pluginManager.selectPlugin(movie.pluginID)
                        }
                        navigator.navigate(
                            DetailScreenRootDestination(
                                movie.id,
                                movie.isSeries
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .animateContentSize()
                ) {
                    ImageOverlayCard(title = movie.title, imageUrl = movie.image)
                }
            }
        }
    }
}
