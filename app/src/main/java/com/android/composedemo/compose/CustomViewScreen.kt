package com.android.composedemo.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.android.composedemo.compose.components.DynamicAsyncImage

@Composable
fun CustomViewScreen(modifier: Modifier) {
    LazyColumn(modifier = modifier) {
        items(4) { index ->
            if (index % 2 == 0) {
                DynamicAsyncImage(
                    model = "http://c.hiphotos.baidu.com/image/pic/item/30adcbef76094b36de8a2fe5a1cc7cd98d109d99.jpg",
                    contentDescription = "000000"
                )
            } else {
                AsyncImage(
                    model = "http://h.hiphotos.baidu.com/image/pic/item/7c1ed21b0ef41bd5f2c2a9e953da81cb39db3d1d.jpg",
                    contentDescription = "222222"
                )
            }
        }
    }
}