package com.android.composedemo.model;


import com.google.gson.annotations.SerializedName;

public abstract class BaseResponseData {
    @SerializedName("ret")
    protected Ret ret;

    @SerializedName("success")
    protected boolean success;

    @SerializedName("msgCode")
    protected String msgCode;

    @SerializedName("msgInfo")
    protected String msgInfo;

    public Ret getRet() {
        return ret;
    }

    public BaseResponseData setRet(Ret ret) {
        this.ret = ret;
        return this;
    }

    public boolean isSuccess() {
        return success;
    }

    public BaseResponseData setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public String getMsgCode() {
        return msgCode;
    }

    public BaseResponseData setMsgCode(String msgCode) {
        this.msgCode = msgCode;
        return this;
    }

    public String getMsgInfo() {
        return msgInfo;
    }

    public BaseResponseData setMsgInfo(String msgInfo) {
        this.msgInfo = msgInfo;
        return this;
    }

    public static final class Ret {
        private int code;
        private String msg;

        public int getCode() {
            return code;
        }

        public Ret setCode(int code) {
            this.code = code;
            return this;
        }

        public String getMsg() {
            return msg;
        }

        public Ret setMsg(String msg) {
            this.msg = msg;
            return this;
        }

        @Override
        public String toString() {
            return "Ret{" +
                    "code=" + code +
                    ", msg='" + msg + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "BaseResponseData{" +
                "ret=" + ret +
                ", success=" + success +
                ", msgCode='" + msgCode + '\'' +
                ", msgInfo='" + msgInfo + '\'' +
                '}';
    }
}
