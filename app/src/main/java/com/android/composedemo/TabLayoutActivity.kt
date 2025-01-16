package com.android.composedemo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.android.composedemo.ui.theme.DemoFontFamily
import com.android.composedemo.utils.toPx
import kotlinx.coroutines.launch

/**
 *
 * tab+pager 基础类
 * @author dingpeihua
 * @date 2025/1/15 14:45
 **/
abstract class TabLayoutActivity<T : Any> : BaseActivity() {
    protected val mTabs = mutableStateListOf<T>()

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    final override fun ContentView(modifier: Modifier) {
        val coroutineScope = rememberCoroutineScope()
        val pagerState = rememberPagerState(
            initialPage = 0,
            initialPageOffsetFraction = 0f
        ) { mTabs.size }
        val selectedTabIndex = pagerState.currentPage
        Column(modifier = modifier) {
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTabIndex, modifier = Modifier,
                indicator = @Composable {
//                    TabRowDefaults.PrimaryIndicator(
//                        modifier = Modifier.tabIndicatorOffset(selectedTabIndex, matchContentSize = true),
//                        width = Dp.Unspecified,
//                    )
                    FancyAnimatedIndicatorWithModifier(index = selectedTabIndex)
//                    PagerTabIndicator(tabPositions = tabPositions, pagerState = pagerState)
//                    Box(
//                        Modifier
//                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
//                            .fillMaxWidth()
//                            .fillMaxHeight()
//                            .background(
//                                brush = Brush.verticalGradient(
//                                    colors = listOf(
//                                        Color(0XFF00B2FF),
//                                        Color(0XFF00F0FF)
//                                    )
//                                ),
//                                shape = RoundedCornerShape(16.dp)
//                            )
//                    )
//                    TabRowDefaults.SecondaryIndicator(
//                        Modifier
//                            .height(dimensionResource(id = R.dimen.dp_3))
//                            .background(     brush = Brush.verticalGradient(
//                                colors = listOf(
//                                    Color(0XFF00B2FF),
//                                    Color(0XFF00F0FF)
//                                )
//                            ),
//                                shape = RoundedCornerShape(16.dp)
//                            )
//                            .customTabIndicatorOffset(
//                                currentTabPosition = tabPositions[pagerState.currentPage],
//                            )
//                    )
                },
            ) {
                mTabs.forEachIndexed { index, item ->
                    TabView(Modifier.zIndex(1f), item, index, pagerState.currentPage == index) { i ->
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(i)
                        }
                    }
                }
            }
            HorizontalPager(state = pagerState, modifier = Modifier) {
                PageContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.CenterHorizontally),
                    pagerState,
                    index = it
                )
            }
        }
    }

    @Composable
    open fun TabView(
        modifier: Modifier,
        item: T,
        index: Int,
        isSelected: Boolean,
        onTabClick: (Int) -> Unit
    ) {
        Tab(modifier = modifier
            .height(dimensionResource(id = R.dimen.dp_48))
            .background(Color.Transparent),
            selected = isSelected,
            selectedContentColor = TabRowDefaults.primaryContentColor,
            unselectedContentColor = TabRowDefaults.secondaryContentColor,
            onClick = {
                onTabClick(index)
            }) {
            Text(
                item.toString(),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = dimensionResource(id = R.dimen.dp_16)),
                fontSize = dimensionResource(id = R.dimen.sp_20).value.sp,
                fontFamily = DemoFontFamily.NotoSansSc500
            )
        }
    }

    @Composable
    abstract fun PageContent(modifier: Modifier, pagerState: PagerState, index: Int)
}