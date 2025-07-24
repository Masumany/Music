package com.example.moudle_search.bean.search

data class Artist(
    val id: Long, // 歌手ID
    val name: String, // 歌手名称
    val avatarUrl: String?, // 歌手头像
    val musicSize: Int // 作品数量
)