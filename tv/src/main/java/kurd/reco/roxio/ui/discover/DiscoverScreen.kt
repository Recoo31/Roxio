package kurd.reco.roxio.ui.discover

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.DetailScreenRootDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kurd.reco.core.Global
import kurd.reco.core.api.Resource
import kurd.reco.core.api.model.DiscoverCategory
import kurd.reco.core.api.model.DiscoverFilter
import kurd.reco.core.api.model.DiscoverSubCategory
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.data.ItemDirection
import kurd.reco.core.viewmodels.DiscoverVM
import kurd.reco.roxio.R
import kurd.reco.roxio.common.CircularProgressIndicator
import kurd.reco.roxio.common.FavoriteDialog
import kurd.reco.roxio.common.MoviesRowItem
import kurd.reco.roxio.ui.search.FilterMenuSearch
import org.koin.compose.koinInject

@Destination<RootGraph>
@Composable
fun DiscoverScreen(
    navigator: DestinationsNavigator
) {
    val viewModel: DiscoverVM = koinInject()

    val categories = viewModel.categories
    val discoverItems by viewModel.discoverItems.state.collectAsState()
    val selectedCategory = viewModel.selectedCategory
    val selectedSubCategory = viewModel.selectedSubCategory
    val discoverFilters = viewModel.discoverFilters
    val itemDirection = viewModel.itemDirection

    var currentFavoriteItem by remember { mutableStateOf<HomeItemModel?>(null) }
    var showFavoriteDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var isCategoryHaveFocus by remember { mutableStateOf(false) }

    val gridState = rememberLazyGridState()
    var showFilterMenu by remember { mutableStateOf(false) }

    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            lastVisibleItemIndex > (totalItemsNumber - 3)
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            println("Loading more...")
            viewModel.loadMoreIfNeeded()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
        viewModel.loadFilters()
        if (discoverItems is Resource.Loading && categories.isNotEmpty()) {
            viewModel.selectCategory(categories.first())
        }
    }

    BackHandler {
        if (!isCategoryHaveFocus) {
            focusRequester.requestFocus()
        } else {
            navigator.popBackStack()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(categories) { category ->
                    CategoryItem(
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .onFocusChanged { isCategoryHaveFocus = it.hasFocus },
                        category = category,
                        isSelected = category == selectedCategory,
                        onClick = { viewModel.selectCategory(category) }
                    )
                }
            }

            Column(modifier = Modifier.padding(end = 64.dp).align(Alignment.TopEnd)) {
                IconButton(
                    onClick = { showFilterMenu = !showFilterMenu }
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (showFilterMenu && discoverFilters.isNotEmpty()) {
                    LazyColumn {
                        items(discoverFilters) {
                            FilterItem(
                                filter = it,
                                onClick = {
                                    viewModel.applyFilter(it.id)
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
            }

        }

        // SubCategories if available
        selectedCategory?.let { category ->
            if (category.subCategories.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(category.subCategories) { subCategory ->
                        SubCategoryItem(
                            subCategory = subCategory,
                            isSelected = subCategory == selectedSubCategory,
                            onClick = { viewModel.selectSubCategory(subCategory) }
                        )
                    }
                }
            }
        }

        when (val response = discoverItems) {
            is Resource.Failure -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = response.error,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is Resource.Success -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyVerticalGrid(
                        modifier = Modifier.fillMaxSize(),
                        columns = GridCells.Fixed(
                            when (itemDirection) {
                                ItemDirection.Vertical -> 5
                                ItemDirection.Horizontal -> 3
                            }
                        ),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        state = gridState
                    ) {
                        itemsIndexed(response.value) { index, item ->
                            MoviesRowItem(
                                index = index,
                                itemDirection = itemDirection,
                                onMovieSelected = {
                                    Global.clickedItem = item
                                    navigator.navigate(DetailScreenRootDestination(item.id.toString(), item.isSeries))
                                },
                                onLongClick = {
                                    currentFavoriteItem = it
                                    showFavoriteDialog = true
                                },
                                movie = item,
                                showItemTitle = true,
                                showIndexOverImage = false
                            )
                        }
                    }

                    // Loading indicator for pagination
                    if (viewModel.isLoadingMore) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFavoriteDialog && currentFavoriteItem != null) {
        FavoriteDialog(
            item = currentFavoriteItem!!,
            onDismiss = { showFavoriteDialog = false }
        )
    }
}

@Composable
private fun CategoryItem(
    modifier: Modifier = Modifier,
    category: DiscoverCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.padding(vertical = 4.dp),
        colors = CardDefaults.colors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = category.name,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SubCategoryItem(
    subCategory: DiscoverSubCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.padding(vertical = 4.dp),
        colors = CardDefaults.colors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.inversePrimary
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = subCategory.name,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun FilterItem(
    filter: DiscoverFilter,
    onClick: () -> Unit
) {
    Surface(modifier = Modifier, onClick = onClick) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = filter.name,
                style = MaterialTheme.typography.bodyLarge
            )
//            Icon(
//                painter = painterResource(R.drawable.ic_baseline_chevron_right_24),
//                contentDescription = null,
//                tint = MaterialTheme.colorScheme.onSurfaceVariant
//            )
        }

    }
}