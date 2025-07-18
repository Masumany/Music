package com.example.lib.base

import data.CommentData
import data.ListMusicData
import data.MusicData
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 轮播图接口
 * 说明：无需传入参数，后端返回轮播图数据（包含 imageUrl、url 等）
 */
interface ApiService {
    @GET("banner")
    suspend fun getBanner(): BannerData
}

/**
 * 推荐歌单接口
 * 说明：默认获取推荐歌单，无需 name、picUrl 参数（这些是歌单数据的返回字段）
 */
interface ApiService1 {
    @GET("personalized")
    suspend fun getRecommended(): RecommenedData
}

/**
 * 每日推荐歌曲接口
 */
interface ApiService2 {
    @GET("recommend/songs")
    suspend fun getListData(): ListData
}

/**
 * 歌曲播放 URL 接口
 * 说明：通过歌曲 ID 获取播放地址，参数名固定为 id
 */
interface ApiService3 {
    @GET("song/url") // 去掉多余的斜杠，避免与 BaseUrl 拼接异常
    suspend fun getMusicUrl(
        @Query("id") songId: String // 歌曲 ID（与后端接口参数名对齐）
    ): MusicData
}

/**
 * 歌单详情接口（获取歌单内的歌曲列表）
 */
interface ApiService4 {
    @GET("playlist/track/all") // 去掉多余的斜杠
    suspend fun getPlayList(
        @Query("id") playlistId: Long // 歌单 ID
    ): ListMusicData
}
interface ApiService5 {
    @GET("comment/music")
    suspend fun getComment(
        @Query("id") commentId: String
    ):CommentData
}