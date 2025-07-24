package com.example.module_login_register.bean

data class AccountInfo (
    val id: Long,
    val userId: Long,
    val userName: String,
    val createTime: Long,
    val status: Int,
    val vipType: Int,
    val authStatus: Int,
    val avatarUrl: String
)