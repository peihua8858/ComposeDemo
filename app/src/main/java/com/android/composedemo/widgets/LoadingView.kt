package com.android.composedemo.widgets

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.AnimatedImageDrawable
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import com.android.composedemo.R
import com.android.composedemo.utils.isAtLeastP


class LoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AdjustableImageView(context, attrs, defStyleAttr) {
    // 创建旋转动画对象
    private var rotateAnimation: RotateAnimation

    init {
        val rotateAnimation = RotateAnimation(
            0f,
            360f,
            RotateAnimation.RELATIVE_TO_SELF,
            0.5f,
            RotateAnimation.RELATIVE_TO_SELF,
            0.5f
        )
        rotateAnimation.setDuration(500) // 动画周期500毫秒
        rotateAnimation.interpolator = LinearInterpolator()
        rotateAnimation.fillAfter = true
        rotateAnimation.repeatCount = ObjectAnimator.INFINITE // 无限循环
        this.rotateAnimation = rotateAnimation
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 启动动画集
        setImageResource(R.mipmap.ic_loading22)
        startAnimation()
    }

    fun startAnimation() {
        if (visibility == VISIBLE) {
            val drawable = drawable
            if (isAtLeastP && drawable is AnimatedImageDrawable) {
                drawable.repeatCount = AnimatedImageDrawable.REPEAT_INFINITE
                drawable.start()
                return
            } else {
                startAnimation(rotateAnimation)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 停止动画集
        stopAnimation()
    }

    fun stopAnimation() {
        val drawable = drawable
        if (isAtLeastP && drawable is AnimatedImageDrawable) {
            drawable.stop()
            return
        } else {
            clearAnimation()
        }
    }

    override fun setVisibility(visibility: Int) {
        val currentVisibility = getVisibility()
        super.setVisibility(visibility)
        if (visibility != currentVisibility) {
            if (visibility == VISIBLE) {
                startAnimation()
            } else if (visibility == GONE || visibility == INVISIBLE) {
                stopAnimation()
            }
        }
    }
}