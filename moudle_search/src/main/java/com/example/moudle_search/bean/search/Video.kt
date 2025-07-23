package com.example.moudle_search.bean.search

data class Video(
    val id: Long, // 视频ID
    val title: String, // 视频标题
    val coverUrl: String?, // 视频封面
    val duration: Long, // 视频时长
    val playCount: Long // 播放量
)
