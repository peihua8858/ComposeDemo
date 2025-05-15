package com.android.composedemo

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.android.composedemo.compose.MarketListView
import com.android.composedemo.data.bean.AdapterBean
import com.android.composedemo.data.viewmodel.DemoHomeViewModel
import com.android.composedemo.utils.isError
import com.android.composedemo.utils.isSuccess
import com.android.composedemo.widgets.pullrefreshlayout.SmartSwipeRefresh
import com.android.composedemo.widgets.pullrefreshlayout.SmartSwipeStateFlag
import com.android.composedemo.widgets.pullrefreshlayout.rememberSmartSwipeRefreshState
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
        SmartSwipeRefreshContentView(modifier, mViewModel)
    }
}

@Composable
fun BaseActivity.SmartSwipeRefreshContentView(modifier: Modifier, viewModel: DemoHomeViewModel) {
    val refreshState = rememberSmartSwipeRefreshState()
    val mLoading = remember { mutableStateOf(true) }
    val modelState3 = remember { mutableStateListOf<AdapterBean<*>>() }
    viewModel.modelState1.observe(this) {
        if (it.isSuccess()) {
            mLoading.value = false
            if (refreshState.refreshFlag == SmartSwipeStateFlag.REFRESHING) {
                modelState3.clear()
            }
            modelState3.addAll(it.data)
            refreshState.refreshFlag = SmartSwipeStateFlag.SUCCESS
            refreshState.loadMoreFlag = SmartSwipeStateFlag.SUCCESS
        } else if (it.isError()) {
            refreshState.refreshFlag = SmartSwipeStateFlag.ERROR
            refreshState.loadMoreFlag = SmartSwipeStateFlag.ERROR
            mLoading.value = false
        }
    }
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
        if (mLoading.value) {
            LoadingView(Modifier)
            viewModel.requestHomeData1()
        } else {
            BindingListView(Modifier, modelState3)
        }
    }
}

@Composable
private fun BaseActivity.BindingListView(
    modifier: Modifier,
    modelState3: MutableList<AdapterBean<*>>
) {
    MarketListView(
        modifier.fillMaxSize(),
        modelState3
    )
}

