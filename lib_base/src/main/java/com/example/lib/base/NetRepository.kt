package com.example.lib.base

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetRepository {
    lateinit var apiService: ApiService
        private set

    fun init(context: Context) {
        val sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(CookieInterceptor(sharedPreferences))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://43.139.173.183:3000/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }
}
