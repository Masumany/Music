package com.example.module_personage.repository

import com.example.module_personage.bean.history.HistoryData
import com.example.module_personage.bean.like.LikeData
import com.example.module_personage.bean.liked.LikedData
import com.example.module_personage.bean.user.Userdetail
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object NetRepository {
    private var retrofit = Retrofit.Builder()
        .baseUrl("http://43.139.173.183:3000")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build()

    val apiService = retrofit.create(ApiService::class.java)
    interface ApiService {
        @GET("/user/detail")
        suspend fun getUserDetail(
            @Query("uid") uid: Int
        ): Response<Userdetail>

        @GET("/user/follows")
        suspend fun getFollows(
            @Query("uid") uid: String
        ): Response<LikeData>

        @GET("/user/followeds")
        suspend fun getFolloweds(
            @Query("uid") uid: String
        ): Response<LikedData>

        @GET("/user/record")
        suspend fun getHistory(
            @Query("uid") uid: String,
            @Query("type") type: Int
        ): Response<HistoryData>
    }
}