package com.android.composedemo.model;

import android.text.TextUtils;

public class ResultCode {
    public static final String SUCCESS = "200";
    public static final String SUCCESS_1 = "1";
    public static final String SUCCESS_0 = "0";
    public static final String CODE_ANDROID_SYS_NO_NETWORK = "AEC10000";
    public static final String CODE_ANDROID_SYS_NETWORK_ERROR = "AEC10001";
    public static final String ANDROID_SYS_NETWORK_ERROR = "ANDROID_SYS_NETWORK_ERROR";

    public static boolean isNoNetWork(String mappingCode, String msg) {
        if (TextUtils.isEmpty(mappingCode)) {
            return false;
        }
        if (CODE_ANDROID_SYS_NO_NETWORK.equalsIgnoreCase(mappingCode)
                || CODE_ANDROID_SYS_NETWORK_ERROR.equalsIgnoreCase(mappingCode)
                || mappingCode.contains(CODE_ANDROID_SYS_NETWORK_ERROR)
                || mappingCode.contains(CODE_ANDROID_SYS_NO_NETWORK)
                || ANDROID_SYS_NETWORK_ERROR.equalsIgnoreCase(msg)
                || !TextUtils.isEmpty(msg) && msg.contains(ANDROID_SYS_NETWORK_ERROR)
        ) {
            return true;
        } else {
            return false;
        }
    }
}
