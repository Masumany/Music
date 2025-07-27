package com.example.module_hot.repository

import android.content.Context
import com.example.lib.base.MyApplication
import com.example.module_hot.bean.list.ListData
import com.example.module_hot.bean.listDetail.ListDetailData
import com.example.module_hot.bean.list_songs.ListSongsData
import com.example.module_hot.bean.mv.MvRankData
import com.example.module_hot.bean.singer.SingerData
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.File
import java.util.concurrent.TimeUnit

object NetRepository {

    private fun getCache(): Cache? {
        return try {
            // 缓存目录：应用内部缓存目录的http_cache子目录
            val cacheDir = File(MyApplication.context.cacheDir, "http_cache")
            // 缓存大小：10MB（10 * 1024 * 1024字节）
            Cache(cacheDir, 10 * 1024 * 1024L)
        } catch (e: Exception) {
            // 异常时不使用缓存
            e.printStackTrace()
            null
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        // 连接池：最多5个空闲连接，保持5分钟
        .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
        // 超时设置（避免长时间等待无响应的请求）
        .connectTimeout(10, TimeUnit.SECONDS) // 连接超时：10秒
        .readTimeout(15, TimeUnit.SECONDS)    // 读取超时：15秒
        .writeTimeout(15, TimeUnit.SECONDS)   // 写入超时：15秒
        //10MB磁盘缓存（缓存GET请求的响应）
        .cache(getCache())
        .build()

    private var retrofit = Retrofit.Builder()
        .baseUrl("http://43.139.173.183:3000/")
        .client(okHttpClient)
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
        suspend fun getSingerList(
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