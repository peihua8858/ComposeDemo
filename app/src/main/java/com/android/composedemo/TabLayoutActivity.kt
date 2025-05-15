package com.android.composedemo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.android.composedemo.compose.components.PagerTabIndicator
import com.android.composedemo.ui.theme.DemoFontFamily
import kotlinx.coroutines.launch

/**
 *
 * tab+pager 基础类
 * @author dingpeihua
 * @date 2025/1/15 14:45
 **/
abstract class TabLayoutActivity<T : Any> : BaseActivity() {
    protected val mTabs = mutableStateListOf<T>()

    @Composable
    final override fun ContentView(modifier: Modifier) {
        val coroutineScope = rememberCoroutineScope()
        val pagerState = rememberPagerState(
            initialPage = 0,
            initialPageOffsetFraction = 0f
        ) { mTabs.size }
        Column(modifier = modifier) {
            tabLayout(Modifier, pagerState, {
                tabIndicator(it, pagerState)
            }) {
                mTabs.forEachIndexed { index, item ->
                    TabView(
                        Modifier.zIndex(1f),
                        item,
                        index,
                        pagerState.currentPage == index
                    ) { i ->
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

    open val tabLayout: @Composable (
        modifier: Modifier, state: PagerState,
        indicator: @Composable (tabPositions: List<TabPosition>) -> Unit,
        tabs: @Composable () -> Unit
    ) -> Unit
        get() {
            return { modifier, pagerState, indicator, tabs ->
                val selectedTabIndex = pagerState.currentPage
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = modifier,
                    indicator = indicator,
                ) { tabs() }
            }
        }
    open val tabIndicator: @Composable (tabPositions: List<TabPosition>, pagerState: PagerState) -> Unit
        get() {
            return { tabPositions, pagerState ->
                PagerTabIndicator(tabPositions = tabPositions, pagerState = pagerState)
            }
        }

//    @Composable
//    open fun TabIndicator(tabPositions: List<TabPosition>, pagerState: PagerState) {
//        PagerTabIndicator(tabPositions = tabPositions, pagerState = pagerState)
//                    TabRowDefaults.PrimaryIndicator(
//                        modifier = Modifier.tabIndicatorOffset(selectedTabIndex, matchContentSize = true),
//                        width = Dp.Unspecified,
//                    )
//                    FancyAnimatedIndicatorWithModifier(index = selectedTabIndex)
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
//    }

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