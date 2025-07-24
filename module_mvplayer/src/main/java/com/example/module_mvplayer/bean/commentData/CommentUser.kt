package com.example.module_mvplayer.bean.commentData

data class CommentUser(
    val userId: Long, // 用户ID
    val nickname: String, // 昵称
    val avatarUrl: String, // 头像URL
    val vipType: Int // VIP类型
)
