package com.android.composedemo

import android.app.Application
import android.content.Context

class ComposeDemoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        context = this
    }

    init {
        instance = this;
    }



    companion object {
        lateinit var instance: ComposeDemoApp
            private set
        const val TAG = "ComposeDemoApp";
        lateinit var context: Context
        @JvmStatic
        fun getAPP(): ComposeDemoApp {
            return instance;
        }
    }

}