package com.example.module_login_register.netWork

import android.content.SharedPreferences
import okhttp3.Interceptor

class CookieInterceptor(private val sharedPreferences: SharedPreferences): Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        //获取cookie
        val cookies = sharedPreferences.getString("cookies", "") ?: ""
        //为请求添加cookie
        val request = chain.request().newBuilder().addHeader("Cookie", cookies).build()
        // 处理响应
        val response = chain.proceed(request)

        // 保存响应中的新 Cookie
        if (response.headers("Set-Cookie").isNotEmpty()) {
            val cookies = response.headers("Set-Cookie").joinToString("; ")
            sharedPreferences.edit().putString("cookies", cookies).apply()
        }

        return response
    }
}