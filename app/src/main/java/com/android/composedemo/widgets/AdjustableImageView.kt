package com.android.composedemo.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import kotlin.math.roundToInt

/**
 *
 * 根据图片实际宽高比例调整图片的宽高，避免recyclerView中图片因复用导致变形
 * 1、如果高度固定，则宽度根据高度及图片比例计算
 * 2、如果宽度固定，则高度根据宽度及图片比例计算
 * @author dingpeihua
 * @date 2024/5/29 12:06
 **/
open class AdjustableImageView : ImageView {
    /**
     * 图片宽高比
     *  with / height
     *
     */
    private var aspectRatio = 0f

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(
        context, attrs
    )
constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )
    fun setAspectRatio(aspectRatio: Float) {
        this.aspectRatio = aspectRatio
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (aspectRatio > 0) {
            try {
                if (measureViewSize(widthMeasureSpec, heightMeasureSpec, aspectRatio)) return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // aspectRatio <0 高度随图片变
            val d = drawable
            if (d != null) {
                try {
                    val intrinsicWidth = d.intrinsicWidth.toFloat()
                    val intrinsicHeight = d.intrinsicHeight.toFloat()
                    val aspectRatio = intrinsicWidth / intrinsicHeight
                    if (measureViewSize(widthMeasureSpec, heightMeasureSpec, aspectRatio)) return
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun measureViewSize(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        aspectRatio: Float
    ): Boolean {
        val params = layoutParams
        var height = params.height
        var width = params.width
        if (width != ViewGroup.LayoutParams.WRAP_CONTENT && width != ViewGroup.LayoutParams.MATCH_PARENT) {
            width = MeasureSpec.getSize(widthMeasureSpec)
            //宽度定- 高度根据使得图片的宽度充满屏幕
            val imgHeight = (width / aspectRatio).roundToInt()
            setMeasuredDimension(width, imgHeight)
            return true
        } else if (height != ViewGroup.LayoutParams.WRAP_CONTENT && height != ViewGroup.LayoutParams.MATCH_PARENT) {
            height = MeasureSpec.getSize(heightMeasureSpec)
            //高度定- 宽度根据使得图片的高度充满屏幕
            val imgWidth = (height * aspectRatio).roundToInt()
            setMeasuredDimension(imgWidth, height)
            return true
        }
        return false
    }
}
