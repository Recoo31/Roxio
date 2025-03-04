package kurd.reco.mobile.common

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kurd.reco.mobile.ui.theme.RoxioTheme

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun PreviewCardCategorySelection() {
    val categories = listOf(
        "Action",
        "Comedy",
        "Drama",
        "Horror",
        "Sci-Fi",
        "Romance"
    )

    RoxioTheme(
        darkTheme = true
    ) {
        MovieCategorySelector(categories = categories) {

        }
    }
}

@Composable
fun MovieCategorySelector(
    modifier: Modifier = Modifier,
    categories: List<String>,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
            )
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            CategoryChipBox(
                modifier = Modifier
                    .shadow(4.dp, shape = MaterialTheme.shapes.small)
                    .padding(8.dp),
                category = category,
                onSelected = {
                    onCategorySelected(category)
                }
            )
        }
    }
}

@Composable
fun CategoryChipBox(
    modifier: Modifier = Modifier,
    category: String,
    onSelected: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small) // Clip the shape for rounded corners
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.small
            )
            .clickable(
                onClick = onSelected,
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = category,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}
