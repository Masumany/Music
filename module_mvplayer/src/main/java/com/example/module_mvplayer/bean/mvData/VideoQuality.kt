package com.example.module_mvplayer.bean.mvData

data class VideoQuality (
    val br: Int, // 清晰度等级（240/480/720/1080）
    val type: String, // 格式类型
    val resolution: Int // 分辨率（240/480/720/1080）
)