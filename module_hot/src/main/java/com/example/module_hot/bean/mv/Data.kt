package com.example.module_hot.bean.mv

data class Data(
    val artistId: Int,
    val artistName: String,
    val artists: List<Artist>,
    val briefDesc: Any,
    val cover: String,
    val desc: Any,
    val duration: Int,
    val id: Int,
    val lastRank: Int,
    val mark: Int,
    val mv: Mv,
    val name: String,
    val playCount: Int,
    val score: Int,
    val subed: Boolean
)