package com.android.composedemo

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.TabPosition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.composedemo.GitHub.GithubContentView
import com.android.composedemo.GitHub.viewmodel.GithubViewModel
import com.android.composedemo.data.viewmodel.DemoHomeViewModel

class TabPagerViewActivity : TabLayoutActivity<String>() {
    private val mViewModel by viewModels<DemoHomeViewModel>()
    private val mGithubViewModel by viewModels<GithubViewModel> { GithubViewModel.Factory(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mTabs = mutableListOf<String>()
        mTabs.add("Compose List")
        mTabs.add("Refresh List")
        mTabs.add("Smart Refresh")
        mTabs.add("Paging3 Grid")
        mTabs.add("Paging3 Staggered Grid")
        mTabs.add("Paging3 List")
        mTabs.add("Github")
        this.mTabs.addAll(mTabs)
        mViewModel.requestHomeData2(1)
    }

    @Composable
    override fun TabIndicator(tabPositions: List<TabPosition>, pagerState: PagerState) {
//        super.TabIndicator(tabPositions, pagerState)
    }

    @Composable
    override fun PageContent(modifier: Modifier, pagerState: PagerState, index: Int) {
        when (index) {
            0 -> MarketListView(modifier, mViewModel.modelState3)
            1 -> RefreshViewScreen(modifier, mViewModel)
            2 -> SmartSwipeRefreshContentView(modifier, mViewModel)
            3 -> Paging3GridContentView(modifier, mViewModel)
            4 -> Paging3StaggeredGridContentView(modifier, mViewModel)
            5 -> Paging3ContentView(modifier, mViewModel)
            6 -> GithubContentView(modifier, mGithubViewModel)
        }
    }
}