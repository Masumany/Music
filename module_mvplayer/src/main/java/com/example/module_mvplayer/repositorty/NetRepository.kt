package com.example.module_mvplayer.repositorty

import com.example.module_mvplayer.bean.commentData.CommentData
import com.example.module_mvplayer.bean.mvData.MvData
import com.example.module_mvplayer.bean.mvInfo.MvInfoData
import com.example.module_mvplayer.bean.mvPlayUrl.MvPlayUrl
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object NetRepository {

    private var retrofit = Retrofit.Builder()
        .baseUrl("http://43.139.173.183:3000/")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build()

    val apiService = retrofit.create(ApiService::class.java)
    interface ApiService {
        @GET("/comment/mv")
        suspend fun getMvComment(
            @Query("id") id: String,
            @Query("offset") offset: Int, // 偏移量（分页）
            @Query("limit") limit: Int // 每页数量
        ): Response<CommentData>

        //搜索使用
        @GET("/mv/detail")
        suspend fun getMvDetail(
            @Query("mvid") mvId: String
        ): Response<MvData>

        @GET("/mv/detail/info")
        suspend fun getMvInfo(
            @Query("mvid") mvid: String,
        ): Response<MvInfoData>

        @GET("/mv/url")
        suspend fun getMvPlayUrl(
            @Query("id") mvId: String,
            @Query("r") r: Int = 1080
        ): Response<MvPlayUrl>
    }
}