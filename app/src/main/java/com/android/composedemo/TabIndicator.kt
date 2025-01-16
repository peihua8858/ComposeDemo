package com.android.composedemo

import android.graphics.RectF
import androidx.annotation.Dimension
import androidx.annotation.Dimension.Companion.DP
import androidx.annotation.FloatRange
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.TabIndicatorScope
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.*
import com.android.composedemo.utils.dLog
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Fit a linear 0F - 1F curve to an ease out sine (decelerating) curve.  */
private fun decInterp(@FloatRange(from = 0.0, to = 1.0) fraction: Float): Float {
    // Ease out sine
    return sin((fraction * Math.PI) / 2.0).toFloat()
}

/** Fit a linear 0F - 1F curve to an ease in sine (accelerating) curve.  */
private fun accInterp(@FloatRange(from = 0.0, to = 1.0) fraction: Float): Float {
    // Ease in sine
    return (1.0 - cos((fraction * Math.PI) / 2.0)).toFloat()
}

private val ScrollableTabRowScrollSpec: AnimationSpec<Float> =
    tween(durationMillis = 250, easing = FastOutSlowInEasing)

/** [AnimationSpec] used when an indicator is updating width and/or offset. */
private val TabRowIndicatorSpec: AnimationSpec<Dp> =
    tween(durationMillis = 250, easing = FastOutSlowInEasing)

/**
 * PagerTap 指示器
 * @param  percent  指示器占用整个tab宽度的比例
 * @param  height   指示器的高度
 * @param  color    指示器的颜色
 */
@Composable
fun PagerTabIndicator(
    tabPositions: List<TabPosition>,
    pagerState: PagerState,
    color: Color = MaterialTheme.colorScheme.primary,
    radius: Dp = 20.dp,
    height: Dp = 4.dp,
) {
    val fraction1 = remember { mutableStateOf(0f) }
    Canvas(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 获取当前页面和下一个页面的位置
        val currentPage = minOf(tabPositions.lastIndex, pagerState.currentPage)
        val currentTab = tabPositions[currentPage]
        val nextIndex = minOf(tabPositions.lastIndex, pagerState.currentPage + 1)
        val nextTab = tabPositions.getOrNull(nextIndex)

        // 获取滑动状态的百分比
        val fraction = pagerState.currentPageOffsetFraction
        val pageOffset = pagerState.getOffsetDistanceInPages(nextIndex)
        val contentWidth = currentTab.contentWidth
        // 计算开始指示器的左边界
        val startIndicator = calculateTabViewContentBounds(currentTab, contentWidth)
        // 如果有下一个标签就计算它的内容边界
        val endIndicator = calculateTabViewContentBounds(nextTab, contentWidth)
//        dLog { "startIndicator: $startIndicator, endIndicator: $endIndicator,\nfraction:$fraction,contentWidth:$contentWidth,nextContentWidth:${nextTab?.contentWidth}" }
        // 计算指示器在X轴上的位置
        val x = lerp(
            startIndicator.left,
            if (endIndicator.left == 0f) startIndicator.right else endIndicator.left,
            fraction
        )
        val canvasHeight = size.height
        val tempFraction=(fraction + 0.5f).coerceIn(0f, 1f)
        dLog { "tempFraction:$tempFraction,fraction:$fraction" }
        // 绘制指示器
        drawRoundRect(
            color = color,
            topLeft = Offset(x, canvasHeight - height.toPx()),
            size = Size(
                lerp(startIndicator.width(), endIndicator.width(), abs(tempFraction)),
                height.toPx()
            ),
            cornerRadius = CornerRadius(radius.toPx())
        )
    }
}

fun DrawScope.calculateTabViewContentBounds(
    tabPosition: TabPosition?,
    @Dimension(unit = DP) minWidth: Dp
): RectF {
    if (tabPosition == null) {
        return RectF()
    }
    var tabViewContentWidth = tabPosition.contentWidth.toPx()
    val minWidthPx = minWidth.toPx()

    // 确保指示器宽度至少为最小宽度
    if (tabViewContentWidth < minWidthPx) {
        tabViewContentWidth = minWidthPx
    }

    val tabViewCenterX = (tabPosition.left + tabPosition.right).toPx() / 2
    val contentLeftBounds = tabViewCenterX - (tabViewContentWidth / 2)
    val contentRightBounds = tabViewCenterX + (tabViewContentWidth / 2)

    return RectF(contentLeftBounds, 0f, contentRightBounds, 0f)
}

fun calculateTabViewContentLeft(
    tabPosition: TabPosition?,
    @Dimension(unit = DP) minWidth: Dp
): Dp {
    if (tabPosition == null) {
        return 0.dp
    }
    var tabViewContentWidth = tabPosition.contentWidth
    val minWidthPx = minWidth

    // 确保指示器宽度至少为最小宽度
    if (tabViewContentWidth < minWidthPx) {
        tabViewContentWidth = minWidthPx
    }

    val tabViewCenterX = (tabPosition.left + tabPosition.right) / 2
    val contentLeftBounds = tabViewCenterX - (tabViewContentWidth / 2)
    return contentLeftBounds
}

fun calculateTabViewContentRight(
    tabPosition: TabPosition?,
    @Dimension(unit = DP) minWidth: Dp
): Dp {
    if (tabPosition == null) {
        return 0.dp
    }
    var tabViewContentWidth = tabPosition.contentWidth
    val minWidthPx = minWidth

    // 确保指示器宽度至少为最小宽度
    if (tabViewContentWidth < minWidthPx) {
        tabViewContentWidth = minWidthPx
    }

    val tabViewCenterX = (tabPosition.left + tabPosition.right) / 2
    val contentRightBounds = tabViewCenterX + (tabViewContentWidth / 2)
    return contentRightBounds
}

fun Modifier.customTabIndicatorOffset(
    currentTabPosition: TabPosition,
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "customTabIndicatorOffset"
        value = currentTabPosition
    }
) {
    val currentTabWidth by animateDpAsState(
        targetValue = currentTabPosition.contentWidth,
        animationSpec = TabRowIndicatorSpec
    )

    val indicatorOffset by animateDpAsState(
        targetValue = ((currentTabPosition.left + currentTabPosition.right - currentTabPosition.contentWidth) / 2),
        animationSpec = TabRowIndicatorSpec
    )
    fillMaxWidth()
        .wrapContentSize(Alignment.BottomStart)
        .offset { IntOffset(x = indicatorOffset.roundToPx(), y = 0) }
        .width(currentTabWidth)
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabIndicatorScope.FancyAnimatedIndicatorWithModifier(index: Int) {
    val colors =
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary,
        )
    var startAnimatable by remember { mutableStateOf<Animatable<Dp, AnimationVector1D>?>(null) }
    var endAnimatable by remember { mutableStateOf<Animatable<Dp, AnimationVector1D>?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val indicatorColor: Color by animateColorAsState(colors[index % colors.size], label = "")

    Box(
        Modifier.tabIndicatorLayout {
                measurable: Measurable,
                constraints: Constraints,
                tabPositions: List<TabPosition> ->
            val newStart = tabPositions[index].left
            val newEnd = tabPositions[index].right
            val startAnim =
                startAnimatable
                    ?: Animatable(newStart, Dp.VectorConverter).also { startAnimatable = it }

            val endAnim =
                endAnimatable
                    ?: Animatable(newEnd, Dp.VectorConverter).also { endAnimatable = it }

            if (endAnim.targetValue != newEnd) {
                coroutineScope.launch {
                    endAnim.animateTo(
                        newEnd,
                        animationSpec =
                        if (endAnim.targetValue < newEnd) {
                            spring(dampingRatio = 1f, stiffness = 1000f)
                        } else {
                            spring(dampingRatio = 1f, stiffness = 50f)
                        }
                    )
                }
            }

            if (startAnim.targetValue != newStart) {
                coroutineScope.launch {
                    startAnim.animateTo(
                        newStart,
                        animationSpec =
                        // Handle directionality here, if we are moving to the right, we
                        // want the right side of the indicator to move faster, if we are
                        // moving to the left, we want the left side to move faster.
                        if (startAnim.targetValue < newStart) {
                            spring(dampingRatio = 1f, stiffness = 50f)
                        } else {
                            spring(dampingRatio = 1f, stiffness = 1000f)
                        }
                    )
                }
            }

            val indicatorEnd = endAnim.value.roundToPx()
            val indicatorStart = startAnim.value.roundToPx()

            // Apply an offset from the start to correctly position the indicator around the tab
            val placeable =
                measurable.measure(
                    constraints.copy(
                        maxWidth = indicatorEnd - indicatorStart,
                        minWidth = indicatorEnd - indicatorStart,
                    )
                )
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeable.place(indicatorStart, 0)
            }
        }
            .padding(5.dp)
            .fillMaxSize()
            .drawWithContent {
                drawRoundRect(
                    color = indicatorColor,
                    cornerRadius = CornerRadius(5.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
    )
}