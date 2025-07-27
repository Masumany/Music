package com.example.module_hot.bean.list


data class ListItem(
    val id: Long,
    val listName: String,
    val listUpdateTime: Long,
    val listCoverUrl: String,
    val firstSongText: String, // 第一首歌曲的显示文本（如“歌名 - 歌手”）
    val secondSongText: String, // 第二首歌曲的显示文本（无则空）
    val thirdSongText: String, // 第三首歌曲的显示文本（无则空）
    val songCoverUrl: String
)
