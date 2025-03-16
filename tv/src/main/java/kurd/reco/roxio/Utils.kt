package kurd.reco.roxio

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import androidx.tv.material3.Border
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.ShapeDefaults
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

@Composable
fun defaultBorderStroke() = BorderStroke(
    width = 4.dp,
    color = MaterialTheme.colorScheme.primary
)

@Composable
fun defaultBorder(width: Dp = 4.dp, color: Color = MaterialTheme.colorScheme.primary, shape: Shape = ShapeDefaults.Small) = Border(
    border = BorderStroke(
        width = width,
        color = color
    ),
    shape = shape
)


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

fun extractDominantColor(drawable: Drawable?, defaultColor: Color, onColorExtracted: (Color) -> Unit) {
    if (drawable is BitmapDrawable) {
        val bitmap = drawable.bitmap.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && it.config == Bitmap.Config.HARDWARE) it.copy(Bitmap.Config.ARGB_8888, true) else it
        }

        Palette.from(bitmap).generate { palette ->
            onColorExtracted(Color(palette?.getDominantColor(defaultColor.toArgb()) ?: defaultColor.toArgb()))
        }
    }
}