package com.example.lib.base

import android.app.Application
import android.content.Context
import com.therouter.TheRouter


// 自定义 Application 类
class MyApplication : Application() {

    companion object {
        // 延迟初始化：确保Application创建后才赋值
        lateinit var context: Context
            private set // 私有set，避免外部修改
    }

    override fun onCreate() {
        super.onCreate()
        //初始化全局上下文（供其他模块获取）
        context = this
        // 初始化路由
        initRouter()

        //初始化网络库（RetrofitClient）
        initNetWork()
    }

    private fun initRouter() {
        TheRouter.init(this)
    }

    private fun initNetWork() {
        RetrofitClient.init(this) // 传入Application上下文
    }
}