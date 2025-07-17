package com.example.module_musicplayer
import android.app.Application
import com.therouter.TheRouter

class MyApplication : Application() {
        override fun onCreate() {
            super.onCreate()
            // 初始化 TheRouter（必须在 Application 中调用）
            TheRouter.init(this)
        }
    }
