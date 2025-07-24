package com.example.module_hot.bean.mv

data class MvRankData(
    val code: Int,
    val `data`: List<Data>,
    val hasMore: Boolean,
    val updateTime: Long
)