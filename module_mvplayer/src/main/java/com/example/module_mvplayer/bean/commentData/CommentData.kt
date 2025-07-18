package com.example.module_mvplayer.bean.commentData

data class CommentData(
    val code: Int,
    val total: Int, // 评论总数
    val more: Boolean,
    val hotComments: List<Comment>, // 热门评论列表
    val comments: List<Comment>, // 普通评论列表（按时间排序）
    val isMusician: Boolean, // 是否为音乐人账号
    val userId: Long // 评论用户ID
)