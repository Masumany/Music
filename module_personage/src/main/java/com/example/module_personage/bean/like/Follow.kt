package com.example.module_personage.bean.like

data class Follow(
    // 用户唯一ID
    val userId: Int,
    // 用户昵称
    val nickname: String,
    //头像URL
    val avatarUrl: String,
)