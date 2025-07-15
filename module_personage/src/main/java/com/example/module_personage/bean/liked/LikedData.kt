package com.example.module_personage.bean.liked

data class LikedData(
    val code: Int,
    val followeds: List<Followed>,
    val more: Boolean,
    val size: Int
)