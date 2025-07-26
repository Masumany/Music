package com.example.moudle_search.bean

import com.google.gson.annotations.SerializedName

data class SongsResultData(
    @SerializedName("code")
    val code: Int, // 响应状态码（200为成功）

    @SerializedName("result")
    val result: SongResult
)

// 搜索结果容器类
data class SongResult(
    @SerializedName("songCount")
    val songCount: Int, // 歌曲总数

    @SerializedName("songs")
    val songs: List<Song1> // 歌曲列表
)

// 歌曲核心信息类
data class Song1(
    @SerializedName("id")
    val id: Long, // 歌曲唯一Id

    @SerializedName("name")
    val name: String, // 歌曲名字

    @SerializedName("al")
    val al: Album1, // 专辑信息（包含封面）

    @SerializedName("ar")
    val ar: List<Artist1> // 歌手列表（支持多人演唱）
)

// 专辑信息类
data class Album1(
    @SerializedName("picUrl")
    val picUrl: String // 歌曲封面图片链接（高清）
)

// 歌手信息类
data class Artist1(
    @SerializedName("id")
    val id: Int, // 歌手唯一Id

    @SerializedName("name")
    val name: String // 歌手名字
)