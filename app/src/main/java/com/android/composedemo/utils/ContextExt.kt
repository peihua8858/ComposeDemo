@file:JvmName("ContextUtil")
@file:JvmMultifileClass

package com.android.composedemo.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowMetrics
import android.view.inputmethod.InputMethodManager
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.fontscaling.FontScaleConverterFactory
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastRoundToInt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding


/**
 * 输入管理器
 * @author dingpeihua
 * @date 2021/9/28 16:41
 * @version 1.0
 */
val Context?.inputMethodManager: InputMethodManager?
    get() = this?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

val Context.isLandScape: Boolean
    get() {
        val isLandScape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        Log.d("isLandScape", isLandScape.toString())
        Log.d("isLandScape", resources.configuration.orientation.toString())
        return isLandScape
    }

//val ViewBinding.isLandScape: Boolean
//    get() = root.isLandScape
val View.isLandScape: Boolean
    get() = context.isLandScape
val Context.isPortrait: Boolean
    get() {
        val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        Log.d("isPortrait", isPortrait.toString())
        Log.d("isPortrait", resources.configuration.orientation.toString())
        return isPortrait
    }
val View.isPortrait: Boolean
    get() = context.isPortrait
val View.packageManager: PackageManager
    get() = context.packageManager
val Context.screenWidth: Int
    get() {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            return windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.widthPixels
        }
    }
val Activity.screenWidth: Int
    get() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            return windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.widthPixels
        }
    }
val Activity.screenHeight: Int
    get() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            return windowMetrics.bounds.height() - insets.top - insets.bottom
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.widthPixels
        }
    }
val Fragment.screenWidth: Int
    get() = activity?.screenWidth ?: 0
val Fragment.screenHeight: Int
    get() = activity?.screenHeight ?: 0
val View.screenWidth: Int
    get() = (context as Activity).screenWidth
val View.screenHeight: Int
    get() = (context as Activity).screenHeight

fun ViewBinding.getDimensionPixelSize(id: Int): Int {
    return root.resources.getDimensionPixelSize(id)
}

fun Context.getDimensionPixelSize(id: Int): Int {
    return resources.getDimensionPixelSize(id)
}

fun Context.getDimension(id: Int): Float {
    return resources.getDimension(id)
}

fun ViewBinding.getDimension(id: Int): Float {
    return root.resources.getDimension(id)
}

fun ViewBinding.getDrawableCompat(@DrawableRes id: Int): Drawable? {
    if (id == 0) {
        return null
    }
    return ContextCompat.getDrawable(root.context, id)
}

fun Context.getDrawableCompat(@DrawableRes id: Int): Drawable? {
    if (id == 0) {
        return null
    }
    return ContextCompat.getDrawable(this, id)
}

@Composable
@ReadOnlyComposable
fun dimensionResourceByPx(@DimenRes id: Int): Float {
    val context = LocalContext.current
    val pxValue = context.resources.getDimensionPixelSize(id)
    return pxValue.toFloat()
}

@Composable
@ReadOnlyComposable
fun Int.toDp(): Dp {
    val density = LocalDensity.current
    return Dp(this / density.density)
}

@Composable
@ReadOnlyComposable
fun Float.toDp(): Dp {
    val density = LocalDensity.current
    return Dp(this / density.density)
}

@Composable
fun Int.toSp(): TextUnit {
    return toDp().toSp()

}

@Composable
fun Float.toSp(): TextUnit {
    return toDp().toSp()
}

@Composable
@ReadOnlyComposable
fun Dp.toPx(): Float {
    val density = LocalDensity.current
    return (this.value * density.density)
}

@Composable
fun Dp.roundToPx(): Int {
    val px = toPx()
    return if (px.isInfinite()) Constraints.Infinity else px.fastRoundToInt()
}

private const val MinScaleForNonLinear = 1.03f


@SuppressLint("RestrictedApi")
@Composable
@ReadOnlyComposable
fun Dp.toSp(): TextUnit {
    val density = LocalDensity.current
    val fontScale = density.fontScale
    if (!(fontScale >= MinScaleForNonLinear)) {
        return (value / fontScale).sp
    }
    val converter = FontScaleConverterFactory.forScale(fontScale)
    return (converter?.convertDpToSp(value) ?: (value / fontScale)).sp
}

