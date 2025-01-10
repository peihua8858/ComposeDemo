package com.android.composedemo.utils

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey

/**
 *
 * 渲染到最后一个时触发加载更多
 * @author dingpeihua
 * @date 2025/1/10 16:06
 **/
fun <T : Any> LazyListScope.items(
    items: LazyPagingItems<T>,
    key: ((item: T) -> Any)? = null,
    itemContent: @Composable LazyItemScope.(value: T?) -> Unit
) {
    items(
        count = items.itemCount,
        key = if (key == null) null else { index ->
            items.itemKey()
        }
    ) { index ->
        if (index >= items.itemCount - 1) {
            //最后一项触发加载更多
            itemContent(items[index])
        } else {
            itemContent(items.peek(index))
        }
    }
}

/**
 *
 * 仅渲染不触发加载更多
 * @author dingpeihua
 * @date 2025/1/10 16:06
 **/
fun <T : Any> LazyGridScope.items(
    items: LazyPagingItems<T>,
    key: ((item: T) -> Any)? = null,
    itemContent: @Composable LazyGridItemScope.(index: Int, value: T?) -> Unit
) {

    items(
        count = items.itemCount,
        key = if (key == null) null else { index ->
            items.itemKey()
        }
    ) { index ->
        itemContent(index, items.peek(index))
    }
}
/**
 *
 * 滑动到最后触发加载更多
 * @author dingpeihua
 * @date 2025/1/10 16:06
 **/
@Composable
fun <T : Any> LazyListState.LaunchedLoadMore(items: LazyPagingItems<T>) {
    // 监测滚动状态以自动加载更多
    LaunchedEffect(this) {
        snapshotFlow { layoutInfo }
            .collect { layoutInfo ->
                val itemCount = items.itemCount
                val lastVisibleItemIndex =
                    layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                if (itemCount > 0 && lastVisibleItemIndex >= itemCount - 1
                    && items.loadState.append.endOfPaginationReached.not()
                    && items.loadState.append is LoadState.NotLoading
                ) {
                    // 当最后一项可见，并且没有加载状态时
                    Logcat.d("lastVisibleItemIndex:$lastVisibleItemIndex,itemCount:$itemCount,加载更多")
                    items[itemCount - 1] // 触发加载更多
                }
            }
    }
}

/**
 *
 * 滑动到最后触发加载更多
 * @author dingpeihua
 * @date 2025/1/10 16:06
 **/
@Composable
fun <T : Any> LazyGridState.LaunchedLoadMore(items: LazyPagingItems<T>) {
    // 监测滚动状态以自动加载更多
    LaunchedEffect(this) {
        snapshotFlow { layoutInfo }
            .collect { layoutInfo ->
                val itemCount = items.itemCount
                val lastVisibleItemIndex =
                    layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                if (itemCount > 0 && lastVisibleItemIndex >= itemCount - 1
                    && items.loadState.append.endOfPaginationReached.not()
                    && items.loadState.append is LoadState.NotLoading
                ) {
                    // 当最后一项可见，并且没有加载状态时
                    Logcat.d("lastVisibleItemIndex:$lastVisibleItemIndex,itemCount:$itemCount,加载更多")
                    items[itemCount - 1] // 触发加载更多
                }
            }
    }
}