package com.android.composedemo.model;

import com.google.gson.annotations.SerializedName;

public class BaseResponse<T> extends BaseResponseData {

    @SerializedName("model")
    private T model;

    public T getModel() {
        return model;
    }

    public void setModel(T model) {
        this.model = model;
    }

    @Override
    public int hashCode() {
        if (model == null) {
            return super.hashCode();
        }
        return model.hashCode();
    }

    @Override
    public String toString() {
        return "BaseResponse{" +
                "model=" + model +
                ", ret=" + ret +
                ", success=" + success +
                ", msgCode='" + msgCode + '\'' +
                ", msgInfo='" + msgInfo + '\'' +
                '}';
    }
}
