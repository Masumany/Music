package com.example.module_hot.bean.list

data class RewardToplist(
    val coverUrl: String,
    val name: String,
    val position: Int,
    val songs: List<Song>
)