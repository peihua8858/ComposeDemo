package com.android.composedemo

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.android.composedemo.data.bean.AdapterBean
import com.android.composedemo.data.viewmodel.DemoHomeViewModel
import com.android.composedemo.utils.isError
import com.android.composedemo.utils.isSuccess
import com.android.composedemo.widgets.refreshlayout.PullToRefresh
import com.android.composedemo.widgets.refreshlayout.rememberPullToRefreshState

/**
 * use [PullToRefresh]
 */
class PullToRefreshOrLoadMoreViewActivity : BaseActivity() {
    private val mViewModel by viewModels<DemoHomeViewModel>()
    private val mLoading = mutableStateOf(true)
    private val modelState3 = mutableStateListOf<AdapterBean<*>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Compose Demo"
        mViewModel.requestHomeData()
    }

    @Composable
    @ExperimentalMaterial3Api
    override fun ContentView(modifier: Modifier) {
        val refreshing = rememberPullToRefreshState(isRefreshing = false,false)
        mViewModel.modelState.observe(this) {
            if (it.isSuccess()) {
                mLoading.value = false
                if (refreshing.isLoadingMore) {
                    modelState3.addAll(it.data)
                } else {
                    modelState3.clear()
                    modelState3.addAll(it.data)
                }
                refreshing.isRefreshing = false
                refreshing.isLoadingMore = false
            } else if (it.isError()) {
                refreshing.isRefreshing = false
                refreshing.isLoadingMore = false
                mLoading.value = false
            }
        }
        // 使用 LocalConfiguration 获取当前配置
        PullToRefresh(
            state = refreshing,
            onRefresh = {
                refreshing.isRefreshing = true
                mViewModel.requestHomeData()
            },
            onLoadMore = {
                refreshing.isLoadingMore = true
                mViewModel.requestHomeData()
            },
            modifier = modifier
        ) {
            if (mLoading.value) {
                LoadingView(Modifier)
            } else {
                BindingListView(Modifier)
            }
        }
    }

    @Composable
    fun LoadingView(modifier: Modifier) {
        Box(modifier = modifier.fillMaxSize()) {
            val painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(data = R.mipmap.ic_loading1)
                    .build()
            )
            val transition = rememberInfiniteTransition(label = "")
            val progress by transition.animateValue(
                0f,
                1f,
                Float.VectorConverter,
                infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1332,
                        easing = LinearEasing
                    )
                ), label = ""
            )
            Image(
                painter = painter,
                contentDescription = "",
                modifier = modifier
                    .align(Alignment.Center)
                    .rotate(progress * 360),
            )
        }
    }

    @Composable
    private fun BindingListView(modifier: Modifier) {
        MarketListView(
            modifier.fillMaxSize(),
            modelState3
        )
    }

}

