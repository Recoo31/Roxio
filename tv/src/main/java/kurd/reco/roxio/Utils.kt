package kurd.reco.roxio

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.api.model.SearchModel


@Immutable
data class Padding(
    val start: Dp,
    val top: Dp,
    val end: Dp,
    val bottom: Dp,
)

val ParentPadding = PaddingValues(vertical = 16.dp, horizontal = 58.dp)

@Composable
fun rememberChildPadding(direction: LayoutDirection = LocalLayoutDirection.current): Padding {
    return remember {
        Padding(
            start = ParentPadding.calculateStartPadding(direction) + 8.dp,
            top = ParentPadding.calculateTopPadding(),
            end = ParentPadding.calculateEndPadding(direction) + 8.dp,
            bottom = ParentPadding.calculateBottomPadding()
        )
    }
}

fun List<SearchModel>.toMovieList(): List<HomeItemModel> {
    return this.map {
        HomeItemModel(
            id = it.id.toString(),
            title = it.title,
            poster = it.image,
            isLiveTv = false,
            isSeries = it.isSeries
        )
    }
}