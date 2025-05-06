package com.android.composedemo.utils

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.Px
import androidx.annotation.StringRes
import androidx.core.graphics.TypefaceCompat

fun View.setMarginTop(@Px marginTop: Int) {
    var lp = layoutParams as? MarginLayoutParams
    if (lp == null) {
        lp = MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    lp.topMargin = marginTop
    layoutParams = lp
}

fun View.setMarginStart(@Px marginStart: Int) {
    var lp = layoutParams as? MarginLayoutParams
    if (lp == null) {
        lp = MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    lp.marginStart = marginStart
    layoutParams = lp
}

fun View.setMarginEnd(@Px end: Int) {
    var lp = layoutParams as? MarginLayoutParams
    if (lp == null) {
        lp = MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    lp.marginEnd = end
    layoutParams = lp
}

fun View.setMargin(
    @Px marginStart: Int,
    @Px marginTop: Int,
    @Px marginEnd: Int,
    @Px marginBottom: Int
) {
    var lp = layoutParams as? MarginLayoutParams
    if (lp == null) {
        lp = MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    lp.topMargin = marginTop
    lp.leftMargin = marginStart
    lp.rightMargin = marginEnd
    lp.bottomMargin = marginBottom
    layoutParams = lp
}

fun View.setPaddingTop(@Px top: Int) {
    setPaddingRelative(paddingStart, top, paddingEnd, paddingBottom)
}

fun View.setPaddingBottom(@Px bottom: Int) {
    setPaddingRelative(paddingStart, paddingTop, paddingEnd, bottom)
}

///**
// * 设置Edit text cursor color
// *
// */
//fun EditText.setCursorColor(color: Int) {
//    try {
//        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_search_cursor)
//        drawable?.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
//        setCursorColorDrawable(drawable)
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }
//}

/**
 *
 * 设置Edit text cursor color
 * @param drawable drawable
 * @date 2024/5/13 17:38
 **/
fun EditText.setCursorColorDrawable(drawable: Drawable?) {
    try {
        val fEditor = TextView::class.java.getDeclaredField("mEditor")
        fEditor.isAccessible = true
        val editor = fEditor.get(this)
        val editorClass = editor.javaClass
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setTextCursorDrawable(drawable)
        }
        val cursorDrawable = editorClass.getDeclaredField("mDrawableForCursor")
        cursorDrawable.isAccessible = true
        cursorDrawable.set(editor, drawable)
        val method = editorClass.getDeclaredMethod("updateCursorPosition")
        method.isAccessible = true
        method.invoke(editor)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun ViewGroup.inflate(layoutRes: Int): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, false)
}
//
//fun View.comViewAnimation(
//    duration: Long,
//    isOpen: Boolean,
//    model: (AnimatorListenerModel<Animator>.() -> Unit)? = null
//) {
//    val screenWidth = screenWidth
//    val screenHeight = screenHeight
//    GSearchLog.d("ViewExt", "screenWidth = $screenWidth, screenHeight = $screenHeight")
//    pivotX = screenWidth / 2f
//    pivotY = 0f
//    // 属性持有者为缩放和透明度
//    val scaleXAni = PropertyValuesHolder.ofFloat("scaleX", scaleX, if (isOpen) 1f else 1.8f)
//    val scaleYAni = (PropertyValuesHolder.ofFloat("scaleY", scaleY, if (isOpen) 1f else 1.8f))
//    val alphaAni = (PropertyValuesHolder.ofFloat("alpha", alpha, if (isOpen) 1f else 0f))
//    ObjectAnimator.ofPropertyValuesHolder(this, scaleXAni, scaleYAni, alphaAni).apply {
//        this.duration = duration
//        if (isOpen) {
//            //cubic-bezier(0.92, 0.28, 0.18, 1.04)
//            //cubic-bezier(0.98, 0.00, 1.00, 0.98)
//            //cubic-bezier(0.25, 0.10, 0.25, 1.00)
//            //https://cubic-bezier.com/#.26,1,.48,1
//            //https://cubic-bezier.tupulin.com/#cubic-bezier(0.26, 1.00, 0.48, 1.00)
//            interpolator = PathInterpolatorCompat.create(
//                0.26f, 1.00f, 0.48f, 1.00f
////                0.25f, 0.10f, 0.25f, 1.00f
//            )
//        }
//        if (model != null) {
//            val listener = AnimatorListenerModel<Animator>().apply(model)
//            addListener(InternalAnimatorListenerAdapter(listener))
//        }
//        start()
//    }
//}

fun TextView.setTextWeight(weigh: Int, fontFamily: String = "noto-sans-sc") {
    val textStyle = Typeface.create(fontFamily, Typeface.NORMAL)
    val typeface = TypefaceCompat.create(context, textStyle, weigh, false)
    this.typeface = typeface
}

fun TextView?.setDrawableStart(start: Drawable?): Drawable? {
    if (this == null) {
        return null
    }
    val drawables: Array<Drawable> = this.compoundDrawablesRelative
    if (start != null && start.bounds.isEmpty) {
        this.setCompoundDrawablesRelativeWithIntrinsicBounds(
            start, drawables[1],
            drawables[2], drawables[3]
        )
    } else {
        this.setCompoundDrawablesRelative(
            start, drawables[1],
            drawables[2], drawables[3]
        )
    }
    return start
}

fun View.getDimensionPixelSize(id: Int): Int {
    return resources.getDimensionPixelSize(id)
}

fun View.getDimension(id: Int): Float {
    return resources.getDimension(id)
}

fun View.getString(@StringRes id: Int): String {
    return resources.getString(id)
}

fun ViewGroup.getItemView(layoutRes: Int): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, false)
}

fun View.dp2px(dp: Float): Float {
    return dp * resources.displayMetrics.density
}
fun View.px2dp(px: Float): Float {
    return px / resources.displayMetrics.density
}
fun View.sp2px(sp: Float): Float {
    return sp * resources.displayMetrics.scaledDensity
}
fun View.px2sp(px: Float): Float {
    return px / resources.displayMetrics.scaledDensity
}
