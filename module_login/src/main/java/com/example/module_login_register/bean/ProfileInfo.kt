package com.example.module_login_register.bean

data class ProfileInfo (
    val userId: Long? = null,
    val nickname: String? = null,
    val signature: String? = null,
    val gender: Int? = null,
    val birthday: Long? = null,
    val province: Int? = null,
    val city: Int? = null,
    val avatarUrl: String? = null,
    val backgroundUrl: String? = null,
    val followeds: Int? = null,
    val follows: Int? = null,
    val eventCount: Int? = null,
    val playlistCount: Int? = null,
    val playlistBeSubscribedCount: Int? = null
)