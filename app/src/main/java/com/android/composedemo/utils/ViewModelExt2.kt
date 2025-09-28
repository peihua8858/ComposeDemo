package com.android.composedemo.utils

import androidx.compose.runtime.MutableState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * ViewModel 状态记录
 * @author dingpeihua
 * @date 2021/2/20 11:28
 * @version 1.0
 */
sealed class ResultData<T> {
    class Initialize<T> : ResultData<T>()
    class Starting<T> : ResultData<T>()
    data class Success<T>(val data: T) : ResultData<T>()
    data class Failure<T>(val error: Throwable) : ResultData<T>()

    inline val isSuccess: Boolean
        get() = this is Success
    inline val isError: Boolean
        get() = this is Failure
    val result: T
        get() {
            if (this is Success) {
                return data
            }
            throw IllegalStateException("ResultData is not Success")
        }
    val failure: Throwable
        get() {
            if (this is Failure) {
                return error
            }
            throw IllegalStateException("ResultData is not Failure")
        }
}

private const val TAG = "ResultData"

@OptIn(ExperimentalContracts::class)
fun <T> ResultData<T>.isInitialize(): Boolean {
    contract {
        returns(true) implies (this@isInitialize is ResultData.Initialize)
    }
    return this is ResultData.Initialize
}

@OptIn(ExperimentalContracts::class)
fun <T> ResultData<T>.isSuccess(): Boolean {
    contract {
        returns(true) implies (this@isSuccess is ResultData.Success)
    }
    return this is ResultData.Success
}

@OptIn(ExperimentalContracts::class)
fun <T> ResultData<T>.isError(): Boolean {
    contract {
        returns(true) implies (this@isError is ResultData.Failure)
    }
    return this is ResultData.Failure
}

@OptIn(ExperimentalContracts::class)
fun <T> ResultData<T>.isStarting(): Boolean {
    contract {
        returns(true) implies (this@isStarting is ResultData.Starting)
    }
    return this is ResultData.Starting
}

/**
 * [ViewModel]在主线程中开启协程扩展
 */
fun <T> ViewModel.apiSyncRequest(
    viewState: MutableLiveData<ResultData<T>>,
    apiDSL: ApiModel<T>.() -> Unit,
) {
    ApiModel<T>().apply(apiDSL)
        .parseMethod(viewState)
        .syncLaunch(viewModelScope)

}

fun <T> ViewModel.apiSyncRequest(
    apiDSL: ApiModel<T>.() -> Unit,
) {
    ApiModel<T>().apply(apiDSL)
        .syncLaunch(viewModelScope)

}

/**
 * [ViewModel]在IO线程中开启协程扩展
 */
fun <T> ViewModel.apiRequest(
    viewState: MutableLiveData<ResultData<T>>,
    apiDSL: ApiModel<T>.() -> Unit,
) {
    ApiModel<T>().apply(apiDSL)
        .parseMethod(viewState)
        .launch(viewModelScope)

}

internal fun <T> ApiModel<T>.parseMethod(viewState: MutableLiveData<ResultData<T>>): ApiModel<T> {
    if (!isOnStart()) {
        onStart { viewState.postValue(ResultData.Starting()) }
    }
    if (!isOnError()) {
        onError { viewState.postValue(ResultData.Failure(it)) }
    }
    if (!isOnResponse()) {
        onResponse { viewState.postValue(ResultData.Success(it)) }
    }
    return this
}


/**
 * [ViewModel]在IO线程中开启协程扩展
 */
fun <T> ViewModel.request(
    viewState: MutableState<ResultData<T>>,
    request: suspend CoroutineScope.() -> T,
) {
    viewModelScope.launch(Dispatchers.Main) {
        viewState.value = ResultData.Starting()
        try {
            val response = withContext(Dispatchers.IO) {
                request()
            }
            viewState.value = ResultData.Success(response)
        } catch (e: Throwable) {
            print(e.stackTraceToString())
            viewState.value = ResultData.Failure(e)
        }
    }
}

/**
 * [ViewModel]在IO线程中开启协程扩展
 */
fun <T> ViewModel.request(
    viewState: MutableLiveData<ResultData<T>>,
    request: suspend CoroutineScope.() -> T,
) {
    viewModelScope.launch(Dispatchers.Main) {
        viewState.postValue(ResultData.Starting())
        try {
            val response = withContext(Dispatchers.IO) {
                request()
            }
            viewState.postValue(ResultData.Success(response))
        } catch (e: Throwable) {
            Logcat.d(TAG, e.stackTraceToString())
            viewState.postValue(ResultData.Failure(e))
        }
    }
}
/**
 * [ViewModel]在IO线程中开启协程扩展
 */
fun <T> ViewModel.request(
    viewState: MutableStateFlow<ResultData<T>>,
    request: suspend CoroutineScope.() -> T,
) {
    viewModelScope.launch(Dispatchers.Main) {
        viewState.value = ResultData.Starting()
        try {
            val response = withContext(Dispatchers.IO) {
                request()
            }
            viewState.value = ResultData.Success(response)
        } catch (e: Throwable) {
            print(e.stackTraceToString())
            viewState.value = ResultData.Failure(e)
        }
    }
}

/**
 * [ViewModel]在IO线程中开启协程扩展
 */
fun <T> ViewModel.request2(
    viewState: MutableLiveData<T>,
    request: suspend CoroutineScope.() -> T,
) {
    viewModelScope.launch(Dispatchers.Main) {
        try {
            val response = withContext(Dispatchers.IO) {
                request()
            }
            viewState.postValue(response)
        } catch (e: Throwable) {
            Logcat.d(TAG, e.stackTraceToString())
            viewState.postValue(null)
        }
    }
}


/**
 * [ViewModel]在IO线程中开启协程扩展
 */
fun <T> ViewModel.requestByIO(
    viewState: MutableLiveData<T>,
    request: suspend CoroutineScope.() -> T,
) {
    viewModelScope.launch(Dispatchers.Main) {
        try {
            val response = withContext(Dispatchers.IO) {
                request()
            }
            viewState.postValue(response)
        } catch (e: Throwable) {
            Logcat.d(TAG, e.stackTraceToString())
            viewState.postValue(null)
        }
    }
}

fun AndroidViewModel.getString(id: Int): String {
    return getApplication<android.app.Application>().getString(id)
}

fun AndroidViewModel.getString(id: Int, vararg args: Any): String {
    return getApplication<android.app.Application>().getString(id, *args)
}

