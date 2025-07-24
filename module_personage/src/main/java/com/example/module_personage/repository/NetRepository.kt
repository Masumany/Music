package com.example.module_personage.repository

import com.example.lib.base.RetrofitClient
import com.example.module_personage.bean.history.HistoryData
import com.example.module_personage.bean.like.LikeData
import com.example.module_personage.bean.liked.LikedData
import com.example.module_personage.bean.user.Userdetail
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

object NetRepository {
    val apiService: ApiService by lazy {
        RetrofitClient.retrofit.create(ApiService::class.java)
    }
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