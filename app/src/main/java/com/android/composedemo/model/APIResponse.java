package com.android.composedemo.model;

import com.google.gson.annotations.SerializedName;

import java.nio.charset.StandardCharsets;

public final class APIResponse<T extends BaseResponseData>  {
    @SerializedName("api")
    private String apiUrl;
    @SerializedName("v")
    private String version;

    @SerializedName("data")
    private T mData;

    private int mDataHash;

    private String mDataMd5;

    private String mTraceId;

    public String getApiUrl() {
        return apiUrl;
    }

    public APIResponse<T> setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public APIResponse<T> setVersion(String version) {
        this.version = version;
        return this;
    }

    public T getData() {
        return mData;
    }

    public APIResponse<T> setData(T data) {
        mData = data;
        return this;
    }

    public int getDataHash() {
        if (mDataHash == 0) {
            setDataHash(new String(mData.toString().getBytes(StandardCharsets.UTF_8)).hashCode());
        }
        return mDataHash;
    }

    public void setDataHash(int hash) {
        this.mDataHash = hash;
    }

    public String getDataMd5() {
        return mDataMd5;
    }

    public void setDataMd5(String md5) {
        this.mDataMd5 = md5;
    }

    public String getTraceId(){
        return mTraceId;
    }

    public void setTraceId(String traceId){
        this.mTraceId = traceId;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
            "apiUrl='" + apiUrl + '\'' +
            ", version='" + version + '\'' +
            ", mData=" + mData +
            '}';
    }
}
