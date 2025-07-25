package com.example.moudle_search.bean

data class VideosResultData(
    val code: Int, // 响应状态码
    val result: VideoResult
)

// MV搜索结果容器类
data class VideoResult(
    val songCount: Int, // 关联歌曲总数
    val mvs: List<MV> // MV列表
)

// MV核心信息类
data class MV(
    val id: Long, // MV唯一ID
    val name: String, // MV名字
    val coverUrl: String, // MV封面图片链接
    val creator: MvCreator? // MV创建者
)

// MV创建者信息类
data class MvCreator(
    val id: Long, // 创建者ID
    val name: String // 创建者名字
)
