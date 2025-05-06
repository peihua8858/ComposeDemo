//package com.android.composedemo.widgets.compose
//
//import android.graphics.PorterDuff
//import android.graphics.PorterDuffXfermode
//import androidx.compose.foundation.Canvas
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Paint
//import androidx.compose.ui.graphics.PaintingStyle
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.drawText
//import androidx.compose.ui.text.rememberTextMeasurer
//import androidx.compose.ui.unit.sp
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//class SongTextView {
//}
//
//@Composable
//fun SongTextView(modifier: Modifier, mText: String) {
//    val mPaint =Paint()
//    mPaint.color=Color.Cyan
//    val xformode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
//    mPaint.
//    mPaint.setTextSize(60.0f)
//    mPaint.isAntiAlias = true
//    mPaint.style= PaintingStyle.Fill
//    mPaint.setStyle(android.graphics.Paint.Style.FILL_AND_STROKE)
//    mPaint.setXfermode(null)
//    mPaint.setTextAlign(android.graphics.Paint.Align.LEFT)
//
//    //文字精确高度
//    val fontMetrics: android.graphics.Paint.FontMetrics = mPaint.getFontMetrics()
//    mTextHeight = fontMetrics.bottom - fontMetrics.descent - fontMetrics.ascent
//    mTextWidth = mPaint.measureText(mText)
//    val scope = rememberCoroutineScope()
//    val textMeasurer = rememberTextMeasurer()
//
//
//    val defaultStyle = TextStyle(
//        fontSize = 24.sp,
//        color = Color.Black,
//    )
//    val changeStyle = TextStyle(
//        fontSize = 24.sp,
//        color = Color.Red,
//    )
//
//    val textLayoutResult = remember(mText) {
//        textMeasurer.measure(mText, defaultStyle)
//    }
//
//    Canvas(modifier = modifier) {
//        drawText(
//            textMeasurer = textMeasurer,
//            text = mText,
//            style = defaultStyle,
//            topLeft = Offset(
//                x = center.x - textLayoutResult.size.width / 2,
//                y = center.y - textLayoutResult.size.height / 2,
//            )
//        )
//        scope.launch {
//            while (true) {
//                drawText(
//                    textMeasurer = textMeasurer,
//                    text = mText,
//                    style = defaultStyle,
//                    topLeft = Offset(
//                        x = center.x - textLayoutResult.size.width / 2,
//                        y = center.y - textLayoutResult.size.height / 2,
//                    )
//                )
//                delay(100)
//            }
//
//        }
//    }
//}