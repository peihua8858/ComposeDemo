package com.android.composedemo.ui.theme

import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

object MarketFontFamily {
    val NotoSansSc500 = FontFamily(
        Font(
            DeviceFontFamilyName("noto-sans-sc"),
            weight = FontWeight(500)
        )
    )
    val NotoSansSc400 = FontFamily(
        Font(
            DeviceFontFamilyName("noto-sans-sc"),
            weight = FontWeight(400)
        )
    )
}