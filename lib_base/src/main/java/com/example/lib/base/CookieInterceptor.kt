package com.example.lib.base

import android.content.SharedPreferences
import okhttp3.Interceptor

class CookieInterceptor(private val sharedPreferences: SharedPreferences) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        // 从 SharedPreferences 获取旧 Cookie
        val savedCookies = sharedPreferences.getString("cookies", "") ?: ""
        // 为请求添加旧 Cookie
        val request = chain.request().newBuilder()
            .addHeader("Cookie", savedCookies)
            .build()

        //执行请求，获取响应
        val response = chain.proceed(request)

        //从响应中获取新 Cookie
        if (response.headers("Set-Cookie").isNotEmpty()) {
            // 响应返回的新 Cookie
            val newCookies = response.headers("Set-Cookie").joinToString("; ")
            // 保存新 Cookie 到 SharedPreferences
            sharedPreferences.edit().putString("cookies", newCookies).apply()
        }

        return response
    }
}