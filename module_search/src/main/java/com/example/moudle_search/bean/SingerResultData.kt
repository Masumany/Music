package com.example.moudle_search.bean

import com.google.gson.annotations.SerializedName

data class SingerResultData(
    @SerializedName("code")
    val code: Int, // 响应状态码

    @SerializedName("result")
    val result: SingerResult
)

// 搜索结果容器类
data class SingerResult(
    @SerializedName("songCount")
    val songCount: Int, // 关联歌曲总数

    @SerializedName("artists")
    val singers: List<Singer2> // 提取的歌手列表
)

// 歌手核心信息类
data class Singer2(
    @SerializedName("id")
    val id: Int, // 歌手唯一ID

    @SerializedName("name")
    val name: String, // 歌手名字

    @SerializedName("img1v1Url")
    val avatarUrl: String? // 歌手照片
)