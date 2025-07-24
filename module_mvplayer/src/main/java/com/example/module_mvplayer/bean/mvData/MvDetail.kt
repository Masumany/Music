package com.example.module_mvplayer.bean.mvData

data class MvDetail(
    val id: Long,
    val title: String, // MV 标题
    val aliaName: String, // 别名
    val desc: String, // MV 描述
    val area: String, // 地区
    val publishTime: String, // 发布时间
    val artists: List<Artist>, // 参与艺术家
    val videos: List<VideoResource> // 不同清晰度
)
