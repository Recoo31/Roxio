package kurd.reco.mobile.ui.home


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kurd.reco.core.api.model.PagerDataClass

@Composable
fun ViewPager(
    items: List<PagerDataClass>,
    onItemClicked: (PagerDataClass) -> Unit
) {
    val pagerState = rememberPagerState {
        items.size
    }
    val animationScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Preload images using Coil
   LaunchedEffect(items) {
       items.forEach { item ->
           ImageRequest.Builder(context)
               .data(item.poster)
               .build()
       }
   }

//    LaunchedEffect(Unit) {
//        while (true) {
//            delay(3000)
//            animationScope.launch {
//                val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
//                pagerState.animateScrollToPage(
//                    nextPage,
//                    animationSpec = tween(durationMillis = 1000)
//                )
//            }
//        }
//    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
        ) { page ->
            val item = items[page]
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

                ImageWithShadow(item.poster)

                if (!item.logoImage.isNullOrEmpty()) {
                    AsyncImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        model = item.logoImage,
                        contentDescription = null,
                    )
                } else if (!item.title.isNullOrEmpty()) {
                    Text(
                        text = item.title!!,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 80.dp),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        textAlign = TextAlign.Center,
                    )
                }

                ElevatedButton(
                    onClick = { onItemClicked(item) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                ) {
                    Text(text = "Watch Now")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedCard {
            Row(
                Modifier
                    .padding(8.dp)
                    .wrapContentHeight(),
            ) {
                repeat(pagerState.pageCount) { pageIndex ->
                    val color =
                        if (pagerState.currentPage == pageIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageWithShadow(imageUrl: String, bgColor: Color = MaterialTheme.colorScheme.background) {
    Box {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
                .clip(MaterialTheme.shapes.large),
        )

        // Gradient from bottom to title
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            bgColor.copy(alpha = 0.3f),
                            bgColor.copy(alpha = 0.6f),
                            bgColor
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )
    }
}