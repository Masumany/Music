package com.example.lib.base




import com.example.lib.base.BannerData
import com.example.lib.base.ListData
import com.example.lib.base.RecommenedData
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("banner")
    suspend fun getBanner(
        @Query("imageUrl") imageUrl: String,
        @Query("url") url: String
    ): BannerData
}
interface ApiService1 {
    @GET("personalized")
    suspend fun getRecommended(
        @Query("name") name:String,
        @Query("picUrl") picUrl:String
    ): RecommenedData
}
interface ApiService2 {
    @GET("recommend/songs")
    suspend fun getListData(): ListData
}