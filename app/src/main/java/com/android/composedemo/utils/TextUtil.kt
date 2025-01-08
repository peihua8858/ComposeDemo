package com.android.composedemo.utils

/**
 *
 * 限制指定的字符数[maxLength],超过[maxLength]的字符用...代替
 * @param
 * @return
 * @date 2024/5/6 15:08
 **/
fun String?.ellipsize(maxLength: Int): String? {
    if (this == null || this.length <= maxLength) {
        return this
    }
    return (this.subSequence(0, maxLength) + "...").toString()

}

private operator fun CharSequence.plus(s: String): CharSequence {
    val sb = StringBuilder(this)
    sb.append(s)
    return sb
}

fun CharSequence?.substringSafety(startIndex: Int, endIndex: Int): CharSequence? {
    if (this == null) {
        return null
    }
    return this.subSequence(startIndex, kotlin.math.min(endIndex, this.length))+" "
}
/**
 * 将Object对象转成Integer类型
 *
 * @param value
 * @return 如果value不能转成Integer，则默认0
 */
fun Any?.toInt(): Int {
    return this.toInt(0)
}

/**
 * 将Object对象转成Integer类型
 *
 * @param value
 * @return 如果value不能转成Integer，则默认0
 */
fun Any?.toInt(defaultValue: Int = 0): Int {
    when (this) {
        is Int -> {
            return this
        }
        is Number -> {
            return this.toInt()
        }
        is String -> {
            try {
                return this.toDouble().toInt()
            } catch (ignored: Exception) {
            }
        }
        else -> {
            try {
                return Integer.valueOf((this.toString()))
            } catch (ignored: NumberFormatException) {
            }
        }
    }
    return defaultValue
}

/**
 * 将Object对象转成Integer类型
 *
 * @param value
 * @return 如果value不能转成Integer，则默认0
 */
fun Any?.toFloat(): Float {
    return this.toFloat(0f)
}

/**
 * 将Object对象转成Integer类型
 *
 * @param value
 * @return 如果value不能转成Integer，则默认0
 */
fun Any?.toFloat(defaultValue: Float = 0f): Float {
    when (this) {
        is Float -> {
            return this
        }
        is Number -> {
            return this.toFloat()
        }
        is String -> {
            try {
                return this.toDouble().toFloat()
            } catch (ignored: Exception) {
            }
        }
        else -> {
            try {
                return this.toString().toFloat()
            } catch (ignored: NumberFormatException) {
            }
        }
    }
    return defaultValue
}