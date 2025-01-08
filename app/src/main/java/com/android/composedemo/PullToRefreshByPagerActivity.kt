package com.android.composedemo

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.activity.viewModels
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.android.composedemo.data.bean.AdapterBean
import com.android.composedemo.data.bean.ModuleBean
import com.android.composedemo.data.viewmodel.DemoHomeViewModel
import com.android.composedemo.widgets.pullrefreshlayout.PullToRefresh
import com.android.composedemo.widgets.pullrefreshlayout.rememberPullToRefreshState

/**
 * use [PullToRefresh]
 */
class PullToRefreshByPagerActivity : BaseActivity() {
    private val mViewModel by viewModels<DemoHomeViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Compose Demo"
    }

    @Composable
    @ExperimentalMaterial3Api
    override fun ContentView(modifier: Modifier) {
        val items = mViewModel.modelState2.collectAsLazyPagingItems()
        val refreshing =
            rememberPullToRefreshState(isRefreshing = items.loadState.refresh is LoadState.Loading)
        PullToRefresh(
            state = refreshing,
            onRefresh = {
                items.refresh()
            },
            modifier = modifier
        ) {
            if (items.loadState.refresh is LoadState.Loading) {
                if (items.itemCount == 0) {
                    LoadingView(Modifier)
                }
            } else if (items.loadState.refresh is LoadState.Error) {
                ErrorView(Modifier)
            }
            BindingListView(Modifier, items) {
                if (items.loadState.append is LoadState.Loading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(16.dp)
                            )
                        }
                    }
                } else if (items.loadState.append is LoadState.Error) {
                    val errorState = items.loadState.append as LoadState.Error
                    item {
                        Text("Error loading more: ${errorState.error.localizedMessage}")
                    }
                }
            }
            // 自动加载下一页逻辑
            val lazyListState = rememberLazyListState()
            LaunchedEffect(lazyListState) {
                snapshotFlow { lazyListState.layoutInfo }
                    .collect { layoutInfo ->
                        val lastVisibleItemIndex =
                            layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                        if (lastVisibleItemIndex >= items.itemCount - 1 && items.loadState.append is LoadState.NotLoading) {
                            // 当最后一项可见，并且没有加载状态时
                            items.retry() // 触发加载更多
                        }
                    }
            }
        }
    }

    @Composable
    private fun ErrorView(modifier: Modifier.Companion) {
        Box(modifier = modifier.fillMaxSize()) {
            val painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(data = R.mipmap.ic_no_data_found)
                    .build()
            )
            Image(
                painter = painter,
                contentDescription = "",
                modifier = modifier
                    .align(Alignment.Center)
            )
        }
    }

    @Composable
    fun LoadingView(modifier: Modifier) {
        Box(modifier = modifier.fillMaxSize()) {
            val painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(data = R.mipmap.ic_loading1)
                    .build()
            )
            val transition = rememberInfiniteTransition(label = "")
            val progress by transition.animateValue(
                0f,
                1f,
                Float.VectorConverter,
                infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1332,
                        easing = LinearEasing
                    )
                ), label = ""
            )
            Image(
                painter = painter,
                contentDescription = "",
                modifier = modifier
                    .align(Alignment.Center)
                    .rotate(progress * 360),
            )
        }
    }

    // content: @Composable () -> Unit
    @Composable
    private fun BindingListView(
        modifier: Modifier,
        data: LazyPagingItems<AdapterBean<*>>,
        composeLoadMore: LazyListScope.() -> Unit
    ) {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(items = data) { message ->
                val itemData = message?.data as? ModuleBean ?: return@items
                when (message.itemType) {
                    Constants.TYPE_TITLE -> {
                        TitleItemView(module = itemData)
                    }

                    Constants.TYPE_ITEM -> {
                        HomeItemView(module = itemData)
                    }

                    Constants.TYPE_BANNER -> {
                        BannerView(module = itemData)
                    }

                    Constants.TYPE_AI_BANNER -> {
                        AiBannerView(module = itemData)
                    }

                    Constants.TYPE_POST_BANNER -> {
                        PostBannerView(module = itemData)
                    }
                }
            }
            composeLoadMore()
        }
    }
}
public fun <T : Any> LazyListScope.items(
    items: LazyPagingItems<T>,
    key: ((item: T) -> Any)? = null,
    itemContent: @Composable LazyItemScope.(value: T?) -> Unit
) {
    items(
        count = items.itemCount,
        key = if (key == null) null else { index ->
            val item = items.peek(index)
            if (item == null) {
                PagingPlaceholderKey(index)
            } else {
                key(item)
            }
        }
    ) { index ->
        itemContent(items[index])
    }
}
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