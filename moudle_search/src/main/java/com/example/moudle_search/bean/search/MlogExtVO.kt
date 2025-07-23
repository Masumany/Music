package com.example.moudle_search.bean.search

data class MlogExtVO(
    val artistName: String,
    val artists: List<ArtistX>,
    val canCollect: Boolean,
    val channelTag: Any,
    val commentCount: Int,
    val likedCount: Int,
    val playCount: Int,
    val rcmdInfo: Any,
    val song: Song,
    val specialTag: Any,
    val strongPushIcon: Any,
    val strongPushMark: Any
)