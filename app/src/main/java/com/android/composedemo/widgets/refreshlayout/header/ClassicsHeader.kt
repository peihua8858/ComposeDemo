package com.android.composedemo.widgets.refreshlayout.header

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import com.android.composedemo.R
import com.android.composedemo.utils.getDimensionPixelSize
import com.android.composedemo.utils.isAtLeastP
import com.android.composedemo.utils.toInt
import com.android.composedemo.widgets.refreshlayout.PullRefreshLayout

/**
 *
 * 下拉刷新头部视图
 * @author dingpeihua
 * @date 2024/8/1 14:59
 **/
class ClassicsHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), PullRefreshLayout.OnPullListener {
    private var mArrowView: ImageView
    private var isLoadingFinish = false
    private val mRotateAnimation: RotateAnimation
    private val mFrameAnimator: ValueAnimator
    private val frameResources = intArrayOf(
        R.mipmap.ic_loading2, R.mipmap.ic_loading3, R.mipmap.ic_loading4
    )
    private var currentFrameIndex = 0

    init {
        val dp40 = getDimensionPixelSize(R.dimen.dp_40)
        val dp48 = getDimensionPixelSize(R.dimen.dp_48)
        minimumHeight = dp40
        val lpProgress = LayoutParams(dp48, dp48)
        lpProgress.addRule(CENTER_IN_PARENT)
        val progressLayout = FrameLayout(context)
        mArrowView = ImageView(context)
        mArrowView.setImageResource(R.mipmap.ic_loading1)
        val p = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        p.gravity = Gravity.CENTER
        progressLayout.addView(mArrowView, p)
        addView(progressLayout, lpProgress)
        val rotateAnimation = RotateAnimation(
            0f,
            360f,
            RotateAnimation.RELATIVE_TO_SELF,
            0.5f,
            RotateAnimation.RELATIVE_TO_SELF,
            0.5f
        )
        rotateAnimation.duration = 400 // 动画周期200毫秒
        rotateAnimation.interpolator = LinearInterpolator()
        rotateAnimation.fillAfter = true
        rotateAnimation.repeatCount = ObjectAnimator.INFINITE // 无限循环
        this.mRotateAnimation = rotateAnimation

        val animator = ValueAnimator.ofInt(0, frameResources.size - 1)
        animator.setDuration(30) // 动画总时长
        animator.repeatCount = 0 // 不重复
        animator.repeatMode = ValueAnimator.RESTART // 每次重新开始
        animator.addUpdateListener { animation ->
            currentFrameIndex = animation.animatedValue.toInt()
            mArrowView.setImageResource(frameResources[currentFrameIndex])
        }
        animator.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator) {
                startRotateAnimation()
            }
        })
        mFrameAnimator = animator
    }


    var isRotate: Boolean = true

    override fun onPullChange(percent: Float) {
        if (isRotate) {
            mArrowView.rotation = percent * 360
        }
    }

    override fun onPullHoldTrigger() {
        //释放立即刷新
        isRotate = false
        isLoadingFinish = false
        stopAnimation(false)
        mArrowView.setImageResource(R.mipmap.ic_loading1)
    }

    override fun onPullHoldUnTrigger() {
        //下拉可以刷新
        isRotate = true
        isLoadingFinish = false
        mArrowView.visibility = VISIBLE
        stopAnimation(false)
        mArrowView.setImageResource(R.mipmap.ic_loading1)
    }
    private val animationCallback = object : Animatable2.AnimationCallback() {
        override fun onAnimationEnd(d: Drawable) {
            if (isAtLeastP && d is AnimatedImageDrawable) {
                d.stop()
                //由于动画不能从头开始，所以动画结束而接口还未请求完成，则继续旋转View
                if (!isLoadingFinish) {
                    startRotateAnimation()
                }
            }
        }
    }
    override fun onPullHolding() {
        Log.e("onPullHolding", "onPullHolding: ")
        val drawable = mArrowView.drawable
        if (isAtLeastP && drawable is AnimatedImageDrawable) {
            drawable.repeatCount = 0//不重复，此处不能重复，如果加载数据时间长于动画时长，则动画会从头开始，这与UI预期不符
            drawable.registerAnimationCallback(animationCallback)
            drawable.start()
            return
        }
        starFrameAnimation()
    }


    private fun starFrameAnimation() {
        mFrameAnimator.start()
    }

    private fun startRotateAnimation() {
        mArrowView.startAnimation(mRotateAnimation)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation(true)
    }

    override fun onPullFinish(flag: Int) {
        isLoadingFinish = true
        Log.e("onPullFinish", "onPullFinish: ")
        //刷新完成
        stopAnimation(false)
    }

    override fun onPullReset() {
        Log.e("onPullReset", "onPullReset: ")
        onPullHoldUnTrigger()
    }

    private fun stopAnimation(rmCallback:Boolean) {
        val drawable = mArrowView.drawable
        if (isAtLeastP && drawable is AnimatedImageDrawable) {
            drawable.stop()
            if (rmCallback) {
                drawable.unregisterAnimationCallback(animationCallback)
            }
        }
        mFrameAnimator.cancel()
        mArrowView.clearAnimation()
    }
}
