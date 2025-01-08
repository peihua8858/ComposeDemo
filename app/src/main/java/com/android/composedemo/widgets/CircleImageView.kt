package com.android.composedemo.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Xfermode
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.ColorInt
import com.android.composedemo.R
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 带双层边框的圆形imageView
 */
class CircleImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {
    private val xfermode: Xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)

    private var width = 0
    private var height = 0
    private var radius = 0f

    private val srcRectF = RectF()

    /**
     * 圆形ImageView的外有边框的宽度
     */
     var mBorderWidth = 0f

    /**
     * 圆形ImageView的外有边框的的颜色
     */
     var mBorderColor = 0

    /**
     * 圆形ImageView的内边框宽度
     */
     var mInnerBorderWidth = 0f

    /**
     * 圆形ImageView的内边框颜色
     */
     var mInnerBorderColor = 0

    private val path = Path()
    private val paint = Paint()

    /**
     * 圆形ImageView的边框总宽度
     */
    private var mBorderTotalWidth = 0f

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyleAttr, 0)
        mBorderWidth = a.getDimension(R.styleable.CircleImageView_civ_border_width, 0f)
        mBorderColor = a.getColor(R.styleable.CircleImageView_civ_border_color, Color.TRANSPARENT)
        mInnerBorderWidth = a.getDimension(R.styleable.CircleImageView_civ_inner_border_width, 0f)
        mInnerBorderColor =
            a.getColor(R.styleable.CircleImageView_civ_inner_border_color, Color.TRANSPARENT)
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val mWidth = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        val mHeight = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        mBorderTotalWidth = mInnerBorderWidth + mBorderWidth
        setMeasuredDimension(
            (mWidth + mBorderTotalWidth * 2f).roundToInt(),
            (mHeight + mBorderTotalWidth * 2f).roundToInt()
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        width = w
        height = h
        radius = ((min(width.toFloat(), height.toFloat()) - mBorderTotalWidth * 2) / 2.0f)
        srcRectF[0f, 0f, width.toFloat()] = height.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        // 使用图形混合模式来显示指定区域的图片
        canvas.saveLayer(srcRectF, null, Canvas.ALL_SAVE_FLAG)
        val sx = 1.0f * (width - mBorderTotalWidth * 2) / width
        val sy = 1.0f * (height - mBorderTotalWidth * 2) / height
        // 缩小画布&#xff0c;使图片内容不被border、padding覆盖
        canvas.scale(sx, sy, (getWidth()) / 2.0f, (getHeight()) / 2.0f)

        super.onDraw(canvas)
        paint.reset()
        path.reset()
        path.addCircle(getWidth() / 2.0f, getHeight() / 2.0f, radius, Path.Direction.CCW)

        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.setXfermode(xfermode)
        canvas.drawPath(path, paint)
        paint.setXfermode(null)

        // 恢复画布
        canvas.restore()

        //画圆角ImageView的边框
        if (mBorderWidth > 0 || mInnerBorderWidth > 0) {
            drawRoundBorder(canvas)
        }
    }

    /**
     * 设置带有边框的圆角ImageView
     *
     * @param borderWidth 边框宽度
     * @param borderColor 边框颜色
     */
    fun setRoundBorderImageView(borderWidth: Float, @ColorInt borderColor: Int) {
        this.mBorderColor = borderColor
        this.mBorderWidth = borderWidth
    }

    /**
     * 设置带有边框的圆角ImageView
     *
     * @param borderWidth      边框宽度
     * @param borderColor      边框颜色
     * @param innerBorderWidth 内边框宽度
     * @param innerBorderColor 内边框颜色
     */
    fun setRoundBorderImageView(
        borderWidth: Float,
        @ColorInt borderColor: Int,
        innerBorderWidth: Float,
        @ColorInt innerBorderColor: Int
    ) {
        this.mBorderColor = borderColor
        this.mBorderWidth = borderWidth
        this.mInnerBorderWidth = innerBorderWidth
        this.mInnerBorderColor = innerBorderColor
    }

    /**
     * 画圆角ImageView的边框
     *
     * @param canvas 画布
     */
    private fun drawRoundBorder(canvas: Canvas) {
        if (mInnerBorderWidth > 0) {
            val bRadius = ((min(width, height) - mBorderWidth) / 2.0f)
            drawCircleBorder(canvas, mBorderWidth, mBorderColor, bRadius)
            val iRadius = ((min(width, height) - mBorderTotalWidth - mBorderWidth) / 2.0f)
            drawCircleBorder(canvas, mInnerBorderWidth, mInnerBorderColor, iRadius)
        } else {
            val iRadius = ((min(width, height) - mBorderTotalWidth - mBorderWidth) / 2.0f)
            drawCircleBorder(canvas, mBorderWidth, mBorderColor, iRadius)
        }
    }

    /**
     * 画圆角ImageView的边框
     *
     * @param canvas      画布
     * @param borderWidth 边框宽度
     * @param borderColor 边框颜色
     * @param radius      弧度
     */
    private fun drawCircleBorder(
        canvas: Canvas,
        borderWidth: Float,
        borderColor: Int,
        radius: Float
    ) {
        initBorderPaint(borderWidth, borderColor)
        path.addCircle(width / 2.0f, height / 2.0f, radius, Path.Direction.CCW)
        canvas.drawPath(path, paint)
    }


    /**
     * 初始画圆角ImageView边框的画笔
     *
     * @param borderWidth 边框宽度
     * @param borderColor 边框颜色
     */
    private fun initBorderPaint(borderWidth: Float, borderColor: Int) {
        path.reset()
        paint.strokeWidth = borderWidth
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
    }
}