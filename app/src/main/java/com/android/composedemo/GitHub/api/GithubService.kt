package com.android.composedemo.GitHub.api

import com.android.composedemo.ComposeDemoApp
import com.android.composedemo.GitHub.RetrofitClient
import com.android.composedemo.utils.Logcat
import com.fz.gson.GsonFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.FileReader
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.memberFunctions

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

    suspend fun requestLocalData(): RepoSearchResponse? {
        val json = ComposeDemoApp.getAPP().assets.openFd("GithubData.json")
        return GsonFactory.createGson()
            .fromJson(FileReader(json.fileDescriptor), RepoSearchResponse::class.java)
    }

    fun invoke(service: Any, method: Method, vararg args: Any): Any? {
        return method.invoke(service, *args)
    }
}
fun Method.overridesMethod(cls: KClass<*>) =
    cls.memberFunctions.first { it.name == name} in cls.declaredFunctions

fun <T> createProxy(service: Class<T>): T {
    val serviceMethod = GithubService.create()
    return Proxy.newProxyInstance(
        service.classLoader,
        arrayOf(service),
        object : InvocationHandler {
            @Throws(Throwable::class)
            override fun invoke(proxy: Any, method: Method, args: Array<Any>): Any? {
                if (method.declaringClass == Any::class.java) {
                    return method.invoke(this, *args)
                }
              val m=  proxy.javaClass.getDeclaredMethod(method.name, *method.parameterTypes)
                Logcat.d("invoke: ${method.name},isDefault: ${method.isDefault}")
                return if (m.overridesMethod(proxy::class)
                ) method.invoke(method, service, proxy, *args)
                else serviceMethod.invoke(proxy, method, *args)
            }
        }) as T
}