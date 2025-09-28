package com.android.composedemo.GitHub

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.composedemo.BaseActivity
import com.android.composedemo.GitHub.viewmodel.GithubViewModel
import com.android.composedemo.compose.GithubScreen
import com.android.composedemo.widgets.pullrefreshlayout.PullToRefresh

/**
 * use [PullToRefresh]
 */
class PullToRefreshByGithubPagerActivity : BaseActivity() {
    private val mViewModel by viewModels<GithubViewModel> { GithubViewModel.Factory(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Compose Demo"
    }

    @Composable
    override fun ContentView(modifier: Modifier) {
        GithubScreen(modifier, mViewModel)
    }
}
