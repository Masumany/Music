package com.example.module_mvplayer.bean.commentData

data class Comment(
    val commentId: Long, // 评论ID
    val content: String, // 评论内容
    val time: Long, // 评论时间戳（毫秒）
    val timeStr: String, // 格式化时间（如"2017-04-16"或"06-23"）
    val likedCount: Int, // 点赞数
    val liked: Boolean, // 当前用户是否已点赞
    val user: CommentUser, // 评论用户信息
    val ipLocation: IpLocation? // IP属地
)
