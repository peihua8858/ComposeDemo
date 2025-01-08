package com.android.composedemo.utils

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import kotlin.math.min

/**
 * 根据列数[spanCount]将数据[this]按最大长度拆分成多行数据
 */
fun <T> MutableList<T>.subListToLines(
    spanCount: Int,
    maxCount: Int
): MutableList<MutableList<T>> {
    var lines = maxCount / spanCount
    if (maxCount % spanCount > 0) {
        lines++
    }
    val result = mutableListOf<MutableList<T>>()
    for (i in 0 until lines) {
        val fromIndex = i * spanCount
        val toIndex = fromIndex + spanCount
        result.add(subList(fromIndex, min(size, toIndex)))
    }
    return result
}


/**
 * 对象深度拷贝
 * [T] extend [Serializable]
 * @author dingpeihua
 * @date 2021/3/2 15:29
 * @version 1.0
 */
fun <T : Serializable> Collection<T>.deepClone(): Collection<T>? {
    try {
        ByteArrayOutputStream().use { byteOut ->
            ObjectOutputStream(byteOut).use { out ->
                out.writeObject(this)
                out.flush()
                ObjectInputStream(ByteArrayInputStream(byteOut.toByteArray())).use { input ->
                    return this::class.java.cast(input.readObject())
                }
            }
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        return null
    }
}

fun <T> Iterable<T>?.toArrayList(): ArrayList<T> {
    if (this == null) return arrayListOf()
    val it = this.iterator()
    val result = ArrayList<T>()
    while (it.hasNext()) {
        result.add(it.next())
    }
    return result
}