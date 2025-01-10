package com.android.composedemo.utils

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

@Composable
fun showToast(msg: String) {
    val context = LocalContext.current
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

@Composable
fun showToast(@StringRes msgResId: Int) {
    val context = LocalContext.current
    Toast.makeText(context, stringResource(msgResId), Toast.LENGTH_SHORT).show()
}