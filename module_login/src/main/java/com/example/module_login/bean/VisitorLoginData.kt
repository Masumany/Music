package com.example.module_login.bean

data class VisitorLoginData(
    val code: Int,
    val cookie: String,
    val createTime: Long,
    val userId: Long
)