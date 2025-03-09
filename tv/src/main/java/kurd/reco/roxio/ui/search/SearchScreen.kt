package kurd.reco.roxio.ui.search

import android.view.KeyEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.ShapeDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.DetailScreenRootDestination
import com.ramcosta.composedestinations.generated.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kurd.reco.core.api.model.HomeItemModel
import kurd.reco.roxio.R
import kurd.reco.roxio.common.CircularProgressIndicator
import kurd.reco.roxio.common.FavoriteDialog
import kurd.reco.roxio.common.ItemDirection
import kurd.reco.roxio.common.MoviesRow
import kurd.reco.roxio.rememberChildPadding
import kurd.reco.roxio.toMovieList
import kurd.reco.roxio.ui.settings.PluginSection
import org.koin.compose.koinInject

@OptIn(ExperimentalComposeUiApi::class)
@Destination<RootGraph>
@Composable
fun SearchScreen(viewModel: SearchVM = koinInject(), navigator: DestinationsNavigator) {
    val focusManager = LocalFocusManager.current

    val searchList = viewModel.searchList
    val selectedFilter = viewModel.filterType
    val searchState = viewModel.searchFieldState

    var isFilterMenuExpanded by remember { mutableStateOf(false) }
    val childPadding = rememberChildPadding()
    val tfInteractionSource = remember { MutableInteractionSource() }
    val tfFocusRequester = remember { FocusRequester() }
    val isTfFocused by tfInteractionSource.collectIsFocusedAsState()
    val searchText by viewModel.searchTextState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }
    var currentFavoriteItem by remember { mutableStateOf<HomeItemModel?>(null) }
    var showFavoriteDialog by remember { mutableStateOf(false) }

    val filteredList = when (selectedFilter) {
        FilterType.MOVIES -> searchList.filter { !it.isSeries }.toMovieList()
        FilterType.SERIES -> searchList.filter { it.isSeries }.toMovieList()
        FilterType.BOTH -> searchList.toMovieList()
    }

    LaunchedEffect(searchText) {
        if (searchText.isNotEmpty() && searchText.toString() != viewModel.lastSearchedText) {
            viewModel.search(searchText.toString())
        }
    }

    if (showSettings) {
        navigator.navigate(SettingsScreenDestination)
    }

    LazyColumn(Modifier
        .fillMaxSize()
        .onKeyEvent {
            if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_MENU) {
                showSettings = true
                true
            } else {
                false
            }
        }
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = ClickableSurfaceDefaults.shape(shape = ShapeDefaults.ExtraSmall),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.inverseOnSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                        pressedContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                        focusedContentColor = MaterialTheme.colorScheme.onSurface,
                        pressedContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = ClickableSurfaceDefaults.border(
                        focusedBorder = Border(
                            border = BorderStroke(
                                width = if (isTfFocused) 2.dp else 1.dp,
                                color = animateColorAsState(
                                    targetValue = if (isTfFocused) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.border,
                                    label = ""
                                ).value
                            ),
                            shape = ShapeDefaults.ExtraSmall
                        )
                    ),
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .height(96.dp)
                        .padding(16.dp),
                    onClick = { tfFocusRequester.requestFocus() }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.9f)
                            .padding(vertical = 16.dp)
                            .padding(start = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            state = searchState,
                            modifier = Modifier
                                .focusRequester(tfFocusRequester)
                                .onKeyEvent {
                                    if (it.nativeKeyEvent.action == KeyEvent.ACTION_UP) {
                                        when (it.nativeKeyEvent.keyCode) {
                                            KeyEvent.KEYCODE_DPAD_DOWN -> {
                                                focusManager.moveFocus(FocusDirection.Down)
                                            }

                                            KeyEvent.KEYCODE_DPAD_UP -> {
                                                focusManager.moveFocus(FocusDirection.Up)
                                            }

                                            KeyEvent.KEYCODE_BACK -> {
                                                focusManager.moveFocus(FocusDirection.Exit)
                                            }
                                        }
                                    }
                                    true
                                },
                            cursorBrush = Brush.verticalGradient(
                                colors = listOf(
                                    LocalContentColor.current,
                                    LocalContentColor.current,
                                )
                            ),
                            keyboardOptions = KeyboardOptions(
                                autoCorrectEnabled = false,
                                imeAction = ImeAction.Search
                            ),
                            onKeyboardAction = {
                                viewModel.search(searchState.text.toString())
                                focusManager.moveFocus(FocusDirection.Down)
                            },
                            interactionSource = tfInteractionSource,
                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        if (searchState.text.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Search...",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                )
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { isFilterMenuExpanded = !isFilterMenuExpanded }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_filter_list_24),
                            contentDescription = null
                        )
                    }
                }

            }
            if (isFilterMenuExpanded) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    FilterMenu(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp, top = 16.dp),
                        selectedFilter = selectedFilter,
                        onSelect = {
                            viewModel.filterType = it
                            isFilterMenuExpanded = false
                            focusManager.moveFocus(FocusDirection.Down)
                        },
                        onDismiss = { isFilterMenuExpanded = false }
                    )
                }
            }
        }

        item {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                MoviesRow(
                    modifier = Modifier
                        .padding(top = childPadding.top * 2),
                    movieList = filteredList,
                    itemDirection = ItemDirection.Horizontal,
                    onMovieSelected = {
                        navigator.navigate(
                            DetailScreenRootDestination(
                                id = it.id.toString(),
                                isSeries = it.isSeries
                            )
                        )
                    },
                    onLongClick = {
                        currentFavoriteItem = it
                        showFavoriteDialog = true
                    }
                )
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
