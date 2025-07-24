package com.example.module_mvplayer.bean.mvData

data class MvItem(
    val id: Long, // MV 唯一 ID
    val cover: String, // 封面图片 URL
    val name: String, // MV 名称
    val playCount: Long, // 播放次数
    val artistName: String, // 主艺术家名称
    val artists: List<Artist>, // 参与艺术家列表
    val mv: MvDetail // MV 详细信息
)
