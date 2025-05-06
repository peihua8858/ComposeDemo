package com.android.composedemo.compose

import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.android.composedemo.utils.dLog
import com.android.composedemo.utils.dp2px
import com.android.composedemo.utils.sp2px
import com.android.composedemo.widgets.LyricTextView
import com.android.composedemo.widgets.SongTextView
import com.android.composedemo.widgets.lrc.LrcView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SongTextContentView(modifier: Modifier) {
    TextSongView(modifier)
}

@Composable
fun TextSongView(modifier: Modifier) {
    val scope = rememberCoroutineScope()
    var process = 0f
    var currentMillis = 0
    Column(modifier = modifier/*.verticalScroll(rememberScrollState())*/) {
//        AndroidView(modifier = Modifier, factory = {
//            val params = ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                it.dp2px(100f).toInt()
//            )
//            SongTextView(it).apply {
//                layoutParams = params
//            }
//        })
//        AndroidView(modifier = Modifier, factory = {
//            val params = ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                it.dp2px(200f).toInt()
//            )
//            LyricTextView(it).apply {
//                text = "过完整个夏天,忧伤并没有好一些"
//                textSize = this.sp2px(32f)
//                scope.launch {
//                    while (true) {
//                        dLog { "process:$process" }
//                        setProgress(process)
//                        delay(100)
//                        if (process > 1f) {
//                            process = 0f
//                        } else {
//                            process += 0.01f
//                        }
//                    }
//                }
//                layoutParams = params
//            }
//        })
        AndroidView(modifier = Modifier.fillMaxSize(), factory = {
            val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            LrcView(it).apply {
                setLrc(it.readAssetLrc())
                layoutParams = params
                scope.launch {
                    while (true) {
                        dLog { "currentMillis:$currentMillis" }
                        setCurrentPlayerMillis(currentMillis)
                        delay(30)
                        if (currentMillis > 280000) {
                            currentMillis = 0
                        } else {
                            currentMillis += 30
                        }
                    }
                }
            }
        })
    }
}

fun Context.readAssetLrc(): String {
    return assets.open("黄昏.lrc").bufferedReader().use {
        val result =it.readText()
        dLog { "result:$result" }
        return result
    }
}
