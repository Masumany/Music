package com.example.lib.base

import android.content.Context
import android.content.SharedPreferences
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    }
    val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(CookieInterceptor(sharedPreferences))
            .build()
    }

    val retrofit:Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://43.139.173.183:3000/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
    }
}
