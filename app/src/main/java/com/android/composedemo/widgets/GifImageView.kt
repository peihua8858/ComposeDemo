package com.android.composedemo.widgets

import android.content.Context
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.android.composedemo.utils.eLog
import com.android.composedemo.utils.screenHeight
import com.android.composedemo.utils.screenWidth
import com.fz.common.coroutine.WorkScope
import com.fz.common.file.createFileName
import com.fz.common.file.fetchFileName
import com.fz.common.file.writeToFile
import com.fz.common.utils.dLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.ByteBuffer
import kotlin.math.max

/**
 * gif 图片播放
 *
 * @author dingpeihua
 */
class GifImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ImageView(context, attrs, defStyleAttr), CoroutineScope by WorkScope() {
    private val mScreenWidth by lazy { this.screenWidth }
    private val mScreenHeight by lazy { this.screenHeight }

    init {
        setScaleType(ScaleType.FIT_CENTER)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @Throws(IOException::class)
    fun loadImage(url: String) {
        val file = File(url)
        if (!file.exists()) return
        val source = ImageDecoder.createSource(file)
        val drawable = ImageDecoder.decodeDrawable(
            source,
            ImageDecoder.OnHeaderDecodedListener { decoder, info, source ->
                decoder.setAllocator(ImageDecoder.ALLOCATOR_DEFAULT)
                val screenWidth = this.mScreenWidth.toDouble()
                val screenHeight = this.mScreenHeight.toDouble()
                val widthSample = info.size.width.toDouble() / screenWidth
                val heightSample = info.size.height.toDouble() / screenHeight
                var scale = max(widthSample, heightSample)
                val sample = (scale + 0.5).toInt()
                if (sample > 1) {
                    decoder.setTargetSampleSize(sample)
                }
            })
        if (drawable is AnimatedImageDrawable) {
            val animatedImageDrawable = drawable
            animatedImageDrawable.setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE)
            animatedImageDrawable.start()
        }
        setImageDrawable(drawable)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @Throws(IOException::class)
    fun loadImage(url: Any) {
        val source = if (url is Int) {
            ImageDecoder.createSource(resources, url)
        } else if (url is String) {
            if (url.startsWith("http")) {
                loadImageByNetwork(url)
                return
            }
            val file = File(url)
            if (!file.exists()) return
            ImageDecoder.createSource(file)
        } else if (url is File) {
            ImageDecoder.createSource(url)
        } else if (url is ByteBuffer) {
            ImageDecoder.createSource(url)
        } else if (url is InputStream) {
            ImageDecoder.createSource(resources, url)
        } else {
            return
        }
        val drawable = ImageDecoder.decodeDrawable(
            source,
            ImageDecoder.OnHeaderDecodedListener { decoder, info, source ->
                decoder.setAllocator(ImageDecoder.ALLOCATOR_DEFAULT)
                val screenWidth = this.mScreenWidth.toDouble()
                val screenHeight = this.mScreenHeight.toDouble()
                val widthSample = info.size.width.toDouble() / screenWidth
                val heightSample = info.size.height.toDouble() / screenHeight
                var scale = max(widthSample, heightSample)
                val sample = (scale + 0.5).toInt()
                if (sample > 1) {
                    decoder.setTargetSampleSize(sample)
                }
            })
        setImageDrawable(drawable)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun setImageDrawable(drawable: Drawable) {
        if (drawable is AnimatedImageDrawable) {
            val animatedImageDrawable = drawable
            animatedImageDrawable.setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE)
            animatedImageDrawable.start()
        }
        super.setImageDrawable(drawable)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun loadImageByNetwork(url: String) {
        launch {
            val result = request(url)
            if (result != null) {
                withContext(Dispatchers.Main) {
                    loadImage(result)
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancel()
    }

    fun Context.getDiskCacheDir(): File {
        //如果SD卡存在通过getExternalCacheDir()获取路径，
        //放在路径 /sdcard/Android/data/<application package>/cache/
        val file = externalCacheDir
        //如果SD卡不存在通过getCacheDir()获取路径，
        //放在路径 /data/data/<application package>/cache/
        if (file != null && file.exists()) {
            return file
        }
        return cacheDir
    }

    private fun request(requestUrl: String, timeOut: Int = 5000): File? {
        var inputStream: InputStream? = null
        try {
            val url = URL(requestUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestMethod("GET")
            conn.setRequestProperty("Connection", "Keep-Alive")
            conn.setConnectTimeout(timeOut)
            val responsCode = conn.getResponseCode()
            if (responsCode != 200) {
                eLog { "responsCode = $responsCode, so Fail!!!" }
                return null
            }
            val fileName = requestUrl.fetchFileName() ?: "Media_".createFileName("jpg")
            val parentFile = File(context.getDiskCacheDir(), "images")
            val saveUri = File(parentFile, fileName)
            inputStream = conn.getInputStream()
            saveUri.writeToFile(inputStream, true)
            inputStream.close()
            return saveUri
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            dLog { "catch MalformedURLException e = " + e.message }
        } catch (e: IOException) {
            e.printStackTrace()
            dLog { "catch IOException e = " + e.message + ", inputStream = " + inputStream }
        }
        return null
    }
}
