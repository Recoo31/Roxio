package kurd.reco.roxio.ui.home


import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kurd.reco.core.api.model.PagerDataClass
import kurd.reco.roxio.defaultBorder

@Composable
fun ViewPager(
    items: List<PagerDataClass>,
    onFocus: () -> Unit,
    onItemClicked: (PagerDataClass) -> Unit
) {
    val pagerState = rememberPagerState {
        items.size
    }
    val animationScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Preload images using Coil
    val imageLoader = ImageLoader(context)
    LaunchedEffect(items) {
        items.forEach { item ->
            val request = ImageRequest.Builder(context)
                .data(item.poster)
                .build()
            imageLoader.execute(request)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            animationScope.launch {
                val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
                pagerState.animateScrollToPage(
                    nextPage,
                    animationSpec = tween(durationMillis = 1000)
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .heightIn(max = 300.dp)
                .align(Alignment.CenterHorizontally)

        ) { page ->
            val item = items[page]

            Surface(
                onClick = { onItemClicked(item) },
                border = ClickableSurfaceDefaults.border(
                    focusedBorder = defaultBorder()
                ),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .graphicsLayer {
                            val pageOffset =
                                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

                            lerp(
                                start = 0.75f,
                                stop = 1f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            ).also { scale ->
                                scaleX = scale
                                scaleY = scale
                            }
                            alpha = lerp(
                                start = 0.5f,
                                stop = 1f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            )
                        }
                ) {
                    Box {
                        AsyncImage(
                            model = item.poster,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(MaterialTheme.shapes.large),
                        )
                    }

                    if (!item.logoImage.isNullOrEmpty()) {
                        AsyncImage(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 80.dp),
                            model = item.logoImage,
                            contentDescription = null,
                        )
                    }

                    /*                WideButton(
                                        onClick = { onItemClicked(item) },
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 24.dp)
                                            .onFocusChanged {
                                                if (it.isFocused) {
                                                    onFocus()
                                                }
                                            }
                                    ) {
                                        Text(
                                            text = "Watch Now",
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center
                                        )
                                    }*/
                }

            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.background(
                MaterialTheme.colorScheme.inversePrimary.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.large
            )
        ) {
            Row(
                Modifier
                    .padding(8.dp)
                    .wrapContentHeight(),
            ) {
                repeat(pagerState.pageCount) { pageIndex ->
                    val isSelected = pagerState.currentPage == pageIndex
                    val color =
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.3f
                        )
                    val width = if (isSelected) 16.dp else 8.dp
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(height = 8.dp, width = width)
                    )
                }
            }
        }
    }
}