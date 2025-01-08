package com.android.composedemo.widgets.refreshlayout


import androidx.annotation.FloatRange
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.android.composedemo.R
import com.android.composedemo.utils.Logcat
import com.android.composedemo.widgets.pullrefreshlayout.PullToRefresh
import com.android.composedemo.widgets.pullrefreshlayout.PullToRefreshIndicator
import com.android.composedemo.widgets.pullrefreshlayout.rememberPullToRefreshState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * Creates a [PullToRefreshState] that is remembered across compositions.
 *
 * Changes to [isRefreshing] will result in the [PullToRefreshState] being updated.
 *
 * @param isRefreshing the value for [PullToRefreshState.isRefreshing]
 */
@Composable
fun rememberPullToRefreshState(
    isRefreshing: Boolean,
    isLoadingMore: Boolean
): PullToRefreshState = remember {
    PullToRefreshState(isRefreshing, isLoadingMore)
}.apply {
    this.isRefreshing = isRefreshing
    this.isLoadingMore = isLoadingMore
}

/**
 * A state object that can be hoisted to control and observe changes for [PullToRefresh].
 *
 * In most cases, this will be created via [rememberPullToRefreshState].
 *
 * @param isRefreshing the initial value for [PullToRefreshState.isRefreshing]
 * @param isLoadingMore the initial value for [PullToRefreshState.isLoadingMore]
 */
@Stable
class PullToRefreshState(
    isRefreshing: Boolean,
    isLoadingMore: Boolean
) {
    private val _contentOffset = Animatable(0f)

    /**
     * Whether this [PullToRefreshState] is currently refreshing or not.
     */
    var isRefreshing by mutableStateOf(isRefreshing)

    /**
     * Whether this [PullToRefreshState] is currently loading more or not.
     */
    var isLoadingMore by mutableStateOf(isLoadingMore)

    /**
     * Whether a drag is currently in progress.
     */
    var isPullInProgress: Boolean by mutableStateOf(false)
        internal set

    /**
     * Whether this [PullToRefreshState] is currently animating to its resting position or not.
     */
    val isResting: Boolean get() = _contentOffset.isRunning && !isRefreshing && !isLoadingMore

    /**
     * The current offset for the content, in pixels.
     */
    val contentOffset: Float get() = _contentOffset.value

    internal suspend fun animateOffsetTo(offset: Float) {
        _contentOffset.animateTo(offset)
    }

    /**
     * Dispatch scroll delta in pixels from touch events.
     */
    internal suspend fun dispatchScrollDelta(delta: Float) {
        _contentOffset.snapTo(_contentOffset.value + delta)
    }
}

private class PullToRefreshNestedScrollConnection(
    private val state: PullToRefreshState,
    private val coroutineScope: CoroutineScope,
    private val onRefresh: () -> Unit,
    private val onLoadMore: () -> Unit
) : NestedScrollConnection {
    var enabled: Boolean = false
    var refreshTrigger: Float = 0f
    var loadMoreTrigger: Float = 0f
    var dragMultiplier: Float = 0f

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        Logcat.d("available:$available, source:$source")
        return when {
            // If pulling isn't enabled, return zero
            !enabled -> Offset.Zero
            // If we're refreshing, consume y
            state.isRefreshing -> Offset(0f, available.y)
            state.isLoadingMore -> Offset(0f, -available.y)
            // If the user is pulling up, handle it for loading more
            source == NestedScrollSource.UserInput && available.y < 0 -> dragUpToRefresh(available)
            // If the user is pulling down and wants to load more
//            source == NestedScrollSource.Drag && available.y > 0 -> dragDownLoadMore(available)
            else -> Offset.Zero
        }
    }

    /**
     * 手指向上拉时，
     * 如果当前状态触发的是刷新则内容区向上偏移，直到刷新视图不可见为止
     * 如果当前状态触发的是加载更多则内容区向上偏移，直到触发加载更多
     */
    private fun dragUpToRefresh(available: Offset): Offset {
        state.isPullInProgress = true

        val newOffset = (available.y * dragMultiplier + state.contentOffset).coerceAtLeast(0f)
        val dragConsumed = newOffset - state.contentOffset
        Logcat.d("available:$available, newOffset:$newOffset,dragConsumed:$dragConsumed," +
                "state.contentOffset:${state.contentOffset},dragMultiplier:$dragMultiplier")
        return if (dragConsumed.absoluteValue >= 0.5f) {
            coroutineScope.launch {
                state.dispatchScrollDelta(dragConsumed)
            }
            Offset(x = 0f, y = available.y)
        } else {
            Offset.Zero
        }
    }

    /**
     * 手指向上拉时，
     * 如果当前状态触发的是刷新则内容区向上偏移，直到刷新视图不可见为止
     * 如果当前状态触发的是加载更多则内容区向上偏移，直到触发加载更多
     */
    private fun dragUpToLoadMore(available: Offset): Offset {
        state.isPullInProgress = true
        coroutineScope.launch {
            state.dispatchScrollDelta(available.y * dragMultiplier)
        }
        return Offset(x = 0f, y = available.y)
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        Logcat.d("consumed:$consumed, available:$available, source:$source")
        return when {
            // If pulling isn't enabled, return zero
            !enabled -> Offset.Zero
            // If we're refreshing, return zero
            state.isRefreshing -> Offset.Zero
            state.isLoadingMore -> Offset.Zero
            // If the user is pulling down and there's y remaining, handle it
            source == NestedScrollSource.UserInput && available.y > 0 -> dragDownRefresh(available)
            source == NestedScrollSource.UserInput && available.y < 0 -> dragUpToLoadMore(available)
            else -> Offset.Zero
        }
    }

    /**
     * 手指向下拉时，
     * 如果当前状态触发的是刷新则内容区向下偏移，直到触发刷新
     * 如果当前状态触发的是加载更多则内容区向下偏移，直到加载更多视图不可见为止
     */
    private fun dragDownRefresh(available: Offset): Offset {
        state.isPullInProgress = true
        coroutineScope.launch {
            state.dispatchScrollDelta(available.y * dragMultiplier)
        }
        return Offset(x = 0f, y = available.y)
    }

    /**
     * 手指向下拉时，
     * 如果当前状态触发的是刷新则内容区向下偏移，直到触发刷新
     * 如果当前状态触发的是加载更多则内容区向下偏移，直到加载更多视图不可见为止
     */
    private fun dragDownLoadMore(available: Offset): Offset {
        state.isPullInProgress = true

        val newOffset = (available.y * dragMultiplier + state.contentOffset).coerceAtLeast(0f)
        val dragConsumed = newOffset - state.contentOffset

        return if (dragConsumed.absoluteValue >= 0.5f) {
            coroutineScope.launch {
                state.dispatchScrollDelta(dragConsumed)
            }
            Offset(x = 0f, y = available.y)
        } else {
            Offset.Zero
        }
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        Logcat.d(
            "available:$available,state.contentOffset:${state.contentOffset}," +
                    "state.isRefreshing:${state.isRefreshing},refreshTrigger:$refreshTrigger," +
                    "loadMoreTrigger:$loadMoreTrigger,state.isPullInProgress:${state.isPullInProgress}"
        )
        // If we're dragging, not currently refreshing and scrolled
        // past the trigger point, refresh!
        if (!state.isRefreshing && state.contentOffset >= refreshTrigger) {
            onRefresh()
        } else if (!state.isLoadingMore && state.contentOffset <= -loadMoreTrigger) {
            onLoadMore()
        }

        // Reset the drag in progress state
        state.isPullInProgress = false

        return when {
            // If we're pulling/refreshing/resting, consume velocity
            state.contentOffset != 0f -> available
            // Allow the scrolling layout to fling
            else -> Velocity.Zero
        }
    }
}

/**
 * A layout which implements the pull-to-refresh pattern, allowing the user to refresh content via
 * a vertical pull gesture.
 *
 * This layout requires its content to be scrollable so that it receives vertical swipe events.
 * The scrollable content does not need to be a direct descendant though. Layouts such as
 * [androidx.compose.foundation.lazy.LazyColumn] are automatically scrollable, but others such as
 * [androidx.compose.foundation.layout.Column] require you to provide the
 * [androidx.compose.foundation.verticalScroll] modifier to that content.
 *
 * Apps should provide a [onRefresh] block to be notified each time a swipe to refresh gesture
 * is completed. That block is responsible for updating the [state] as appropriately,
 * typically by setting [PullToRefreshState.isRefreshing] to `true` once a 'refresh' has been
 * started. Once a refresh has completed, the app should then set
 * [PullToRefreshState.isRefreshing] to `false`.
 *
 * If an app wishes to show the progress animation outside of a swipe gesture, it can
 * set [PullToRefreshState.isRefreshing] as required.
 *
 * @param state the state object to be used to control or observe the [PullToRefresh] state.
 * @param onRefresh Lambda which is invoked when a pull to refresh gesture is completed.
 * @param modifier The modifier to apply to this layout.
 * @param enabled Whether the the layout should react to pull gestures or not.
 * @param dragMultiplier Multiplier that will be applied to pull gestures.
 * @param refreshTriggerDistance The minimum pull distance which would trigger a refresh.
 * @param refreshingOffset The content's offset when refreshing. By default this will equal to [refreshTriggerDistance].
 * @param indicatorPadding Content padding for the indicator, to inset the indicator in if required.
 * @param indicator the indicator that represents the current state. By default this will use a [PullToRefreshIndicator].
 * @param clipIndicatorToPadding Whether to clip the indicator to [indicatorPadding]. If false is provided the indicator will be clipped to the [content] bounds. Defaults to true.
 * @param content The content containing a scroll composable.
 */
@Composable
fun PullToRefresh(
    state: PullToRefreshState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @FloatRange(from = 0.0, to = 1.0) dragMultiplier: Float = 0.5f,
    refreshTriggerDistance: Dp = 60.dp,
    refreshingOffset: Dp = refreshTriggerDistance,
    loadMoreTriggerDistance: Dp = 60.dp,
    loadingMoreOffset: Dp = loadMoreTriggerDistance,
    indicatorPadding: PaddingValues = PaddingValues(0.dp),
    refreshIndicator: @Composable (state: PullToRefreshState, refreshTrigger: Dp, refreshingOffset: Dp) -> Unit = { s, trigger, offset ->
        PullToRefreshIndicator(s, trigger, offset)
    },
    loadMoreIndicator: @Composable (state: PullToRefreshState, loadMoreTrigger: Dp, loadingMoreOffset: Dp) -> Unit = { s, trigger, offset ->
        PullToLoadMoreIndicator(s, trigger, offset)
    },
    clipIndicatorToPadding: Boolean = true,
    content: @Composable () -> Unit,
) {
    require(dragMultiplier in 0f..1f) { "dragMultiplier must be >= 0 and <= 1" }
    require(refreshingOffset <= refreshTriggerDistance) { "refreshingOffset must be <= refreshTriggerDistance" }
    require(loadingMoreOffset <= loadMoreTriggerDistance) { "loadingMoreOffset must be <= loadMoreTriggerDistance" }

    val coroutineScope = rememberCoroutineScope()
    val updatedOnRefresh by rememberUpdatedState(onRefresh)
    val updatedOnLoadMore by rememberUpdatedState(onLoadMore)

    val refreshingOffsetPx = with(LocalDensity.current) { refreshingOffset.toPx() }
    val loadingMoreOffsetPx = with(LocalDensity.current) { loadingMoreOffset.toPx() }

    LaunchedEffect(Unit) {
        snapshotFlow { !state.isPullInProgress to state.isRefreshing }
            .distinctUntilChanged()
            .filter { it.first }
            .map { it.second }
            .collectLatest { isRefreshing ->
                state.animateOffsetTo(if (isRefreshing) refreshingOffsetPx else 0f)
            }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { !state.isPullInProgress to state.isLoadingMore }
            .distinctUntilChanged()
            .filter { it.first }
            .map { it.second }
            .collectLatest { isLoadingMore ->
                state.animateOffsetTo(if (isLoadingMore) loadingMoreOffsetPx else 0f)
            }
    }

    // Our nested scroll connection, which updates our state.
    val nestedScrollConnection = remember(state, coroutineScope) {
        PullToRefreshNestedScrollConnection(state, coroutineScope, {
            // On refresh, re-dispatch to the update onRefresh block
            updatedOnRefresh()
        }) {
            updatedOnLoadMore()
        }
    }.apply {
        this.enabled = enabled
        this.dragMultiplier = dragMultiplier
        this.refreshTrigger = with(LocalDensity.current) { refreshTriggerDistance.toPx() }
        this.loadMoreTrigger = with(LocalDensity.current) { loadMoreTriggerDistance.toPx() }
    }

    Box(modifier.nestedScroll(connection = nestedScrollConnection)) {
        Box(modifier = Modifier
            .offset {
                IntOffset(0, state.contentOffset.toInt())
            }
        ) {
            content()
        }
        Box(
            Modifier
                // If we're not clipping to the padding, we use clipToBounds() before the padding()
                // modifier.
                .let { if (!clipIndicatorToPadding) it.clipToBounds() else it }
                .padding(indicatorPadding)
                .matchParentSize()
                // Else, if we're are clipping to the padding, we use clipToBounds() after
                // the padding() modifier.
                .let { if (clipIndicatorToPadding) it.clipToBounds() else it }
        ) {
            Box(Modifier.align(Alignment.TopCenter)) {
                refreshIndicator(state, refreshTriggerDistance, refreshingOffset)
            }
            Box(Modifier.align(Alignment.BottomCenter)) {
                loadMoreIndicator(state, loadMoreTriggerDistance, loadingMoreOffset)
            }
        }


    }
}


@Composable
fun PullToRefreshIndicator(
    state: PullToRefreshState,
    refreshTriggerDistance: Dp,
    refreshingOffset: Dp,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default.copy(color = Color.Gray, fontSize = 14.sp),
) {
    val refreshTriggerPx = with(LocalDensity.current) { refreshTriggerDistance.toPx() }
    val refreshingOffsetPx = with(LocalDensity.current) { refreshingOffset.toPx() }
    val indicatorHeight = 48.dp
    val indicatorHeightPx = with(LocalDensity.current) { indicatorHeight.toPx() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(indicatorHeight)
            .padding(end = 26.dp)
            .graphicsLayer {
                translationY = state.contentOffset - (refreshingOffsetPx + indicatorHeightPx) / 2
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state.isRefreshing) {
            val transition = rememberInfiniteTransition()
            val progress by transition.animateValue(
                0f,
                1f,
                Float.VectorConverter,
                infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1332, // 1 and 1/3 second
                        easing = LinearEasing
                    )
                )
            )
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = R.mipmap.ic_loading1)
                        .build()
                ),
                contentDescription = "refreshing",
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(18.dp)
                    .rotate(progress * 360),
                colorFilter = ColorFilter.tint(textStyle.color)
            )
        } else {
            val progress = ((state.contentOffset - refreshTriggerPx / 2) / refreshTriggerPx * 2)
                .coerceIn(0f, 1f)
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = R.mipmap.ic_loading1)
                        .build()
                ),
                contentDescription = "pull to refresh",
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(18.dp)
                    .rotate(progress * 180),
                colorFilter = ColorFilter.tint(textStyle.color)
            )
        }
        BasicText(
            text = stringResource(
                when {
                    state.isPullInProgress && state.contentOffset >= refreshTriggerPx -> R.string.cptr_release_to_refresh
                    state.isRefreshing -> R.string.cptr_refreshing
                    else -> R.string.cptr_pull_to_refresh
                }
            ),
            style = textStyle
        )
    }
}

@Composable
fun PullToLoadMoreIndicator(
    state: PullToRefreshState,
    loadMoreTriggerDistance: Dp,
    loadingMoreOffset: Dp,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default.copy(color = Color.Gray, fontSize = 14.sp),
) {
    val loadMoreTriggerPx = with(LocalDensity.current) { loadMoreTriggerDistance.toPx() }
    val loadingMoreOffsetPx = with(LocalDensity.current) { loadingMoreOffset.toPx() }
    val indicatorHeight = 48.dp
    val indicatorHeightPx = with(LocalDensity.current) { indicatorHeight.toPx() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(indicatorHeight)
            .padding(end = 26.dp)
            .graphicsLayer {
                translationY = state.contentOffset + (loadingMoreOffsetPx + indicatorHeightPx) / 2
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state.isLoadingMore) {
            val transition = rememberInfiniteTransition()
            val progress by transition.animateValue(
                0f,
                1f,
                Float.VectorConverter,
                infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1332, // 1 and 1/3 second
                        easing = LinearEasing
                    )
                )
            )
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = R.mipmap.ic_loading1)
                        .build()
                ),
                contentDescription = "loading more",
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(18.dp)
                    .rotate(progress * 360),
                colorFilter = ColorFilter.tint(textStyle.color)
            )
        } else {
            val progress = ((state.contentOffset + loadMoreTriggerPx / 2) / loadMoreTriggerPx * 2)
                .coerceIn(0f, 1f)
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = R.mipmap.ic_loading1)
                        .build()
                ),
                contentDescription = "pull to load more",
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(18.dp)
                    .rotate(progress * 180),
                colorFilter = ColorFilter.tint(textStyle.color)
            )
        }
        BasicText(
            text = stringResource(
                when {
                    state.isPullInProgress && state.contentOffset <= loadMoreTriggerPx -> R.string.cptr_release_to_load
                    state.isLoadingMore -> R.string.cptr_loading
                    else -> R.string.cptr_pull_to_load
                }
            ),
            style = textStyle
        )
    }
}
