package com.example.module_login_register.bean

data class UserAccount (
    val code: Int,
    val account: AccountInfo?,
    val profile: ProfileInfo?,
    val loginType: Int,
)