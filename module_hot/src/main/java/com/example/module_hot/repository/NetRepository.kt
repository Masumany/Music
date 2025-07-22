package com.example.module_hot.repository

import com.example.module_hot.bean.list.ListData
import com.example.module_hot.bean.listDetail.ListDetailData
import com.example.module_hot.bean.list_songs.ListSongsData
import com.example.module_hot.bean.mv.MvRankData
import com.example.module_hot.bean.singer.SingerData
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
        @GET("/top/mv")
        suspend fun getMvRank(
        ): Response<MvRankData>

        @GET("/toplist/detail")
        suspend fun getList(
        ): Response<ListData>

        @GET("/toplist/artist")
        suspend fun getArtistList(
        ): Response<SingerData>

        @GET("/playlist/track/all")
        suspend fun getListSongs(
            @Query("id") id: String,
        ): Response<ListSongsData>

        @GET("/playlist/detail/dynamic")
        suspend fun getListDetail(
            @Query("id") id: String,
        ): Response<ListDetailData>
    }
}