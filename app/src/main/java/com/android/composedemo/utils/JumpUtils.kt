package com.android.composedemo.utils

import android.content.Context
import io.flutter.embedding.android.FlutterActivity

fun Context.jumpTarget(url:String) {
//    startActivity(FlutterActivity.createDefaultIntent(this))
//    FlutterActivity.createDefaultIntent(this)
    startActivity(FlutterActivity.withNewEngine()
        .initialRoute("home")
        .dartEntrypointArgs(listOf("home", "ComposeDemo"))
        .build(this))
}