package com.example.module_login_register.bean

data class QrCheckData(
    val code: Int,
    val cookie: String,
    val message: String?
)