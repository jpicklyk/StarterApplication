package com.example.starterapplication

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import net.sfelabs.knox.core.android.AndroidApplicationContextProvider

@HiltAndroidApp
class RootApplication : Application() {

    override fun onCreate() {
        // Initialize context provider BEFORE super.onCreate() so it's available
        // when Hilt creates singleton components that depend on it
        AndroidApplicationContextProvider.init(object : AndroidApplicationContextProvider {
            override fun getContext(): Context = this@RootApplication
        })
        super.onCreate()
    }
}