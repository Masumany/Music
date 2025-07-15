package com.example.module_personage.bean.liked

data class Followed(
    val accountStatus: Int,
    val authStatus: Int,
    val avatarDetail: AvatarDetail,
    val avatarUrl: String,
    val eventCount: Int,
    val expertTags: Any,
    val experts: Any,
    val followed: Boolean,
    val followeds: Int,
    val follows: Int,
    val gender: Int,
    val mutual: Boolean,
    val nickname: String,
    val playlistCount: Int,
    val py: String,
    val remarkName: Any,
    val signature: String,
    val time: Long,
    val userId: Long,
    val userType: Int,
    val vipRights: VipRights,
    val vipType: Int
)