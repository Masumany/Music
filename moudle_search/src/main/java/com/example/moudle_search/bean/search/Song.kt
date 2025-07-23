package com.example.moudle_search.bean.search

data class Song(
    val albumName: String,
    val artists: List<ArtistXX>,
    val coverUrl: String,
    val duration: Int,
    val endTime: Any,
    val id: Int,
    val name: String,
    val privilege: Any,
    val startTime: Any
)