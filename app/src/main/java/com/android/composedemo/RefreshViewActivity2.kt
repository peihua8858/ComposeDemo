package com.android.composedemo

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChild3
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import androidx.core.view.ViewCompat.TYPE_NON_TOUCH
import androidx.core.view.ViewCompat.TYPE_TOUCH
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.android.composedemo.data.bean.AdapterBean
import com.android.composedemo.data.viewmodel.DemoHomeViewModel
import com.android.composedemo.utils.Logcat
import com.android.composedemo.utils.isError
import com.android.composedemo.utils.isStarting
import com.android.composedemo.utils.isSuccess
import com.android.composedemo.widgets.MultiStateView
import com.android.composedemo.widgets.pullrefreshlayout.MyRefreshFooter
import com.android.composedemo.widgets.pullrefreshlayout.MyRefreshHeader
import com.android.composedemo.widgets.pullrefreshlayout.SmartSwipeRefresh
import com.android.composedemo.widgets.pullrefreshlayout.SmartSwipeRefreshState
import com.android.composedemo.widgets.pullrefreshlayout.SmartSwipeStateFlag
import com.android.composedemo.widgets.pullrefreshlayout.SubComposeSmartSwipeRefresh
import com.android.composedemo.widgets.pullrefreshlayout.rememberSmartSwipeRefreshState
import com.android.composedemo.widgets.refreshlayout.PullRefreshLayout
import com.android.composedemo.widgets.refreshlayout.header.ClassicLoadView
import com.android.composedemo.widgets.refreshlayout.header.ClassicsHeader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.floor

class RefreshViewActivity2 : BaseActivity() {
    private val mViewModel by viewModels<DemoHomeViewModel>()
    private val mLoading = mutableStateOf(true)
    private val modelState3 = mutableStateListOf<AdapterBean<*>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Compose Demo"
        mViewModel.requestHomeData()
    }

    @Composable
    @ExperimentalMaterial3Api
    override fun ContentView(modifier: Modifier) {
        val refreshState by remember { mutableStateOf(SmartSwipeRefreshState()) }
        mViewModel.modelState.observe(this) {
            if (it.isStarting()) {
                if (!refreshState.isLoading()) {
                    mLoading.value = true
                }
            } else if (it.isSuccess()) {
                mLoading.value = false
                if (refreshState.refreshFlag == SmartSwipeStateFlag.REFRESHING) {
                    modelState3.clear()
                }
                modelState3.addAll(it.data)
                refreshState.refreshFlag = SmartSwipeStateFlag.SUCCESS
                refreshState.loadMoreFlag = SmartSwipeStateFlag.SUCCESS
            } else if (it.isError()) {
                if (refreshState.refreshFlag == SmartSwipeStateFlag.REFRESHING) {
                    modelState3.clear()
                }
                refreshState.refreshFlag = SmartSwipeStateFlag.ERROR
                refreshState.loadMoreFlag = SmartSwipeStateFlag.ERROR
                mLoading.value = false
            }
        }
        // 使用 LocalConfiguration 获取当前配置
//        if (mLoading.value) {
//            LoadingView(Modifier)
//        } else {
//            AndroidView(factory = {
//                val parent = PullRefreshLayout(it)
//                val composeView = CustomComposeView(it)
//                composeView.setContent {
//                    BindingListView(Modifier,parent)
//                }
//                parent.addView(composeView)
//                parent.setHeaderView(ClassicsHeader(it))
//                parent.setFooterView(ClassicLoadView(it, parent))
//                parent.setOnRefreshListener(object : PullRefreshLayout.OnRefreshListener {
//                    override fun onRefresh() {
//                        mViewModel.requestHomeData()
//                    }
//
//                    override fun onLoadMore() {
//                        mViewModel.requestHomeData()
//                    }
//                })
//                parent
//            })
//        }


        SmartSwipeRefresh1(
            state = refreshState,
            onRefresh = {
                refreshState.refreshFlag = SmartSwipeStateFlag.REFRESHING
                mViewModel.requestHomeData()
            },
            onLoadMore = {
                refreshState.loadMoreFlag = SmartSwipeStateFlag.REFRESHING
                Logcat.d("onLoadMore>>>>>>loadMoreFlag:"+refreshState.loadMoreFlag + ">>>>>>refreshFlag:" + refreshState.refreshFlag)
                mViewModel.requestHomeData()
            },
            headerIndicator = {
                MyRefreshHeader(refreshState.refreshFlag, false)
            },
            footerIndicator = {
                MyRefreshFooter(refreshState.loadMoreFlag, false)
            },
            modifier = modifier
        ) {
            if (mLoading.value) {
                LoadingView(Modifier)
            } else {
                BindingListView(Modifier)
            }
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
    private fun BindingListView(modifier: Modifier) {
        MarketListView(
            modifier
                .fillMaxSize()
                , modelState3
        )
    }

}
@Composable
fun SmartSwipeRefresh1(
    modifier: Modifier = Modifier,
    state: SmartSwipeRefreshState,
    onRefresh: (suspend () -> Unit)? = null,
    onLoadMore: (suspend () -> Unit)? = null,
    headerIndicator: @Composable (() -> Unit)? = { MyRefreshHeader(flag = state.refreshFlag) },
    footerIndicator: @Composable (() -> Unit)? = { MyRefreshFooter(flag = state.loadMoreFlag) },
    contentScrollState: ScrollableState? = null,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val connection = remember(coroutineScope) {
        SmartSwipeRefreshNestedScrollConnection(state, coroutineScope)
    }

    LaunchedEffect(state.refreshFlag) {
        when (state.refreshFlag) {
            SmartSwipeStateFlag.REFRESHING -> {
                state.animateIsOver = false
                onRefresh?.invoke() // Trigger refresh callback
            }
            SmartSwipeStateFlag.SUCCESS, SmartSwipeStateFlag.ERROR -> {
                delay(500)
                state.animateOffsetTo(0f) // Reset position after refresh
                state.refreshFlag = SmartSwipeStateFlag.IDLE // Reset refresh flag
            }
            else -> {}
        }
    }

    LaunchedEffect(state.loadMoreFlag) {
        when (state.loadMoreFlag) {
            SmartSwipeStateFlag.REFRESHING -> {
                state.animateIsOver = false
                onLoadMore?.invoke() // Trigger load more callback
            }
            SmartSwipeStateFlag.SUCCESS, SmartSwipeStateFlag.ERROR -> {
                delay(500)
                state.animateOffsetTo(0f) // Reset position after loading more
                state.loadMoreFlag = SmartSwipeStateFlag.IDLE // Reset load more flag
            }
            else -> {}
        }
    }

    Box(modifier = modifier.clipToBounds()) {
        SubComposeSmartSwipeRefresh(headerIndicator = headerIndicator, footerIndicator = footerIndicator) { header, footer ->
            state.headerHeight = header.toFloat()
            state.footerHeight = footer.toFloat()

            Box(modifier = Modifier.nestedScroll(connection)) {
                // Content Box
//                val p = with(LocalDensity.current) { state.indicatorOffset.toDp() }
//                val contentModifier = when {
//                    p > 0.dp -> Modifier.padding(top = p)
//                    p < 0.dp && contentScrollState != null -> Modifier.padding(bottom = -p)
//                    p < 0.dp -> Modifier.graphicsLayer { translationY = state.indicatorOffset }
//                    else -> Modifier
//                }
                val contentModifier = Modifier.padding(top = (state.indicatorOffset).coerceAtLeast(0f).dp)
                Box(modifier = contentModifier) {
                    content()
                }
                headerIndicator?.let {
                    Box(modifier = Modifier
                        .align(Alignment.TopCenter)
                        .graphicsLayer {
                            translationY = -header.toFloat() + state.indicatorOffset
                        }) {
                        headerIndicator()
                    }
                }
                footerIndicator?.let {
                    Box(modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .graphicsLayer {
                            translationY = footer.toFloat() + state.indicatorOffset
                        }) {
                        footerIndicator()
                    }
                }
            }
        }
    }
}

private class SmartSwipeRefreshNestedScrollConnection(
    val state: SmartSwipeRefreshState,
    private val coroutineScope: CoroutineScope
) : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        return when {
            state.isLoading() -> Offset.Zero // Don't scroll if loading
            available.y < 0 && state.indicatorOffset > 0 -> {
                // Header can drag
                val canConsumed = (available.y * state.stickinessLevel).coerceAtLeast(0 - state.indicatorOffset)
                scroll(canConsumed)
            }
            available.y > 0 && state.indicatorOffset < 0 -> {
                val canConsumed = (available.y * state.stickinessLevel).coerceAtMost(0 - state.indicatorOffset)
                scroll(canConsumed)
            }
            else -> Offset.Zero
        }
    }

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        return when {
            state.isLoading() -> Offset.Zero
            available.y > 0 && state.enableRefresh && state.headerHeight != 0f -> {
                val canConsumed = if (source == NestedScrollSource.Fling) {
                    (available.y * state.stickinessLevel).coerceAtMost(
                        state.strategyIndicatorHeight(
                            state.flingHeaderIndicatorStrategy
                        ) - state.indicatorOffset
                    )
                } else {
                    (available.y * state.stickinessLevel).coerceAtMost(
                        state.strategyIndicatorHeight(
                            state.dragHeaderIndicatorStrategy
                        ) - state.indicatorOffset
                    )
                }
                scroll(canConsumed)
            }

            available.y < 0 && state.enableLoadMore && state.footerHeight != 0f -> {
                val canConsumed = if (source == NestedScrollSource.Fling) {
                    (available.y * state.stickinessLevel).coerceAtLeast(
                        -state.strategyIndicatorHeight(
                            state.flingFooterIndicatorStrategy
                        ) - state.indicatorOffset
                    )
                } else {
                    (available.y * state.stickinessLevel).coerceAtLeast(
                        -state.strategyIndicatorHeight(
                            state.dragFooterIndicatorStrategy
                        ) - state.indicatorOffset
                    )
                }
                scroll(canConsumed)
            }

            else -> Offset.Zero
        }
    }

    private fun scroll(canConsumed: Float): Offset {
        return if (canConsumed.absoluteValue > 0.5f) {
            coroutineScope.launch {
                state.snapOffsetTo(state.indicatorOffset + canConsumed)
            }
            Offset(0f, canConsumed / state.stickinessLevel)
        } else {
            Offset.Zero
        }
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        if (state.isLoading()) {
            return Velocity.Zero
        }

        state.releaseIsEdge = state.indicatorOffset != 0f

        if (state.indicatorOffset >= state.headerHeight && state.releaseIsEdge) {
            if (state.refreshFlag != SmartSwipeStateFlag.REFRESHING) {
                state.refreshFlag = SmartSwipeStateFlag.REFRESHING
                state.animateOffsetTo(state.headerHeight)
                return available
            }
        }

        if (state.indicatorOffset <= -state.footerHeight && state.releaseIsEdge) {
            if (state.loadMoreFlag != SmartSwipeStateFlag.REFRESHING) {
                state.loadMoreFlag = SmartSwipeStateFlag.REFRESHING
                state.animateOffsetTo(-state.footerHeight)
                return available
            }
        }
        return super.onPreFling(available)
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        if (state.isLoading()) {
            return Velocity.Zero
        }
        // Reset states based on position
        if (state.refreshFlag != SmartSwipeStateFlag.REFRESHING && state.indicatorOffset > 0) {
            state.refreshFlag = SmartSwipeStateFlag.IDLE
            state.animateOffsetTo(0f) // Reset position
        }
        if (state.loadMoreFlag != SmartSwipeStateFlag.REFRESHING && state.indicatorOffset < 0) {
            state.loadMoreFlag = SmartSwipeStateFlag.IDLE
            state.animateOffsetTo(0f) // Reset position
        }
        return super.onPostFling(consumed, available)
    }
}


private class SmartSwipeRefreshNestedScrollConnection2(
    val state: SmartSwipeRefreshState, private val coroutineScope: CoroutineScope
) : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        return when {
            state.isLoading() -> Offset.Zero
            available.y < 0 && state.indicatorOffset > 0 -> {
                // header can drag [state.indicatorOffset, 0]
                val canConsumed =
                    (available.y * state.stickinessLevel).coerceAtLeast(0 - state.indicatorOffset)
                scroll(canConsumed)
            }

            available.y > 0 && state.indicatorOffset < 0 -> {
                // footer can drag [state.indicatorOffset, 0]
                val canConsumed =
                    (available.y * state.stickinessLevel).coerceAtMost(0 - state.indicatorOffset)
                scroll(canConsumed)
            }

            else -> Offset.Zero
        }
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        return when {
            state.isLoading() -> Offset.Zero
            available.y > 0 && state.enableRefresh && state.headerHeight != 0f -> {
                val canConsumed = if (source == NestedScrollSource.Fling) {
                    (available.y * state.stickinessLevel).coerceAtMost(
                        state.strategyIndicatorHeight(
                            state.flingHeaderIndicatorStrategy
                        ) - state.indicatorOffset
                    )
                } else {
                    (available.y * state.stickinessLevel).coerceAtMost(
                        state.strategyIndicatorHeight(
                            state.dragHeaderIndicatorStrategy
                        ) - state.indicatorOffset
                    )
                }
                scroll(canConsumed)
            }

            available.y < 0 && state.enableLoadMore && state.footerHeight != 0f -> {
                val canConsumed = if (source == NestedScrollSource.Fling) {
                    (available.y * state.stickinessLevel).coerceAtLeast(
                        -state.strategyIndicatorHeight(
                            state.flingFooterIndicatorStrategy
                        ) - state.indicatorOffset
                    )
                } else {
                    (available.y * state.stickinessLevel).coerceAtLeast(
                        -state.strategyIndicatorHeight(
                            state.dragFooterIndicatorStrategy
                        ) - state.indicatorOffset
                    )
                }
                scroll(canConsumed)
            }

            else -> Offset.Zero
        }
    }

    private fun scroll(canConsumed: Float): Offset {
        return if (canConsumed.absoluteValue > 0.5f) {
            coroutineScope.launch {
                state.snapOffsetTo(state.indicatorOffset + canConsumed)
            }
            Offset(0f, canConsumed / state.stickinessLevel)
        } else {
            Offset.Zero
        }
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        if (state.isLoading()) {
            return Velocity.Zero
        }

        state.releaseIsEdge = state.indicatorOffset != 0f

        if (state.indicatorOffset >= state.headerHeight && state.releaseIsEdge) {
            if (state.refreshFlag != SmartSwipeStateFlag.REFRESHING) {
                state.refreshFlag = SmartSwipeStateFlag.REFRESHING
                state.animateOffsetTo(state.headerHeight)
                return available
            }
        }

        if (state.indicatorOffset <= -state.footerHeight && state.releaseIsEdge) {
            if (state.loadMoreFlag != SmartSwipeStateFlag.REFRESHING) {
                state.loadMoreFlag = SmartSwipeStateFlag.REFRESHING
                state.animateOffsetTo(-state.footerHeight)
                return available
            }
        }
        return super.onPreFling(available)
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        if (state.isLoading()) {
            return Velocity.Zero
        }
        if (state.refreshFlag != SmartSwipeStateFlag.REFRESHING && state.indicatorOffset > 0) {
            state.refreshFlag = SmartSwipeStateFlag.IDLE
            state.animateOffsetTo(0f)
        }
        if (state.loadMoreFlag != SmartSwipeStateFlag.REFRESHING && state.indicatorOffset < 0) {
            state.loadMoreFlag = SmartSwipeStateFlag.IDLE
            state.animateOffsetTo(0f)
        }
        return super.onPostFling(consumed, available)
    }
}