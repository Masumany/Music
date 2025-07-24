package com.example.module_mvplayer.bean.mvData

data class MvData(
    val code: Int,
    val data: MvItem, // MV 列表
    val hasMore: Boolean,
    val updateTime: Long // 更新时间戳
)