package com.example.module_recommened.api



import com.example.module_recommened.RecommenedData
import com.example.module_recommened.model.ListData
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("banner/{imageUrl}")
    suspend fun getBanner(): BannerData
}
interface ApiService1 {
    @GET("personalized")
    suspend fun getRecommended(
        @Query("name") name:String,
        @Query("picUrl") picUrl:String
    ):RecommenedData
}
interface ApiService2 {
    @GET("recommend/songs")
    suspend fun getListData(): ListData
}