package com.android.composedemo.ui.theme

import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

object DemoFontFamily {
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

    val SansSerif500 = FontFamily(
        Font(
            DeviceFontFamilyName(FontFamily.SansSerif.name),
            weight = FontWeight(500)
        )
    )
    val SansSerif400 = FontFamily(
        Font(
            DeviceFontFamilyName(FontFamily.SansSerif.name),
            weight = FontWeight(400)
        )
    )
    val Monospace500 = FontFamily(
        Font(
            DeviceFontFamilyName(FontFamily.Monospace.name),
            weight = FontWeight(500)
        )
    )
    val Monospace400 = FontFamily(
        Font(
            DeviceFontFamilyName(FontFamily.Monospace.name),
            weight = FontWeight(400)
        )
    )

}