package kurd.reco.mobile.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.generated.destinations.DetailScreenRootDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kurd.reco.core.api.Resource
import kurd.reco.mobile.ui.detail.composables.CustomIconButton
import kurd.reco.mobile.ui.home.HomeVM
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySheet(
    title: String,
    navigator: DestinationsNavigator,
    viewModel: HomeVM = koinInject(),
    onDismiss: () -> Unit,
) {
    val categoryList by viewModel.selectedCategoryList.state.collectAsStateWithLifecycle()
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopStart) {
            CustomIconButton(
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                viewModel.resetCategory()
                onDismiss()
            }

            Text(
                text = title,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        when (val response = categoryList) {
            is Resource.Loading -> {
                LazyVerticalGrid(columns = GridCells.Adaptive(140.dp)) {
                    items(10) {
                        ShimmerMovieCard()
                    }
                }
            }
            is Resource.Success -> {
                LazyVerticalGrid(columns = GridCells.Adaptive(140.dp)) {
                    items(response.value) { item ->
                        MovieCard(item = item, showTitle = false) {
                            if (item.isLiveTv) {
                                viewModel.getUrl(item.id, item.title)
                            } else {
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
            is Resource.Failure -> {

            }
        }
    }
}