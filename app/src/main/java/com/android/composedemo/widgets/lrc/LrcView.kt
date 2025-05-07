package com.android.composedemo.widgets.lrc

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.createBitmap
import com.android.composedemo.R
import com.android.composedemo.utils.Logcat
import com.android.composedemo.utils.dLog
import com.android.composedemo.utils.dp2px
import com.android.composedemo.utils.toInt
import com.fz.gson.GsonUtils
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Created by 王松 on 2016/10/21.
 */
class LrcView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private var list: MutableList<LrcBean> = mutableListOf()
    private val gPaint: Paint
    private val sPaint: Paint
    private val hPaint: Paint
    private var width = 0
    private var height = 0
    private var currentPosition = 0
    private var player: MediaPlayer? = null
    private var lastPosition = 0
    private var highLineColor: Int = Color.RED
    private var lrcColor: Int = Color.BLACK
    private var mode = KARAOKE
    private var currentPlayerMillis = -1L
    private val mHTextHeight: Float
    private val mSTextHeight: Float
    private var mGap = 0f
    private val xformode: PorterDuffXfermode
    fun setHighLineColor(highLineColor: Int) {
        this.highLineColor = highLineColor
    }

    fun setLrcColor(lrcColor: Int) {
        this.lrcColor = lrcColor
    }

    fun setMode(mode: Int) {
        this.mode = mode
    }

    fun setPlayer(player: MediaPlayer?) {
        this.player = player
    }

    fun setCurrentPlayerMillis(currentPlayerMillis: Long) {
        this.currentPlayerMillis = currentPlayerMillis
    }

    /**
     * 标准歌词字符串
     *
     * @param lrc
     */
    fun setLrc(lrc: String) {
        list.clear()
        list.addAll(LrcBean.create(lrc))
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.LrcView) {
            highLineColor = getColor(R.styleable.LrcView_hignLineColor, Color.RED)
            lrcColor = getColor(R.styleable.LrcView_lrcColor, Color.BLACK)
            mode = getInt(R.styleable.LrcView_lrcMode, mode)
        }
        gPaint = Paint()
        gPaint.isAntiAlias = true
        gPaint.setColor(lrcColor)
        gPaint.textSize = 36f
        gPaint.textAlign = Paint.Align.CENTER
        val sfontMetrics = gPaint.getFontMetrics()
        mSTextHeight = sfontMetrics.bottom - sfontMetrics.descent - sfontMetrics.ascent
        sPaint = Paint()
        sPaint.isAntiAlias = true
        sPaint.setColor(lrcColor)
        sPaint.textSize = 40f
        sPaint.textAlign = Paint.Align.CENTER

        xformode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        hPaint = Paint()
        hPaint.xfermode = xformode
        hPaint.isAntiAlias = true
        hPaint.setColor(highLineColor)
        hPaint.textSize = 40f
        hPaint.textAlign = Paint.Align.CENTER
        val fontMetrics = hPaint.getFontMetrics()
        mHTextHeight = fontMetrics.bottom - fontMetrics.descent - fontMetrics.ascent
        mGap = dp2px(20f)

    }

    override fun onDraw(canvas: Canvas) {
        if (width == 0 || height == 0) {
            width = measuredWidth
            height = measuredHeight
        }
        if (list.isEmpty()) {
            canvas.drawText("暂无歌词", width / 2f, height / 2f, gPaint)
            return
        }

        getCurrentPosition()

        //        drawLrc1(canvas);
        var currentMillis = currentPlayerMillis
        if (currentMillis < 0) {
            currentMillis = (player?.getCurrentPosition() ?: 0).toLong()
        }

        drawLrc2(canvas, currentMillis)
        val textHeight = mHTextHeight + mGap
        val v =currentPosition * textHeight
        scrollBy(0, v.toInt() - scrollY)
        if (scrollY.toFloat() == currentPosition * textHeight) {
            lastPosition = currentPosition
        }
        postInvalidateDelayed(100)
    }

    private fun drawLrc2(canvas: Canvas, currentMillis: Long) {
        val textHeight = mHTextHeight + mGap
        if (mode == 0) {
            for ((index, item) in list.withIndex()) {
                if (index == currentPosition) {
                    canvas.drawText(item.lrc, width / 2f, height / 2f + textHeight * index, hPaint)
                } else {
                    canvas.drawText(item.lrc, width / 2f, height / 2f + textHeight * index, gPaint)
                }
            }
        } else {
            for ((index, item) in list.withIndex()) {
                if (index == currentPosition) {
                    drawHighLine(canvas, currentMillis, textHeight, item)
                } else {
                    canvas.drawText(item.lrc, width / 2f, height / 2f + textHeight * index, gPaint)
                }
            }
        }
    }
    fun drawHighLine(canvas: Canvas, currentMillis: Long, textHeight: Float, item: LrcBean) {
        val lrcContent = item.lrc
        val start = item.start
        val end = item.end
        val highLineWidth = sPaint.measureText(lrcContent).toInt()
        val leftOffset = (width - highLineWidth) / 2f
        val i = ((currentMillis - start) * 1.0f / (end - start) * highLineWidth)
        val srcBitmap = createBitmap(measuredWidth, ceil(textHeight+20).roundToInt())
        val srcCanvas = Canvas(srcBitmap)
        srcCanvas.drawText(lrcContent,  highLineWidth / 2f, textHeight, sPaint)
        val rectF = RectF(0f, 0f, i, textHeight+20)
        srcCanvas.drawRect(rectF, hPaint)
        canvas.drawBitmap(srcBitmap, leftOffset,  height / 2f + (textHeight) * (currentPosition - 1), null)
    }

    private fun getCurrentPosition() {
        try {
            var currentMillis = currentPlayerMillis
            if (currentMillis < 0) {
                if (player != null) {
                    currentMillis = player!!.currentPosition.toLong()
                }
            }
            if (currentMillis < list[0].start) {
                currentPosition = 0
                return
            }
            if (currentMillis > list[list.size - 1].start) {
                currentPosition = list.size - 1
                return
            }
            for (i in list.indices) {
                if (currentMillis >= list[i].start && currentMillis < list[i].end) {
                    currentPosition = i
                    return
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            postInvalidateDelayed(100)
        }
    }

    companion object {
        const val KARAOKE: Int = 1
    }
}

data class LrcBean(var lrc: String, var start: Long = 0, var end: Long = 0){
    companion object{
        fun create(lrcStr: String): List<LrcBean> {
            val list: MutableList<LrcBean> = ArrayList<LrcBean>()
            val lrcText = lrcStr.replace("&#58;".toRegex(), ":")
                .replace("&#10;".toRegex(), "\n")
                .replace("&#46;".toRegex(), ".")
                .replace("&#32;".toRegex(), " ")
                .replace("&#45;".toRegex(), "-")
                .replace("&#13;".toRegex(), "\r")
                .replace("&#39;".toRegex(), "'")
            Logcat.d(">>>>lrcText:$lrcText")
            val split = lrcText.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            Logcat.d(">>>>split:" + split.size)
            Logcat.d(">>>>split:" + split.contentToString())
            for ((index,lrc) in split.withIndex() ) {
                //先截取时间
                if (!lrc.contains("[")) {
                    continue
                }
                if (!lrc.contains("]")) {
                    continue
                }
                val time = lrc.substring(lrc.indexOf("[")+1, lrc.indexOf("]"))
                dLog { "time:$time" }
                val min = time.substring(0, time.indexOf(":"))
                var millis ="0"
                val seconds = if (time.contains(".")) {
                    millis = time.substring(time.indexOf(".") + 1)
                    time.substring(time.indexOf(":") + 1, time.indexOf("."))
                }else{
                    time.substring(time.indexOf(":") + 1)
                }
                val startTime =
                    min.toLong() * 60 * 1000 + seconds.toLong() * 1000 + millis.toLong() * 10
                var text = lrc.substring(lrc.indexOf("]") + 1)
                if ("" == text) {
                    text = "music"
                }

                val lrcBean = LrcBean(text, startTime, 0)
                list.add(lrcBean)
                if (list.size > 1) {
                    list[list.size - 2].end = startTime
                }
                if (index == split.size - 1) {
                    list[list.size - 1].end = startTime + 100000
                }
            }
            Logcat.d(">>>>list:" + GsonUtils.toJson(list))
            return list
        }
    }
}
