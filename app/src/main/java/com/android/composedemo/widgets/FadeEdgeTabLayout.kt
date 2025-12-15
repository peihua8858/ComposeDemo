package com.android.composedemo.widgets

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.alibaba.genie.studycenter.geniebaselib.view.indicator.TabLayout
import androidx.core.graphics.withTranslation
import com.alibaba.genie.studycenter.content.R

/**
 * 带淡入淡出遮罩的 TabLayout
 */
class FadeEdgeTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TabLayout(context, attrs, defStyleAttr) {

    enum class FadeDirection(val value: Int) { LEFT(1), RIGHT(2), TOP(4), BOTTOM(8) }

    private val fadePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var fadeWidthPx = dpToPx(48)
    private var fadeStartColor = Color.TRANSPARENT
    private var fadeEndColor = Color.BLACK
    private var fadeDirections = mutableSetOf(FadeDirection.LEFT, FadeDirection.RIGHT)

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.FadeEdgeTabLayout,
            defStyleAttr,
            0
        ).apply {
            fadeWidthPx = getDimensionPixelSize(
                R.styleable.FadeEdgeTabLayout_fadeWidth, fadeWidthPx
            )
            fadeStartColor = getColor(
                R.styleable.FadeEdgeTabLayout_fadeStartColor, fadeStartColor
            )
            fadeEndColor = getColor(
                R.styleable.FadeEdgeTabLayout_fadeEndColor, fadeEndColor
            )

            val dirFlags = getInt(R.styleable.FadeEdgeTabLayout_fadeDirection, 1 or 2) // 默认左右
            fadeDirections.clear()
            FadeDirection.values().forEach { dir ->
                if (dirFlags and dir.value == dir.value) {
                    fadeDirections.add(dir)
                }
            }
            recycle()
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        // 绘制 TabLayout 的内容
        val savedLayer = canvas.saveLayer(null, null)
        super.dispatchDraw(canvas)

        // 修复遮罩跟随滚动问题
        canvas.withTranslation(scrollX.toFloat(), 0f) {
            fadeDirections.forEach { dir ->
                when (dir) {
                    FadeDirection.LEFT -> drawLeftFade(canvas)
                    FadeDirection.RIGHT -> drawRightFade(canvas)
                    FadeDirection.TOP -> drawTopFade(canvas)
                    FadeDirection.BOTTOM -> drawBottomFade(canvas)
                }
            }

            fadePaint.xfermode = null
        }
        canvas.restoreToCount(savedLayer)
    }

    private fun drawLeftFade(canvas: Canvas) {
        fadePaint.shader = LinearGradient(
            0f, 0f, fadeWidthPx.toFloat(), 0f,
            fadeStartColor, fadeEndColor,
            Shader.TileMode.CLAMP
        )
        fadePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        canvas.drawRect(0f, 0f, fadeWidthPx.toFloat(), height.toFloat(), fadePaint)
    }

    private fun drawRightFade(canvas: Canvas) {
        fadePaint.shader = LinearGradient(
            (width - fadeWidthPx).toFloat(), 0f, width.toFloat(), 0f,
            fadeEndColor, fadeStartColor,
            Shader.TileMode.CLAMP
        )
        fadePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        canvas.drawRect(
            (width - fadeWidthPx).toFloat(),
            0f,
            width.toFloat(),
            height.toFloat(),
            fadePaint
        )
    }

    private fun drawTopFade(canvas: Canvas) {
        fadePaint.shader = LinearGradient(
            0f, 0f, 0f, fadeWidthPx.toFloat(),
            fadeStartColor, fadeEndColor,
            Shader.TileMode.CLAMP
        )
        fadePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        canvas.drawRect(0f, 0f, width.toFloat(), fadeWidthPx.toFloat(), fadePaint)
    }

    private fun drawBottomFade(canvas: Canvas) {
        fadePaint.shader = LinearGradient(
            0f, (height - fadeWidthPx).toFloat(), 0f, height.toFloat(),
            fadeEndColor, fadeStartColor,
            Shader.TileMode.CLAMP
        )
        fadePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        canvas.drawRect(
            0f,
            (height - fadeWidthPx).toFloat(),
            width.toFloat(),
            height.toFloat(),
            fadePaint
        )
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
