package com.example.moudle_search.bean

import com.google.gson.annotations.SerializedName

data class ListsResultData(
    @SerializedName("code")
    val code: Int, // 响应状态码

    @SerializedName("result")
    val result: ListResult
)

// 歌单搜索结果容器类
data class ListResult(
    @SerializedName("songCount")
    val songCount: Int, // 关联歌曲总数

    @SerializedName("playlists")
    val playlists: List<Playlist> // 歌单列表
)

// 歌单核心信息类
data class Playlist(
    @SerializedName("id")
    val id: Long, // 歌单唯一ID

    @SerializedName("name")
    val name: String, // 歌单名字

    @SerializedName("coverImgUrl")
    val coverImgUrl: String, // 歌单封面图片链接

    @SerializedName("creator")
    val creator: Creator? // 歌单创建者
)

// 歌单创建者信息类
data class Creator(
    @SerializedName("id")
    val id: Long, // 创建者ID

    @SerializedName("name")
    val name: String // 创建者名字
)