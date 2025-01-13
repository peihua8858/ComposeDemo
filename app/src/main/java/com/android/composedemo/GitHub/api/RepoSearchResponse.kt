package com.android.composedemo.GitHub.api

import com.android.composedemo.GitHub.model.Repo
import com.google.gson.annotations.SerializedName

data class RepoSearchResponse(
    @SerializedName("total_count") val total: Int = 0,
    @SerializedName("items") val items: List<Repo> = emptyList(),
    val nextPage: Int? = null
) {
    fun isEmpty() = items.isEmpty()
}
