package kurd.reco.roxio.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.data.db.favorite.Favorite
import kurd.reco.core.data.db.favorite.FavoriteDao
import kurd.reco.core.plugin.PluginManager
import kurd.reco.roxio.R
import org.koin.compose.koinInject

@Composable
fun FavoriteDialog(
    item: HomeItemModel,
    favoriteDao: FavoriteDao = koinInject(),
    pluginManager: PluginManager = koinInject(),
    onDismiss: () -> Unit,
) {
    val isFavorited = favoriteDao.getFavoriteById(item.id.toString()) != null

    TvAlertDialog(
        onDismiss = onDismiss,
        title = if (isFavorited) stringResource(R.string.delete_fav) else stringResource(R.string.add_fav),
        message = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(item.title)
                        }
                        append(
                            if (isFavorited)
                                stringResource(R.string.delete_fav_desc)
                            else
                                stringResource(R.string.add_fav_desc)
                        )
                    }
                )
                Box(
                    modifier = Modifier.width(350.dp).padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = item.poster,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .sizeIn(maxHeight = 500.dp, minHeight = 300.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                }
            }
        },
        onConfirm = {
            if (isFavorited) {
                favoriteDao.deleteFavoriteById(item.id.toString())
            } else {
                favoriteDao.insertFavorite(
                    Favorite(
                        id = item.id.toString(),
                        title = item.title ?: "",
                        image = item.poster,
                        isSeries = item.isSeries,
                        pluginID = pluginManager.getLastSelectedPlugin()!!.id,
                        isLiveTv = item.isLiveTv
                    )
                )
            }
            onDismiss()
        }
    )
}
