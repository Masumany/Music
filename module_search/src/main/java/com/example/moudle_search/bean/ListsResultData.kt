package com.example.moudle_search.bean

data class ListsResultData(
    val code: Int, // 响应状态码
    val result: ListResult
)

// 歌单搜索结果容器类
data class ListResult(
    val songCount: Int, // 关联歌曲总数
    val playlists: List<Playlist> // 歌单列表
)

// 歌单核心信息类
data class Playlist(
    val id: Long, // 歌单唯一ID
    val name: String, // 歌单名字
    val coverImgUrl: String, // 歌单封面图片链接
    val creator: Creator? // 歌单创建者
)

// 歌单创建者信息类
data class Creator(
    val id: Long, // 创建者ID
    val name: String // 创建者名字
)