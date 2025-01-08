package com.android.composedemo;


import com.android.composedemo.utils.Logcat;
import com.fz.gson.GsonFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AbstractResponseCallback<T> {
    private Type clazz;

    public AbstractResponseCallback() {
        Class<?> callbackClass = getClass();
        Type[] types = callbackClass.getGenericInterfaces();
        Type type;
        if (types.length == 0) {
            type = callbackClass.getGenericSuperclass();
        } else {
            type = types[0];
        }
        Type[] args = ((ParameterizedType) type).getActualTypeArguments();
        this.clazz = args[0];
    }

    public final void onResponseCallback(String response) {
        Logcat.d("readLocalData2", "readLocalData2>>>clazz = " + clazz);
        T result = GsonFactory.createGson().fromJson(response, clazz);
        onBizSucceed(result);
    }

    public abstract void onBizSucceed(T data);
}
