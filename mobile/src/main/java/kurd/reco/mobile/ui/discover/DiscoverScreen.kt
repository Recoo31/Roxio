package kurd.reco.mobile.ui.discover

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewComfy
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.DetailScreenRootDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kurd.reco.core.AppLog
import kurd.reco.core.api.Resource
import kurd.reco.core.api.model.DiscoverCategory
import kurd.reco.core.api.model.DiscoverFilter
import kurd.reco.core.api.model.DiscoverSubCategory
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.core.data.ItemDirection
import kurd.reco.core.viewmodels.DiscoverVM
import kurd.reco.mobile.R
import kurd.reco.mobile.common.MovieCard
import kurd.reco.mobile.ui.rememberForwardingScrollConnection
import org.koin.compose.koinInject

@Destination<RootGraph>
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    navigator: DestinationsNavigator
) {
    val viewModel: DiscoverVM = koinInject()
    val configuration = LocalConfiguration.current.screenWidthDp.dp
    val isWideScreen = configuration > 600.dp

    val categories = viewModel.categories
    val discoverItems by viewModel.discoverItems.state.collectAsState()
    val selectedCategory = viewModel.selectedCategory
    val selectedSubCategory = viewModel.selectedSubCategory
    val discoverFilters = viewModel.discoverFilters
    val itemDirection = viewModel.itemDirection

    val gridState = rememberLazyGridState()
    var showFilterMenu by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()
    val nestedScrollConnection = rememberForwardingScrollConnection(lazyListState)

    // Monitor scroll position for pagination
    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            lastVisibleItemIndex > (totalItemsNumber - 3)
        }
    }

    // Load more items when reaching end of list
    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadMoreIfNeeded()
        }
    }

    // Initial data loading
    LaunchedEffect(Unit) {
        try {
            viewModel.loadCategories()
            viewModel.loadFilters()
            if (discoverItems is Resource.Loading) {
                viewModel.selectCategory(categories.first())
            }
        } catch (e: Exception) {
            AppLog.e("DiscoverScreen", "Error loading data")
        }
    }

    LazyColumn(state = lazyListState) {
        item {
            DiscoverHeader(
                categories = categories,
                selectedCategory = selectedCategory,
                selectedSubCategory = selectedSubCategory,
                hasFilters = discoverFilters.isNotEmpty(),
                onCategorySelected = viewModel::selectCategory,
                onSubCategorySelected = viewModel::selectSubCategory,
                onFilterClick = { showFilterMenu = true }
            )
        }

        item {
            when {
                categories.isEmpty() -> {
                    EmptyStateMessage(message = "No categories available")
                }
                else -> {
                    DiscoverContent(
                        modifier = Modifier.nestedScroll(nestedScrollConnection),
                        discoverItems = discoverItems,
                        isLoadingMore = viewModel.isLoadingMore,
                        itemDirection = itemDirection,
                        isWideScreen = isWideScreen,
                        gridState = gridState,
                        onItemClick = { item ->
                            navigator.navigate(
                                DetailScreenRootDestination(
                                    item.id.toString(),
                                    item.isSeries
                                )
                            )
                        }
                    )
                }
            }
        }
    }

    // Filter dialog
    if (showFilterMenu) {
        FilterDialog(
            filters = discoverFilters,
            onFilterSelected = { filterId ->
                viewModel.applyFilter(filterId)
                showFilterMenu = false
            },
            onDismiss = { showFilterMenu = false }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiscoverHeader(
    categories: List<DiscoverCategory>,
    selectedCategory: DiscoverCategory?,
    selectedSubCategory: DiscoverSubCategory?,
    hasFilters: Boolean,
    onCategorySelected: (DiscoverCategory) -> Unit,
    onSubCategorySelected: (DiscoverSubCategory) -> Unit,
    onFilterClick: () -> Unit
) {
    val categoriesListState = rememberLazyListState()
    val subCategoriesListState = rememberLazyListState()
    
    // Scroll to selected category
    LaunchedEffect(selectedCategory) {
        selectedCategory?.let { category ->
            val index = categories.indexOf(category)
            if (index >= 0) {
                categoriesListState.animateScrollToItem(index)
            }
        }
    }
    
    // Scroll to selected subcategory
    LaunchedEffect(selectedSubCategory) {
        selectedSubCategory?.let { subCategory ->
            selectedCategory?.subCategories?.let { subCategories ->
                val index = subCategories.indexOf(subCategory)
                if (index >= 0) {
                    subCategoriesListState.animateScrollToItem(index)
                }
            }
        }
    }

    Column {
        TopAppBar(
            title = { Text("Discover") },
            actions = {
                if (hasFilters) {
                    IconButton(onClick = onFilterClick) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter"
                        )
                    }
                }
            }
        )

        if (categories.isNotEmpty()) {
            // Categories row
            LazyRow(
                state = categoriesListState,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(categories) { category ->
                    CategoryItem(
                        category = category,
                        isSelected = category == selectedCategory,
                        onClick = { onCategorySelected(category) }
                    )
                }
            }

            // Subcategories row if available
            selectedCategory?.let { category ->
                if (category.subCategories.isNotEmpty()) {
                    LazyRow(
                        state = subCategoriesListState,
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
                                onClick = { onSubCategorySelected(subCategory) }
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun DiscoverContent(
    modifier: Modifier = Modifier,
    discoverItems: Resource<List<HomeItemModel>>,
    isLoadingMore: Boolean,
    itemDirection: ItemDirection,
    isWideScreen: Boolean,
    gridState: LazyGridState,
    onItemClick: (HomeItemModel) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Loading indicator for pagination
        if (isLoadingMore) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                CircularProgressIndicator()
            }
        }

        when (discoverItems) {
            is Resource.Failure -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = discoverItems.error,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Resource.Loading -> {
            }

            is Resource.Success -> {
                LazyVerticalGrid(
                    modifier = modifier
                        .heightIn(max = LocalConfiguration.current.screenHeightDp.dp)
                        .fillMaxWidth(),
                    state = gridState,
                    columns = GridCells.Fixed(
                        when (itemDirection) {
                            ItemDirection.Vertical -> if (isWideScreen) 6 else 3
                            ItemDirection.Horizontal -> if (isWideScreen) 3 else 2
                        }
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(discoverItems.value) { item ->
                        MovieCard(
                            item = item,
                            showTitle = true,
                            itemDirection = itemDirection
                        ) {
                            onItemClick(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterDialog(
    filters: List<DiscoverFilter>,
    onFilterSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.headlineMedium
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column {
                if (filters.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No filters available",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(filters) { filter ->
                            FilterItem(
                                filter = filter,
                                onClick = { onFilterSelected(filter.id) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
private fun CategoryItem(
    category: DiscoverCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = category.name,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
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
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = subCategory.name,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FilterItem(
    filter: DiscoverFilter,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = filter.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}