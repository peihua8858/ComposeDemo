package com.android.composedemo.GitHub.api;

import static com.android.composedemo.GitHub.api.GithubService.HOST;

import com.android.composedemo.ComposeDemoApp;
import com.android.composedemo.GitHub.RetrofitClient;
import com.fz.gson.GsonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GithubService2 {

    @GET("search/repositories?sort=start")
    RepoSearchResponse requestData(
            @Query("q") String query,
            @Query("page") int page,
            @Query("per_page") int pageSize
    );

    default RepoSearchResponse requestLocalData() throws IOException {
        try (InputStream is = ComposeDemoApp.getAPP().getAssets().open("GithubData.json")) {
            return GsonUtils.fromJson(new InputStreamReader(is), RepoSearchResponse.class);
        }
    }

    static GithubService2 create() {
        return RetrofitClient.getClient(HOST).create(GithubService2.class);
    }
}
