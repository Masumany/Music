package com.example.moudle_search.bean.search

data class Result(
    val artist: List<Artist>,
    val new_mlog: List<NewMlog>,
    val orders: List<String>,
    val playlist: List<Playlist>
)