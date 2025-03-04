package kurd.reco.roxio.common

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Tab
import androidx.tv.material3.TabRow
import androidx.tv.material3.Text
import kurd.reco.roxio.ui.theme.RoxioTheme

@Composable
fun TopCategory(
    categoryList: List<String>,
    onClick: (String) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    TabRow(
        modifier = Modifier.padding(8.dp),
        selectedTabIndex = selectedTabIndex
    ) {
        categoryList.forEachIndexed { index, tab ->
            Tab(
                selected = index == selectedTabIndex,
                onFocus = {
                    selectedTabIndex = index
                },
                onClick = {
                    onClick(tab)
                }
            ) {
                Text(
                    text = tab,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun TopCategoryPreview() {
    RoxioTheme {
        val list = listOf("Action", "Comedy", "Drama", "Horror")
        TopCategory(list) {}
    }
}