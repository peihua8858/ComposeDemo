package com.android.composedemo.compose.components

import android.graphics.RectF
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabIndicatorScope
import androidx.compose.material3.TabPosition
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
import kotlin.math.absoluteValue
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

fun Float.format(digits: Int):Float = "%.${digits}f".format(this).toFloat()
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
    Canvas(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 获取当前页面和下一个页面的位置
        //此处不能使用pagerState.currentPage，因为滑动到中间过度到下一页时这个值会变化
        val settledPage = pagerState.settledPage
        val currentTab = tabPositions[settledPage]
        val targetPage = pagerState.targetPage
        val targetTab = tabPositions[targetPage]
        val startIndicator = calculateTabViewContentBounds(currentTab)
        val fraction = pagerState.getOffsetDistanceInPages(settledPage).absoluteValue
        val targetIndicator = calculateTabViewContentBounds(targetTab)
        val targetIndicatorOffset = lerp(startIndicator.left, targetIndicator.left, fraction)
        val indicatorWidth = lerp(startIndicator.width(), targetIndicator.width(), fraction)
        dLog { "fraction:$fraction,targetIndicatorOffset:$targetIndicatorOffset,indicatorWidth:$indicatorWidth" }
        val canvasHeight = size.height
        // 绘制指示器
        drawRoundRect(
            color = color,
            topLeft = Offset(targetIndicatorOffset, canvasHeight - height.toPx()),
            size = Size(
                indicatorWidth,
                height.toPx()
            ),
            cornerRadius = CornerRadius(radius.toPx())
        )
    }
}

fun DrawScope.calculateTabViewContentBounds(
    tabPosition: TabPosition?,
): RectF {
    if (tabPosition == null) {
        return RectF()
    }
    val tabViewContentWidth = tabPosition.contentWidth.toPx()
    val tabViewCenterX = (tabPosition.left + tabPosition.right).toPx() / 2
    val contentLeftBounds = tabViewCenterX - (tabViewContentWidth / 2)
    val contentRightBounds = tabViewCenterX + (tabViewContentWidth / 2)
    return RectF(contentLeftBounds, 0f, contentRightBounds, 0f)
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
        Modifier
            .tabIndicatorLayout { measurable: Measurable,
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


//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun PagerTabIndicator(
//    tabPositions: List<TabPosition>, // TabPosition列表
//    pagerState: PagerState, // PageState用于获取当前页和切换进度
//    color: Color = MaterialTheme.colorScheme.primary, // 指示器颜色
//    @FloatRange(from = 0.0, to = 1.0) percent: Float = 1f // 指示器宽度占Tab宽度的比例
//) {
//
//    // 获取当前选中的页和切换进度
//    val currentPage by rememberUpdatedState(newValue = pagerState.currentPage)
//    val fraction by rememberUpdatedState(newValue = pagerState.currentPageOffsetFraction)
//
//    // 获取当前tab、前一个tab、后一个tab的TabPosition
//    val currentTab = tabPositions[currentPage]
//    val previousTab = tabPositions.getOrNull(currentPage - 1)
//    val nextTab = tabPositions.getOrNull(currentPage + 1)
//
//    Canvas(
//        modifier = Modifier.fillMaxSize(), // 充满TabRow的大小
//        onDraw = {
//            // 计算指示器宽度
//            val indicatorWidth = currentTab.width.toPx() * percent
//
//            // 计算指示器x轴起始位置
//            val indicatorOffset = if (fraction > 0 && nextTab != null) {
//                // 正在向右滑动到下一页,在当前tab和下一tab之间插值
//                lerp(currentTab.left, nextTab.left, fraction).toPx()
//            } else if (fraction < 0 && previousTab != null) {
//                // 正在向左滑动到上一页,在当前tab和上一tab之间插值
//                lerp(currentTab.left, previousTab.left, -fraction).toPx()
//            } else {
//                // 未在滑动,使用当前tab的left
//                currentTab.left.toPx()
//            }
//
//            // 绘制指示器
//            val canvasHeight = size.height // 高度为整个Canvas高度
//            drawRoundRect(
//                color = color,
//                topLeft = Offset( // 设置圆角矩形的起始点
//                    indicatorOffset + (currentTab.width.toPx() * (1 - percent) / 2),
//                    0F
//                ),
//                size = Size( // 设置宽高
//                    indicatorWidth + indicatorWidth * abs(fraction),
//                    canvasHeight
//                ),
//                cornerRadius = CornerRadius(26.dp.toPx()) // 圆角半径
//            )
//        }
//    )
//}
