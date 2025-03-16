package kurd.reco.mobile.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import coil.compose.AsyncImage
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.data.db.favorite.Favorite
import kurd.reco.core.data.db.favorite.FavoriteDao
import kurd.reco.core.plugin.PluginManager
import kurd.reco.mobile.R

@Composable
fun FavoriteDialog(
    item: HomeItemModel,
    favoriteDao: FavoriteDao,
    pluginManager: PluginManager,
    onDismiss: () -> Unit,
) {
    val isFavorited = favoriteDao.getFavoriteById(item.id.toString()) != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isFavorited) stringResource(R.string.delete_fav) else stringResource(R.string.add_fav)) },
        text = {
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
                    },
                    modifier = Modifier.align(Alignment.Start)
                )
                Box(
                    modifier = Modifier.width(250.dp).padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = item.poster,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .sizeIn(maxHeight = 300.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
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
            }) {
                Text("Evet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
