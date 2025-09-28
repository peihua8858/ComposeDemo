package com.android.composedemo.compose.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.android.composedemo.R

@Composable
fun LoadingView(modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        val painter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current)
                .data(data = R.mipmap.ic_loading1)
                .build()
        )
        val transition = rememberInfiniteTransition(label = "")
        val progress by transition.animateValue(
            0f,
            1f,
            Float.VectorConverter,
            infiniteRepeatable(
                animation = tween(
                    durationMillis = 1332,
                    easing = LinearEasing
                )
            ), label = ""
        )
        Image(
            painter = painter,
            contentDescription = "",
            modifier = modifier
                .align(Alignment.Center)
                .rotate(progress * 360),
        )
    }
}