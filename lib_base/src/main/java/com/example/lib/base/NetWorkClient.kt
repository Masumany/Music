package com.example.lib.base

import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {
    private const val BASE_URL = "http://43.139.173.183:3000/"

    // 构建Retrofit实例
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // JSON解析器
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create()) // 支持RxJava
            .build()
    }

    // 提供API服务实例
    val apiService by lazy {
        retrofit.create(MusicApiService::class.java)
    }
}
