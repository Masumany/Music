package com.example.moudle_search.bean

data class SingerResultData(
    val code: Int, // 响应状态码
    val result: SingerResult
)

// 搜索结果容器类
data class SingerResult(
    val songCount: Int, // 关联歌曲总数
    val singers: List<Singer2> // 提取的歌手列表
)

// 歌手核心信息类
data class Singer2(
    val id: Int, // 歌手唯一ID
    val name: String, // 歌手名字
    val avatarUrl: String? // 歌手照片
)