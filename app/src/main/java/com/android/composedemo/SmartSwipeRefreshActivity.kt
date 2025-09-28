package com.android.composedemo

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.composedemo.compose.SmartSwipeRefreshScreen
import com.android.composedemo.data.viewmodel.DemoHomeViewModel
import com.android.composedemo.widgets.refreshlayout.PullToRefresh

/**
 * use [PullToRefresh]
 */
class SmartSwipeRefreshActivity : BaseActivity() {
    private val mViewModel by viewModels<DemoHomeViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Compose Demo"
//        mViewModel.requestHomeData()
    }

    @Composable
    override fun ContentView(modifier: Modifier) {
        SmartSwipeRefreshScreen(modifier, mViewModel)
    }
}


