package com.example.module_personage.bean.history

data class Song(
    val id: Long,               // 歌曲唯一ID
    val name: String,           // 歌曲名
    val al: Al,                 // 专辑信息
    val ar: List<Ar>            // 歌手列表
)