@file:JvmName("Utils")
@file:JvmMultifileClass

package com.android.composedemo.utils

import android.content.Context
import android.os.Looper
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

fun Any?.isMainThread(): Boolean {
    return Looper.myLooper() === Looper.getMainLooper()
}

fun Any?.isWorkThread(): Boolean {
    return Looper.myLooper() !== Looper.getMainLooper()
}

fun Any?.checkMainThread(msg: String?): Boolean {
    if (isMainThread()) {
        return true
    }
    throw IllegalStateException(msg)
}

fun Any?.checkMainThread(msg: () -> String): Boolean {
    if (isMainThread()) {
        return true
    }
    throw IllegalStateException(msg())
}

fun Any?.checkWorkThread(msg: () -> String): Boolean {
    if (isWorkThread()) {
        return true
    }
    throw IllegalStateException(msg())
}

/**
 * 布局方向是从右到左
 *
 * @author dingpeihua
 * @date 2017/8/29 15:11
 * @version 1.0
 */
fun Any?.isAppRtl(local: Locale?): Boolean {
    return TextUtilsCompat.getLayoutDirectionFromLocale(local) == ViewCompat.LAYOUT_DIRECTION_RTL
}

/**
 * 布局方向是从右到左
 *
 * @author dingpeihua
 * @date 2017/8/29 15:11
 * @version 1.0
 */
fun Locale?.isAppRtl(): Boolean {
    return TextUtilsCompat.getLayoutDirectionFromLocale(this) == ViewCompat.LAYOUT_DIRECTION_RTL
}

/**
 * 布局方向是从左到右
 *
 * @author dingpeihua
 * @date 2017/8/29 15:11
 * @version 1.0
 */
fun Locale?.isAppLtr(): Boolean {
    return TextUtilsCompat.getLayoutDirectionFromLocale(this) == ViewCompat.LAYOUT_DIRECTION_LTR
}

/**
 * 布局方向是从左到右
 *
 * @author dingpeihua
 * @date 2017/8/29 15:11
 * @version 1.0
 */
fun Any?.isAppLtr(local: Locale?): Boolean {
    return TextUtilsCompat.getLayoutDirectionFromLocale(local) == ViewCompat.LAYOUT_DIRECTION_LTR
}

fun Context.isRtl(): Boolean {
    if (isN) {
        return resources.configuration.locales.get(0).isAppRtl()
    }
    return resources.configuration.locale.isAppRtl()
}

@OptIn(ExperimentalContracts::class)
fun Any?.isNotNull(): Boolean {
    contract {
        returns(true) implies (this@isNotNull != null)
    }
    return this != null
}

@OptIn(ExperimentalContracts::class)
fun Any?.isNull(): Boolean {
    contract {
        returns(false) implies (this@isNull != null)
    }
    return this == null
}

@OptIn(ExperimentalContracts::class)
fun Any?.checkNotNull(value: Any?, msg: String): Boolean {
    contract {
        returns() implies (value != null)
    }
    return checkNotNull(value) { msg }
}

@OptIn(ExperimentalContracts::class)
fun <T : Any> checkNotNull(value: T?, lazyMessage: () -> Any): Boolean {
    contract {
        returns() implies (value != null)
    }
    if (value == null) {
        val message = lazyMessage()
        throw IllegalStateException(message.toString())
    } else {
        return false
    }
}

fun Any?.checkNotNull(msg: String?): Boolean {
    if (isNotNull()) {
        return true
    }
    throw NullPointerException(msg)
}

fun Any.rangeArray(min: Int, length: Int): Array<String?> {
    val data: Array<String?> = arrayOfNulls(length)
    for (i in 0 until length) {
        data[i] = ((min + i).toString())
    }
    return data
}

suspend fun <T> Any?.retryIO(
    times: Int = Int.MAX_VALUE,
    initialDelay: Long = 100, // 0.1 second
    maxDelay: Long = 1000,    // 1 second
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: Exception) {
            eLog { e.stackTraceToString() }
            // you can log an error here and/or make a more finer-grained
            // analysis of the cause to see if retry is needed
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block() // last attempt
}

fun Any.toMap(): MutableMap<String, Any> {
    val fields = javaClass.declaredFields
    val result = mutableMapOf<String, Any>()
    try {
        for (field in fields) {
            val key = field.name
            if (key.isNotEmpty()) {
                field.isAccessible = true
                val value = field.get(this)
                if (value != null) {
                    result[key] = value
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result
}