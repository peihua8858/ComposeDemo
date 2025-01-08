package com.android.composedemo.data.bean

data class ResponseModel(
    val page: PageBean?,
    val menu: MenuBean?
)

data class ModuleResponseModel(
    val dataList: MutableList<Data>?,
    val moduleType: String,
    val moduleId: String
)