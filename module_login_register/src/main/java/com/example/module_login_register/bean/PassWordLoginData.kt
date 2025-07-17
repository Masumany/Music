package com.example.module_login_register.bean

data class PassWordLoginData(
    val code: Int,
    val loginType: Int,
    val message: String,
    val redirectUrl: String
)