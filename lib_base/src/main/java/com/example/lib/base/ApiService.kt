package com.example.lib.base

import data.ArtistDetailData
import data.CommentData
import data.ListMusicData
import data.MusicData
import data.SimilarData
import data.SingerMvData
import data.SongWordData
import data.TopData
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
        @Query("id") playListId: String, // 歌单 ID
    ): ListMusicData
}
/**
 * 获取评论接口
 **/
interface ApiService5 {
    @GET("comment/music")
    suspend fun getComment(
        @Query("id") commentId: String
    ):CommentData
}

/**
 * 获取歌词接口
  **/
interface ApiService6 {
    @GET("/lyric")
    suspend fun getSongWord(
        @Query("id") songWordId: String
    ): SongWordData
}

/**
 * 获取歌手详情接口
 */
interface ApiService7 {
    @GET("/artist/detail")
    suspend fun getArtistDetail(
        @Query("id")singerId: Long): ArtistDetailData
}
/**
 * 获取歌手相似歌手接口
 */
interface ApiService8 {
    @GET("/simi/artist")
    suspend fun getSimilar(
        @Query("id")singerId: Long
    ): SimilarData
}
/**
 * 获取歌手热门50首歌曲接口
 */
interface ApiService9 {
    @GET("/artist/top/song")
    suspend fun getSingerHot(
        @Query("id")singerId: Long
    ):TopData
}
 /**
  * 获取歌手MV接口
  */
interface ApiService10 {
    @GET("/artist/mv")
    suspend fun getSingerMv(
        @Query("id")singerId: Long
    ):SingerMvData
}