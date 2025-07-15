package com.example.module_personage.bean.like

data class Follow(
    val accountStatus: Int,
    val authStatus: Int,
    val avatarDetail: AvatarDetail,
    val avatarUrl: String,
    val blacklist: Boolean,
    val eventCount: Int,
    val expertTags: Any,
    val experts: Experts,
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
    val time: Int,
    val userId: Int,
    val userType: Int,
    val vipRights: VipRights,
    val vipType: Int
)