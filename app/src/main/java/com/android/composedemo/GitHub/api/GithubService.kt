package com.android.composedemo.GitHub.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Query

interface GithubService {
    companion object {
        const val HOST = "https://api.github.com/"
    }

    //?sort=stars&q=ios%20in:name,description&page=1&per_page=30
    @GET("search/repositories?sort=start")
    suspend fun requestData(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("per_page") pageSize: Int
    )

}