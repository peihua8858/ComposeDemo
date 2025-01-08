package com.android.composedemo.data.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.composedemo.ComposeDemoApp
import com.android.composedemo.utils.ResultData
import com.android.composedemo.utils.request
import com.android.composedemo.R
import com.android.composedemo.data.bean.ModuleResponseModel
import com.android.composedemo.model.APIResponse
import com.android.composedemo.model.BaseResponse
import com.android.composedemo.model.RequestException
import com.android.composedemo.model.ResultCode
import com.fz.gson.GsonUtils
import com.google.gson.reflect.TypeToken
import java.nio.charset.Charset

class DemoModuleViewModel : ViewModel() {
    val modelState = MutableLiveData<ResultData<ModuleResponseModel>>()
    fun requestData(moduleId: String) {
        request(modelState) {
            val response = readLocalData()
            val model = response.model ?: throw RequestException(-1, "model is null")
            if (model.dataList.isNullOrEmpty()) {
                throw RequestException(-1, "dataList is empty")
            }
            model
        }
    }
    private fun readLocalData(): BaseResponse<ModuleResponseModel> {
        ComposeDemoApp.getAPP().assets.open("render_data.json").use { inputStream ->
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            val content = String(buffer, Charset.forName("UTF-8"))
            val typeReference: TypeToken<BaseResponse<ModuleResponseModel>> =
                object : TypeToken<BaseResponse<ModuleResponseModel>>() {
                }
            val apiRsp: APIResponse<BaseResponse<ModuleResponseModel>> =
                GsonUtils.fromJson(content, APIResponse::class.java, typeReference.type)
            val data = apiRsp.getData()
            if (ResultCode.SUCCESS == data.msgCode || "SUCCESS" == data.msgCode) {
                return data
            } else {
                if (ResultCode.isNoNetWork(data.getMsgCode(), data.getMsgInfo())) {
                    data.setMsgInfo(ComposeDemoApp.getAPP().getString(R.string.contacts_network_error))
                }
                return data
            }
        }
    }
}