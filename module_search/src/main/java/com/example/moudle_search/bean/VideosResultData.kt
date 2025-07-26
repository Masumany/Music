package com.example.moudle_search.bean

import com.google.gson.annotations.SerializedName

data class VideosResultData(
    @SerializedName("code")
    val code: Int, // 响应状态码

    @SerializedName("result")
    val result: VideoResult
)

// MV搜索结果容器类
data class VideoResult(
    @SerializedName("mvs")
    val mvs: List<MV> // MV列表
)

// MV核心信息类
data class MV(
    @SerializedName("id")
    val id: Long, // MV唯一ID

    @SerializedName("name")
    val name: String, // MV名字

    @SerializedName("coverUrl")
    val coverUrl: String, // MV封面图片链接

    @SerializedName("creator")
    val creator: MvCreator? // MV创建者（可为null）
)

// MV创建者信息类
data class MvCreator(
    @SerializedName("id")
    val id: Long, // 创建者ID

    @SerializedName("name")
    val name: String // 创建者名字
)