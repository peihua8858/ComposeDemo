package com.android.composedemo.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.android.composedemo.compose.components.LoadingView
import com.android.composedemo.data.bean.AdapterBean
import com.android.composedemo.data.viewmodel.DemoHomeViewModel
import com.android.composedemo.utils.isError
import com.android.composedemo.utils.isInitialize
import com.android.composedemo.utils.isStarting
import com.android.composedemo.utils.isSuccess
import com.android.composedemo.widgets.pullrefreshlayout.SmartSwipeRefresh
import com.android.composedemo.widgets.pullrefreshlayout.SmartSwipeStateFlag
import com.android.composedemo.widgets.pullrefreshlayout.rememberSmartSwipeRefreshState


@Composable
fun SmartSwipeRefreshScreen(modifier: Modifier, viewModel: DemoHomeViewModel) {
    val refreshState = rememberSmartSwipeRefreshState()
    val mLoading = remember { mutableStateOf(true) }
    val modelState3 = remember { mutableStateListOf<AdapterBean<*>>() }
    val result = viewModel.modelState1.value
    // 使用 LocalConfiguration 获取当前配置
    SmartSwipeRefresh(
        state = refreshState,
        onRefresh = {
            refreshState.refreshFlag = SmartSwipeStateFlag.REFRESHING
            viewModel.requestHomeData1()
        },
        onLoadMore = {
            refreshState.loadMoreFlag = SmartSwipeStateFlag.REFRESHING
            viewModel.requestHomeData1()
        },
        modifier = modifier
    ) {
        if (result.isStarting()) {
            LoadingView(Modifier)
        } else if (result.isInitialize()) {
            viewModel.requestHomeData1()
        } else if (result.isSuccess()) {
            mLoading.value = false
            if (refreshState.refreshFlag == SmartSwipeStateFlag.REFRESHING) {
                modelState3.clear()
            }
            modelState3.addAll(result.data)
            MarketListView(
                modifier.fillMaxSize(),
                modelState3
            )
            refreshState.refreshFlag = SmartSwipeStateFlag.SUCCESS
            refreshState.loadMoreFlag = SmartSwipeStateFlag.SUCCESS
        } else if (result.isError()) {
            refreshState.refreshFlag = SmartSwipeStateFlag.ERROR
            refreshState.loadMoreFlag = SmartSwipeStateFlag.ERROR
            mLoading.value = false
        }
    }
}