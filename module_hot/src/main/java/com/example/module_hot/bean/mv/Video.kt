package com.example.module_hot.bean.mv

data class Video(
    val check: Boolean,
    val container: String,
    val duration: Int,
    val height: Int,
    val md5: String,
    val size: Int,
    val tag: String,
    val tagSign: TagSign,
    val url: String,
    val width: Int
)