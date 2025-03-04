package kurd.reco.roxio.ui.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import kurd.reco.core.api.model.SeriesDataModel
import kurd.reco.roxio.ui.theme.RoxioTheme

@Composable
fun SeasonsSelector(
    season: List<SeriesDataModel>,
    selectedSeason: Int,
    onSeasonSelected: (Int) -> Unit
) {
    LazyRow {
        items(season.size) { item ->
            SeasonSelectorItem(
                item = item,
                isSelected = item == selectedSeason,
                onSeasonSelected = { onSeasonSelected(it) },
            )
        }
    }
}

@Composable
fun SeasonSelectorItem(
    item: Int,
    isSelected: Boolean,
    onSeasonSelected: (Int) -> Unit,
) {
    Surface(
        modifier = Modifier
            .animateContentSize()
            .padding(horizontal = 8.dp, vertical = 16.dp)
            .size(
                width = 120.dp,
                height = 60.dp
            ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
        ),
        shape = ClickableSurfaceDefaults.shape(
            MaterialTheme.shapes.medium
        ),
        onClick = {
            onSeasonSelected(item)
        },
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "Season ${item + 1}",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SeasonsSelectorPreview() {
    RoxioTheme {
        val seasons = listOf(
            SeriesDataModel("1", "Season 1", emptyList()),
            SeriesDataModel("2", "Season 2", emptyList()),
            SeriesDataModel("3", "Season 3", emptyList()),
            SeriesDataModel("4", "Season 4", emptyList()),
            SeriesDataModel("5", "Season 5", emptyList()),
        )
        SeasonsSelector(
            season = seasons,
            selectedSeason = 0,
            onSeasonSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SeasonSelectorItemPreview() {
    RoxioTheme {
        SeasonSelectorItem(
            item = 0,
            isSelected = true,
            onSeasonSelected = {}
        )
    }
}