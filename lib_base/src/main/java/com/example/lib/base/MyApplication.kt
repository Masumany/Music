package com.example.lib.base

import android.app.Application


// 自定义 Application 类
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化 NetRepository，传入 Application 上下文
        RetrofitClient.init(this)
    }
}