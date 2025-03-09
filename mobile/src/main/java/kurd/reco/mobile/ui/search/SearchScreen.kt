package kurd.reco.mobile.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.DetailScreenRootDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.valentinilk.shimmer.shimmer
import kurd.reco.core.api.model.SearchModel
import kurd.reco.mobile.R
import org.koin.compose.koinInject

@Destination<RootGraph>
@Composable
fun SearchScreen(
    navigator: DestinationsNavigator,
    viewModel: SearchVM = koinInject()
) {
    val searchList = viewModel.searchList
    val selectedFilter = viewModel.filterType
    val searchText by viewModel.searchTextState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var isFilterMenuExpanded by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isWideScreen = screenWidth > 600

    val filteredList = when (selectedFilter) {
        FilterType.BOTH -> searchList
        FilterType.MOVIES -> searchList.filter { !it.isSeries }
        FilterType.SERIES -> searchList.filter { it.isSeries }
    }

    LaunchedEffect(searchText) {
        if (searchText.isNotEmpty() && searchText.toString() != viewModel.lastSearchedText) {
            viewModel.search(searchText.toString())
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                BasicTextField(
                    state = viewModel.searchFieldState,
                    modifier = Modifier.weight(1f),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    textStyle = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    onKeyboardAction = { viewModel.search(searchText.toString()) }
                )

                Box(contentAlignment = Alignment.Center) {
                    IconButton(
                        onClick = { isFilterMenuExpanded = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_filter_list_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    FilterMenu(
                        isFilterMenuExpanded,
                        selectedFilter = selectedFilter,
                        onSelect = { viewModel.filterType = it },
                        onDismiss = { isFilterMenuExpanded = false }
                    )

                }
            }

            val dividerColor =
                if (searchText.isNotEmpty()) MaterialTheme.colorScheme.primary else DividerDefaults.color

            HorizontalDivider(Modifier.padding(8.dp), thickness = 3.dp, color = dividerColor)

            if (isWideScreen) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = (screenWidth / 3).dp)
                    ) {
                    if (isLoading) {
                        items(3) { ShimmerSearchItem() }
                    } else {
                        items(filteredList) { item ->
                            SearchItem(item) {
                                navigator.navigate(
                                    DetailScreenRootDestination(
                                        item.id.toString(),
                                        item.isSeries
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn {
                    if (isLoading) {
                        items(5) {
                            ShimmerSearchItem()
                        }
                    } else {
                        items(filteredList) { item ->
                            SearchItem(item) {
                                navigator.navigate(
                                    DetailScreenRootDestination(
                                        item.id.toString(),
                                        item.isSeries
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

//        if (!pluginLoaded) {
//            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
//                CircularProgressIndicator()
//            }
//        }
    }
}

@Composable
fun FilterMenu(
    isFilterMenuExpanded: Boolean,
    selectedFilter: FilterType,
    onSelect: (FilterType) -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = isFilterMenuExpanded,
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 0.dp
    ) {
        FilterMenuItem(
            text = stringResource(R.string.all),
            isSelected = selectedFilter == FilterType.BOTH,
            onClick = { onSelect(FilterType.BOTH) }
        )
        FilterMenuItem(
            text = stringResource(R.string.movies),
            isSelected = selectedFilter == FilterType.MOVIES,
            onClick = { onSelect(FilterType.MOVIES) }
        )
        FilterMenuItem(
            text = stringResource(R.string.tv_series),
            isSelected = selectedFilter == FilterType.SERIES,
            onClick = { onSelect(FilterType.SERIES) }
        )
    }
}

@Composable
fun FilterMenuItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent

    DropdownMenuItem(
        text = {
            Text(
                text = text,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(backgroundColor, shape = MaterialTheme.shapes.large)
    )
}


@Composable
fun ShimmerSearchItem() {
    ElevatedCard(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .shimmer(),
    ) {
        Column(
            Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
        ) {
            // Title Placeholder
            Box(
                modifier = Modifier
                    .size(340.dp, 24.dp)
                    .padding(vertical = 4.dp)
                    .background(Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp))
            )

            // Image Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .padding(vertical = 8.dp)
                    .background(Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
            )
        }
    }
}


@Composable
fun SearchItem(
    item: SearchModel,
    onCLick: () -> Unit = {}
) {
    ElevatedCard(
        modifier = Modifier.padding(8.dp),
        shape = MaterialTheme.shapes.small,
        onClick = {
            onCLick()
        }
    ) {
        Column(Modifier.padding(horizontal = 8.dp)) {
            Text(
                item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            AsyncImage(
                model = item.image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .padding(bottom = 8.dp)
                    .clip(MaterialTheme.shapes.medium),
            )

        }
    }
}