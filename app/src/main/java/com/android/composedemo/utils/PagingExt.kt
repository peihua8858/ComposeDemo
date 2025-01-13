package com.android.composedemo.utils

import android.os.Parcel
import android.os.Parcelable
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

private data class PagingPlaceholderKey(private val index: Int) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(index)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<PagingPlaceholderKey> =
            object : Parcelable.Creator<PagingPlaceholderKey> {
                override fun createFromParcel(parcel: Parcel) =
                    PagingPlaceholderKey(parcel.readInt())

                override fun newArray(size: Int) = arrayOfNulls<PagingPlaceholderKey?>(size)
            }
    }
}

/**
 *
 * 渲染到最后一个时触发加载更多
 * @author dingpeihua
 * @date 2025/1/10 16:06
 **/
fun <T : Any> LazyListScope.items(
    items: LazyPagingItems<T>,
    isLastLoadMore: Boolean = false,
    key: ((item: T) -> Any)? = null,
    itemContent: @Composable LazyItemScope.(value: T?) -> Unit
) {
    items(
        count = items.itemCount,
        key = if (key == null) {
            items.itemKey()
        } else { index ->
            val item = items.peek(index)
            if (item == null) {
                PagingPlaceholderKey(index)
            } else {
                key(item)
            }
        }
    ) { index ->
        if (isLastLoadMore) {
            if (index >= items.itemCount - 1) {
                //最后一项触发加载更多
                itemContent(items[index])
            } else {
                itemContent(items.peek(index))
            }
        } else {
            itemContent(items[index])
        }
    }
}

/**
 *
 * 仅渲染到最后一行时触发加载更多
 * @author dingpeihua
 * @date 2025/1/10 16:06
 **/
fun <T : Any> LazyGridScope.items(
    items: LazyPagingItems<T>,
    spanCount: Int = -1,
    key: ((item: T) -> Any)? = null,
    itemContent: @Composable LazyGridItemScope.(index: Int, value: T?) -> Unit
) {
    val itemCount = items.itemCount
    val rowCount = itemCount / spanCount + if (itemCount % spanCount == 0) 0 else 1
    items(
        count = itemCount,
        key = if (key == null) {
            items.itemKey()
        } else { index ->
            val item = items.peek(index)
            if (item == null) {
                PagingPlaceholderKey(index)
            } else {
                key(item)
            }
        }
    ) { index ->
        if (spanCount == -1) {
            itemContent(index, items[index])
        } else {
            val rowIndex = index / spanCount
            if (rowIndex >= rowCount - 1) {
                //最后一项触发加载更多
                itemContent(index, items[index])
            } else {
                itemContent(index, items.peek(index))
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

fun <T : Any> LazyPagingItems<T>.forEach(action: (T) -> Unit) {
    for (index in 0 until itemCount) {
        val item = peek(index)
        if (item != null) {
            action(item)
        }
    }
}