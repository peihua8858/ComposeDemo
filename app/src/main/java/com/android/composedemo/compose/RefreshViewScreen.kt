package com.android.composedemo.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.composedemo.compose.components.LoadingView
import com.android.composedemo.data.bean.AdapterBean
import com.android.composedemo.data.viewmodel.DemoHomeViewModel
import com.android.composedemo.utils.ResultData
import com.android.composedemo.widgets.pullrefreshlayout.PullToRefresh
import com.android.composedemo.widgets.pullrefreshlayout.rememberPullToRefreshState


@Composable
fun RefreshViewScreen(
    modifier: Modifier,
    viewModel: DemoHomeViewModel = viewModel<DemoHomeViewModel>(),
) {
    var refreshing by remember { mutableStateOf(false) }
    val modelState3 = remember { mutableStateListOf<AdapterBean<*>>() }
    val result = viewModel.modelState.value
    // 使用 LocalConfiguration 获取当前配置
    PullToRefresh(
        state = rememberPullToRefreshState(isRefreshing = refreshing),
        onRefresh = {
            refreshing = true
            viewModel.requestHomeData()
        },
        modifier = modifier
    ) {
        when (result) {
            is ResultData.Starting -> {
                LoadingView(Modifier)
            }

            is ResultData.Success -> {
                modelState3.addAll(result.data)
                refreshing = false
                MarketListView(modifier.fillMaxSize(), modelState3)
            }

            is ResultData.Failure -> {
                refreshing = false
            }
            is ResultData.Initialize -> {
                viewModel.requestHomeData()
            }
        }
    }
}