package com.android.composedemo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage, modifier = Modifier,

            ) {
                mTabs.forEachIndexed { index, item ->
                    TabView(Modifier, item, index, pagerState.currentPage == index) { i ->
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
        Tab(modifier = Modifier.height(dimensionResource(id = R.dimen.dp_48))
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