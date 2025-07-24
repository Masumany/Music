package com.example.module_personage.bean.user

data class Profile(
    val userId: Int,              // 用户ID
    val nickname: String,         // 昵称
    val avatarUrl: String,        // 头像URL
    val signature: String,        // 个性签名
    val gender: Int,              // 性别 (0:保密, 1:男, 2:女)
    val birthday: Long,           // 生日时间戳
    val city: Int,                // 所在城市代码
    val province: Int,            // 所在省份代码
    val description: String,      // 个人描述
    val followeds: Int,           // 粉丝数
    val follows: Int,             // 关注数
    val playlistCount: Int,       // 歌单数量
    val followed: Boolean,        // 当前用户是否关注该用户
    val vipType: Int,             // VIP类型
)