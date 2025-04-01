package kurd.reco.mobile.ui

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

//https://gist.github.com/bhaskar966/b8ae46ace8d8ee4c93f878abd14e1204
/**
 * Creates a NestedScrollConnection that forwards scroll events to the provided LazyListState.
 * This allows nested scrollable components to scroll the parent component.
 *
 * @param scrollState The parent LazyListState to forward scroll events to
 * @param scrollFactor Factor to multiply scroll delta by (1.0f means same speed, 0.5f means half speed)
 * @param scope The coroutine scope to use for auto scroll along the axis inside the NestedScrollConnection
 * @return A NestedScrollConnection that can be used with .nestedScroll() modifier
 */
@Composable
fun rememberForwardingScrollConnection(
    scrollState: LazyListState,
    scrollFactor: Float = 0.5f,
    scope: CoroutineScope = rememberCoroutineScope()
): NestedScrollConnection {

    return remember(scrollState, scrollFactor) {
        createForwardingScrollConnection(scrollState, scope, scrollFactor)
    }
}

/**
 * Non-composable version that can be used outside of composition
 */
fun createForwardingScrollConnection(
    scrollState: LazyListState,
    scope: CoroutineScope,
    scrollFactor: Float = 0.5f
): NestedScrollConnection {
    return object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val adjustedDelta = available * scrollFactor
            scope.launch {
                scrollState.scroll {
                    scrollBy(-adjustedDelta.y)
                }
            }
            return Offset.Zero
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            val adjustedDelta = available * scrollFactor
            scope.launch {
                scrollState.scroll {
                    scrollBy(-adjustedDelta.y)
                }
            }
            return Offset.Zero
        }
    }
}
