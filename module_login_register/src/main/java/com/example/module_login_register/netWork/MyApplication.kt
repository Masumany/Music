package com.example.module_login_register.netWork

import android.app.Application
import com.example.module_login_register.repository.NetRepository

// 自定义 Application 类
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化 NetRepository，传入 Application 上下文
        NetRepository.init(this)
    }
}