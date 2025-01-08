package com.android.composedemo.utils

import android.widget.ImageView
import com.android.composedemo.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop

fun ImageView.loadCircleAvatar(url: String?) {
    Glide.with(context).asDrawable().load(url)
        .placeholder(R.drawable.ic_avatar_placeholder)
        .error(R.drawable.ic_avatar_placeholder)
        .transform(CircleCrop()).into(this)
}