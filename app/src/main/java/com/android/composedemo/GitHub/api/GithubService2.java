package com.android.composedemo.GitHub.api;

import static com.android.composedemo.GitHub.api.GithubService.HOST;

import android.annotation.SuppressLint;

import androidx.annotation.Nullable;

import com.android.composedemo.ComposeDemoApp;
import com.android.composedemo.GitHub.RetrofitClient;
import com.fz.gson.GsonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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

    default Object invoke(Method method, Object... args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(this, args);
    }

    static GithubService2 create() {
        return RetrofitClient.getClient(HOST).create(GithubService2.class);
    }

    @SuppressLint("NewApi")

    public static <T> T createProxy(final Class<T> service) {
        T serviceMethod = RetrofitClient.getClient(HOST).create(service);
        return (T) Proxy.newProxyInstance(
                service.getClassLoader(),
                new Class<?>[]{service},
                new InvocationHandler() {
                    private final Platform platform = Platform.get();
                    private final Object[] emptyArgs = new Object[0];

                    @Override
                    public @Nullable Object invoke(Object proxy, Method method, @Nullable Object[] args)
                            throws Throwable {
                        // If the method is a method from Object then defer to normal invocation.
                        if (method.getDeclaringClass() == Object.class) {
                            return method.invoke(this, args);
                        }
                        args = args != null ? args : emptyArgs;
                        return platform.isDefaultMethod(method)
                                ? platform.invokeDefaultMethod(method, service, proxy, args)
                                : method.invoke(serviceMethod, args);
                    }
                });
    }
}
