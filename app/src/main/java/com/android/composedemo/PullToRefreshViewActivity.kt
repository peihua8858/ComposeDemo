package com.android.composedemo

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.composedemo.compose.RefreshViewScreen
import com.android.composedemo.data.viewmodel.DemoHomeViewModel
import com.android.composedemo.widgets.pullrefreshlayout.PullToRefresh

/**
 * use [PullToRefresh]
 */
class PullToRefreshViewActivity : BaseActivity() {
    private val mViewModel by viewModels<DemoHomeViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Compose Demo"
//        mViewModel.requestHomeData()
    }

    @Composable
    @ExperimentalMaterial3Api
    override fun ContentView(modifier: Modifier) {
        RefreshViewScreen(modifier, mViewModel)
    }
}