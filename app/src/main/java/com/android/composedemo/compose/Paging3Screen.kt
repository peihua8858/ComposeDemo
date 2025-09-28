package com.android.composedemo.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.android.composedemo.Constants
import com.android.composedemo.R
import com.android.composedemo.compose.components.LoadingView
import com.android.composedemo.data.bean.AdapterBean
import com.android.composedemo.data.bean.ModuleBean
import com.android.composedemo.data.viewmodel.DemoHomeViewModel
import com.android.composedemo.utils.items
import com.android.composedemo.utils.showToast
import com.android.composedemo.widgets.pullrefreshlayout.PullToRefresh
import com.android.composedemo.widgets.pullrefreshlayout.rememberPullToRefreshState


@Composable
//@ExperimentalMaterial3Api
fun Paging3Screen(modifier: Modifier, viewModel: DemoHomeViewModel) {
    val items = viewModel.data.collectAsLazyPagingItems()
    val refreshing =
        rememberPullToRefreshState(isRefreshing = items.loadState.refresh is LoadState.Loading)
    val lazyListState = rememberLazyListState()
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
            if (items.itemCount == 0) {
                ErrorView(Modifier)
            } else {
                showToast("刷新失败")
            }
        }
        val loadMoreState = items.loadState.append
        BindingListView(Modifier, items, lazyListState) {
            item {
                if (loadMoreState is LoadState.Error) {
                    Text("Error loading more: ${loadMoreState.error.localizedMessage}")
                } else if (loadMoreState.endOfPaginationReached.not()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(dimensionResource(R.dimen.dp_16))
                        )
                    }
                }
            }
        }
        // 自动加载下一页逻辑
//            LaunchedEffect(lazyListState) {
//                snapshotFlow { lazyListState.layoutInfo }
//                    .collect { layoutInfo ->
//                        val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
//                        // 确保到达最后一项并且没有加载状态
//                        if (lastVisibleItemIndex >= items.itemCount - 1 &&
//                            items.loadState.append is LoadState.NotLoading &&
//                            items.loadState.refresh !is LoadState.Loading
//                        ) {
//                            Logcat.d("lastVisibleItemIndex:$lastVisibleItemIndex,加载更多")
//                           items.retry() // 请求下一页
//                            Logcat.d("PagingDebug", "Append LoadState: ${items.loadState.append}")
//                        }
//                    }
//            }
//            LaunchedEffect(lazyListState) {
//                snapshotFlow { lazyListState.layoutInfo }
//                    .collect { layoutInfo ->
//                        val lastVisibleItemIndex =
//                            layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
//                        if (lastVisibleItemIndex >= items.itemCount - 1 && items.loadState.append is LoadState.NotLoading) {
//                            // 当最后一项可见，并且没有加载状态时
//                            items.retry() // 触发加载更多
//                        }
//                    }
//            }
    }
}


//@Composable
//fun LoadingView(modifier: Modifier) {
//    Box(modifier = modifier.fillMaxSize()) {
//        val painter = rememberAsyncImagePainter(
//            ImageRequest.Builder(LocalContext.current)
//                .data(data = R.mipmap.ic_loading1)
//                .build()
//        )
//        val transition = rememberInfiniteTransition(label = "")
//        val progress by transition.animateValue(
//            0f,
//            1f,
//            Float.VectorConverter,
//            infiniteRepeatable(
//                animation = tween(
//                    durationMillis = 1332,
//                    easing = LinearEasing
//                )
//            ), label = ""
//        )
//        Image(
//            painter = painter,
//            contentDescription = "",
//            modifier = modifier
//                .align(Alignment.Center)
//                .rotate(progress * 360),
//        )
//    }
//}

@Composable
private fun BindingListView(
    modifier: Modifier,
    data: LazyPagingItems<AdapterBean<*>>,
    state: LazyListState = rememberLazyListState(),
    composeLoadMore: LazyListScope.() -> Unit
) {
    LazyColumn(modifier = modifier.fillMaxSize(), state = state) {
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