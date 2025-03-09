package com.android.composedemo

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.android.composedemo.data.bean.AdapterBean
import com.android.composedemo.data.bean.Data
import com.android.composedemo.data.viewmodel.DemoHomeViewModel
import com.android.composedemo.ui.theme.DemoFontFamily
import com.android.composedemo.utils.ellipsize
import com.android.composedemo.utils.items
import com.android.composedemo.utils.showToast
import com.android.composedemo.widgets.pullrefreshlayout.PullToRefresh
import com.android.composedemo.widgets.pullrefreshlayout.rememberPullToRefreshState

/**
 * use [PullToRefresh]
 */
class PullToRefreshByPagerStaggeredGridViewActivity : BaseActivity() {
    private val mViewModel by viewModels<DemoHomeViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Compose Demo"
    }

    @Composable
    override fun ContentView(modifier: Modifier) {
        Paging3StaggeredGridContentView(modifier, mViewModel)
    }
}

@Composable
//@ExperimentalMaterial3Api
fun Paging3StaggeredGridContentView(modifier: Modifier, viewModel: DemoHomeViewModel) {
    val items = viewModel.gridViewData.collectAsLazyPagingItems()
    val refreshing =
        rememberPullToRefreshState(isRefreshing = items.loadState.refresh is LoadState.Loading)
    val lazyListState = rememberLazyStaggeredGridState()
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
            item(span = StaggeredGridItemSpan.FullLine) {
                if (loadMoreState is LoadState.Error) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Error loading more: ${loadMoreState.error.localizedMessage}"
                    )
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
        // 监测滚动状态以自动加载更多
//            lazyListState.LaunchedLoadMore(items)
    }
}

@Composable
private fun BindingListView(
    modifier: Modifier,
    data: LazyPagingItems<AdapterBean<*>>,
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    composeLoadMore: LazyStaggeredGridScope.() -> Unit
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
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(if (isLandScape) Constants.LANDSCAPE_COLUMN_COUNT else Constants.PORTRAIT_COLUMN_COUNT),
        modifier = modifier
            .fillMaxSize()
            .padding(start = mMarin, end = mMarin),
        // item 和 item 之间的纵向间距
        verticalItemSpacing = mGap,
        // item 和 item 之间的横向间距
        horizontalArrangement = Arrangement.spacedBy(mGap),
        state = state
    ) {
        items(items = data, spanCount = spanCount) { index, message ->
            val itemData = message?.data as? Data ?: return@items
            StaggeredItemView(
                modifier = Modifier
                    .wrapContentHeight(Alignment.CenterVertically), item = itemData
            )
        }
        composeLoadMore()
    }
}

@Composable
fun StaggeredItemView(modifier: Modifier, item: Data) {
    Column(modifier = modifier
        .fillMaxWidth()
        .clip(shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_10)))
        .background(color = colorResource(id = R.color.white))
        .clickable {

        }) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.icon)
                .error(R.drawable.ic_avatar_placeholder)
                .placeholder(R.drawable.ic_avatar_placeholder)
                .build(),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_10)))
                .border(
                    width = dimensionResource(id = R.dimen.dp_0_25),
                    color = colorResource(id = R.color.color_1f1f1f_20),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_10)) // 设置圆角边框
                ),
            contentDescription = ""
        )
        val subTitle = item.subTitle
        if (item.title.isEmpty().not()) {
            Text(
                text = item.title.ellipsize(10) ?: "",
                color = colorResource(id = R.color.color_1f1f1f),
                fontSize = dimensionResource(id = R.dimen.sp_16).value.sp,
                fontFamily = DemoFontFamily.NotoSansSc400,
                maxLines = 1,
                modifier = Modifier.padding(
                    start = dimensionResource(id = R.dimen.dp_10),
                    top = dimensionResource(id = R.dimen.dp_10),
                    end = dimensionResource(id = R.dimen.dp_10),
                    bottom = if(subTitle.isEmpty())dimensionResource(id = R.dimen.dp_10) else 0.dp
                )
            )
        }
        if (subTitle.isEmpty().not()) {
            Text(
                text = subTitle.ellipsize(19) ?: "",
                color = colorResource(id = R.color.color_1f1f1f_50),
                fontSize = dimensionResource(id = R.dimen.sp_10).value.sp,
                fontFamily = DemoFontFamily.NotoSansSc400,
                modifier = Modifier.padding(
                    top = dimensionResource(id = R.dimen.dp_3),
                    start = dimensionResource(id = R.dimen.dp_10),
                    end = dimensionResource(id = R.dimen.dp_10),
                    bottom = dimensionResource(id = R.dimen.dp_10)
                ),
                maxLines = 1
            )
        }
    }
}