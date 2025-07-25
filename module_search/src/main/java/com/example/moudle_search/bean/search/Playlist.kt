package com.example.moudle_search.bean.search

data class Playlist(
    val id: Long, // 歌单ID
    val name: String, // 歌单名称
    val coverImgUrl: String?, // 歌单封面
    val trackCount: Int, // 歌曲数量
    val creator: SimpleUser? // 创建者
)

data class SimpleUser(
    val nickname: String // 用户名
)