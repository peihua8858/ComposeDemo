package com.android.composedemo.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.composedemo.compose.components.TextSongView

@Composable
fun SongTextScreen(modifier: Modifier) {
    TextSongView(modifier)
}