package com.android.composedemo

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.composedemo.GitHub.GithubContentView
import com.android.composedemo.GitHub.viewmodel.GithubViewModel
import com.android.composedemo.compose.CustomViewScreen
import com.android.composedemo.compose.MarketListView
import com.android.composedemo.compose.SongTextContentView
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
            SmartSwipeRefreshContentView(modifier, mViewModel)
        })
        mTabs.add(TabModel("Paging3 Grid") { modifier, state, index ->
            Paging3GridContentView(modifier, mViewModel)
        })
        mTabs.add(TabModel("Paging3 Staggered Grid") { modifier, state, index ->
            Paging3StaggeredGridContentView(modifier, mViewModel)
        })
        mTabs.add(TabModel("Paging3 List") { modifier, state, index ->
            Paging3ContentView(modifier, mViewModel)
        })
        mTabs.add(TabModel("Github") { modifier, state, index ->
            GithubContentView(modifier, mGithubViewModel)
        })
        mTabs.add(TabModel("Song lrc text") { modifier, state, index ->
            SongTextContentView(modifier)
        })
        this.mTabs.addAll(mTabs)
        mViewModel.requestHomeData2(1)
    }

    @Composable
    override fun PageContent(modifier: Modifier, pagerState: PagerState, index: Int) {
        mTabs[index].page(modifier, pagerState, index)
//        when (index) {
//            7 -> MarketListView(modifier, mViewModel.modelState3)
//            1 -> RefreshViewScreen(modifier, mViewModel)
//            2 -> SmartSwipeRefreshContentView(modifier, mViewModel)
//            3 -> Paging3GridContentView(modifier, mViewModel)
//            4 -> Paging3StaggeredGridContentView(modifier, mViewModel)
//            5 -> Paging3ContentView(modifier, mViewModel)
//            7 -> GithubContentView(modifier, mGithubViewModel)
//            8 -> SongTextContentView(modifier)
//        }
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