package com.example.module_personage.bean

import com.google.gson.annotations.SerializedName

// 根数据类
data class ListsResultData(
    @SerializedName("code")
    val code: Int,

    @SerializedName("result")
    val result: ListResult
)

// 歌单结果容器
data class ListResult(
    @SerializedName("songCount")
    val songCount: Int,

    @SerializedName("playlist")
    val playlists:  List<Playlist>? = null
)

// 歌单信息
data class Playlist(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("coverImgUrl")
    val coverImgUrl: String,

    @SerializedName("creator")
    val creator: Creator?,

    // 补充 JSON 中需要的字段
    @SerializedName("playCount")
    val playCount: Long, // 播放次数

    @SerializedName("trackCount")
    val trackCount: Int, // 歌曲数量

    @SerializedName("description")
    val description: String? // 歌单描述（可能为 null）
)

// 歌单创建者
data class Creator(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String
)