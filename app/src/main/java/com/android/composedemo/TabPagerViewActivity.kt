package com.android.composedemo

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.composedemo.GitHub.viewmodel.GithubViewModel
import com.android.composedemo.compose.CustomViewScreen
import com.android.composedemo.compose.GithubScreen
import com.android.composedemo.compose.MarketListView
import com.android.composedemo.compose.Paging3Screen
import com.android.composedemo.compose.Paging3GridScreen
import com.android.composedemo.compose.Paging3StaggeredGridScreen
import com.android.composedemo.compose.RefreshViewScreen
import com.android.composedemo.compose.SmartSwipeRefreshScreen
import com.android.composedemo.compose.SongTextScreen
import com.android.composedemo.data.viewmodel.DemoHomeViewModel

class TabPagerViewActivity : TabLayoutActivity<TabModel>() {
    private val mViewModel by viewModels<DemoHomeViewModel>()
    private val mGithubViewModel by viewModels<GithubViewModel> { GithubViewModel.Factory(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mTabs = mutableListOf<TabModel>()
        mTabs.add(TabModel("Custom View") { modifier, state, index ->
            CustomViewScreen(modifier)
        })
        mTabs.add(TabModel("Compose List") { modifier, state, index ->
            MarketListView(modifier, mViewModel.modelState3)
        })
        mTabs.add(TabModel("Refresh List") { modifier, state, index ->
            RefreshViewScreen(modifier, mViewModel)
        })
        mTabs.add(TabModel("Smart Refresh") { modifier, state, index ->
            SmartSwipeRefreshScreen(modifier, mViewModel)
        })
        mTabs.add(TabModel("Paging3 Grid") { modifier, state, index ->
            Paging3GridScreen(modifier, mViewModel)
        })
        mTabs.add(TabModel("Paging3 Staggered Grid") { modifier, state, index ->
            Paging3StaggeredGridScreen(modifier, mViewModel)
        })
        mTabs.add(TabModel("Paging3 List") { modifier, state, index ->
            Paging3Screen(modifier, mViewModel)
        })
        mTabs.add(TabModel("Github") { modifier, state, index ->
            GithubScreen(modifier, mGithubViewModel)
        })
        mTabs.add(TabModel("Song lrc text") { modifier, state, index ->
            SongTextScreen(modifier)
        })
        this.mTabs.addAll(mTabs)
        mViewModel.requestHomeData2(1)
    }

    @Composable
    override fun PageContent(modifier: Modifier, pagerState: PagerState, index: Int) {
        mTabs[index].page(modifier, pagerState, index)
    }
}

class TabModel(
    var title: String,
    var page: @Composable (modifier: Modifier, pagerState: PagerState, index: Int) -> Unit,
) {
    override fun toString(): String {
        return title
    }
}