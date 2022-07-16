package com.example.restaurant_review.Model


import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * MyApplication.java
 * Created by Pin Wen
 * A way to getContext globally.
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
            private set
    }
}