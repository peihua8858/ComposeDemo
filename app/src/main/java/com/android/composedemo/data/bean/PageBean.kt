package com.android.composedemo.data.bean

data class PageBean(
    val description: String,
    val isDefault: Boolean,
    val moduleList: MutableList<ModuleBean>,
    val pageId: Int,
    val pageSubTitle: Any,
    val pageTitle: String,
    val pageType: String,
    val picUrl: String,
    val source: Any
)