package kurd.reco.roxio.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

@Composable
fun FilterMenuSearch(
    modifier: Modifier = Modifier,
    selectedFilter: FilterType? = null,
    onSelect: (FilterType) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.medium
                )
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(8.dp)
        ) {
            FilterMenuItem(
                text = "All",
                isSelected = selectedFilter == FilterType.BOTH,
                onClick = { onSelect(FilterType.BOTH) }
            )
            FilterMenuItem(
                text = "Movies",
                isSelected = selectedFilter == FilterType.MOVIES,
                onClick = { onSelect(FilterType.MOVIES) }
            )
            FilterMenuItem(
                text = "Series",
                isSelected = selectedFilter == FilterType.SERIES,
                onClick = { onSelect(FilterType.SERIES) }
            )
        }
    }
}


@Composable
fun FilterMenuItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .padding(8.dp),
        onClick = onClick,
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent
        )
    ) {
        Text(
            text = text,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}