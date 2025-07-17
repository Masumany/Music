package com.example.music

import okhttp3.Cookie
import okhttp3.Interceptor
import okhttp3.Response

object CookieManager {
    private var cookie: String = ""

    fun saveCookie(newCookie : String){
        cookie = newCookie
    }

    fun getCookie() : String{
        return cookie
    }

    class CookieInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val cookie = CookieManager.getCookie()
            val request = chain.request().newBuilder().addHeader("Cookie", cookie).build()
            return chain.proceed(request)
        }
    }
}