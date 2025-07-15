package com.example.module_personage.bean.like

data class LikeData(
    val code: Int,
    val follow: List<Follow>,
    val more: Boolean,
    val touchCount: Int
)