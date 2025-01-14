package com.android.composedemo.GitHub

import com.fz.gson.GsonFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient {
    companion object {
        @JvmStatic
        fun getClient(host: String): Retrofit {
            return Retrofit.Builder()
                .baseUrl(host)
                .addConverterFactory(GsonConverterFactory.create(GsonFactory.createGson()))
                .build()
        }
    }
}