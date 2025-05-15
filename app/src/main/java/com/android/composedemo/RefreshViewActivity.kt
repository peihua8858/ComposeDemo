package com.android.composedemo

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Velocity
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
import com.android.composedemo.compose.MarketListView
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
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.floor

class RefreshViewActivity : BaseActivity() {
    private val mViewModel by viewModels<DemoHomeViewModel>()
    private val mLoading = mutableStateOf(true)
    private val modelState3 = mutableStateListOf<AdapterBean<*>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Compose Demo"
        mViewModel.requestHomeData()
    }

//    @Composable
//    @ExperimentalMaterial3Api
//    override fun ContentView(modifier: Modifier) {
//        var refreshing by remember { mutableStateOf(true) }
//        var loadMoreing by remember { mutableStateOf(true) }
//        val refreshState  by remember { mutableStateOf(SmartSwipeRefreshState()) }
////        val refreshState = rememberSmartSwipeRefreshState()
//        mViewModel.modelState.observe(this) {
//            if (it.isStarting()) {
//                if (!refreshState.isLoading()) {
//                    mLoading.value = true
//                }
//            } else if (it.isSuccess()) {
//                refreshing = false
//                loadMoreing = false
//                mLoading.value = false
//                if (refreshState.refreshFlag == SmartSwipeStateFlag.REFRESHING) {
//                    modelState3.clear()
//                }
//                modelState3.addAll(it.data)
//                refreshState.refreshFlag = SmartSwipeStateFlag.SUCCESS
//                refreshState.loadMoreFlag = SmartSwipeStateFlag.SUCCESS
//            } else if (it.isError()) {
//                if (refreshState.refreshFlag == SmartSwipeStateFlag.REFRESHING) {
//                    modelState3.clear()
//                }
//                refreshState.refreshFlag = SmartSwipeStateFlag.ERROR
//                refreshState.loadMoreFlag = SmartSwipeStateFlag.ERROR
//                mLoading.value = false
//                refreshing = false
//                loadMoreing = false
//            }
//        }
////        Box(modifier.pullToRefresh(refreshing, state = pullRefreshState) {
////            refreshing = true
////            mViewModel.requestHomeData()
////        }) {
////            BindingListView(Modifier)
//////            GlowIndicator(pullRefreshState, 60.dp)
//////            PullToRefreshIndicator2(pullRefreshState, 60.dp, 60.dp)
////            PullToRefreshDefaults.Indicator(state = pullRefreshState, isRefreshing = refreshing,Modifier.align(Alignment.TopCenter))
//////            PullToRefreshIndicator(refreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
////        }
//
////        PullToRefresh(
////            state = com.android.composedemo.widgets.pullrefreshlayout.rememberPullToRefreshState(isRefreshing = refreshing),
////            onRefresh = {
////                refreshing = true
////                mViewModel.requestHomeData()
////            },
////            modifier = modifier
////        ) {
////            if (mLoading.value) {
////                LoadingView(Modifier)
////            } else {
////                BindingListView(Modifier)
////            }
////        }
//
//
////        PullToRefreshOrLoadMore(
////            state = com.android.composedemo.widgets.pullrefreshlayout.rememberPullToRefreshOrLoadMoreState(isRefreshing = refreshing,loadMoreing),
////            onRefresh = {
////                refreshing = true
//////                mViewModel.requestHomeData()
////            },
////            onLoadMore = {
////                loadMoreing = true
////               Logcat.d("onLoadMore>>>>>>")
////            },
////            modifier = modifier
////        ) {
////            if (mLoading.value) {
////                LoadingView(Modifier)
////            } else {
////                BindingListView(Modifier)
////            }
////        }
//
//
//
//        SmartSwipeRefresh(
//            state = refreshState,
//            onRefresh = {
//                refreshState.refreshFlag = SmartSwipeStateFlag.REFRESHING
//                mViewModel.requestHomeData()
//            },
//            onLoadMore = {
//                refreshState.loadMoreFlag = SmartSwipeStateFlag.REFRESHING
//                Logcat.d("onLoadMore>>>>>>loadMoreFlag:"+refreshState.loadMoreFlag + ">>>>>>refreshFlag:" + refreshState.refreshFlag)
//                mViewModel.requestHomeData()
//            },
//            headerIndicator = {
//                MyRefreshHeader(refreshState, false)
//            },
//            footerIndicator = {
//                MyRefreshFooter(refreshState.loadMoreFlag, false)
//            },
//            modifier = modifier
//        ) {
//            if (mLoading.value) {
//                LoadingView(Modifier)
//            } else {
//                BindingListView(Modifier)
//            }
//        }
//
//    }


    @Composable
    @ExperimentalMaterial3Api
    override fun ContentView(modifier: Modifier) {
        val refreshState  by remember { mutableStateOf(SmartSwipeRefreshState()) }
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
        SmartSwipeRefresh(
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
                .nestedScroll(
                    rememberNestedScrollInteropConnection(),
                    dispatcher = NestedScrollDispatcher()
                ), modelState3
        )
    }

}

@Composable
fun rememberNestedScrollInteropConnection(
    hostView: View = LocalView.current
): NestedScrollConnection = remember(hostView) {
    NestedScrollInteropConnection(hostView)
}

class CustomComposeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr), NestedScrollingChild3,
    NestedScrollingChild, NestedScrollingParent3 {
    private val mChildHelper = NestedScrollingChildHelper(this)
    private val mParentHelper = NestedScrollingParentHelper(this)
    private val content = mutableStateOf<(@Composable () -> Unit)?>(null)

    @Suppress("RedundantVisibilityModifier")
    protected override var shouldCreateCompositionOnAttachedToWindow: Boolean = false
        private set

    @Composable
    override fun Content() {
        content.value?.invoke()
    }

    override fun getAccessibilityClassName(): CharSequence {
        return javaClass.name
    }

    /**
     * Set the Jetpack Compose UI content for this view.
     * Initial composition will occur when the view becomes attached to a window or when
     * [createComposition] is called, whichever comes first.
     */
    fun setContent(content: @Composable () -> Unit) {
        shouldCreateCompositionOnAttachedToWindow = true
        this.content.value = content
        if (isAttachedToWindow) {
            createComposition()
        }
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        try {
            mChildHelper.isNestedScrollingEnabled = enabled
        } catch (e: Exception) {
            Log.e(MultiStateView.TAG, "setNestedScrollingEnabled>>>" + e.message)
        }
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return mChildHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return mChildHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        mChildHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return mChildHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int,
        offsetInWindow: IntArray?
    ): Boolean {
        return mChildHelper.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow
        )
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?
    ): Boolean {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedFling(
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

//    fun setScrollViewResId(mScrollViewResId: Int) {
//        this.mScrollViewResId = mScrollViewResId
//    }
//
//    override fun canScrollHorizontally(direction: Int): Boolean {
//        if (mScrollViewResId != NO_ID) {
//            val scrollView = findViewById<View>(mScrollViewResId)
//            if (scrollView != null) {
//                return scrollView.canScrollHorizontally(direction)
//            }
//        }
//        return super.canScrollHorizontally(direction)
//    }
//
//    override fun canScrollVertically(direction: Int): Boolean {
//        if (mScrollViewResId != NO_ID) {
//            val scrollView = findViewById<View>(mScrollViewResId)
//            if (scrollView != null) {
//                return scrollView.canScrollVertically(direction)
//            }
//        }
//        return super.canScrollVertically(direction)
//    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int,
        consumed: IntArray
    ) {
        mChildHelper.dispatchNestedScroll(
            dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
            offsetInWindow, type, consumed
        )
    }

    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        return mChildHelper.startNestedScroll(axes, type)
    }

    override fun stopNestedScroll(type: Int) {
        mChildHelper.stopNestedScroll(type)
    }

    override fun hasNestedScrollingParent(type: Int): Boolean {
        return mChildHelper.hasNestedScrollingParent(type)
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return mChildHelper.dispatchNestedScroll(
            dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
            offsetInWindow, type
        )
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
//        onNestedScrollInternal(dyUnconsumed, type, consumed);
//        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        onNestedScrollInternal(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return (axes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        mParentHelper.onNestedScrollAccepted(child, target, axes, type)
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, type)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        mParentHelper.onStopNestedScroll(target, type)
        stopNestedScroll(type)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
//        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
//        onNestedScrollInternal(dyUnconsumed, type, null);
        onNestedScrollInternal(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, null)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        dispatchNestedPreScroll(dx, dy, consumed, null, type)
    }

    protected fun onNestedScrollInternal(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray?
    ) {
        mChildHelper.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            null,
            type,
            consumed
        )
    }
}

class NestedScrollInteropConnection(
    private val view: View
) : NestedScrollConnection {

    private val nestedScrollChildHelper = NestedScrollingChildHelper(view).apply {
        isNestedScrollingEnabled = true
    }

    private val consumedScrollCache = IntArray(2)

    init {
        // Enables nested scrolling for the root view [AndroidComposeView].
        // Like in Compose, nested scrolling is a default implementation
        ViewCompat.setNestedScrollingEnabled(view, true)
    }

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        // Using the return of startNestedScroll to determine if nested scrolling will happen.
        if (nestedScrollChildHelper.startNestedScroll(
                available.scrollAxes,
                source.toViewType()
            )
        ) {
            // reuse
            consumedScrollCache.fill(0)

            nestedScrollChildHelper.dispatchNestedPreScroll(
                composeToViewOffset(available.x),
                composeToViewOffset(available.y),
                consumedScrollCache,
                null,
                source.toViewType()
            )

            return toOffset(consumedScrollCache, available)
        }

        return Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        // Using the return of startNestedScroll to determine if nested scrolling will happen.
        if (nestedScrollChildHelper.startNestedScroll(
                available.scrollAxes,
                source.toViewType()
            )
        ) {
            consumedScrollCache.fill(0)

            nestedScrollChildHelper.dispatchNestedScroll(
                composeToViewOffset(consumed.x),
                composeToViewOffset(consumed.y),
                composeToViewOffset(available.x),
                composeToViewOffset(available.y),
                null,
                source.toViewType(),
                consumedScrollCache,
            )

            return toOffset(consumedScrollCache, available)
        }

        return Offset.Zero
    }

    override suspend fun onPreFling(available: Velocity): Velocity {

        val result = if (nestedScrollChildHelper.dispatchNestedPreFling(
                available.x.toViewVelocity(),
                available.y.toViewVelocity(),
            )
        ) {
            available
        } else {
            Velocity.Zero
        }

        interruptOngoingScrolls()

        return result
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        val result = if (nestedScrollChildHelper.dispatchNestedFling(
                available.x.toViewVelocity(),
                available.y.toViewVelocity(),
                true
            )
        ) {
            available
        } else {
            Velocity.Zero
        }

        interruptOngoingScrolls()

        return result
    }

    private fun interruptOngoingScrolls() {
        if (nestedScrollChildHelper.hasNestedScrollingParent(TYPE_TOUCH)) {
            nestedScrollChildHelper.stopNestedScroll(TYPE_TOUCH)
        }

        if (nestedScrollChildHelper.hasNestedScrollingParent(TYPE_NON_TOUCH)) {
            nestedScrollChildHelper.stopNestedScroll(TYPE_NON_TOUCH)
        }
    }
}

// Relative ceil for rounding. Ceiling away from zero to avoid missing scrolling deltas to rounding
// issues.
private fun Float.ceilAwayFromZero(): Float = if (this >= 0) ceil(this) else floor(this)

// Compose coordinate system is the opposite of view's system
internal fun composeToViewOffset(offset: Float): Int = offset.ceilAwayFromZero().toInt() * -1

// Compose scrolling sign system is the opposite of view's system
private fun Int.reverseAxis(): Float = this * -1f

private fun Float.toViewVelocity(): Float = this * -1f
private const val ScrollingAxesThreshold = 0.5f
private val Offset.scrollAxes: Int
    get() {
        var axes = ViewCompat.SCROLL_AXIS_NONE
        if (x.absoluteValue >= ScrollingAxesThreshold) {
            axes = axes or ViewCompat.SCROLL_AXIS_HORIZONTAL
        }
        if (y.absoluteValue >= ScrollingAxesThreshold) {
            axes = axes or ViewCompat.SCROLL_AXIS_VERTICAL
        }
        return axes
    }

/**
 * Converts the view world array into compose [Offset] entity. This is bound by the values in the
 * available [Offset] in order to account for rounding errors produced by the Int to Float
 * conversions.
 */
private fun toOffset(consumed: IntArray, available: Offset): Offset {
    val offsetX = if (available.x >= 0) {
        consumed[0].reverseAxis().coerceAtMost(available.x)
    } else {
        consumed[0].reverseAxis().coerceAtLeast(available.x)
    }

    val offsetY = if (available.y >= 0) {
        consumed[1].reverseAxis().coerceAtMost(available.y)
    } else {
        consumed[1].reverseAxis().coerceAtLeast(available.y)
    }

    return Offset(offsetX, offsetY)
}

private fun NestedScrollSource.toViewType(): Int = when (this) {
    NestedScrollSource.Drag -> TYPE_TOUCH
    else -> TYPE_NON_TOUCH
}
