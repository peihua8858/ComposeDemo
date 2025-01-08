package com.android.composedemo.data.bean

import android.os.Parcelable
import com.android.composedemo.data.bean.Data
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModuleBean(
    val actionUrl: String,
    var dataList: MutableList<Data>?,
    val description: String,
    val matchedPlanId: Int,
    val moduleId: String,
    val moduleIndex: Int,
    var moduleName: String,
    val moduleSubTitle: String,
    val moduleTitle: String,
    var moduleType: String,
    val showItemCount: Int,
    val totalCount: Int,
    var showMore: Boolean
) :Parcelable