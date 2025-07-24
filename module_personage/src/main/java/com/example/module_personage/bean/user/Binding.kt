package com.example.module_personage.bean.user

data class Binding(
    val bindingTime: Long,
    val expired: Boolean,
    val expiresIn: Int,
    val id: Int,
    val refreshTime: Int,
    val tokenJsonStr: Any,
    val type: Int,
    val url: String,
    val userId: Int
)