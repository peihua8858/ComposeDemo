package com.android.composedemo.data.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.android.composedemo.ComposeDemoApp
import com.android.composedemo.Constants
import com.android.composedemo.R
import com.android.composedemo.data.bean.AdapterBean
import com.android.composedemo.data.bean.ResponseModel
import com.android.composedemo.model.APIResponse
import com.android.composedemo.model.BaseResponse
import com.android.composedemo.model.RequestException
import com.android.composedemo.model.ResultCode
import com.android.composedemo.utils.Logcat
import com.android.composedemo.utils.ResultData
import com.android.composedemo.utils.apiSyncRequest
import com.android.composedemo.utils.request
import com.fz.gson.GsonUtils
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import java.nio.charset.Charset
import kotlin.random.Random

/**
 *
 * AI广场首页ViewModel
 *
 * @author dingpeihua
 * @date 2024/9/13 13:44
 **/
class DemoHomeViewModel : ViewModel() {
    //    val modelState3 = mutableStateOf<MutableList<AdapterBean<*>>>(mutableListOf())
    val modelState3 = mutableStateListOf<AdapterBean<*>>()

    //paging3 分页器
    val modelState2 = Pager( PagingConfig(
        pageSize = 6,
        initialLoadSize = 6,  // 可根据需要调整
        maxSize = 100, // 可选，最大加载数据量
        enablePlaceholders = false // 根据需要设置
    ), null) { HomePagingSource(this) }
        .flow.cachedIn(viewModelScope)

    /**
     * 请求首页数据
     */
    val modelState = MutableLiveData<ResultData<MutableList<AdapterBean<*>>>>()
    val fileNames = mutableListOf("render_data.json", "render_data2.json")

    suspend fun requestPagingData(pageSize: Int): MutableList<AdapterBean<*>> {
        delay(1000)
        val index = Random.nextInt(fileNames.size)
        val model = readLocalData(fileNames[index]).model
            ?: throw RequestException(-1, "model is null")
        val moduleList = model.page?.moduleList
        var adapterList = mutableListOf<AdapterBean<*>>()
        if (!moduleList.isNullOrEmpty()) {
            for ((i, item) in moduleList.withIndex()) {
                val moduleType = item.moduleType
                if (i > 0) {
                    adapterList.add(AdapterBean(Constants.TYPE_TITLE, item))
                }
                when (moduleType) {
                    "ai_market_banner_ydp" -> {
                        adapterList.add(AdapterBean(Constants.TYPE_BANNER, item))
                    }

                    "ai_market_banner_ydp_4" -> {
                        adapterList.add(AdapterBean(Constants.TYPE_AI_BANNER, item))
                    }

                    "ai_market_poster_ydp_4" -> {
                        adapterList.add(AdapterBean(Constants.TYPE_POST_BANNER, item))
                    }

                    else -> {
                        adapterList.add(AdapterBean(Constants.TYPE_ITEM, item))
                    }
                }
            }
        }
        if (pageSize > 0) {
            adapterList = adapterList.subList(0, pageSize)
        }
        return adapterList
    }

    /**
     * 请求首页数据
     */
    fun requestHomeData() {
        request(modelState) {
            requestPagingData(0)
        }
    }

    fun requestHomeData2(page: Int) {
        apiSyncRequest {
            onRequest {
                delay(1000)
                val index = Random.nextInt(fileNames.size)
                val model = readLocalData(fileNames[index]).model
                    ?: throw RequestException(-1, "model is null")
                val moduleList = model.page?.moduleList
                val adapterList = mutableListOf<AdapterBean<*>>()
                if (!moduleList.isNullOrEmpty()) {
                    for ((i, item) in moduleList.withIndex()) {
                        val moduleType = item.moduleType
                        if (i > 0) {
                            adapterList.add(AdapterBean(Constants.TYPE_TITLE, item))
                        }
                        if (moduleType == "ai_market_banner_ydp") {
                            adapterList.add(AdapterBean(Constants.TYPE_BANNER, item))
                        } else if (moduleType == "ai_market_banner_ydp_4") {
                            adapterList.add(AdapterBean(Constants.TYPE_AI_BANNER, item))
                        } else if (moduleType == "ai_market_poster_ydp_4") {
                            adapterList.add(AdapterBean(Constants.TYPE_POST_BANNER, item))
                        } else {
                            adapterList.add(AdapterBean(Constants.TYPE_ITEM, item))
                        }
                    }
                }
                adapterList
            }
            onResponse {
                if (page == 1) {
                    modelState3.clear()
                }
                modelState3.addAll(it)
            }
            onError {
                if (page == 1) {
                    modelState3.clear()
                }
            }
        }
    }

    private fun readLocalData(): BaseResponse<ResponseModel> {
        return readLocalData("render_data.json")
    }

    private fun readLocalData(fileName: String): BaseResponse<ResponseModel> {
        Logcat.d("readLocalData", " >>>fileName = $fileName")
        ComposeDemoApp.getAPP().assets.open(fileName).use { inputStream ->
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            val content = String(buffer, Charset.forName("UTF-8"))
            val typeReference: TypeToken<BaseResponse<ResponseModel>> =
                object : TypeToken<BaseResponse<ResponseModel>>() {
                }
            val apiRsp: APIResponse<BaseResponse<ResponseModel>> =
                GsonUtils.fromJson(content, APIResponse::class.java, typeReference.type)
            val data = apiRsp.data
            if (ResultCode.SUCCESS == data.msgCode || "SUCCESS" == data.msgCode) {
                return data
            } else {
                if (ResultCode.isNoNetWork(data.msgCode, data.msgInfo)) {
                    data.setMsgInfo(
                        ComposeDemoApp.getAPP().getString(R.string.contacts_network_error)
                    )
                }
                return data
            }
        }
    }
}

class HomePagingSource(private val viewModel: DemoHomeViewModel) :
    PagingSource<Int, AdapterBean<*>>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AdapterBean<*>> {
        val currentPage = params.key ?: 1
        val pageSize = params.loadSize
        Logcat.d("HomePagingSource", " >>>currentPage = $currentPage, pageSize = $pageSize")
        val response = viewModel.requestPagingData(pageSize)
        return LoadResult.Page(
            data = response,
            prevKey = if (currentPage == 1) null else currentPage - 1,
            nextKey = if (response.isEmpty()) null else currentPage + 1
        )
    }
    override fun getRefreshKey(state: PagingState<Int, AdapterBean<*>>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val page = state.closestPageToPosition(anchorPosition)
            page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1) // 返回刷新会使用的键
        }
    }
}