package com.android.composedemo

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.composedemo.compose.Paging3GridScreen
import com.android.composedemo.data.viewmodel.DemoHomeViewModel
import com.android.composedemo.widgets.pullrefreshlayout.PullToRefresh

/**
 * use [PullToRefresh]
 */
class PullToRefreshByPagerGridViewActivity : BaseActivity() {
    private val mViewModel by viewModels<DemoHomeViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Compose Demo"
    }

    @Composable
    override fun ContentView(modifier: Modifier) {
        Paging3GridScreen(modifier,mViewModel)
    }
}
