package com.android.composedemo.GitHub.api

import com.android.composedemo.ComposeDemoApp
import com.android.composedemo.GitHub.RetrofitClient
import com.android.composedemo.utils.Logcat
import com.fz.gson.GsonUtils
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.InputStreamReader
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

@JvmDefaultWithoutCompatibility
interface GithubService {
    companion object {
        const val HOST = "https://api.github.com/"
        fun create(): GithubService {
            return RetrofitClient.getClient(HOST).create(GithubService::class.java)
        }

        fun createLocal(): GithubService {
            return createProxy(GithubService::class.java)
        }
    }

    @GET("search/repositories?sort=start")
    suspend fun requestData(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("per_page") pageSize: Int
    ): RepoSearchResponse?

    fun requestLocalData(): RepoSearchResponse? {
        ComposeDemoApp.getAPP().assets.open("GithubData.json").use {
            return GsonUtils.fromJson(InputStreamReader(it), RepoSearchResponse::class.java)
        }
    }

    fun invoke(method: Method, vararg args: Any?): Any? {
        return method.invoke(this, *args)
    }
}

fun <T> createProxy(service: Class<T>): T {
    val serviceMethod = GithubService.create()
    return Proxy.newProxyInstance(
        service.classLoader,
        arrayOf(service),
        object : InvocationHandler {
            private val platform: Platform = Platform.get()
            private val emptyArgs = arrayOfNulls<Any>(0)

            @Throws(Throwable::class)
            override fun invoke(proxy: Any, method: Method, args: Array<Any>?): Any? {
                val tempArgs = args ?: emptyArgs
                if (method.declaringClass == Any::class.java) {
                    return method.invoke(this, *tempArgs)
                }

                Logcat.d("invoke: ${method.name},isDefault: ${method.isDefault},proxy: ${proxy.javaClass},this: ${this.javaClass}")
                Logcat.d("Invoking method: ${method.name} with args: ${tempArgs.joinToString()}")
                return if (method.isDefault
                ) platform.invokeDefaultMethod(method, service, proxy, tempArgs)
                else serviceMethod.invoke(method, *tempArgs)
            }
        }) as T
}
