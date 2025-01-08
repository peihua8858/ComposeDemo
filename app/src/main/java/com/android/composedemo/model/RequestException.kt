package com.android.composedemo.model

/**
 *
 *  异常包装
 * @author dingpeihua
 * @date 2024/6/18 20:08
 **/
class RequestException(val code: String, message: String) : RuntimeException(message) {
    constructor(code: Int, message: String) : this(code.toString(), message)
}