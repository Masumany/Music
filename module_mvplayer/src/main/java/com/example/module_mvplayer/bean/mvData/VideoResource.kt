package com.example.module_mvplayer.bean.mvData

data class VideoResource(
    val tag: String, // 清晰度标识
    val url: String, // 播放地址
    val duration: Long, // 时长
    val size: Long, // 文件大小
    val width: Int, // 视频宽度
    val height: Int, // 视频高度
    val container: String, // 容器格式
    val tagSign: VideoQuality // 清晰度详细信息
)
