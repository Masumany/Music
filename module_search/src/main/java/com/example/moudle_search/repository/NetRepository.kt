package com.example.moudle_search.repository

import com.example.moudle_search.bean.ListsResultData
import com.example.moudle_search.bean.SingerResultData
import com.example.moudle_search.bean.SongsResultData
import com.example.moudle_search.bean.VideosResultData
import com.example.moudle_search.bean.list_songs.ListSongsData
import com.example.moudle_search.bean.searchHot.SearchHotData
import com.example.moudle_search.bean.searchKeyWord.SearchKeyWordData
import com.example.moudle_search.bean.searchSuggestionData.SearchSuggestionData
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

        @GET("/search/default")
        suspend fun getSearchKeyWord(): Response<SearchKeyWordData>

        @GET("/search/hot")
        suspend fun getHotSearch(): Response<SearchHotData>

        @GET("/search/suggest")
        suspend fun getSearchSuggest(
            @Query("keywords") keywords: String,
            @Query("type") type: String = "mobile"
        ): Response<SearchSuggestionData>

        @GET("/cloudsearch")
        suspend fun getSongsResult(
            @Query("keywords") keywords: String,
            @Query("type") type: Int = 1
        ): Response<SongsResultData>

        @GET("/cloudsearch")
        suspend fun getSingersResult(
            @Query("keywords") keywords: String,
            @Query("type") type: Int = 100
        ): Response<SingerResultData>

        @GET("/cloudsearch")
        suspend fun getVideosResult(
            @Query("keywords") keywords: String,
            @Query("type") type: Int = 1004
        ): Response<VideosResultData>

        @GET("/cloudsearch")
        suspend fun getListsResult(
            @Query("keywords") keywords: String,
            @Query("type") type: Int = 1000
        ): Response<ListsResultData>

        @GET("/playlist/track/all")
        suspend fun getListSongs(
            @Query("id") id: String,
        ): Response<ListSongsData>
    }
}