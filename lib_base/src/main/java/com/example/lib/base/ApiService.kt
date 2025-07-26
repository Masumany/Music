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

interface MusicApiService {

    // 获取轮播图数据
    @GET("banner")
    suspend fun getBanner(): BannerData

    // 获取推荐歌单
    @GET("personalized")
    suspend fun getRecommended(): RecommenedData

    // 获取每日推荐歌曲
    @GET("recommend/songs")
    suspend fun getDailyRecommendSongs(): ListData

    // 获取歌曲播放URL
    @GET("song/url")
    suspend fun getMusicUrl(@Query("id") songId: String): MusicData

    // 获取歌单内的歌曲列表
    @GET("playlist/track/all")
    suspend fun getPlayList(@Query("id") playListId: String): ListMusicData

    // 获取歌曲评论
    @GET("comment/music")
    suspend fun getComment(@Query("id") songId: String): CommentData

    // 获取歌曲歌词
    @GET("lyric")
    suspend fun getSongLyric(@Query("id") songId: String): SongWordData

    // 获取歌手详情
    @GET("artist/detail")
    suspend fun getArtistDetail(@Query("id") singerId: Long): ArtistDetailData

    // 获取相似歌手
    @GET("simi/artist")
    suspend fun getSimilarArtists(@Query("id") singerId: Long): SimilarData

    // 获取歌手热门50首歌曲
    @GET("artist/top/song")
    suspend fun getSingerHotSongs(@Query("id") singerId: Long): TopData

    // 获取歌手MV
    @GET("artist/mv")
    suspend fun getSingerMvs(@Query("id") singerId: Long): SingerMvData
}
