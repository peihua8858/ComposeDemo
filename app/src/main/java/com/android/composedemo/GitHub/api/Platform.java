package com.android.composedemo.GitHub.api;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.android.composedemo.utils.Logcat;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Executor;

import retrofit2.CallAdapter;


@RequiresApi(api = Build.VERSION_CODES.O)
class Platform {
    private static final Platform PLATFORM = findPlatform();

    static Platform get() {
        return PLATFORM;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static Platform findPlatform() {
        return "Dalvik".equals(System.getProperty("java.vm.name"))
                ? new Platform.Android() //
                : new Platform(true);
    }

    private final boolean hasJava8Types;
    private final @Nullable Constructor<MethodHandles.Lookup> lookupConstructor;

    @RequiresApi(api = Build.VERSION_CODES.O)
    Platform(boolean hasJava8Types) {
        this.hasJava8Types = hasJava8Types;

        Constructor<MethodHandles.Lookup> lookupConstructor = null;
        if (hasJava8Types) {
            try {
                // Because the service interface might not be public, we need to use a MethodHandle lookup
                // that ignores the visibility of the declaringClass.
                lookupConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
                lookupConstructor.setAccessible(true);
            } catch (NoClassDefFoundError ignored) {
                // Android API 24 or 25 where Lookup doesn't exist. Calling default methods on non-public
                // interfaces will fail, but there's nothing we can do about it.
            } catch (NoSuchMethodException ignored) {
                // Assume JDK 14+ which contains a fix that allows a regular lookup to succeed.
                // See https://bugs.openjdk.java.net/browse/JDK-8209005.
            }
        }
        this.lookupConstructor = lookupConstructor;
    }

    @Nullable
    Executor defaultCallbackExecutor() {
        return null;
    }


    int defaultCallAdapterFactoriesSize() {
        return hasJava8Types ? 2 : 1;
    }

    int defaultConverterFactoriesSize() {
        return hasJava8Types ? 1 : 0;
    }

    boolean isDefaultMethod(Method method) {
        return hasJava8Types && method.isDefault();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    Object invokeDefaultMethod(Method method, Class<?> declaringClass, Object object, Object... args)
            throws Throwable {
        MethodHandles.Lookup lookup =
                lookupConstructor != null
                        ? lookupConstructor.newInstance(declaringClass, -1 /* trusted */)
                        : MethodHandles.lookup();
        return lookup.unreflectSpecial(method, declaringClass).bindTo(object).invokeWithArguments(args);
    }

    static final class Android extends Platform {
        Android() {
            super(Build.VERSION.SDK_INT >= 24);
        }

        @Override
        public Executor defaultCallbackExecutor() {
            return new Platform.Android.MainThreadExecutor();
        }

        @Nullable
        @Override
        Object invokeDefaultMethod(
                Method method, Class<?> declaringClass, Object object, Object... args) throws Throwable {
            if (Build.VERSION.SDK_INT < 26) {
                throw new UnsupportedOperationException(
                        "Calling default methods on API 24 and 25 is not supported");
            }
            return super.invokeDefaultMethod(method, declaringClass, object, args);
        }

        static final class MainThreadExecutor implements Executor {
            private final Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void execute(Runnable r) {
                handler.post(r);
            }
        }
    }
}
