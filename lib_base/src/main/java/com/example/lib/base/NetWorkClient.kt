package com.example.lib.base

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object NetWorkClient {
    private const val BASE_URL = "http://43.139.173.183:3000/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .build()
    val apiService:ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
    val apiService1:ApiService1 by lazy {
        retrofit.create(ApiService1::class.java)
    }
    val apiService2:ApiService2 by lazy {
        retrofit.create(ApiService2::class.java)
    }
    val apiService3:ApiService3 by lazy {
        retrofit.create(ApiService3::class.java)
    }
    val apiService4:ApiService4 by lazy {
        retrofit.create(ApiService4::class.java)
    }
}