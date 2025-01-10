package com.android.composedemo

import android.content.res.Configuration
import android.os.Bundle
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.android.composedemo.data.bean.AdapterBean
import com.android.composedemo.data.bean.Data
import com.android.composedemo.data.viewmodel.DemoHomeViewModel
import com.android.composedemo.utils.LaunchedLoadMore
import com.android.composedemo.utils.items
import com.android.composedemo.utils.showToast
import com.android.composedemo.widgets.pullrefreshlayout.PullToRefresh
import com.android.composedemo.widgets.pullrefreshlayout.rememberPullToRefreshState

/**
 * use [PullToRefresh]
 */
class PullToRefreshByPagerGridViewActivity : BaseActivity() {
    private val mViewModel by viewModels<DemoHomeViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Compose Demo"
    }

    @Composable
    @ExperimentalMaterial3Api
    override fun ContentView(modifier: Modifier) {
        val items = mViewModel.gridViewData.collectAsLazyPagingItems()
        val refreshing =
            rememberPullToRefreshState(isRefreshing = items.loadState.refresh is LoadState.Loading)
        val lazyListState = rememberLazyGridState()
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
                item(span = { GridItemSpan(maxLineSpan) }) {
                    if (loadMoreState is LoadState.Error) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Error loading more: ${loadMoreState.error.localizedMessage}"
                        )
                    } else if(loadMoreState.endOfPaginationReached.not()) {
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
            // 监测滚动状态以自动加载更多
            lazyListState.LaunchedLoadMore(items)
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

    @Composable
    private fun BindingListView(
        modifier: Modifier,
        data: LazyPagingItems<AdapterBean<*>>,
        state: LazyGridState = rememberLazyGridState(),
        composeLoadMore: LazyGridScope.() -> Unit
    ) {
        val configuration = LocalConfiguration.current
        val isLandScape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        /**
         * 列表左右间距
         */
        val mMarin = dimensionResource(R.dimen.dp_32)

        /**
         * 列表item之间的间距
         */
        val mGap = dimensionResource(R.dimen.dp_8)
        val spanCount =
            if (isLandScape) Constants.LANDSCAPE_COLUMN_COUNT else Constants.PORTRAIT_COLUMN_COUNT
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isLandScape) Constants.LANDSCAPE_COLUMN_COUNT else Constants.PORTRAIT_COLUMN_COUNT),
            modifier = modifier.fillMaxSize(),
            state = state
        ) {
            items(items = data) { index, message ->
                val itemData = message?.data as? Data ?: return@items
                val spanIndex = index % spanCount
                ItemView(
                    modifier = Modifier
                        .wrapContentHeight(Alignment.CenterVertically)
                        .padding(
                            start = (if (isLandScape && spanIndex > 0) mGap else if (spanIndex == 0) mMarin else mMarin / 2),
                            bottom = mGap * 2,
                            end = if (spanIndex == spanCount - 1) mMarin else if (isLandScape) mGap else 0.dp
                        ), item = itemData
                )
            }
            composeLoadMore()
        }
    }

}