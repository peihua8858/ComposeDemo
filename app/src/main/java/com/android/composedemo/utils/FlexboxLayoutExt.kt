package com.android.composedemo.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.android.composedemo.widgets.LinearLineWrapLayout
import com.google.android.flexbox.FlexboxLayout

fun <H : ViewHolder> FlexboxLayout.setAdapter(adapter: RecyclerView.Adapter<H>) {
    this.removeAllViews()
    val itemCount = adapter.itemCount
    for (index in 0 until itemCount) {
        val itemType = adapter.getItemViewType(index)
        val holder = adapter.createViewHolder(this, itemType)
        val itemView = holder.itemView
        adapter.bindViewHolder(holder, index)
        addView(itemView)
    }
}

fun <H : ViewHolder> LinearLineWrapLayout.setAdapter(
    adapter: RecyclerView.Adapter<H>,
    callback: (View, Int) -> Unit
) {
    this.removeAllViews()
    val itemCount = adapter.itemCount
    for (index in 0 until itemCount) {
        val itemType = adapter.getItemViewType(index)
        val holder = adapter.createViewHolder(this, itemType)
        val itemView = holder.itemView
        adapter.bindViewHolder(holder, index)
        addView(itemView)
        callback(itemView, index)
    }
}
